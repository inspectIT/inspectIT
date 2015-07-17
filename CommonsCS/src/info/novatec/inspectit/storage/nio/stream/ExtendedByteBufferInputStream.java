package info.novatec.inspectit.storage.nio.stream;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.IStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.read.ReadingChannelManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This is a specially designed {@link InputStream} to read the data from our storage. The stream
 * has a number of buffers that will data from the disk will be filled. Later, these buffers will be
 * used to stream the data when needed (fulfilling the {@link InputStream} functionality).
 * <p>
 * The stream uses {@link ByteBufferProvider} to get the buffers and will release the buffers on the
 * {@link #close()} method. It's a must to call a {@link #close()} after the stream has been used.
 * 
 * @author Ivan Senic
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ExtendedByteBufferInputStream extends AbstractExtendedByteBufferInputStream {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ReadingChannelManager}.
	 */
	@Autowired
	private ReadingChannelManager readingChannelManager;

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	private StorageManager storageManager;

	/**
	 * {@link ExecutorService} for reading tasks executions.
	 */
	@Autowired
	@Resource(name = "storageExecutorService")
	private ExecutorService executorService;

	/**
	 * {@link IStorageData} to read data for.
	 */
	private IStorageData storageData;

	/**
	 * List of descriptors that point to the data.
	 */
	private List<IStorageDescriptor> descriptors;

	/**
	 * Next index of the descriptor to be read.
	 */
	private AtomicInteger nextDescriptorIndex = new AtomicInteger(0);

	/**
	 * Set of opened paths.
	 */
	private Set<Path> openedChannelPaths = Collections.newSetFromMap(new ConcurrentHashMap<Path, Boolean>(16, 0.75f, 1));

	/**
	 * No-arg constructor.
	 */
	public ExtendedByteBufferInputStream() {
	}

	/**
	 * Default constructor. Sets number of buffers to 3. Same as calling {@link
	 * #ExtendedByteBufferInputStream(StorageData, List, 3)}.
	 * 
	 * @param storageData
	 *            {@link StorageData} to read information for.
	 * @param descriptors
	 *            List of descriptors that point to the data.
	 */
	public ExtendedByteBufferInputStream(IStorageData storageData, List<IStorageDescriptor> descriptors) {
		this(storageData, descriptors, 3);
	}

	/**
	 * Secondary constructor. Sets the amount of buffers to use.
	 * 
	 * @param numberOfBuffers
	 *            Amount of buffers to use during read.
	 * @param storageData
	 *            {@link StorageData} to read information for.
	 * @param descriptors
	 *            List of descriptors that point to the data.
	 */
	public ExtendedByteBufferInputStream(IStorageData storageData, List<IStorageDescriptor> descriptors, int numberOfBuffers) {
		super(numberOfBuffers);
		this.storageData = storageData;
		this.descriptors = descriptors;
	}

	/**
	 * Prepares the stream for read. Must be called before any read operation is executed.
	 */
	public void prepare() {
		super.prepare();

		// set total size
		long totalSize = 0;
		for (IStorageDescriptor descriptor : descriptors) {
			totalSize += descriptor.getSize();
		}
		setTotalSize(totalSize);

		executorService.execute(new ReadTask());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Releases all byte buffers that are hold.
	 */
	@Override
	public synchronized void close() throws IOException {
		if (isClosed()) {
			return;
		}
		super.close();

		// close opened channel paths
		for (Path path : openedChannelPaths) {
			readingChannelManager.finalizeChannel(path);
		}
	}

	/**
	 * Sets {@link #storageData}.
	 * 
	 * @param storageData
	 *            New value for {@link #storageData}
	 */
	public void setStorageData(IStorageData storageData) {
		this.storageData = storageData;
	}

	/**
	 * Gets {@link #descriptors}.
	 * 
	 * @return {@link #descriptors}
	 */
	public List<IStorageDescriptor> getDescriptors() {
		return descriptors;
	}

	/**
	 * Sets {@link #descriptors}.
	 * 
	 * @param descriptors
	 *            New value for {@link #descriptors}
	 */
	public void setDescriptors(List<IStorageDescriptor> descriptors) {
		this.descriptors = descriptors;
	}

	/**
	 * Sets {@link #readingChannelManager}.
	 * 
	 * @param readingChannelManager
	 *            New value for {@link #readingChannelManager}
	 */
	public void setReadingChannelManager(ReadingChannelManager readingChannelManager) {
		this.readingChannelManager = readingChannelManager;
	}

	/**
	 * Sets {@link #storageManager}.
	 * 
	 * @param storageManager
	 *            New value for {@link #storageManager}
	 */
	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	/**
	 * Sets {@link #executorService}.
	 * 
	 * @param executorService
	 *            New value for {@link #executorService}
	 */
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Read task that reads one by one descriptor and puts the full buffers to the full buffers
	 * queue.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ReadTask implements Runnable {

		/**
		 * Lock for stop reading.
		 */
		private Lock continueReadLock = new ReentrantLock();

		/**
		 * Condition for signaling continue reading can occur.
		 */
		private Condition canContinueRead = continueReadLock.newCondition();

		/**
		 * Flag for waiting, since signal/await is problematic due it's unknown if the waiting
		 * thread will go into the await state before the signal comes.
		 */
		private AtomicBoolean wait = new AtomicBoolean();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			// we run the task until all descriptors are processed
			while (nextDescriptorIndex.get() < descriptors.size()) {
				IStorageDescriptor storageDescriptor = descriptors.get(nextDescriptorIndex.get());
				Path channelPath = storageManager.getChannelPath(storageData, storageDescriptor);
				openedChannelPaths.add(channelPath);
				long readPosition = storageDescriptor.getPosition();
				long readSize = 0;
				while (readSize < storageDescriptor.getSize()) {
					// we read until whole descriptor size has been read
					ByteBuffer buffer = null;
					try {
						buffer = getEmptyBuffers().take();
					} catch (InterruptedException e) {
						Thread.interrupted();
					}
					buffer.clear();
					final ByteBuffer finalByteBuffer = buffer;
					// in single shot we can read only till the buffer's capacity
					long singleReadSize = Math.min(storageDescriptor.getSize() - readSize, buffer.capacity());
					WriteReadCompletionRunnable completionRunnable = new WriteReadCompletionRunnable() {
						@Override
						public void run() {
							if (isCompleted()) {
								// add buffer to the queue
								getFullBuffers().add(finalByteBuffer);
							} else {
								// if is failed, return buffer to empty buffers and decrease the
								// total read size because we can not read that amount of bytes
								finalByteBuffer.clear();
								getEmptyBuffers().add(finalByteBuffer);
								setTotalSize(getTotalSize() - getAttemptedWriteReadSize());
							}
							// signal continue reading if await is active
							continueReadLock.lock();
							try {
								wait.set(false);
								canContinueRead.signal();
							} finally {
								continueReadLock.unlock();
							}
						}
					};

					try {
						// execute read
						wait.set(true);
						readingChannelManager.read(finalByteBuffer, readPosition, singleReadSize, channelPath, completionRunnable);
						// update the position and size for this descriptor
						readSize += singleReadSize;
						readPosition += singleReadSize;
						if (readSize < storageDescriptor.getSize()) {
							// if the descriptor has not been read completely we have to block until
							// the read is finished
							// this ensures that the data for one descriptor will be read in order
							while (wait.get()) {
								continueReadLock.lock();
								try {
									canContinueRead.awaitNanos(5000);
								} catch (InterruptedException e) {
									Thread.interrupted();
								} finally {
									continueReadLock.unlock();
								}
							}
						}
					} catch (IOException e) {
						log.warn("Exception occurred trying to read in the ReadTask.", e);
					}
				}
				nextDescriptorIndex.incrementAndGet();
			}
		}
	}

}
