package rocks.inspectit.shared.cs.storage.nio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for all channel managers.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractChannelManager {

	/**
	 * All channels.
	 */
	private Map<Path, CustomAsyncChannel> writingChannelsMap = new ConcurrentHashMap<>(64, 0.75f, 1);

	/**
	 * Opened channels queue.
	 */
	private ConcurrentLinkedQueue<CustomAsyncChannel> openedChannelsQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Executor service for IO operations.
	 */
	@Autowired
	@Resource(name = "IOExecutorService")
	private ExecutorService executorService;

	/**
	 * Count of opened channels.
	 * <p>
	 * Note that the count does not have to be in the constant equality to the
	 * {@link #openedChannelsQueue} size. It's not critical to have the ACID relation here, because
	 * this count is just for us to know when do we need to close one channel.
	 */
	private AtomicInteger openedChannelsCount = new AtomicInteger(0);

	/**
	 * Subclasses should provide the max number of opened channels.
	 *
	 * @return Returns the number of maximum opened channels.
	 */
	protected abstract int getMaxOpenedChannels();

	/**
	 * Returns the channel. If necessary channel will be open.
	 *
	 * @param channelPath
	 *            Path for the channel.
	 * @return {@link CustomAsyncChannel} that is already open.
	 * @throws IOException
	 *             If exception occurs during channel creation.
	 */
	protected CustomAsyncChannel getChannel(Path channelPath) throws IOException {
		CustomAsyncChannel channel = writingChannelsMap.get(channelPath);
		if (channel == null) {
			channel = createNewChannel(channelPath);
		}
		return channel;
	}

	/**
	 * Creates a new channel. We need to do this in synchronized method since the channel also has
	 * to be opened, thus we can not use putIfAbsent.
	 *
	 * @param channelPath
	 *            Path.
	 * @return Created channel.
	 * @throws IOException
	 *             If exception occurs during channel creation.
	 */
	private synchronized CustomAsyncChannel createNewChannel(Path channelPath) throws IOException {
		CustomAsyncChannel channel = writingChannelsMap.get(channelPath);
		if (channel == null) {
			channel = new CustomAsyncChannel(channelPath);
			this.openAsyncChannel(channel);
			writingChannelsMap.put(channelPath, channel);
		}
		return channel;
	}

	/**
	 * Opens the {@link CustomAsyncChannel} and adds it to the beginning of the
	 * {@link #openedChannelsQueue}. If limit for opened channels is reached, one channel will be
	 * closed.
	 *
	 * @param customAsyncChannel
	 *            Channel to open.
	 * @throws IOException
	 *             If {@link IOException} occurs during opening.
	 */
	protected void openAsyncChannel(CustomAsyncChannel customAsyncChannel) throws IOException {
		boolean channelOpened = customAsyncChannel.openChannel(executorService);
		if (channelOpened) {
			openedChannelsQueue.add(customAsyncChannel);
			int channelsOpened = openedChannelsCount.incrementAndGet();

			// don't excess the max number of allowed channels
			while (channelsOpened > getMaxOpenedChannels()) {
				closeOldestAsyncChannel();
				channelsOpened = openedChannelsCount.get();
			}
		}
	}

	/**
	 * Closes the last opened channel in the queue.
	 *
	 * @throws IOException
	 *             If {@link IOException} happens.
	 */
	protected void closeOldestAsyncChannel() throws IOException {
		CustomAsyncChannel channelToClose = openedChannelsQueue.poll();
		if (null != channelToClose) {
			if (channelToClose.closeChannel()) {
				openedChannelsCount.decrementAndGet();
			}
		}
	}

	/**
	 * Closes the {@link CustomAsyncChannel} and removes it from the {@link #openedChannelsQueue}.
	 *
	 * @param channelToClose
	 *            Channel to close.
	 * @throws IOException
	 *             If {@link IOException} occurs during closing.
	 */
	protected void closeAsyncChannel(CustomAsyncChannel channelToClose) throws IOException {
		openedChannelsQueue.remove(channelToClose);
		if (channelToClose.closeChannel()) {
			openedChannelsCount.decrementAndGet();
		}
	}

	/**
	 * Finalize all open channels. Before closing the channels, force will be performed, so that all
	 * pending writing on the file channel are performed.
	 *
	 * @throws IOException
	 *             When {@link IOException} occurs.
	 */
	public void finalizeAllChannels() throws IOException {
		for (CustomAsyncChannel channel : writingChannelsMap.values()) {
			if (channel.isOpened()) {
				closeAsyncChannel(channel);
			}
			writingChannelsMap.values().remove(channel);
		}
	}

	/**
	 * Finalize the file channel with specified channel's file path. Before closing the channel,
	 * force will be performed, so that all pending writing on the file channel are performed.
	 *
	 * @param channelPath
	 *            Path to the channel's file.
	 * @throws IOException
	 *             When {@link IOException} occurs.
	 */
	public void finalizeChannel(Path channelPath) throws IOException {
		CustomAsyncChannel channel = writingChannelsMap.remove(channelPath);
		if ((channel != null) && channel.isOpened()) {
			closeAsyncChannel(channel);
		}
	}

	/**
	 * Returns executor service status. This methods just returns the result of
	 * {@link #executorService#toString()} method.
	 *
	 * @return Returns executor service status. This methods just returns the result of
	 *         {@link #executorService#toString()} method.
	 */
	public String getExecutorServiceStatus() {
		return executorService.toString();
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("currentOpenedChannels", openedChannelsCount.get());
		toStringBuilder.append("executorService", executorService);
		toStringBuilder.append("writingChannelsMap", writingChannelsMap);
		return toStringBuilder.toString();
	}
}
