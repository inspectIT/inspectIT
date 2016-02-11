package info.novatec.inspectit.storage.nio.stream;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Class that is used for providing the correct instance of {@link ExtendedByteBufferOutputStream}
 * via Spring framework.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class StreamProvider {

	/**
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferOutputStream}
	 *         that has been prepared for use.
	 * @throws IOException
	 *             if output stream can not be obtained
	 */
	public ExtendedByteBufferOutputStream getExtendedByteBufferOutputStream() throws IOException {
		ExtendedByteBufferOutputStream stream = createExtendedByteBufferOutputStream();
		stream.prepare();
		return stream;
	}

	/**
	 * @return Returns the newly initialized instance of the {@link ExtendedByteBufferOutputStream}.
	 */
	protected abstract ExtendedByteBufferOutputStream createExtendedByteBufferOutputStream();

	/**
	 * Returns the {@link SocketExtendedByteBufferInputStream} initialized by Spring and prepared.
	 * Caller must first call {@link SocketExtendedByteBufferInputStream#reset(int)} before reading
	 * any data.
	 * 
	 * @param socketChannel
	 *            Underlying {@link SocketChannel} for the stream.
	 * @return {@link SocketExtendedByteBufferInputStream}.
	 * @throws IOException
	 *             if input stream can not be obtained
	 */
	public SocketExtendedByteBufferInputStream getSocketExtendedByteBufferInputStream(SocketChannel socketChannel) throws IOException {
		SocketExtendedByteBufferInputStream inputStream = createSocketExtendedByteBufferInputStream();
		inputStream.setSocketChannel(socketChannel);
		inputStream.prepare();
		return inputStream;
	}

	/**
	 * Returns the {@link SocketExtendedByteBufferInputStream} initialized by Spring and prepared.
	 * Caller can immediately use this stream to read data from.
	 * 
	 * @param socketChannel
	 *            Underlying {@link SocketChannel} for the stream.
	 * @param totalSize
	 *            Size to read from {@link SocketChannel}.
	 * @return {@link SocketExtendedByteBufferInputStream}.
	 * @throws IOException
	 *             if input stream can not be obtained
	 */
	public SocketExtendedByteBufferInputStream getSocketExtendedByteBufferInputStream(SocketChannel socketChannel, long totalSize) throws IOException {
		SocketExtendedByteBufferInputStream inputStream = createSocketExtendedByteBufferInputStream();
		inputStream.setSocketChannel(socketChannel);
		inputStream.setTotalSize(totalSize);
		inputStream.prepare();
		return inputStream;
	}

	/**
	 * @return Returns the newly initialized instance of the
	 *         {@link SocketExtendedByteBufferInputStream}.
	 */
	protected abstract SocketExtendedByteBufferInputStream createSocketExtendedByteBufferInputStream();

}
