package info.novatec.inspectit.storage.nio.write;

import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.nio.AbstractChannelManager;
import info.novatec.inspectit.storage.nio.CustomAsyncChannel;
import info.novatec.inspectit.storage.nio.WriteReadAttachment;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.stream.ExtendedByteBufferOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Channel manager for writing the data.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class WritingChannelManager extends AbstractChannelManager {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * Max opened channels.
	 */
	@Value(value = "${storage.maxWriteChannelsOpened}")
	private int maxOpenedChannels = 128;

	/**
	 * Writes the content of the {@link ByteBuffer} to the channel that has the supplied path.
	 * Channel will be open if necessary.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} that holds the data to be written. Note that the caller of this
	 *            method is responsible for maintaining the buffer's position and limit.
	 * @param channelPath
	 *            Path to the channel's file.
	 * @param completionRunnable
	 *            Runnable that will be executed after the complete content of the buffer has been
	 *            written. If not needed null can be passed.
	 * @return Position where the data will be written in the channel.
	 * @throws IOException
	 *             Delegates the {@link IOException} from I/O operations.
	 */
	public long write(ByteBuffer byteBuffer, Path channelPath, WriteReadCompletionRunnable completionRunnable) throws IOException {
		CustomAsyncChannel channel = super.getChannel(channelPath);

		long writingSize = byteBuffer.limit() - byteBuffer.position();
		long writingPosition = channel.reserveWritingPosition(writingSize);

		WriteReadAttachment attachment = new WriteReadAttachment();
		attachment.setByteBuffer(byteBuffer);
		attachment.setSize(writingSize);
		attachment.setPosition(writingPosition);
		completionRunnable.setAttemptedWriteReadSize(writingSize);
		completionRunnable.setAttemptedWriteReadPosition(writingPosition);
		attachment.setCompletionRunnable(completionRunnable);
		attachment.setFileChannel(channel.getFileChannel());

		boolean wrote = false;
		while (!wrote) {
			wrote = channel.write(byteBuffer, writingPosition, attachment, new WritingCompletionHandler());
			if (!wrote) {
				if (log.isDebugEnabled()) {
					log.info("Failed to submit writing IO task, channel is closed. Trying to reopen the channel..");
				}
				this.openAsyncChannel(channel);
			}
		}

		return writingPosition;
	}

	/**
	 * Writes the content in the {@link ExtendedByteBufferOutputStream} to the given channel path.
	 * This write will actually be a series of asynchronous writes, each for a single buffer
	 * provided by the {@link ExtendedByteBufferOutputStream}.
	 * 
	 * @param extendedByteBufferOutputStream
	 * @param channelPath
	 *            Path to the channel's file.
	 * @param completionRunnable
	 *            Runnable that will be executed after the complete content of each buffer. Note
	 *            that the same completion runnable can be executed more than one time.
	 * @param extendedByteBufferOutputStream
	 *            the stream to write to.
	 * @return Position where the data will be written in the channel.
	 * @throws IOException
	 *             Delegates the {@link IOException} from I/O operations.
	 */
	public long write(ExtendedByteBufferOutputStream extendedByteBufferOutputStream, Path channelPath, WriteReadCompletionRunnable completionRunnable) throws IOException {
		CustomAsyncChannel channel = super.getChannel(channelPath);
		List<ByteBuffer> byteBuffers = extendedByteBufferOutputStream.getAllByteBuffers();
		long totalWritingSize = extendedByteBufferOutputStream.getTotalWriteSize();
		long writingPosition = channel.reserveWritingPosition(totalWritingSize);
		completionRunnable.setAttemptedWriteReadSize(totalWritingSize);
		completionRunnable.setAttemptedWriteReadPosition(writingPosition);
		long returnWritingPosition = writingPosition;

		for (ByteBuffer byteBuffer : byteBuffers) {
			long writingSize = byteBuffer.limit() - byteBuffer.position();
			WriteReadAttachment attachment = new WriteReadAttachment();
			attachment.setByteBuffer(byteBuffer);
			attachment.setSize(writingSize);
			attachment.setPosition(writingPosition);
			attachment.setCompletionRunnable(completionRunnable);
			attachment.setFileChannel(channel.getFileChannel());

			boolean wrote = false;
			while (!wrote) {
				wrote = channel.write(byteBuffer, writingPosition, attachment, new WritingCompletionHandler());
				if (!wrote) {
					if (log.isDebugEnabled()) {
						log.info("Failed to submit writing IO task, channel is closed. Trying to reopen the channel..");
					}
					this.openAsyncChannel(channel);
				}
			}
			writingPosition += writingSize;
		}

		return returnWritingPosition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getMaxOpenedChannels() {
		return maxOpenedChannels;
	}

}
