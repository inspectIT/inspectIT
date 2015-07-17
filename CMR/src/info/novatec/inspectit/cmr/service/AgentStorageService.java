package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.property.spring.PropertyUpdate;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.spring.logger.Log;

import java.lang.ref.SoftReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The default implementation of the {@link IAgentStorageService} interface. Uses an implementation
 * of the {@link DefaultDataDao} interface to save and retrieve the data objects from the database.
 * 
 * @author Patrice Bouillet
 * 
 */
@Service
public class AgentStorageService implements IAgentStorageService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Queue capacity for incoming data.
	 */
	private static final int QUEUE_CAPACITY = 50;

	/**
	 * Amount of milliseconds after which the data is thrown away if queue is full.
	 */
	private static final long DATA_THROW_TIMEOUT_MILLIS = 10;

	/**
	 * The default data DAO.
	 */
	@Autowired
	private DefaultDataDao defaultDataDao;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider platformIdentDateSaver;

	/**
	 * {@link CmrManagementService}.
	 */
	@Autowired
	ICmrManagementService cmrManagementService;

	/**
	 * Queue to store and remove list of data that has to be processed.
	 */
	private ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>> dataObjectsBlockingQueue = new ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>>(QUEUE_CAPACITY);

	/**
	 * Count of thread to process data.
	 */
	@Value("${cmr.agentStorageServiceThreadCount}")
	private int threadCount;

	/**
	 * List of currently active threads that process the data.
	 */
	private List<Thread> threadList = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public AgentStorageService() {
	}

	/**
	 * Constructor that can be used in testing for suppling the queue.
	 * 
	 * @param dataObjectsBlockingQueue
	 *            Queue.
	 */
	AgentStorageService(ArrayBlockingQueue<SoftReference<List<? extends DefaultData>>> dataObjectsBlockingQueue) {
		this.dataObjectsBlockingQueue = dataObjectsBlockingQueue;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public void addDataObjects(final List<? extends DefaultData> dataObjects) throws RemoteException {
		SoftReference<List<? extends DefaultData>> softReference = new SoftReference<List<? extends DefaultData>>(dataObjects);
		if (!dataObjects.isEmpty()) {
			platformIdentDateSaver.registerDataSent(dataObjects.get(0).getPlatformIdent());
		}
		try {
			boolean added = dataObjectsBlockingQueue.offer(softReference, DATA_THROW_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
			if (!added) {
				int droppedSize = dataObjects.size();
				if (log.isTraceEnabled()) {
					log.trace("Data dropped on the CMR due to the high volume of incoming data from Agent(s). Dropped data objects count: " + droppedSize);
				}
				cmrManagementService.addDroppedDataCount(droppedSize);
			}
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Updates the number of data processing threads. The new number of threads should be defined in
	 * {@link #threadCount} before calling this method.
	 * <p>
	 * This is an automated properties update execution method.
	 */
	@PropertyUpdate(properties = { "cmr.agentStorageServiceThreadCount" })
	public synchronized void updateThreadCount() {
		if (threadCount <= 0) {
			threadCount = 1;
		}

		int threadListSize = threadList.size();
		if (threadCount < threadListSize) {
			// remove threads
			for (int i = 0; i < threadListSize - threadCount; i++) {
				Thread thread = threadList.remove(i);
				thread.interrupt();
				try {
					thread.join();
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
		} else if (threadCount > threadListSize) {
			// add new threads
			for (int i = threadListSize; i < threadCount; i++) {
				ProcessDataThread processDataThread = new ProcessDataThread(i);
				processDataThread.start();
				threadList.add(processDataThread);
			}

		}
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		updateThreadCount();

		if (log.isInfoEnabled()) {
			log.info("|-Agent Storage Service active...");
		}

	}

	/**
	 * Thread class that is processing the data coming to the Agent service and invoking the
	 * {@link DefaultDataDao}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ProcessDataThread extends Thread {

		/**
		 * Default constructor.
		 * 
		 * @param threadId
		 *            Id of the thread that will be added to the thread name.
		 */
		public ProcessDataThread(int threadId) {
			setName("agent-storage-service-process-data-thread-" + threadId);
			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			while (true) {
				if (isInterrupted()) {
					break;
				}

				SoftReference<List<? extends DefaultData>> softReference = null;
				try {
					softReference = dataObjectsBlockingQueue.take();
				} catch (InterruptedException e) {
					this.interrupt();
					return;
				}

				List<? extends DefaultData> defaultDataList = softReference.get();
				if (defaultDataList != null) {
					for (DefaultData data : defaultDataList) {
						data.finalizeData();
					}

					long time = 0;
					if (log.isDebugEnabled()) {
						time = System.nanoTime();
					}

					defaultDataDao.saveAll(defaultDataList);

					if (log.isDebugEnabled()) {
						log.debug("Data Objects count: " + defaultDataList.size() + " Save duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
					}
				}
			}
		}
	}
}
