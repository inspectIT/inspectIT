package info.novatec.inspectit.storage.nio;

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
	 * Completion {@link Runnable}.
	 */
	private WriteReadCompletionRunnable completionRunnable;

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
	 * @param writingPosition
	 *            Writing position.
	 * @param writingSize
	 *            Writing size.
	 * @param completionRunnable
	 *            Completion {@link Runnable}.
	 * @param fileChannel
	 *            Channel where write is performed.
	 */
	public WriteReadAttachment(ByteBuffer byteBuffer, long writingPosition, long writingSize, WriteReadCompletionRunnable completionRunnable, AsynchronousFileChannel fileChannel) {
		this.byteBuffer = byteBuffer;
		this.position = writingPosition;
		this.size = writingSize;
		this.completionRunnable = completionRunnable;
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
	 * @return the completionRunnable
	 */
	public WriteReadCompletionRunnable getCompletionRunnable() {
		return completionRunnable;
	}

	/**
	 * @param completionRunnable
	 *            the completionRunnable to set
	 */
	public void setCompletionRunnable(WriteReadCompletionRunnable completionRunnable) {
		this.completionRunnable = completionRunnable;
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
