package info.novatec.inspectit.agent.core.impl;

import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.AbstractSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.JmxSensorConfig;
import info.novatec.inspectit.agent.config.impl.JmxSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.config.impl.RepositoryConfig;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.RegistrationException;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.version.VersionService;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The default implementation of the ID Manager.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
@Component
public class IdManager implements IIdManager, InitializingBean, DisposableBean {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The configuration storage used to access some information which needs to be registered at the
	 * server.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The versioning service.
	 */
	private final VersionService versionService;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	private final IConnection connection;

	/**
	 * The id of this platform.
	 */
	private long platformId = -1;

	/**
	 * The mapping between the local and remote method ids.
	 */
	private Map<Long, Long> methodIdMap = new HashMap<Long, Long>();

	/**
	 * The mapping between the local and remote sensor type ids.
	 */
	private Map<Long, Long> sensorTypeIdMap = new HashMap<Long, Long>();

	/**
	 * The mapping between the local and remote jmxDefinitionData ids.
	 */
	private Map<Long, Long> jmxDefinitionDataIdMap = new HashMap<Long, Long>();

	/**
	 * The {@link Thread} used to register the outstanding methods, sensor types etc.
	 */
	private volatile RegistrationThread registrationThread;

	/**
	 * The methods to register at the server.
	 */
	private LinkedList<RegisteredSensorConfig> methodsToRegister = new LinkedList<RegisteredSensorConfig>(); // NOPMD

	/**
	 * The jmx definition data to register at the server.
	 */
	private LinkedList<JmxSensorConfig> jmxDefinitionDataIdentToRegister = new LinkedList<JmxSensorConfig>(); // NOPMD

	/**
	 * The sensor types to register at the server.
	 */
	private LinkedList<AbstractSensorTypeConfig> sensorTypesToRegister = new LinkedList<AbstractSensorTypeConfig>(); // NOPMD

	/**
	 * The mapping between the sensor types and methods to register at the server.
	 */
	private LinkedList<SensorTypeToMethodMapping> sensorTypeToMethodRegister = new LinkedList<SensorTypeToMethodMapping>(); // NOPMD

	/**
	 * If set to <code>true</code>, the connection to server created an exception.
	 */
	private volatile boolean serverErrorOccured = false;

	/**
	 * If set to <code>true</code> any attempt to get the platform id will fail cause the underlying
	 * JVM is shutting down.
	 */
	private volatile boolean shutdownInitialized = false;

	/**
	 * Default constructor. Needs an implementation of the {@link IConnection} interface to
	 * establish the connection to the server.
	 * 
	 * @param configurationStorage
	 *            The configuration storage.
	 * @param connection
	 *            The connection to the server.
	 * @param versionService
	 *            The versioning service.
	 */
	@Autowired
	public IdManager(IConfigurationStorage configurationStorage, IConnection connection, VersionService versionService) {
		this.configurationStorage = configurationStorage;
		this.connection = connection;
		this.versionService = versionService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		if (null == registrationThread) {
			registrationThread = new RegistrationThread();
			registrationThread.start();
		}

		// register all method sensor types saved in the configuration storage
		for (MethodSensorTypeConfig config : configurationStorage.getMethodSensorTypes()) {
			this.registerMethodSensorType(config);
		}

		// register all platform sensor types saved in the configuration storage
		for (PlatformSensorTypeConfig config : configurationStorage.getPlatformSensorTypes()) {
			this.registerPlatformSensorType(config);
		}

		// register all jmx sensor types saved in the configuration storage
		for (JmxSensorTypeConfig config : configurationStorage.getJmxSensorTypes()) {
			this.registerJmxSensorType(config);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		// set the registration thread to null to indicate that the while loop
		// will be finished on the next run.
		Thread temp = registrationThread;
		registrationThread = null; // NOPMD
		synchronized (temp) {
			temp.interrupt();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPlatformRegistered() {
		return -1 != platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPlatformId() throws IdNotAvailableException {
		// if we are not connected to the server and the platform id was not
		// received yet we are throwing an IdNotAvailableException
		if (!connection.isConnected() && !isPlatformRegistered() && !shutdownInitialized) {
			if (!serverErrorOccured) {
				try {
					registrationThread.connect();
					registrationThread.registerPlatform();
				} catch (Throwable throwable) { // NOPMD
					serverErrorOccured = true;
					throw new IdNotAvailableException("Connection is not established yet, cannot retrieve platform ID", throwable);
				}
			} else {
				throw new IdNotAvailableException("Cannot retrieve platform ID");
			}
		} else if (!isPlatformRegistered() && !shutdownInitialized) {
			if (!serverErrorOccured) {
				// If the platform is not registered and no server error
				// occurred, the registration is started
				try {
					registrationThread.registerPlatform();
				} catch (Throwable throwable) { // NOPMD
					serverErrorOccured = true;
					log.warn("Could not register the platform even though the connection seems to be established, will try later!");
					throw new IdNotAvailableException("Could not register the platform even though the connection seems to be established, will try later!", throwable);
				}
			} else {
				throw new IdNotAvailableException("Cannot retrieve platform ID");
			}
		} else if (shutdownInitialized) {
			throw new IdNotAvailableException("Cannot retrieve platform ID because the shutdown has been initialized.");
		}

		return platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterPlatform() {
		this.shutdownInitialized = true;
		if (connection.isConnected() && isPlatformRegistered()) {
			try {
				connection.unregisterPlatform(configurationStorage.getAgentName());
				platformId = -1;
			} catch (Throwable e) { // NOPMD
				log.warn("Could not un-register the platform.");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRegisteredMethodId(long methodId) throws IdNotAvailableException {
		Long methodIdentifier = Long.valueOf(methodId);

		// do not enter the block if the method ID map already contains this
		// identifier (which means that it is already registered).
		if (!methodIdMap.containsKey(methodIdentifier)) {
			throw new IdNotAvailableException("Method ID '" + methodId + "' is not mapped");
		} else {
			Long registeredMethodIdentifier = methodIdMap.get(methodIdentifier);
			return registeredMethodIdentifier.longValue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRegisteredSensorTypeId(long sensorTypeId) throws IdNotAvailableException {
		// same procedure here as in the #getRegisteredMethodId(...) method.
		Long sensorTypeIdentifier = Long.valueOf(sensorTypeId);

		if (!sensorTypeIdMap.containsKey(sensorTypeIdentifier)) {
			throw new IdNotAvailableException("Sensor Type ID '" + sensorTypeId + "' is not mapped");
		} else {
			Long registeredSensorTypeIdentifier = sensorTypeIdMap.get(sensorTypeIdentifier);
			return registeredSensorTypeIdentifier.longValue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRegisteredmBeanId(long mBeanId) throws IdNotAvailableException {
		// same procedure here as in the #getRegisteredMethodId(...) method.
		Long mBeanIdentifier = Long.valueOf(mBeanId);

		if (!sensorTypeIdMap.containsKey(mBeanIdentifier)) {
			throw new IdNotAvailableException("mBean '" + mBeanId + "' is not mapped");
		} else {
			Long registeredSensorTypeIdentifier = (Long) jmxDefinitionDataIdMap.get(mBeanIdentifier);
			return registeredSensorTypeIdentifier.longValue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethod(RegisteredSensorConfig registeredSensorConfig) {
		long id;
		synchronized (methodsToRegister) {
			id = methodIdMap.size() + methodsToRegister.size();
		}
		registeredSensorConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerMethod(registeredSensorConfig);
			} catch (Throwable throwable) { // NOPMD
				synchronized (methodsToRegister) {
					methodsToRegister.addLast(registeredSensorConfig);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (methodsToRegister) {
				methodsToRegister.addLast(registeredSensorConfig);
			}
		}

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethodSensorType(MethodSensorTypeConfig methodSensorTypeConfig) {
		// same procedure here as in #registerMethod(...)
		long id;
		synchronized (sensorTypesToRegister) {
			id = sensorTypeIdMap.size() + sensorTypesToRegister.size();
		}
		methodSensorTypeConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerSensorType(methodSensorTypeConfig);
			} catch (Throwable throwable) { // NOPMD
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.addLast(methodSensorTypeConfig);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (sensorTypesToRegister) {
				sensorTypesToRegister.addLast(methodSensorTypeConfig);
			}
		}

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensorTypeToMethod(long sensorTypeId, long methodId) {
		// nearly same procedure as in #registerMethod(...) but without
		// returning a value. This mapping only needs to be registered.

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.addSensorTypeToMethod(Long.valueOf(sensorTypeId), Long.valueOf(methodId));
			} catch (Throwable throwable) { // NOPMD
				synchronized (sensorTypeToMethodRegister) {
					sensorTypeToMethodRegister.addLast(new SensorTypeToMethodMapping(sensorTypeId, methodId));
				}

				// start the thread to retry the registration
				synchronized (registrationThread) {
					registrationThread.notifyAll();
				}
			}
		} else {
			synchronized (sensorTypeToMethodRegister) {
				sensorTypeToMethodRegister.addLast(new SensorTypeToMethodMapping(sensorTypeId, methodId));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatformSensorType(PlatformSensorTypeConfig platformSensorTypeConfig) {
		// same procedure here as in #registerMethod(...)
		long id;
		synchronized (sensorTypesToRegister) {
			id = sensorTypeIdMap.size() + sensorTypesToRegister.size();
		}
		platformSensorTypeConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerSensorType(platformSensorTypeConfig);
			} catch (Throwable throwable) { // NOPMD
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.addLast(platformSensorTypeConfig);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (sensorTypesToRegister) {
				sensorTypesToRegister.addLast(platformSensorTypeConfig);
			}
		}

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerJmxSensorConfig(JmxSensorConfig config) {
		long id;
		synchronized (jmxDefinitionDataIdentToRegister) {
			id = jmxDefinitionDataIdMap.size() + jmxDefinitionDataIdentToRegister.size();
		}
		config.setId(id);
		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerJmxDefinitionData(config);
			} catch (Throwable throwable) { // NOPMD
				synchronized (jmxDefinitionDataIdentToRegister) {
					jmxDefinitionDataIdentToRegister.addLast(config);

					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (jmxDefinitionDataIdentToRegister) {
				jmxDefinitionDataIdentToRegister.addLast(config);
			}
		}

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerJmxSensorType(JmxSensorTypeConfig jmxSensorTypeConfig) {
		// same procedure here as in #registerMethod(...)
		long id;
		synchronized (sensorTypesToRegister) {
			id = sensorTypeIdMap.size() + sensorTypesToRegister.size();
		}
		jmxSensorTypeConfig.setId(id);

		if (!serverErrorOccured) {
			try {
				if (!isPlatformRegistered()) {
					getPlatformId();
				}

				registrationThread.registerSensorType(jmxSensorTypeConfig);
			} catch (Throwable throwable) { // NOPMD
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.addLast(jmxSensorTypeConfig);
					// start the thread to retry the registration
					synchronized (registrationThread) {
						registrationThread.notifyAll();
					}
				}
			}
		} else {
			synchronized (sensorTypesToRegister) {
				sensorTypesToRegister.addLast(jmxSensorTypeConfig);
			}
		}
		return id;
	}

	/**
	 * Private inner class used to store the mapping between the sensor type IDs and the method IDs.
	 * Only used if they are not yet registered.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static class SensorTypeToMethodMapping {

		/**
		 * The sensor type identifier.
		 */
		private long sensorTypeId;

		/**
		 * The method identifier.
		 */
		private long methodId;

		/**
		 * Creates a new instance.
		 * 
		 * @param sensorTypeId
		 *            the sensor type id.
		 * @param methodId
		 *            the method id.
		 */
		public SensorTypeToMethodMapping(long sensorTypeId, long methodId) {
			this.sensorTypeId = sensorTypeId;
			this.methodId = methodId;
		}

		public long getSensorTypeId() {
			return sensorTypeId;
		}

		public long getMethodId() {
			return methodId;
		}

	}

	/**
	 * The {@link Thread} used to register the outstanding methods, sensor types etc.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private class RegistrationThread extends Thread {

		/**
		 * The default wait time between the registrations.
		 */
		private static final long REGISTRATION_WAIT_TIME = 10000L;

		/**
		 * Creates a new instance of the <code>RegistrationThread</code> as a daemon thread.
		 */
		public RegistrationThread() {
			setName("inspectit-registration-thread");
			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();
			// break out of the while loop if the registrationThread is set
			// to null in the stop method of the surrounding class.
			while (registrationThread == thisThread) { // NOPMD
				try {
					synchronized (this) {
						if (serverErrorOccured) {
							// wait for the given time till we try the
							// registering again.
							wait(REGISTRATION_WAIT_TIME);
						} else if (methodsToRegister.isEmpty() && sensorTypesToRegister.isEmpty() && sensorTypeToMethodRegister.isEmpty()) {
							// Wait for a Object#notify()
							wait();
						}
					}
				} catch (InterruptedException e) { // NOCHK
					// nothing to do
				}

				doRegistration();
			}
		}

		/**
		 * Execute the registration if needed.
		 */
		private void doRegistration() {
			try {
				// not connected? -> connect
				if (!connection.isConnected()) {
					connect();
				}

				// register the agent
				if (!isPlatformRegistered()) {
					registerPlatform();
				}

				registerMethods();
				registerJmxDefinitionDataIdents();
				registerSensorTypes();
				registerSensorTypeToMethodMapping();

				// clear the flag
				serverErrorOccured = false;
			} catch (ServerUnavailableException serverUnavailableException) {
				if (serverUnavailableException.isServerTimeout()) {
					log.error("Server timeout while trying to register something at the server.");
				} else {
					if (!serverErrorOccured) {
						log.error("Server unavailable while trying to register something at the server.");
					}
					serverErrorOccured = true;
				}
			} catch (RegistrationException registrationException) {
				log.error("Registration exception occurred while trying to register something at the server.", registrationException);
			} catch (ConnectException connectException) {
				if (!serverErrorOccured) {
					log.error("Connection to the server failed.", connectException);
				}
				serverErrorOccured = true;
			}
		}

		/**
		 * Establish the connection to the server.
		 * 
		 * @exception ConnectException
		 *                Throws a ConnectException if there was a problem connecting to the
		 *                repository.
		 */
		private void connect() throws ConnectException {
			RepositoryConfig repositoryConfig = configurationStorage.getRepositoryConfig();
			connection.connect(repositoryConfig.getHost(), repositoryConfig.getPort());
		}

		/**
		 * Registers the platform at the CMR.
		 * 
		 * @throws ServerUnavailableException
		 *             If the sending wasn't successful in any way, a
		 *             {@link ServerUnavailableException} exception is thrown.
		 * @throws RegistrationException
		 *             This exception is thrown when a problem with the registration process
		 *             appears.
		 */
		private void registerPlatform() throws ServerUnavailableException, RegistrationException {
			platformId = connection.registerPlatform(configurationStorage.getAgentName(), versionService.getVersionAsString());

			if (log.isDebugEnabled()) {
				log.debug("Received platform ID: " + platformId);
			}
		}

		/**
		 * Registers all sensor type to method mappings on the server.
		 * 
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the mapping on the
		 *             server.
		 */
		private void registerSensorTypeToMethodMapping() throws ServerUnavailableException, RegistrationException {
			while (!sensorTypeToMethodRegister.isEmpty()) {
				SensorTypeToMethodMapping mapping;
				mapping = sensorTypeToMethodRegister.getFirst();

				Long sensorTypeId = Long.valueOf(mapping.getSensorTypeId());
				Long methodId = Long.valueOf(mapping.getMethodId());

				this.addSensorTypeToMethod(sensorTypeId, methodId);
				synchronized (sensorTypeToMethodRegister) {
					sensorTypeToMethodRegister.removeFirst();
				}
			}
		}

		/**
		 * Registers the mapping between the sensor type and a method.
		 * 
		 * @param sensorTypeId
		 *            The sensor type id.
		 * @param methodId
		 *            The method id.
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the mapping on the
		 *             server.
		 */
		private void addSensorTypeToMethod(Long sensorTypeId, Long methodId) throws ServerUnavailableException, RegistrationException {
			if (!sensorTypeIdMap.containsKey(sensorTypeId)) {
				throw new RegistrationException("Sensor type ID could not be found in the map!");
			}

			if (!methodIdMap.containsKey(methodId)) {
				throw new RegistrationException("Method ID could not be found in the map!");
			}

			Long serverSensorTypeId = sensorTypeIdMap.get(sensorTypeId);
			Long serverMethodId = methodIdMap.get(methodId);

			connection.addSensorTypeToMethod(serverSensorTypeId.longValue(), serverMethodId.longValue());

			if (log.isDebugEnabled()) {
				log.debug("Mapping registered (method -> sensor type) :: local:" + methodId + "->" + sensorTypeId + " global:" + serverMethodId + "->" + serverSensorTypeId);
			}
		}

		/**
		 * Registers all sensor types on the server.
		 * 
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occured.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the sensor types on the
		 *             server.
		 */
		private void registerSensorTypes() throws ServerUnavailableException, RegistrationException {
			while (!sensorTypesToRegister.isEmpty()) {
				AbstractSensorTypeConfig astc = sensorTypesToRegister.getFirst();

				this.registerSensorType(astc);
				synchronized (sensorTypesToRegister) {
					sensorTypesToRegister.removeFirst();
				}
			}
		}

		/**
		 * Registers a sensor type configuration at the server. Accepts
		 * {@link MethodSensorTypeConfig} and {@link PlatformSensorTypeConfig} objects.
		 * 
		 * @param astc
		 *            The sensor type configuration.
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the sensor types on the
		 *             server.
		 */
		private void registerSensorType(AbstractSensorTypeConfig astc) throws ServerUnavailableException, RegistrationException {
			long registeredId;
			if (astc instanceof MethodSensorTypeConfig) {
				registeredId = connection.registerMethodSensorType(platformId, (MethodSensorTypeConfig) astc);
			} else if (astc instanceof JmxSensorTypeConfig) {
				registeredId = connection.registerJmxSensorType(platformId, (JmxSensorTypeConfig) astc);
			} else if (astc instanceof PlatformSensorTypeConfig) {
				registeredId = connection.registerPlatformSensorType(platformId, (PlatformSensorTypeConfig) astc);
			} else {
				throw new RegistrationException("Could not register sensor type, because unhandled type: " + astc.getClass().getName());
			}

			synchronized (sensorTypesToRegister) {
				Long localId = Long.valueOf(sensorTypeIdMap.size());
				sensorTypeIdMap.put(localId, Long.valueOf(registeredId));

				if (log.isDebugEnabled()) {
					log.debug("Sensor type " + astc.toString() + " registered. ID (local/global): " + localId + "/" + registeredId);
				}
			}
		}

		/**
		 * Registers all methods on the server.
		 * 
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the sensor types on the
		 *             server.
		 */
		private void registerMethods() throws ServerUnavailableException, RegistrationException {
			while (!methodsToRegister.isEmpty()) {
				RegisteredSensorConfig rsc;
				rsc = methodsToRegister.getFirst();
				this.registerMethod(rsc);
				synchronized (methodsToRegister) {
					methodsToRegister.removeFirst();
				}
			}
		}

		/**
		 * Registers a method on the server and maps the local and global id.
		 * 
		 * @param rsc
		 *            The {@link RegisteredSensorConfig} to be registered at the server.
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the sensor types on the
		 *             server.
		 */
		private void registerMethod(RegisteredSensorConfig rsc) throws ServerUnavailableException, RegistrationException {
			long registeredId = connection.registerMethod(platformId, rsc);
			synchronized (methodsToRegister) {
				Long localId = Long.valueOf(methodIdMap.size());
				methodIdMap.put(localId, Long.valueOf(registeredId));

				if (log.isDebugEnabled()) {
					log.debug("Method " + rsc.toString() + " registered. ID (local/global): " + localId + "/" + registeredId);
				}
			}
		}

		/**
		 * Registers all jmxDefinitionData on the server.
		 * 
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the sensor types on the
		 *             server.
		 */
		private void registerJmxDefinitionDataIdents() throws ServerUnavailableException, RegistrationException {
			while (!jmxDefinitionDataIdentToRegister.isEmpty()) {
				JmxSensorConfig jsc = (JmxSensorConfig) jmxDefinitionDataIdentToRegister.getFirst();
				this.registerJmxDefinitionData(jsc);
				synchronized (jmxDefinitionDataIdentToRegister) {
					jmxDefinitionDataIdentToRegister.removeFirst();
				}
			}
		}

		/**
		 * Registers a JmxDefinitionData on the server and maps the local and global id.
		 * 
		 * @param config
		 *            The {@link JmxSensorConfig} to be registered at the server.
		 * @throws ServerUnavailableException
		 *             Thrown if a server error occurred.
		 * @throws RegistrationException
		 *             Thrown if something happened while trying to register the sensor types on the
		 *             server.
		 */
		private void registerJmxDefinitionData(JmxSensorConfig config) throws ServerUnavailableException, RegistrationException {
			long registeredId = connection.registerJmxDefinitionData(platformId, config);
			synchronized (jmxDefinitionDataIdentToRegister) {
				Long localId = Long.valueOf(jmxDefinitionDataIdMap.size());
				jmxDefinitionDataIdMap.put(localId, Long.valueOf(registeredId));
				
				if (log.isDebugEnabled()) {
					log.debug("Method " + config.toString() + " registered. ID (local/global): " + localId + "/" + registeredId);
				}
			}
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
