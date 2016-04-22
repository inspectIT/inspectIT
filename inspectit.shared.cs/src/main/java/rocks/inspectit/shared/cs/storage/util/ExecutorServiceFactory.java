package rocks.inspectit.shared.cs.storage.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.FactoryBean;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Bean factory for providing the executor service.
 *
 * @author Ivan Senic
 *
 */
public class ExecutorServiceFactory implements FactoryBean<ExecutorService> {

	/**
	 * Prefix to be added to the name of each thread.
	 */
	private String threadNamePrefix;

	/**
	 * Should created threads be daemons.
	 */
	private boolean daemon;

	/**
	 * Number of threads in the executor.
	 */
	private int executorThreads;

	/**
	 * Will this factory create a singleton.
	 */
	private boolean isBeanSingleton;

	/**
	 * Should the provided executor be {@link ScheduledExecutorService}.
	 */
	private boolean isScheduledExecutor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutorService getObject() throws Exception {
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-thread-%d").setDaemon(daemon).build();

		if (!isScheduledExecutor) {
			return Executors.newFixedThreadPool(executorThreads, threadFactory);
		} else {
			// I set remove on cancel policy, because i don't want to have the canceled tasks still
			// in the queue
			ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(executorThreads, threadFactory);
			scheduledExecutor.setRemoveOnCancelPolicy(true);
			return scheduledExecutor;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getObjectType() {
		if (!isScheduledExecutor) {
			return ExecutorService.class;
		} else {
			return ScheduledExecutorService.class;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return isBeanSingleton;
	}

	/**
	 * Sets {@link #threadNamePrefix}.
	 *
	 * @param threadNamePrefix
	 *            New value for {@link #threadNamePrefix}
	 */
	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	/**
	 * Sets {@link #daemon}.
	 *
	 * @param daemon
	 *            New value for {@link #daemon}
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * @param executorThreads
	 *            the executorThreads to set
	 */
	public void setExecutorThreads(int executorThreads) {
		this.executorThreads = executorThreads;
	}

	/**
	 * @param isBeanSingleton
	 *            the isBeanSingleton to set
	 */
	public void setBeanSingleton(boolean isBeanSingleton) {
		this.isBeanSingleton = isBeanSingleton;
	}

	/**
	 * @param isScheduledExecutor
	 *            the isScheduledExecutor to set
	 */
	public void setScheduledExecutor(boolean isScheduledExecutor) {
		this.isScheduledExecutor = isScheduledExecutor;
	}

}
