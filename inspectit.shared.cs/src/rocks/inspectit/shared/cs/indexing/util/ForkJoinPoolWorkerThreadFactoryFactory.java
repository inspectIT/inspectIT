package info.novatec.inspectit.indexing.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

import org.springframework.beans.factory.FactoryBean;

/**
 * Bean Factory for the ForkJoinWorkerThreadFactory.
 * 
 * @author Tobias Angerstein
 *
 */
public class ForkJoinPoolWorkerThreadFactoryFactory implements FactoryBean<ForkJoinWorkerThreadFactory> {

	/**
	 * Prefix name of the Thread.
	 */
	private String threadNamePrefix;

	/**
	 * {@inheritDoc}
	 */
	public ForkJoinWorkerThreadFactory getObject() throws Exception {
		return new WorkerThreadFactory(threadNamePrefix);
	}

	@Override
	public Class<?> getObjectType() {
		return WorkerThreadFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
	
	/**
	 * Getter of threadNamePrefix.
	 * 
	 * @return
	 * 		threadNamePrefix
	 */
	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	/**
	 * Setter of threadNamePrefix.
	 * 
	 * @param threadNamePrefix
	 * 			threadNamePrefix
	 */
	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	/**
	 * Inherited Class of WorkerThread.
	 * 
	 * @author Tobias Angerstein
	 */
	static class WorkerThread extends ForkJoinWorkerThread {

		/**
		 * User constructor of superclass.
		 * 
		 * @param pool
		 *            the forkJoinPool
		 */
		public WorkerThread(ForkJoinPool pool) {
			super(pool);
		}
	}

	/**
	 * WorkerThreadFactory which uses the defined name for the WorkerThread.
	 * 
	 * @author Tobias Angerstein
	 */
	static class WorkerThreadFactory implements ForkJoinWorkerThreadFactory {
		
		/**
		 * ThreadName. 
		 */
		private String threadNamePrefix;
		
		/**
		 * Number of current threads.
		 */
		private int threadCounter;
		
		/**
		 * Constructor.
		 * 
		 * @param threadNamePrefix
		 * 					threadName;
		 */
		public WorkerThreadFactory(String threadNamePrefix) {
			this.threadNamePrefix = threadNamePrefix;
			this.threadCounter = 0;
		}
		/**
		 * {@inheritDoc}
		 * <p>
		 * Returns new Thread with set name. 
		 */
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			ForkJoinWorkerThread workerThread = new WorkerThread(pool);
			workerThread.setName(threadNamePrefix + "-" + ++threadCounter);
			return workerThread;
		}

	}
}