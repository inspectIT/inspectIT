package info.novatec.inspectit.indexing.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

import org.springframework.beans.factory.FactoryBean;
/**
 * Bean Factory for the ForkJoinWorkerThreadFactory.
 * @author tan
 *
 */
public class ForkJoinPoolWorkerThreadFactoryFactory implements FactoryBean<ForkJoinWorkerThreadFactory> {
	/**
	 * Prefix name of the Thread.
	 */
	private String threadNamePrefix;
	
	

	/**
	 * 
	 * {@inheritDoc}
	 */
	public ForkJoinWorkerThreadFactory getObject() throws Exception {
		return new WorkerThreadFactory();

	}

	@Override
	public Class<?> getObjectType() {
		return WorkerThreadFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
	
	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}
	/**
	 * Inherited Class of WorkerThread.
	 * @author tan
	 *
	 */
	static class WorkerThread extends ForkJoinWorkerThread {
		/**
		 * User constructor of superclass.
		 * @param pool the forkJoinPool
		 */
		public WorkerThread(ForkJoinPool pool) {
			super(pool);
		}
	}
	/**
	 * WorkerThreadFactory which uses the defined name for the WorkerThread.
	 * @author tan
	 *
	 */
	class WorkerThreadFactory implements ForkJoinWorkerThreadFactory {

		/**
		 * Returns new Thread with setted name.
		 * {@inheritDoc}
		 */
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			ForkJoinWorkerThread workerThread = new WorkerThread(pool);
			workerThread.setName(ForkJoinPoolWorkerThreadFactoryFactory.this.threadNamePrefix);
			return workerThread;
		}

	}
}