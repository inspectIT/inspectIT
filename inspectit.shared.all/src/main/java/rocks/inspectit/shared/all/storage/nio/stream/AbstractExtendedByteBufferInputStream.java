package rocks.inspectit.shared.all.storage.nio.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.esotericsoftware.kryo.io.ByteBufferInputStream;

import rocks.inspectit.shared.all.storage.nio.ByteBufferProvider;

/**
 * This is abstract class for all input streams that can read data with limited numbers of buffers.
 * <p>
 * The implementing classes must ensure that a buffer is taken from the empty buffers queue, filled
 * with data and then placed in the full buffers queue. It's on the implementing classes to
 * introduce the logic for this.
 * <p>
 * This class implements the classic input stream methods like {@link #read()},
 * {@link #read(byte[])} and {@link #read(byte[], int, int)}, as well as {@link #close()} and
 * {@link #prepare()}. Thus, sub-classes don't have to care about implementing this, as long as they
 * provide the data in the full buffers queue.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractExtendedByteBufferInputStream extends ByteBufferInputStream {

	/**
	 * Minimum amount of buffers that can be used.
	 */
	private static final int MIN_BUFFERS = 2;

	/**
	 * Maximum amount of buffers that can be used.
	 */
	private static final int MAX_BUFFERS = 5;

	/**
	 * MAx amount of tries to get the full or empty buffer from the queues.
	 */
	protected static final int MAX_BUFFER_POOL_TRIES = 30;

	/**
	 * {@link ByteBufferProvider}.
	 */
	@Autowired
	ByteBufferProvider byteBufferProvider;

	/**
	 * Amount of buffers to use during read.
	 */
	private int numberOfBuffers;

	/**
	 * Total size that has to be read.
	 */
	private long totalSize;

	/**
	 * Current streaming position.
	 */
	private long position;

	/**
	 * Queue of empty buffers. These buffers will be filled with the information from the disk.
	 */
	private LinkedBlockingQueue<ByteBuffer> emptyBuffers = new LinkedBlockingQueue<ByteBuffer>();

	/**
	 * Queue of full buffers. These buffers will be used to stream data.
	 */
	private LinkedBlockingQueue<ByteBuffer> fullBuffers = new LinkedBlockingQueue<ByteBuffer>();

	/**
	 * If stream has been closed.
	 */
	private volatile boolean closed;

	/**
	 * Boolean for flagging that in some way reading has failed. For example a read from socket
	 * returned an IO exception or the read resulted with -1 read size signaling that the stream has
	 * been closed unexpectedly.
	 * <p>
	 * Sub-classes must set this to true when data could not be provided anymore (stream reached end
	 * for example).
	 */
	private volatile boolean readFailed;

	/**
	 * No-arg constructor.
	 */
	public AbstractExtendedByteBufferInputStream() {
	}

	/**
	 * Constructor that defines number of bytes to use.
	 *
	 * @param numberOfBuffers
	 *            Number of buffers.
	 */
	public AbstractExtendedByteBufferInputStream(int numberOfBuffers) {
		this.numberOfBuffers = numberOfBuffers;
	}

	/**
	 * Prepares the stream for read. Must be called before any read operation is executed.
	 * <p>
	 * Implementing classes must extend this method in way that full buffers queue is filled with
	 * data that will be available for the reader of input stream.
	 *
	 * @throws IOException
	 *             if preparation fails due to inability to obtain defined number of byte buffers
	 */
	public void prepare() throws IOException {
		// get the buffers first
		int buffers = numberOfBuffers;
		if (buffers < MIN_BUFFERS) {
			buffers = MIN_BUFFERS;
		} else if (buffers > MAX_BUFFERS) {
			buffers = MAX_BUFFERS;
		}
		for (int i = 0; i < buffers; i++) {
			ByteBuffer byteBuffer = byteBufferProvider.acquireByteBuffer();
			emptyBuffers.add(byteBuffer);
		}
		numberOfBuffers = buffers;
	}

	/**
	 * Returns if the stream has more bytes remaining to stream.
	 *
	 * @return True if stream can provide more bytes.
	 */
	public boolean hasRemaining() {
		return bytesLeft() > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() throws IOException {
		return (int) bytesLeft();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		// if we are empty, return -1 by the input stream contract
		if ((0 == totalSize) || !hasRemaining()) {
			return -1;
		}

		try {
			// change the buffer if necessary
			if (hasRemaining() || (null == super.getByteBuffer())) {
				bufferChange();
			}

			if (!super.getByteBuffer().hasRemaining()) {
				// check if we can read more
				if (hasRemaining()) {
					bufferChange();
					int read = super.read();
					position += read;
					return read;
				} else {
					return -1;
				}
			} else {
				int read = super.read();
				position += read;
				return read;
			}
		} catch (ReadFailedException e) {
			throw new IOException("Read from the input stream failed.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		// if we are empty, return -1 by the input stream contract
		if ((0 == totalSize) || !hasRemaining()) {
			return -1;
		}

		// don't try to read anything if the required length is 0
		if (0 == len) {
			return 0;
		}

		try {
			// change the buffer if necessary
			if (hasRemaining() && (null == super.getByteBuffer())) {
				bufferChange();
			}

			int bufferRemaining = super.getByteBuffer().remaining();
			if (bufferRemaining >= len) {
				int read = super.read(b, off, len);
				position += read;
				return read;
			} else {
				int res = 0;
				if (bufferRemaining > 0) {
					super.getByteBuffer().get(b, off, bufferRemaining);
					res = bufferRemaining;
					position += bufferRemaining;
				}
				if (hasRemaining()) {
					bufferChange();
					int read = this.read(b, off + bufferRemaining, len - bufferRemaining);
					res += read;
				}

				if (res > 0) {
					return res;
				} else {
					return -1;
				}
			}
		} catch (ReadFailedException e) {
			throw new IOException("Read from the input stream failed.", e);
		}
	}

	/**
	 * Changes the current buffer used for streaming with a full one.
	 *
	 * @throws ReadFailedException
	 *             if buffer change can not be performed due to the flagged {@link #readFailed}.
	 */
	private synchronized void bufferChange() throws ReadFailedException {
		ByteBuffer current = super.getByteBuffer();
		if (null != current) {
			current.clear();
			emptyBuffers.add(current);
		}

		int tries = 0;
		while (true) {
			try {
				// poll for full buffer
				ByteBuffer buffer = fullBuffers.poll(100, TimeUnit.MILLISECONDS);
				if (null != buffer) {
					// if we have full buffer, set is as current and break from while
					super.setByteBuffer(buffer);
					break;
				} else {
					tries++;
					if (readFailed || (tries > MAX_BUFFER_POOL_TRIES)) {
						throw new ReadFailedException("Time-out trying to get the full byte buffer to read from.");
					}
				}
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
	}

	/**
	 * Return number of bytes left for read.
	 *
	 * @return Number of bytes left.
	 */
	private long bytesLeft() {
		return totalSize - position;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Releases all byte buffers that are hold.
	 */
	@Override
	public synchronized void close() throws IOException {
		if (closed) {
			return;
		}

		int releasedBuffers = 0;
		while (releasedBuffers < numberOfBuffers) {
			// release buffers from both queues
			while (!fullBuffers.isEmpty()) {
				ByteBuffer byteBuffer = fullBuffers.poll();
				if (null != byteBuffer) {
					byteBufferProvider.releaseByteBuffer(byteBuffer);
					releasedBuffers++;
				}
			}
			while (!emptyBuffers.isEmpty()) {
				ByteBuffer byteBuffer = emptyBuffers.poll();
				if (null != byteBuffer) {
					byteBufferProvider.releaseByteBuffer(byteBuffer);
					releasedBuffers++;
				}
			}

			// also release the one we could have set for current reading
			ByteBuffer currentBuffer = super.getByteBuffer();
			if (null != currentBuffer) {
				byteBufferProvider.releaseByteBuffer(currentBuffer);
				releasedBuffers++;
				super.setByteBuffer(null);
			}
		}

		closed = true;
	}

	/**
	 * Gets {@link #totalSize}.
	 *
	 * @return {@link #totalSize}
	 */
	public long getTotalSize() {
		return totalSize;
	}

	/**
	 * Sets {@link #totalSize}.
	 *
	 * @param totalSize
	 *            New value for {@link #totalSize}
	 */
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	/**
	 * Gets {@link #emptyBuffers}.
	 *
	 * @return {@link #emptyBuffers}
	 */
	public LinkedBlockingQueue<ByteBuffer> getEmptyBuffers() {
		return emptyBuffers;
	}

	/**
	 * Gets {@link #fullBuffers}.
	 *
	 * @return {@link #fullBuffers}
	 */
	public LinkedBlockingQueue<ByteBuffer> getFullBuffers() {
		return fullBuffers;
	}

	/**
	 * Gets {@link #closed}.
	 *
	 * @return {@link #closed}
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Sets {@link #byteBufferProvider}.
	 *
	 * @param byteBufferProvider
	 *            New value for {@link #byteBufferProvider}
	 */
	public void setByteBufferProvider(ByteBufferProvider byteBufferProvider) {
		this.byteBufferProvider = byteBufferProvider;
	}

	/**
	 * Sets {@link #position}.
	 *
	 * @param position
	 *            New value for {@link #position}
	 */
	protected void setPosition(long position) {
		this.position = position;
	}

	/**
	 * Sets {@link #readFailed}.
	 *
	 * @param readFailed
	 *            New value for {@link #readFailed}
	 */
	protected void setReadFailed(boolean readFailed) {
		this.readFailed = readFailed;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Closing the stream on finalize.
	 */
	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	/**
	 * Checked exception to be used when read has failed. As this will not be propagated outside
	 * this class, it's private and final.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static final class ReadFailedException extends Exception {

		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = 706702393154564017L;

		/**
		 * Default constructor.
		 *
		 * @param message
		 *            Message
		 */
		ReadFailedException(String message) {
			super(message);
		}

	}

}
