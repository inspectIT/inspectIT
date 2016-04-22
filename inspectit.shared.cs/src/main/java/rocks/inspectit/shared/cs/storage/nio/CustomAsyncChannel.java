package rocks.inspectit.shared.cs.storage.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that defines special need operation with {@link AsynchronousFileChannel}.
 *
 * @author Ivan Senic
 *
 */
public class CustomAsyncChannel {

	/**
	 * The log of this class. Can not be assigned via spring because this is not a component.
	 */
	private Logger log = LoggerFactory.getLogger(CustomAsyncChannel.class);;

	/**
	 * Path where the channel's file is.
	 */
	private Path path;

	/**
	 * {@link AsynchronousFileChannel}.
	 */
	private AsynchronousFileChannel fileChannel;

	/**
	 * Next writing position.
	 */
	private AtomicLong nextWritingPosition = new AtomicLong();

	/**
	 * Read write lock.
	 */
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	/**
	 * Lock for opening and closing. Has to be the write lock, because the state of channel is
	 * changing.
	 */
	private Lock openCloseLock = readWriteLock.writeLock();

	/**
	 * Lock for writing. This lock only has to be closed, when the {@link #openCloseLock} is locked.
	 * Thus, we can use read lock.
	 */
	private Lock writeReadChannelLock = readWriteLock.readLock();

	/**
	 * Default constructor.
	 *
	 * @param path
	 *            Path to the channel's file.
	 */
	public CustomAsyncChannel(Path path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * @return the fileChannel
	 */
	public AsynchronousFileChannel getFileChannel() {
		return fileChannel;
	}

	/**
	 * Opens the channel creating the file in the given path to the directory. The
	 * {@link AsynchronousFileChannel} will work with default {@link ExecutorService}.
	 *
	 * @throws IOException
	 *             When {@link IOException} occurs during opening.
	 */
	public void openChannel() throws IOException {
		this.openChannel(null);
	}

	/**
	 * Opens the channel creating the file in the given path to the directory. The
	 * {@link AsynchronousFileChannel} will work with provided {@link ExecutorService}.
	 *
	 * @param executorService
	 *            Executor service that has threads that will work on
	 *            {@link AsynchronousFileChannel}.
	 * @return True if the channel is opened, false if the channel is already opened and thus
	 *         operation is skipped.
	 * @throws IOException
	 *             When {@link IOException} occurs during opening.
	 */
	public boolean openChannel(ExecutorService executorService) throws IOException {
		openCloseLock.lock();
		try {
			if (!this.isOpened()) {
				Set<OpenOption> optionsSet = new HashSet<>();
				optionsSet.add(StandardOpenOption.CREATE);
				optionsSet.add(StandardOpenOption.WRITE);
				optionsSet.add(StandardOpenOption.READ);
				fileChannel = AsynchronousFileChannel.open(path, optionsSet, executorService, new FileAttribute<?>[0]);

				if (log.isDebugEnabled()) {
					log.info("Channel opened for path " + path + ". Next write position is " + nextWritingPosition.get() + ".");
				}

				return true;
			} else {
				if (log.isDebugEnabled()) {
					log.info("Tried to open already opened channel for path " + path + ".");
				}

				return false;
			}
		} finally {
			openCloseLock.unlock();
		}
	}

	/**
	 * Closes the channel. Note that no write will be possible after calling this method.
	 *
	 * @return True if channel was closed, false if the channel was already closed.
	 *
	 * @throws IOException
	 *             If {@link IOException} happens during closing.
	 */
	public boolean closeChannel() throws IOException {
		openCloseLock.lock();
		try {
			if (this.isOpened()) {
				fileChannel.force(true);
				fileChannel.close();
				if (log.isDebugEnabled()) {
					log.info("Channel closed for path " + path + ". Next write position is " + nextWritingPosition.get() + ".");
				}

				return true;
			} else {
				if (log.isDebugEnabled()) {
					log.info("Tried to close already closed channel for path " + path + ".");
				}

				return false;
			}
		} finally {
			openCloseLock.unlock();
		}
	}

	/**
	 * Returns if channel is open.
	 *
	 * @return Returns if channel is open.
	 */
	public boolean isOpened() {
		return (fileChannel != null) && fileChannel.isOpen();
	}

	/**
	 * Writes to the file channel if the channel is open. If the channel is closed, the write will
	 * not be done, and false will be returned.
	 *
	 * @param <A>
	 *            Type of attachment.
	 * @param src
	 *            Buffer.
	 * @param position
	 *            Position.
	 * @param attachment
	 *            Attachment.
	 * @param handler
	 *            Completion handler.
	 * @return True if write succeeds. Fails if the channel is closed.
	 */
	public <A> boolean write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
		writeReadChannelLock.lock();
		try {
			if (this.isOpened()) {
				fileChannel.write(src, position, attachment, handler);
				return true;
			} else {
				return false;
			}
		} finally {
			writeReadChannelLock.unlock();
		}
	}

	/**
	 * Reads to the file channel if the channel is open. If the channel is closed, the read will not
	 * be done, and false will be returned.
	 *
	 * @param <A>
	 *            Type of attachment.
	 * @param dst
	 *            Destination buffer. Buffer.
	 * @param position
	 *            Position.
	 * @param attachment
	 *            Attachment.
	 * @param handler
	 *            Completion handler.
	 * @return True if read succeeds. Fails if the channel is closed.
	 */
	public <A> boolean read(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
		writeReadChannelLock.lock();
		try {
			if (this.isOpened()) {
				fileChannel.read(dst, position, attachment, handler);
				return true;
			} else {
				return false;
			}
		} finally {
			writeReadChannelLock.unlock();
		}
	}

	/**
	 * Reserves the writing position in this channel with the given size. This method is thread
	 * safe.
	 *
	 * @param writeSize
	 *            Size of writing that has to be done.
	 * @return Returns the position where file should be written.
	 */
	public long reserveWritingPosition(long writeSize) {
		while (true) {
			long writingPosition = nextWritingPosition.get();
			if (nextWritingPosition.compareAndSet(writingPosition, writingPosition + writeSize)) {
				return writingPosition;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("path", path);
		toStringBuilder.append("opened", isOpened());
		toStringBuilder.append("nextWritingPosition", nextWritingPosition.get());
		return toStringBuilder.toString();
	}
}
