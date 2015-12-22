package info.novatec.inspectit.storage;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.WritingStatus;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.stream.ExtendedByteBufferOutputStream;
import info.novatec.inspectit.storage.nio.stream.StreamProvider;
import info.novatec.inspectit.storage.nio.write.WritingChannelManager;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.processor.write.AbstractWriteDataProcessor;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.esotericsoftware.kryo.io.Output;

/**
 * {@link StorageWriter} is class that contains shared functionality for writing data on one
 * storage. It can be overwritten, with special additional functionality, but care needs to be taken
 * that methods of this class are correctly called in super classes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageWriter implements IWriter {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * Amount of time to re-check if the writing tasks are done and finalization can start.
	 */
	private static final int FINALIZATION_TASKS_SLEEP_TIME = 500;

	/**
	 * Total amount of tasks submitted to {@link #writingExecutorService}.
	 */
	private long totalTasks = 0;

	/**
	 * Total amount of finished tasks by {@link #writingExecutorService}.
	 */
	private long finishedTasks = 0;

	/**
	 * {@link StorageManager}.
	 */
	@Autowired
	StorageManager storageManager;

	/**
	 * Storage to write to.
	 */
	private StorageData storageData;

	/**
	 * Indexing tree handler.
	 */
	@Autowired
	StorageIndexingTreeHandler indexingTreeHandler;

	/**
	 * Path used for writing.
	 */
	private Path writingFolderPath;

	/**
	 * {@link WritingChannelManager}.
	 */
	@Autowired
	WritingChannelManager writingChannelManager;

	/**
	 * {@link SerializationManagerProvider}.
	 */
	@Autowired
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * Queue for {@link ISerializer} that are available.
	 */
	BlockingQueue<ISerializer> serializerQueue = new LinkedBlockingQueue<ISerializer>();

	/**
	 * {@link ExecutorService} for writing tasks.
	 */
	@Autowired
	@Resource(name = "storageExecutorService")
	private ScheduledThreadPoolExecutor writingExecutorService;

	/**
	 * {@link ExecutorService} for not-writing tasks.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService scheduledExecutorService;

	/**
	 * {@link StreamProvider}.
	 */
	@Autowired
	StreamProvider streamProvider;

	/**
	 * List of finalization data processors.
	 */
	@Autowired
	List<AbstractWriteDataProcessor> writeDataProcessors;

	/**
	 * Opened channels {@link Paths}. These paths need to be closed when writing is finalized.
	 */
	private Set<Path> openedChannelPaths = Collections.newSetFromMap(new ConcurrentHashMap<Path, Boolean>(32, 0.75f, 1));

	/**
	 * Defines if the writer is ready for writing, thus is writing active.
	 */
	private volatile boolean writingOn = false;

	/**
	 * Status of writing. Initially status is {@link WritingStatus#GOOD}.
	 */
	private WritingStatus writingStatus = WritingStatus.GOOD;

	/**
	 * If the writer is finalized.
	 */
	private boolean finalized = false;

	/**
	 * Future for the task of checking the writing status.
	 */
	private ScheduledFuture<?> checkWritingStatusFuture;

	/**
	 * The set of the currently active writing tasks represented by {@link FutureTask}. When this
	 * set is empty, it means that no writing tasks is currently being executed.
	 */
	private Set<FutureTask<?>> activeWritingTasks = Collections.newSetFromMap(new ConcurrentHashMap<FutureTask<?>, Boolean>(256, 0.75f, 4));

	/**
	 * Process the list of objects against the all the {@link AbstractDataProcessor}s that are
	 * provided. Processor define which data will be stored, when and in which format.
	 * <p>
	 * If null is passed as a processors list the data will be directly written.
	 * <p>
	 * The write will be done asynchronously, thus the method will return after creating writing
	 * tasks and not waiting for actual write to take place.
	 * 
	 * @param defaultDataList
	 *            List of objects to process.
	 * @param processors
	 *            List of processors. Can be null, and in this case direct write will be executed.
	 * @return Returns collection of void {@link Future}s, one for each writing task that has been
	 *         creating while data has been processed. These futures provide only the information
	 *         when the single writing task is executed, but not when the serialized bytes are
	 *         actually written on disk.
	 */
	public Collection<Future<Void>> process(Collection<? extends DefaultData> defaultDataList, Collection<AbstractDataProcessor> processors) {
		List<Future<Void>> futureList = new ArrayList<>();
		if (null != processors && !processors.isEmpty()) {
			// first prepare processors
			for (AbstractDataProcessor processor : processors) {
				processor.setStorageWriter(this);
			}

			// the write all data
			for (DefaultData defaultData : defaultDataList) {
				for (AbstractDataProcessor processor : processors) {
					futureList.addAll(processor.process(defaultData));
				}
			}

			// at the end flush the data from processors and reset its storage writer
			for (AbstractDataProcessor processor : processors) {
				futureList.addAll(processor.flush());
				processor.setStorageWriter(null);
			}
		} else {
			// the write all data with out processing
			for (DefaultData defaultData : defaultDataList) {
				Future<Void> future = this.write(defaultData);
				futureList.add(future);
			}
		}
		return futureList;
	}

	/**
	 * Processes the write of collection in the way that this method will return only when all data
	 * is written on the disk. In any other way this method is same as
	 * {@link #process(Collection, Collection)}.
	 * 
	 * @param defaultDataList
	 *            List of objects to process.
	 * @param processors
	 *            List of processors. Can be null, and in this case direct write will be executed.
	 */
	public void processSynchronously(Collection<? extends DefaultData> defaultDataList, Collection<AbstractDataProcessor> processors) {
		Collection<Future<Void>> futures = this.process(defaultDataList, processors);
		while (!futures.isEmpty()) {
			for (Iterator<Future<Void>> it = futures.iterator(); it.hasNext();) {
				Future<Void> future = it.next();
				if (future.isDone()) {
					it.remove();
				}
			}

			// if still are not done sleep
			if (!futures.isEmpty()) {
				try {
					Thread.sleep(FINALIZATION_TASKS_SLEEP_TIME);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is only submitting a new writing task, thus it is thread safe and very fast.
	 */
	public Future<Void> write(DefaultData defaultData) {
		return write(defaultData, Collections.emptyMap());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is only submitting a new writing task, thus it is thread safe and very fast.
	 */
	public Future<Void> write(DefaultData defaultData, Map<?, ?> kryoPreferences) {
		if (writingOn && storageManager.canWriteMore()) {
			for (AbstractWriteDataProcessor processor : writeDataProcessors) {
				try {
					processor.process(defaultData, kryoPreferences);
				} catch (Exception e) {
					log.error("Exception occurred processing the data with the finalization data processor " + processor.getClass().getName(), e);
				}
			}

			WriteTask writeTask = new WriteTask(defaultData, kryoPreferences);
			WriteFutureTask writeFutureTask = new WriteFutureTask(writeTask);
			activeWritingTasks.add(writeFutureTask);
			writingExecutorService.submit(writeFutureTask);
			return writeFutureTask;
		} else {
			return null;
		}
	}

	/**
	 * Informs the {@link StorageWriter} to prepare for writing. The writer will perform all
	 * necessary operations so that calls to {@link #write(DefaultData)} can be executed. The
	 * {@link StorageWriter} will be in prepared state until {@link #finalizeWrite()} method is
	 * called.
	 * 
	 * @param storageData
	 *            Storage to write to.
	 * @return True if the preparation was successfully done, otherwise false.
	 * @throws IOException
	 *             IOException occurred.
	 */
	public synchronized boolean prepareForWrite(StorageData storageData) throws IOException {
		if (!writingOn) {
			this.storageData = storageData;
			writingFolderPath = storageManager.getStoragePath(storageData);
			// if path does not exists create
			if (!Files.exists(writingFolderPath)) {
				Files.createDirectories(writingFolderPath);
			}

			// prepare the indexing tree handler
			indexingTreeHandler.prepare();

			// activate check writing status task manually
			checkWritingStatusFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					if (writingOn) {
						checkWritingStatus();
					}
				}
			}, 30, 30, TimeUnit.SECONDS);

			for (AbstractWriteDataProcessor processor : writeDataProcessors) {
				try {
					processor.onPrepare(storageManager, this, storageData);
				} catch (Exception e) {
					log.error("Exception occurred trying to process onPrepare of the finalization data processor " + processor.getClass().getName(), e);
				}
			}

			writingOn = true;
			return true;
		}
		return false;
	}

	/**
	 * Cancels the usage of this {@link StorageWriter}.
	 * <p>
	 * Writer will shutdown it's executor service which will disable the further writes. It will
	 * also cancel the indexing tree handler and close all opened channel paths. This method should
	 * only be used when Storage that has been written with this writer is no needed any more.
	 */
	public final synchronized void cancel() {
		shutdown(false);
	}

	/**
	 * Performs all operation prior to finalizing the write and then calls {@link #finalizeWrite()}.
	 */
	public final synchronized void closeStorageWriter() {
		shutdown(true);
	}

	/**
	 * This method will wait until all pending writing tasks are finished, but after it's invocation
	 * no new tasks will be accepted.
	 * <p>
	 * Sub-classes can override this method to include additional writes before the storage write is
	 * finalized. Note that the overriding of this method has to be in the way to first execute the
	 * additional saving, and the call super.finalizeWrite(boolean).
	 * 
	 */
	protected synchronized void finalizeWrite() {
		if (!finalized) {
			for (AbstractWriteDataProcessor processor : writeDataProcessors) {
				try {
					processor.onFinalization(storageManager, this, storageData);
				} catch (Exception e) {
					log.error("Exception occurred trying to process onFinalize of the finalization data processor " + processor.getClass().getName(), e);
				}
			}

			// when nothing more is left save the indexing tree
			// save tree only if executeWrites is true
			indexingTreeHandler.finish();

			finalized = true;

			if (log.isDebugEnabled()) {
				log.debug("Finalization done for storage: " + storageData + ".");
			}
		}
	}

	/**
	 * Shutdown this storage writer. If finalize is true, {@link #finalizeWrite()} will be called in
	 * addition.
	 * 
	 * @param doFinalize
	 *            If {@link #finalizeWrite()} should be called and thus write indexing tree and
	 *            other needed data.
	 */
	private synchronized void shutdown(boolean doFinalize) {
		if (writingOn) {
			// mark writing false so that no more task are created
			writingOn = false;

			// cancel the check writing status task
			checkWritingStatusFuture.cancel(false);

			// wait for pending tasks
			waitForPendingWritingTasks();

			// shut the executor
			shutdownWritingExecutorService();

			if (doFinalize) {
				finalizeWrite();
			}

			try {
				// close all opened channels
				for (Path channelPath : openedChannelPaths) {
					writingChannelManager.finalizeChannel(channelPath);
				}
			} catch (IOException e) {
				log.warn("Closing one of the opened file channels failed.", e);
			}

		}
	}

	/**
	 * Correctly shuts down the {@link #writingExecutorService}.
	 */
	private synchronized void shutdownWritingExecutorService() {
		if (writingExecutorService.isShutdown()) {
			return;
		}

		// Disable new tasks from being submitted
		writingExecutorService.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!writingExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
				// Cancel currently executing tasks
				writingExecutorService.shutdownNow();
				// Wait a while for tasks to respond to being canceled
				if (!writingExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
					log.error("Executor service of the Storage writer for the storage " + storageData + " did not terminate.");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			writingExecutorService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Blocks until all pending writing tasks to be finished.
	 */
	private synchronized void waitForPendingWritingTasks() {
		boolean logged = false;
		// check amount of active tasks
		while (true) {
			long activeTasks = getQueuedTaskCount();
			if (activeTasks > 0) {
				if (log.isDebugEnabled() && !logged) {
					log.info("Storage: " + storageData + " is waiting for finalization. Still " + activeTasks + " queued tasks need to be processed.");
					logged = true;
				}
				// if still are not done sleep
				try {
					Thread.sleep(FINALIZATION_TASKS_SLEEP_TIME);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Number of queued tasks in the executor service.
	 * 
	 * @return Number of queued tasks in the executor service.
	 */
	public long getQueuedTaskCount() {
		return activeWritingTasks.size();
	}

	/**
	 * Writes any object to the file with given file name. Note that this will be a synchronus
	 * write.
	 * 
	 * @param object
	 *            Object to write. Note that object of this kind has to be serializable by
	 *            {@link ISerializer}.
	 * @param fileName
	 *            Name of the file to save data to.
	 * @return True if the object was written successfully, otherwise false.
	 */
	public boolean writeNonDefaultDataObject(Object object, String fileName) {
		try {
			ISerializer serializer = null;
			try {
				serializer = serializerQueue.take();
			} catch (InterruptedException e1) {
				Thread.interrupted();
			}
			if (null == serializer) {
				log.error("Serializer instance could not be obtained.");
				return false;
			}

			// prepare path
			Path path = writingFolderPath.resolve(fileName);
			if (Files.exists(path)) {
				try {
					Files.delete(path);
				} catch (IOException e) {
					log.error("Exception thrown trying to delete file from disk", e);
					return false;
				}
			}

			// open and write via NIO api
			try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
				Output output = new Output(outputStream);
				serializer.serialize(object, output);
			} catch (SerializationException e) {
				log.error("Serialization for the object " + object + " failed. Data will be skipped.", e);
				return false;
			} finally {
				serializerQueue.add(serializer);
			}

			return true;
		} catch (Throwable throwable) { // NOPMD
			log.error("Exception occurred while attempting to write data to disk", throwable);
			return false;
		}
	}

	/**
	 * Updates the write status.
	 */
	private void checkWritingStatus() {
		if (null != writingExecutorService) {
			long completedTasks = writingExecutorService.getCompletedTaskCount();
			long queuedTasks = writingExecutorService.getTaskCount() - completedTasks;

			long arrivedTasksForPeriod = queuedTasks + completedTasks - totalTasks;
			long finishedTasksForPeriod = completedTasks - finishedTasks;

			writingStatus = WritingStatus.getWritingStatus(arrivedTasksForPeriod, finishedTasksForPeriod);

			finishedTasks = completedTasks;
			totalTasks = completedTasks + queuedTasks;
		} else {
			writingStatus = WritingStatus.GOOD;
		}
	}

	/**
	 * Task for writing one {@link DefaultData} object to the disk.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public class WriteTask implements Runnable {

		/**
		 * reference to write data.
		 */
		private SoftReference<DefaultData> referenceToWriteData;

		/**
		 * Map of preferences to be passed to the serializer.
		 */
		private Map<?, ?> kryoPreferences;

		/**
		 * Default constructor. Object to be written.
		 * 
		 * @param data
		 *            Data to be written.
		 * @param kryoPreferences
		 *            Map of preferences to be passed to the serializer.
		 */
		public WriteTask(DefaultData data, Map<?, ?> kryoPreferences) {
			referenceToWriteData = new SoftReference<DefaultData>(data);
			this.kryoPreferences = kryoPreferences;
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			ExtendedByteBufferOutputStream extendedByteBufferOutputStream = null;
			try {
				if (!storageManager.canWriteMore()) {
					if (log.isWarnEnabled()) {
						log.warn("Writing of data canceled because of limited hard disk space left for the storage.");
					}
					return;
				}

				// get object from soft reference
				final DefaultData data = referenceToWriteData.get();
				if (null == data) {
					log.warn("Failed to write data to storage. The data to be written was already garbage collected due to the high amount of writing tasks.");
					return;
				}

				int channelId = 0;
				// get channel id
				try {
					channelId = indexingTreeHandler.startWrite(this);
				} catch (IndexingException e) {
					indexingTreeHandler.writeFailed(this);
					if (log.isDebugEnabled()) {
						log.debug("Indexing exception occurred while attempting to write data to disk.", e);
					}
					return;
				}

				if (0 == channelId) {
					indexingTreeHandler.writeFailed(this);
					log.error("Channel ID could not be obtained during attempt to write data to disk. Data will be skipped.");
					return;
				}

				ISerializer serializer = null;
				try {
					serializer = serializerQueue.take();
				} catch (InterruptedException e1) {
					Thread.interrupted();
				}
				if (null == serializer) {
					indexingTreeHandler.writeFailed(this);
					log.error("Serializer instance could not be obtained.");
					return;
				}

				extendedByteBufferOutputStream = streamProvider.getExtendedByteBufferOutputStream();
				try {
					Output output = new Output(extendedByteBufferOutputStream);
					serializer.serialize(data, output, kryoPreferences);
					extendedByteBufferOutputStream.flush(false);
				} catch (SerializationException e) {
					extendedByteBufferOutputStream.close();
					indexingTreeHandler.writeFailed(this);
					serializerQueue.add(serializer);
					if (log.isWarnEnabled()) {
						log.warn("Serialization for the object " + data + " failed. Data will be skipped.", e);
					}
					return;
				}
				serializerQueue.add(serializer);

				// final reference needed because of the runnable
				int buffersToWrite = extendedByteBufferOutputStream.getBuffersCount();
				final ExtendedByteBufferOutputStream finalOutputStream = extendedByteBufferOutputStream;
				WriteReadCompletionRunnable completionRunnable = new WriteReadCompletionRunnable(buffersToWrite) {
					@Override
					public void run() {
						finalOutputStream.close();
						if (isCompleted()) {
							indexingTreeHandler.writeSuccessful(WriteTask.this, getAttemptedWriteReadPosition(), getAttemptedWriteReadSize());
						} else {
							indexingTreeHandler.writeFailed(WriteTask.this);

						}
					}
				};

				// write to disk
				Path channelPath = storageManager.getChannelPath(storageData, channelId);
				openedChannelPaths.add(channelPath);
				try {
					// position and size will be set in the completion runnable
					writingChannelManager.write(extendedByteBufferOutputStream, channelPath, completionRunnable);
				} catch (IOException e) {
					// remove from indexing tree if exception occurs
					extendedByteBufferOutputStream.close();
					indexingTreeHandler.writeFailed(this);
					log.error("Exception occurred while attempting to write data to disk", e);
					return;
				}
			} catch (Throwable t) { // NOPMD
				// catch any exception
				if (null != extendedByteBufferOutputStream) {
					extendedByteBufferOutputStream.close();
				}
				indexingTreeHandler.writeFailed(this);
				log.error("Unknown exception occurred during data write", t);
			}
		}

		/**
		 * @return Returns data to be written by this task.
		 */
		public DefaultData getData() {
			return referenceToWriteData.get();
		}

	}

	/**
	 * Writing future task that will remove itself from the {@link StorageWriter#activeWritingTasks}
	 * set after the completion of runnable it has been assigned.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class WriteFutureTask extends FutureTask<Void> {

		/**
		 * Default constructor.
		 * 
		 * @param runnable
		 *            Runnable to execute.
		 */
		public WriteFutureTask(Runnable runnable) {
			super(runnable, null);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void done() {
			activeWritingTasks.remove(this);
		}

	}

	/**
	 * Returns write path for this writer.
	 * 
	 * @return Returns write path for this writer.
	 */
	public Path getWritingFolderPath() {
		return writingFolderPath;
	}

	/**
	 * Returns executor service status. This methods just returns the result of
	 * {@link #executorService#toString()} method.
	 * 
	 * @return Returns executor service status. This methods just returns the result of
	 *         {@link #executorService#toString()} method.
	 */
	public String getExecutorServiceStatus() {
		return writingExecutorService.toString();
	}

	/**
	 * Gets {@link #writingOn}.
	 * 
	 * @return {@link #writingOn}
	 */
	public boolean isWritingOn() {
		return writingOn;
	}

	/**
	 * Gets {@link #storageData}.
	 * 
	 * @return {@link #storageData}
	 */
	public StorageData getStorageData() {
		return storageData;
	}

	/**
	 * Gets {@link #writingStatus}.
	 * 
	 * @return {@link #writingStatus}
	 */
	public WritingStatus getWritingStatus() {
		return writingStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		indexingTreeHandler.registerStorageWriter(this);

		// we create the same number of kryo instances as the size of the executor service
		// this way every write task will not wait for a reference because one will always be
		// available
		int threads = writingExecutorService.getCorePoolSize();
		for (int i = 0; i < threads; i++) {
			serializerQueue.add(serializationManagerProvider.createSerializer());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("storageData", storageData);
		toStringBuilder.append("writingFolderPath", writingFolderPath);
		toStringBuilder.append("writingOn", writingOn);
		toStringBuilder.append("executorService", writingExecutorService);
		toStringBuilder.append("openedChannelPaths", openedChannelPaths);
		return toStringBuilder.toString();
	}

}