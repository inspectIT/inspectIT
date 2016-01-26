package rocks.inspectit.agent.java.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.buffer.IBufferStrategy;
import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IObjectStorage;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.ListListener;
import rocks.inspectit.agent.java.sending.ISendingStrategy;
import rocks.inspectit.agent.java.sensor.jmx.IJmxSensor;
import rocks.inspectit.agent.java.sensor.platform.IPlatformSensor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.ExceptionEvent;
import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Default implementation of the {@link ICoreService} interface.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Alfred Krauss
 *
 */
// must depend on platform manager so that configuration is received
@Component
@DependsOn({ "platformManager" })
public class CoreService implements ICoreService, InitializingBean, DisposableBean {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * The available and registered sending strategies.
	 */
	@Autowired
	private final List<ISendingStrategy> sendingStrategies = new ArrayList<ISendingStrategy>();

	/**
	 * The selected buffer strategy to store the list of value objects.
	 */
	@Autowired
	private IBufferStrategy<DefaultData> bufferStrategy;

	/**
	 * All platform sensors.
	 */
	@Autowired(required = false)
	private List<IPlatformSensor> platformSensors;

	/**
	 * All jmx sensors.
	 */
	@Autowired(required = false)
	private List<IJmxSensor> jmxSensors;

	/**
	 * Executor service that other components can use for asynchronous tasks.
	 */
	@Autowired
	@Qualifier("coreServiceExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * Already used data objects which can be used directly on the CMR to persist.
	 */
	private Map<String, DefaultData> sensorDataObjects = new ConcurrentHashMap<String, DefaultData>();

	/**
	 * Contains object storage instances which will be initialized when sending.
	 */
	private Map<String, IObjectStorage> objectStorages = new ConcurrentHashMap<String, IObjectStorage>();

	/**
	 * Used as second hash table for the measurements when processed before sending.
	 */
	private Map<String, DefaultData> measurementsProcessing = new ConcurrentHashMap<String, DefaultData>();

	/**
	 * Used as second hash table for the object storages when processed before sending.
	 */
	private Map<String, IObjectStorage> objectStoragesProcessing = new ConcurrentHashMap<String, IObjectStorage>();

	/**
	 * Temporary Map to switch the references of the active hash table with the processed one.
	 */
	private Map<String, ?> temp;

	/**
	 * The registered list listeners.
	 */
	private final List<ListListener<?>> listListeners = new ArrayList<ListListener<?>>();

	/**
	 * The default refresh time.
	 */
	private static final long DEFAULT_REFRESH_TIME = 1000L;

	/**
	 * The refresh time for the platformSensorRefresher thread in ms.
	 */
	private long sensorRefreshTime = DEFAULT_REFRESH_TIME;

	/**
	 * The sensorRefresher is a thread which updates the platform informations after the specified
	 * platformSensorRefreshTime.
	 */
	private volatile SensorRefresher sensorRefresher;

	/**
	 * The preparing thread used to execute the preparation of the measurement in a separate
	 * process.
	 */
	private volatile PreparingThread preparingThread;

	/**
	 * The sending thread used to execute the sending of the measurement in a separate process.
	 */
	private volatile SendingThread sendingThread;

	/**
	 * Defines if there was an exception before while trying to send the data. Used to throttle the
	 * printing of log statements.
	 */
	private boolean sendingExceptionNotice = false;

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		for (ISendingStrategy strategy : sendingStrategies) {
			strategy.start(this);
		}

		preparingThread = new PreparingThread();
		preparingThread.start();

		sendingThread = new SendingThread();
		sendingThread.start();

		sensorRefresher = new SensorRefresher();
		sensorRefresher.start();

		Runtime.getRuntime().addShutdownHook(new ShutdownHookSender());
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		for (ISendingStrategy strategy : sendingStrategies) {
			strategy.stop();
		}

		synchronized (preparingThread) {
			preparingThread.interrupt();
		}

		synchronized (sendingThread) {
			sendingThread.interrupt();
		}

		Thread temp = sensorRefresher;
		sensorRefresher = null; // NOPMD
		synchronized (temp) {
			temp.interrupt();
		}

		// shutdown core service
		executorService.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				// Cancel currently executing tasks
				executorService.shutdownNow();
				// Wait a while for tasks to respond to being canceled
				if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
					log.error("Executor service for the inspectIT Core service did not terminate.");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			executorService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendData() {
		// notify the sending thread. if it is currently sending something,
		// nothing should happen
		synchronized (preparingThread) {
			preparingThread.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addJmxSensorValueData(long sensorTypeIdent, String objectName, String attributeName, JmxSensorValueData jmxSensorValueData) {
		StringBuilder builder = new StringBuilder();
		builder.append(sensorTypeIdent);
		builder.append('.');
		builder.append(objectName);
		builder.append('.');
		builder.append(attributeName);
		builder.append('.');
		// Added timestamp to be able to send multiple objects to cmr.
		builder.append(jmxSensorValueData.getTimeStamp().getTime());
		sensorDataObjects.put(builder.toString(), jmxSensorValueData);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix, MethodSensorData methodSensorData) {
		StringBuilder builder = new StringBuilder();
		if (null != prefix) {
			builder.append(prefix);
			builder.append('.');
		}
		builder.append(methodIdent);
		builder.append('.');
		builder.append(sensorTypeIdent);
		sensorDataObjects.put(builder.toString(), methodSensorData);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodSensorData getMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix) {
		StringBuilder builder = new StringBuilder();
		if (null != prefix) {
			builder.append(prefix);
			builder.append('.');
		}
		builder.append(methodIdent);
		builder.append('.');
		builder.append(sensorTypeIdent);
		return (MethodSensorData) sensorDataObjects.get(builder.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorData(long sensorTypeIdent, SystemSensorData systemSensorData) {
		sensorDataObjects.put(Long.toString(sensorTypeIdent), systemSensorData);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemSensorData getPlatformSensorData(long sensorTypeIdent) {
		return (SystemSensorData) sensorDataObjects.get(Long.toString(sensorTypeIdent));
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode, ExceptionSensorData exceptionSensorData) {
		StringBuilder builder = new StringBuilder();
		builder.append(sensorTypeIdent);
		builder.append("::");
		builder.append(throwableIdentityHashCode);
		String key = builder.toString();

		// we always only save the first data object, because this object contains the nested
		// objects to create the whole exception tree
		if (exceptionSensorData.getExceptionEvent().equals(ExceptionEvent.CREATED)) {
			// if a data object with the same hash code was already created, then it has to be For
			// us only the last-most data object is relevant
			sensorDataObjects.put(key, exceptionSensorData);
			notifyListListeners();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ExceptionSensorData getExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode) {
		StringBuilder builder = new StringBuilder();
		builder.append(sensorTypeIdent);
		builder.append("::");
		builder.append(throwableIdentityHashCode);

		return (ExceptionSensorData) sensorDataObjects.get(builder.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addObjectStorage(long sensorTypeIdent, long methodIdent, String prefix, IObjectStorage objectStorage) {
		StringBuilder builder = new StringBuilder();
		if (null != prefix) {
			builder.append(prefix);
			builder.append('.');
		}
		builder.append(methodIdent);
		builder.append('.');
		builder.append(sensorTypeIdent);
		objectStorages.put(builder.toString(), objectStorage);
		notifyListListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public IObjectStorage getObjectStorage(long sensorTypeIdent, long methodIdent, String prefix) {
		StringBuilder builder = new StringBuilder();
		if (null != prefix) {
			builder.append(prefix);
			builder.append('.');
		}
		builder.append(methodIdent);
		builder.append('.');
		builder.append(sensorTypeIdent);
		return objectStorages.get(builder.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public ScheduledExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListListener(ListListener<?> listener) {
		if (!listListeners.contains(listener)) {
			listListeners.add(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListListener(ListListener<?> listener) {
		listListeners.remove(listener);
	}

	/**
	 * Notify all registered listeners that a change occurred in the lists.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void notifyListListeners() {
		if (!listListeners.isEmpty()) {
			List temp = new ArrayList(sensorDataObjects.values());
			temp.addAll(objectStorages.values());
			for (ListListener<?> listListener : listListeners) {
				listListener.contentChanged(temp);
			}
		}
	}

	/**
	 * The SensorRefresher is a {@link Thread} which waits the specified sensorRefreshTime and then
	 * updates the information of the platform and jmx sensor.
	 *
	 * @author Eduard Tudenhoefner
	 * @author Alfred Krauss
	 *
	 */
	private class SensorRefresher extends Thread {

		/**
		 * Creates a new instance of the <code>PlatformSensorRefresher</code> as a daemon thread.
		 */
		public SensorRefresher() {
			setName("inspectit-platform-sensor-refresher-thread");
			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();

			while (sensorRefresher == thisThread) { // NOPMD
				try {
					synchronized (this) {
						wait(sensorRefreshTime);
					}
				} catch (InterruptedException e) {
					log.error("Sensor refresher was interrupted!");
				}

				// iterate the platformSensors and update the information
				if (CollectionUtils.isNotEmpty(platformSensors)) {
					for (IPlatformSensor platformSensor : platformSensors) {
						if (platformSensor.automaticUpdate()) {
							platformSensor.update(CoreService.this);
						}
					}
				}

				// iterate the jmxSensors and update the information
				if (CollectionUtils.isNotEmpty(jmxSensors)) {
					for (IJmxSensor jmxSensor : jmxSensors) {
						jmxSensor.update(CoreService.this);
					}
				}
			}
		}
	}

	/**
	 * Returns the current refresh time of the platform sensors.
	 *
	 * @return The platform sensor refresh time.
	 */
	public long getSensorRefreshTime() {
		return sensorRefreshTime;
	}

	/**
	 * Sets the platform sensor refresh time.
	 *
	 * @param sensorRefreshTime
	 *            The platform sensor refresh time to set.
	 */
	public void setSensorRefreshTime(long sensorRefreshTime) {
		this.sensorRefreshTime = sensorRefreshTime;
	}

	/**
	 * Prepares collected data for sending.
	 *
	 * Get all the value objects from the object storages and generate a list containing all the
	 * value objects.
	 *
	 * <b> WARNING: This code is supposed to be run single-threaded! We ensure single-threaded
	 * invocation by only calling this method within the single <code>PreparingThread</code>. During
	 * the JVM shutdown (in the shutdownhook), it is also ensured that this code is run
	 * singlethreaded. </b>
	 *
	 * @return <code>true</code> if new data were prepared, else <code>false</code>
	 */
	@SuppressWarnings("unchecked")
	private boolean prepareData() {
		// check if measurements are added in the last interval, if not nothing needs to be sent.
		if (sensorDataObjects.isEmpty() && objectStorages.isEmpty()) {
			return false;
		}

		// switch the references so that new data is stored while sending
		temp = sensorDataObjects;
		sensorDataObjects = measurementsProcessing;
		measurementsProcessing = (Map<String, DefaultData>) temp;

		temp = objectStorages;
		objectStorages = objectStoragesProcessing;
		objectStoragesProcessing = (Map<String, IObjectStorage>) temp;

		// copy the measurements values to a new list
		List<DefaultData> tempList = new ArrayList<DefaultData>(measurementsProcessing.values());
		measurementsProcessing.clear();

		// iterate the object storages and get the value objects which will be stored in the same
		// list.
		for (IObjectStorage objectStorage : objectStoragesProcessing.values()) {
			tempList.add(objectStorage.finalizeDataObject());
		}
		objectStoragesProcessing.clear();

		// Now give the strategy the list
		bufferStrategy.addMeasurements(tempList);

		return true;
	}

	/**
	 * sends the data.
	 *
	 * <b> WARNING: This code is supposed to be run single-threaded! We ensure single-threaded
	 * invocation by only calling this method within the single <code>SendingThread</code>. During
	 * the JVM shutdown (in the shutdownhook), it is also ensured that this code is run
	 * singlethreaded. </b>
	 */
	private void send() {
		try {
			while (bufferStrategy.hasNext()) {
				// if we are not connected keep data in buffer strategy
				if (!connection.isConnected()) {
					return;
				}

				List<DefaultData> dataToSend = bufferStrategy.next();
				connection.sendDataObjects(dataToSend);
				sendingExceptionNotice = false;
			}
		} catch (ServerUnavailableException serverUnavailableException) {
			if (serverUnavailableException.isServerTimeout()) {
				log.warn("Timeout on server when sending actual data. Data might be lost!", serverUnavailableException);
			} else {
				if (!sendingExceptionNotice) {
					sendingExceptionNotice = true;
					log.error("Connection problem appeared, stopping sending actual data!", serverUnavailableException);
				}
			}
		} catch (Throwable throwable) { // NOPMD NOCHK
			if (!sendingExceptionNotice) {
				sendingExceptionNotice = true;
				log.error("Connection problem appeared, stopping sending actual data!", throwable);
			}
		}
	}

	/**
	 * This implementation of a {@link Thread} is used to prepare the data and value objects that
	 * have to be sent to the CMR. Prepared data is put into {@link IBufferStrategy}.
	 * <p>
	 * Note that only one thread of this type can be started. Otherwise serious synchronization
	 * problems can appear.
	 *
	 * @author Patrice Bouillet
	 * @author Ivan Senic
	 * @author Stefan Siegl
	 */
	private class PreparingThread extends Thread {

		/**
		 * Creates a new <code>PreparingThread</code> as daemon.
		 */
		public PreparingThread() {
			setName("inspectit-preparing-thread");
			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			while (!isInterrupted()) {
				// wait for activation
				synchronized (this) {
					try {
						if (!isInterrupted()) {
							wait();
						}
					} catch (InterruptedException e) {
						log.error("Preparing thread interrupted and shutting down!");
						break; // we were interrupted during waiting and close ourself down.
					}
				}

				// We got a request from one of the send strategies.

				boolean newDataAvailable = prepareData();
				if (newDataAvailable) {
					// Notify sending thread
					synchronized (sendingThread) {
						sendingThread.notifyAll();
					}
				}
			}
		}
	}

	/**
	 * This implementation of a {@link Thread} is taking the data from the {@link IBufferStrategy}
	 * and sending it to the CMR.
	 * <p>
	 * Note that only one thread of this type can be started. Otherwise serious synchronization
	 * problems can appear.
	 *
	 * @author Ivan Senic
	 * @author Stefan Siegl
	 */
	private class SendingThread extends Thread {

		/**
		 * Creates a new <code>SendingThread</code> as daemon.
		 */
		public SendingThread() {
			setName("inspectit-sending-thread");
			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			while (!isInterrupted()) {
				// wait for activation if there is nothing to send
				if (!bufferStrategy.hasNext()) {
					synchronized (this) {
						try {
							if (!isInterrupted()) {
								wait();
							}
						} catch (InterruptedException e) {
							log.error("Sending thread interrupted and shuting down!");
							break; // we were interrupted during waiting and close ourself down.
						}
					}
				}

				// send the data
				send();
			}
		}
	}

	/**
	 * Used for the JVM Shutdown. Ensure that all threads are closed correctly and tries to send
	 * data one last time to prevent data loss.
	 *
	 * @author Stefan Siegl
	 */
	private class ShutdownHookSender extends Thread {
		@Override
		public void run() {
			log.info("Shutdown initialized, sending remaining data");
			// Stop the CoreService services
			CoreService.this.stop();

			// wait for the shutdown of the preparing thread and sending thread to ensure thread
			// safety on the entities used for preparing and sending. If we get interrupted while
			// waiting, then we stop the ShutdownHook completely. We'll wait only 10 seconds as
			// a maximum for each join and then continue
			try {
				preparingThread.join(10000);
			} catch (InterruptedException e) {
				log.error("ShutdownHook was interrupted while waiting for the preparing thread to shut down. Stopping the shutdown hook");
				return;
			}

			try {
				sendingThread.join(10000);
			} catch (InterruptedException e) {
				log.error("ShutdownHook was interrupted while waiting for the sending thread to shut down. Stopping the shutdown hook");
				return;
			}

			// Try to prepare data for the last time.
			CoreService.this.prepareData();

			// Try to send data for the last time. We do not set a timeout here, the user can simply
			// kill the process for good if it takes too long.
			CoreService.this.send();

			// At the end unregister platform
			log.info("Unregistering the Agent");
			platformManager.unregisterPlatform();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() throws Exception {
		stop();
	}

}
