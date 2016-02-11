package info.novatec.inspectit.storage.nio.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Input stream that uses {@link SocketChannel} as input and provides bytes via our
 * {@link AbstractExtendedByteBufferInputStream} methodology.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class SocketExtendedByteBufferInputStream extends AbstractExtendedByteBufferInputStream {

	/**
	 * {@link SocketChannel} to read from.
	 */
	private SocketChannel socketChannel;

	/**
	 * Executor service to execute socket reads.
	 */
	@Autowired
	@Qualifier("socketReadExecutorService")
	private ExecutorService executorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepare() throws IOException {
		super.prepare();

		if (getTotalSize() > 0) {
			executorService.execute(new SocketReadRunnable(getTotalSize()));
		}
	}

	/**
	 * Resets the input stream so that new read from the channel can be executed with wanted length.
	 * <p>
	 * This method makes stream reusable. Caller must ensure that all the bytes from previous read
	 * have been read, otherwise they will be lost.
	 *
	 * @param length
	 *            New wanted length of read.
	 */
	public synchronized void reset(int length) {
		// all to empty buffers
		while (!getFullBuffers().isEmpty()) {
			ByteBuffer byteBuffer = getFullBuffers().poll();
			if (null != byteBuffer) {
				byteBuffer.clear();
				getEmptyBuffers().add(byteBuffer);
			}
		}

		setTotalSize(length);
		setPosition(0);
		executorService.execute(new SocketReadRunnable(length));
	}

	/**
	 * Runnable that reads from socket.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class SocketReadRunnable implements Runnable {

		/**
		 * Amount that we already have read.
		 */
		private int totalRead;

		/**
		 * Length that this task has to read.
		 */
		private final long length;

		/**
		 * Default constructor.
		 *
		 * @param length
		 *            Amount of bytes that should be read in this task.
		 */
		SocketReadRunnable(long length) {
			this.length = length;
		}

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			while (true) {
				long bytesLeft = length - totalRead;

				// break if nothing
				if (bytesLeft == 0) {
					break;
				}

				// otherwise take an empty buffer
				ByteBuffer byteBuffer = null;
				try {
					byteBuffer = getEmptyBuffers().take();
				} catch (InterruptedException e) {
					Thread.interrupted();
				}

				if (null != byteBuffer) {
					// clear just in case and then set to bytesLeft if its less than remaining
					// capacity
					byteBuffer.clear();
					if (byteBuffer.remaining() > bytesLeft) {
						byteBuffer.limit((int) bytesLeft);
					}

					try {
						int read = socketChannel.read(byteBuffer);
						if (read > 0) {
							byteBuffer.flip();
							getFullBuffers().add(byteBuffer);
							totalRead += read;
						} else {
							byteBuffer.clear();
							getEmptyBuffers().add(byteBuffer);
						}
					} catch (IOException e) {
						byteBuffer.clear();
						getEmptyBuffers().add(byteBuffer);
					}
				}
			}
		}
	}

	/**
	 * Sets {@link #socketChannel}.
	 *
	 * @param socketChannel
	 *            New value for {@link #socketChannel}
	 */
	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
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

}
