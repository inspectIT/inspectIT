package rocks.inspectit.shared.cs.storage.nio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simple POJO class that defines attachment that is supplied to every writing to the
 * {@link AsynchronousFileChannel}.
 *
 * @author Ivan Senic
 *
 */
public class WriteReadAttachment {

	/**
	 * {@link ByteBuffer} where writing is taking bytes from.
	 */
	private ByteBuffer byteBuffer;

	/**
	 * Writing/read position.
	 */
	private long position;

	/**
	 * Writing/read size.
	 */
	private long size;

	/**
	 * Completion {@link WriteReadCompletionRunnable.RunnableFuture}.
	 */
	private WriteReadCompletionRunnable.RunnableFuture completionRunnableFuture;

	/**
	 * Channel where write/read is performed.
	 */
	private AsynchronousFileChannel fileChannel;

	/**
	 * Default constructor.
	 */
	public WriteReadAttachment() {
	}

	/**
	 * Constructor that sets all class fields.
	 *
	 * @param byteBuffer
	 *            {@link ByteBuffer} where writing is taking bytes from.
	 * @param position
	 *            Writing/Reading position.
	 * @param size
	 *            Writing/Reading size.
	 * @param completionRunnableFuture
	 *            Completion {@link WriteReadCompletionRunnable.RunnableFuture}.
	 * @param fileChannel
	 *            Channel where write is performed.
	 */
	public WriteReadAttachment(ByteBuffer byteBuffer, long position, long size, WriteReadCompletionRunnable.RunnableFuture completionRunnableFuture, AsynchronousFileChannel fileChannel) {
		this.byteBuffer = byteBuffer;
		this.position = position;
		this.size = size;
		this.completionRunnableFuture = completionRunnableFuture;
		this.fileChannel = fileChannel;
	}

	/**
	 * @return the byteBuffer
	 */
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	/**
	 * @param byteBuffer
	 *            the byteBuffer to set
	 */
	public void setByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

	/**
	 * @return the position
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(long position) {
		this.position = position;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Gets {@link #completionRunnableFuture}.
	 * 
	 * @return {@link #completionRunnableFuture}
	 */
	public WriteReadCompletionRunnable.RunnableFuture getCompletionRunnableFuture() {
		return this.completionRunnableFuture;
	}

	/**
	 * Sets {@link #completionRunnableFuture}.
	 * 
	 * @param completionRunnableFuture
	 *            New value for {@link #completionRunnableFuture}
	 */
	public void setCompletionRunnableFuture(WriteReadCompletionRunnable.RunnableFuture completionRunnableFuture) {
		this.completionRunnableFuture = completionRunnableFuture;
	}

	/**
	 * @return the fileChannel
	 */
	public AsynchronousFileChannel getFileChannel() {
		return fileChannel;
	}

	/**
	 * @param fileChannel
	 *            the fileChannel to set
	 */
	public void setFileChannel(AsynchronousFileChannel fileChannel) {
		this.fileChannel = fileChannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("byteBuffer", byteBuffer);
		toStringBuilder.append("position", position);
		toStringBuilder.append("size", size);
		return toStringBuilder.toString();
	}

}
