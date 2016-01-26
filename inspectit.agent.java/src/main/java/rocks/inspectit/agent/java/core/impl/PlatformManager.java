package rocks.inspectit.agent.java.core.impl;

import java.net.ConnectException;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.config.impl.RepositoryConfig;
import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.RegistrationException;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.version.VersionService;

/**
 * The platform manager that only holds the platform ident id and performs simple registration on
 * the start-up.
 *
 * @author Ivan Senic
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 *
 */
@Component("platformManager")
public class PlatformManager implements IPlatformManager, InitializingBean {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The configuration storage used to access some information which needs to be registered at the
	 * server.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * The versioning service.
	 */
	@Autowired
	private VersionService versionService;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * The id of this platform.
	 */
	private long platformId = -1;

	/**
	 * If set to <code>true</code>, the connection to server created an exception.
	 */
	private volatile boolean serverErrorOccured = false;

	/**
	 * {@inheritDoc}
	 */
	public boolean isPlatformRegistered() {
		return -1 != platformId;
	}

	/**
	 * {@inheritDoc}
	 * <P>
	 * For now just return the id.
	 */
	public long getPlatformId() throws IdNotAvailableException {
		if (-1 == platformId) {
			throw new IdNotAvailableException("No ID available in the moment.");
		}
		return platformId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterPlatform() {
		if (connection.isConnected() && isPlatformRegistered()) {
			try {
				connection.unregister(platformId);
				connection.disconnect();
				platformId = -1;
			} catch (Throwable e) { // NOPMD
				log.warn("Could not un-register the platform.");
			}
		}
	}

	/**
	 * Performs the registration.
	 *
	 * @throws StorageException
	 *             If setting configuration to the {@link #configurationStorage} fails.
	 */
	private void doRegistration() throws StorageException {
		try {
			// not connected? -> connect
			if (!connection.isConnected()) {
				connect();
			}

			// register the agent
			if (!isPlatformRegistered()) {
				register();
			}

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
	 *                Throws a ConnectException if there was a problem connecting to the repository.
	 */
	private void connect() throws ConnectException {
		RepositoryConfig repositoryConfig = configurationStorage.getRepositoryConfig();
		connection.connect(repositoryConfig.getHost(), repositoryConfig.getPort());
	}

	/**
	 * Registers the platform at the CMR.
	 *
	 * @throws ServerUnavailableException
	 *             If the sending wasn't successful in any way, a {@link ServerUnavailableException}
	 *             exception is thrown.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 * @throws StorageException
	 *             If setting configuration to the {@link #configurationStorage} fails.
	 */
	private void register() throws ServerUnavailableException, RegistrationException, StorageException {
		try {
			AgentConfig agentConfiguration = connection.register(configurationStorage.getAgentName(), versionService.getVersionAsString());
			configurationStorage.setAgentConfiguration(agentConfiguration);
			platformId = agentConfiguration.getPlatformId();

			if (log.isDebugEnabled()) {
				log.debug("Received platform ID: " + platformId);
			}
		} catch (BusinessException exception) {
			log.error("The agent was not able to get configuration from the server. " + exception.getMessage());
			throw new BeanInitializationException("The agent was not able to get configuration from the server.", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		doRegistration();
	}

}
