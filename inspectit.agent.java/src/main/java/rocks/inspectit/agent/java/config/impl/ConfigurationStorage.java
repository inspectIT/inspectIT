package rocks.inspectit.agent.java.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.logback.LogInitializer;
import rocks.inspectit.agent.java.spring.SpringConfiguration;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentEndUserMonitoringConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.RetransformationStrategy;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * New version of the {@link IConfigurationStorage} that reads configuration from the
 * {@link AgentConfig}.
 *
 * @author Ivan Senic
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Alfred Krauss
 */
@Component
public class ConfigurationStorage implements IConfigurationStorage, InitializingBean {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link SpringConfiguration} to process the received configuration.
	 */
	@Autowired
	private SpringConfiguration springConfiguration;

	/**
	 * The name of the property for the agent name.
	 */
	static final String AGENT_NAME_PROPERTY = "inspectit.agent.name";

	/**
	 * The name of the property for the repository IP.
	 */
	static final String REPOSITORY_PROPERTY = "inspectit.repository";

	/**
	 * Default agent name used.
	 */
	private static final String DEFAULT_AGENT_NAME = "inspectIT";

	/**
	 * The repository configuration is used to store the needed information to connect to a remote
	 * CMR.
	 */
	private RepositoryConfig repository;

	/**
	 * The name of the agent.
	 */
	private String agentName;

	/**
	 * Agent configuration.
	 */
	private AgentConfig agentConfiguration;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setRepository(String host, int port) throws StorageException {
		if ((null == host) || "".equals(host)) {
			throw new StorageException("Repository host name cannot be null or empty!");
		}

		if (port < 1) {
			throw new StorageException("Repository port has to be greater than 0!");
		}

		// can not reset repository
		if (null == repository) {
			this.repository = new RepositoryConfig(host, port);
		}

		if (log.isInfoEnabled()) {
			log.info("Repository definition added. Host: " + host + " Port: " + port);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryConfig getRepositoryConfig() {
		return repository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setAgentName(String name) throws StorageException {
		if ((null == name) || "".equals(name)) {
			throw new StorageException("Agent name cannot be null or empty!");
		}

		// don't allow reseting
		if (null == agentName) {
			agentName = name;

			// when we know the name init the logging
			LogInitializer.setAgentNameAndInitLogging(name);
		}

		if (log.isInfoEnabled()) {
			log.info("Agent name set to: " + name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAgentName() {
		return agentName;
	}

	/**
	 * Sets {@link #agentConfiguration}.
	 *
	 * @param agentConfiguration
	 *            New value for {@link #agentConfiguration}
	 * @throws StorageException
	 *             If registration of the components defined in the configuration fails.
	 * @see SpringConfiguration#registerComponents(IConfigurationStorage)
	 */
	@Override
	public void setAgentConfiguration(AgentConfig agentConfiguration) throws StorageException {
		if (null == this.agentConfiguration) {
			this.agentConfiguration = agentConfiguration;
		}

		try {
			springConfiguration.registerComponents(this);
		} catch (Exception e) {
			throw new StorageException("Registration of the configured components failed.", e);
		}

		if (log.isInfoEnabled()) {
			log.info("Agent configuration added with following configuration interface properties:");
			String[] lines = agentConfiguration.getConfigurationInfo().split("\n");
			for (String line : lines) {
				log.info(line);
			}
			log.info("Class-cache exists on the server: " + agentConfiguration.isClassCacheExistsOnCmr());
			if (agentConfiguration.isClassCacheExistsOnCmr()) {
				log.info("Number of initially instrumented classes: " + agentConfiguration.getInitialInstrumentationResults().size());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDataBufferSize() throws StorageException {
		ensureConfigurationExists();

		return agentConfiguration.getDataBufferSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MethodSensorTypeConfig> getMethodSensorTypes() throws StorageException {
		ensureConfigurationExists();

		List<MethodSensorTypeConfig> result = new ArrayList<MethodSensorTypeConfig>();

		if (CollectionUtils.isNotEmpty(agentConfiguration.getMethodSensorTypeConfigs())) {
			result.addAll(agentConfiguration.getMethodSensorTypeConfigs());
		}

		// exception sensor is also method sensor type
		if (null != agentConfiguration.getExceptionSensorTypeConfig()) {
			result.add(agentConfiguration.getExceptionSensorTypeConfig());
		}

		// special sensors are also method sensor types
		if (CollectionUtils.isNotEmpty(agentConfiguration.getSpecialMethodSensorTypeConfigs())) {
			result.addAll(agentConfiguration.getSpecialMethodSensorTypeConfigs());
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExceptionSensorTypeConfig getExceptionSensorType() throws StorageException {
		ensureConfigurationExists();

		return agentConfiguration.getExceptionSensorTypeConfig();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PlatformSensorTypeConfig> getPlatformSensorTypes() throws StorageException {
		ensureConfigurationExists();

		List<PlatformSensorTypeConfig> result = new ArrayList<PlatformSensorTypeConfig>(1);

		if (CollectionUtils.isNotEmpty(agentConfiguration.getPlatformSensorTypeConfigs())) {
			result.addAll(agentConfiguration.getPlatformSensorTypeConfigs());
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<JmxSensorTypeConfig> getJmxSensorTypes() throws StorageException {
		ensureConfigurationExists();

		List<JmxSensorTypeConfig> result = new ArrayList<JmxSensorTypeConfig>(1);

		if (null != agentConfiguration.getJmxSensorTypeConfig()) {
			result.add(agentConfiguration.getJmxSensorTypeConfig());
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RetransformationStrategy getRetransformStrategy() throws StorageException {
		ensureConfigurationExists();

		return agentConfiguration.getRetransformationStrategy();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExceptionSensorActivated() throws StorageException {
		ensureConfigurationExists();

		return null != agentConfiguration.getExceptionSensorTypeConfig();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnhancedExceptionSensorActivated() throws StorageException {
		ensureConfigurationExists();

		if (isExceptionSensorActivated()) {
			return agentConfiguration.getExceptionSensorTypeConfig().isEnhanced();
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<IMatchPattern> getIgnoreClassesPatterns() throws StorageException {
		ensureConfigurationExists();

		if (CollectionUtils.isNotEmpty(agentConfiguration.getExcludeClassesPatterns())) {
			return Collections.unmodifiableCollection(agentConfiguration.getExcludeClassesPatterns());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isClassCacheExistsOnCmr() throws StorageException {
		ensureConfigurationExists();

		return agentConfiguration.isClassCacheExistsOnCmr();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Collection<String>, InstrumentationDefinition> getInitialInstrumentationResults() throws StorageException {
		ensureConfigurationExists();

		return agentConfiguration.getInitialInstrumentationResults();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentEndUserMonitoringConfig getEndUserMonitoringConfig() throws StorageException {
		ensureConfigurationExists();
		return agentConfiguration.getEumConfig();
	}

	/**
	 * Checks if the JVM parameters have the repository and agent information.
	 */
	private void loadConfigurationFromJvmParameters() {

		// check if the information about the repository and agent is provided with the JVM params
		String repositoryProperty = System.getProperty(REPOSITORY_PROPERTY);

		if (null != repositoryProperty) {
			String[] repositoryIpHost = repositoryProperty.split(":");
			if (repositoryIpHost.length == 2) {
				String repositoryIp = repositoryIpHost[0];
				String repositoryPort = repositoryIpHost[1];
				if (StringUtils.isNotBlank(repositoryIp) && StringUtils.isNotBlank(repositoryPort)) {
					log.info("Repository information found in the JVM parameters: IP=" + repositoryIp + " Port=" + repositoryPort);
					try {
						int port = Integer.parseInt(repositoryPort);
						setRepository(repositoryIp, port);
					} catch (Exception e) {
						log.warn("Repository could not be defined from the data in the JVM parameters", e);
					}
				}
			}
		}

		// agent name
		String agentName = System.getProperty(AGENT_NAME_PROPERTY);
		if (StringUtils.isNotBlank(agentName)) {
			try {
				log.info("Agent name found in the JVM parameters: AgentName=" + agentName);
				setAgentName(agentName);
			} catch (Exception e) {
				log.warn("Agent name could not be defined from the data in the JVM parameters", e);
			}
		} else {
			try {
				setAgentName(DEFAULT_AGENT_NAME);
			} catch (StorageException e) {
				log.warn("Agent name could not be defined from default agent name", e);
			}
		}
	}

	/**
	 * Helper method to ensure that {@link #agentConfiguration} is not <code>null</code>.
	 *
	 * @throws StorageException
	 *             If configuration is null.
	 */
	private void ensureConfigurationExists() throws StorageException {
		if (null == agentConfiguration) {
			throw new StorageException("Agent configuration is not set in the Configuration storage");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		loadConfigurationFromJvmParameters();
		if ((null == repository) || StringUtils.isEmpty(agentName)) {
			throw new BeanInitializationException("inspectIT agent must be initialized with IP and port of the CMR via JVM parameters.");
		}
	}

}
