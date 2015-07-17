package info.novatec.inspectit.storage.nio.read;

import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.nio.AbstractChannelManager;
import info.novatec.inspectit.storage.nio.CustomAsyncChannel;
import info.novatec.inspectit.storage.nio.WriteReadAttachment;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import org.slf4j.Logger;

/**
 * Channel manager that performs reading.
 * 
 * @author Ivan Senic
 * 
 */
public class ReadingChannelManager extends AbstractChannelManager {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * Max opened channels.
	 */
	private int maxOpenedChannels = 128;

	/**
	 * Reads the content of the channel with supplied ID to the buffer. The read will start from the
	 * supplied position. Note that this is an asynchronous read, thus it is unknown when will the
	 * data be in the buffer. Best approach is to define a completion handler that can perform the
	 * logic for manipulating the buffer after it has been loaded with data.
	 * <p>
	 * If channel with supplied ID is not open, it will be. The {@link IllegalArgumentException} is
	 * thrown if the size between buffer's capacity and position is smaller than wanted read size.
	 * <p>
	 * It is a responsibility of a caller to assure that the buffer supplied would not be used by
	 * other threads until the read operation is finished.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} that data will be read to.
	 * @param position
	 *            Position in file to start reading from.
	 * @param size
	 *            Wanted read size. If supplied read size is less or equal zero, the amount of bytes
	 *            that will be read is defined as the available bytes in byte buffer.
	 * @param channelPath
	 *            Path of the channel to be read from. If the channel is not open, it will be opened
	 *            first.
	 * @param completionRunnable
	 *            Runnable that will be executed at the end of the read.
	 * @throws IOException
	 *             Delegates {@link IOException} from IO operations.
	 */
	public void read(ByteBuffer byteBuffer, long position, long size, Path channelPath, WriteReadCompletionRunnable completionRunnable) throws IOException {
		long readSize;
		if (size <= 0) {
			readSize = byteBuffer.capacity() - byteBuffer.position();
		} else {
			readSize = size;
		}

		if ((byteBuffer.capacity() - byteBuffer.position()) < readSize) {
			throw new IllegalArgumentException("Buffer capacity not big enough for wanted read size");
		}

		CustomAsyncChannel channel = super.getChannel(channelPath);

		WriteReadAttachment attachment = new WriteReadAttachment();
		attachment.setByteBuffer(byteBuffer);
		attachment.setSize(readSize);
		attachment.setFileChannel(channel.getFileChannel());
		attachment.setCompletionRunnable(completionRunnable);

		boolean read = false;
		while (!read) {
			read = channel.read(byteBuffer, position, attachment, new ReadingCompletionHandler());
			if (!read) {
				if (log.isDebugEnabled()) {
					log.info("Failed to submit reading IO task, channel is closed. Trying to reopen the channel..");
				}
				this.openAsyncChannel(channel);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getMaxOpenedChannels() {
		return maxOpenedChannels;
	}
}
