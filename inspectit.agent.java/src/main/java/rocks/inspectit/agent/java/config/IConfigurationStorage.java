package rocks.inspectit.agent.java.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import rocks.inspectit.agent.java.config.impl.RepositoryConfig;
import rocks.inspectit.agent.java.config.impl.UnregisteredJmxConfig;
import rocks.inspectit.agent.java.sensor.exception.IExceptionSensor;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.StrategyConfig;
import rocks.inspectit.shared.all.pattern.IMatchPattern;

/**
 * This storage is used by all configuration readers to store the information into.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Alfred Krauss
 * @author Ivan Senic
 */
public interface IConfigurationStorage {

	/**
	 * Sets the repository. Internally, a {@link RepositoryConfig} class is instantiated and filled
	 * with the proper arguments.
	 *
	 * @param host
	 *            The host ip / name.
	 * @param port
	 *            The host port.
	 * @throws StorageException
	 *             Thrown if something with the host name or port is wrong.
	 */
	void setRepository(String host, int port) throws StorageException;

	/**
	 * Sets the {@link AgentConfig}.
	 *
	 * @param agentConfiguration
	 *            Agent configuration received from the server.
	 * @throws StorageException
	 *             If registration of the components defined in the configuration fails.
	 */
	void setAgentConfiguration(AgentConfig agentConfiguration) throws StorageException;

	/**
	 * Returns an instance of the {@link RepositoryConfig} class which is filled with the values by
	 * the {@link #setRepository(String, int)} method.
	 *
	 * @return The {@link RepositoryConfig} instance.
	 */
	RepositoryConfig getRepositoryConfig();

	/**
	 * Sets the name of the Agent.
	 *
	 * @param name
	 *            The name of the Agent to set.
	 * @throws StorageException
	 *             Thrown if something with the agent name is wrong.
	 */
	void setAgentName(String name) throws StorageException;

	/**
	 * Returns the name of the Agent.
	 *
	 * @return The name of the Agent.
	 */
	String getAgentName();

	/**
	 * Returns a {@link StrategyConfig} instance containing the buffer strategy information.
	 *
	 * @return An instance of {@link StrategyConfig}.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	StrategyConfig getBufferStrategyConfig() throws StorageException;

	/**
	 * Returns a {@link StrategyConfig} instance containing the sending strategy
	 * information.
	 *
	 * @return Used {@link StrategyConfig} instances.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	StrategyConfig getSendingStrategyConfig() throws StorageException;

	/**
	 * Returns a {@link List} of the {@link MethodSensorTypeConfig} classes.
	 *
	 * @return A {@link List} of {@link MethodSensorTypeConfig} classes.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	List<MethodSensorTypeConfig> getMethodSensorTypes() throws StorageException;

	/**
	 * Returns a {@link List} of the {@link JmxSensorTypeConfig} classes.
	 *
	 * @return A {@link List} of {@link JmxSensorTypeConfig} classes.
	 */
	List<JmxSensorTypeConfig> getJmxSensorTypes();

	/**
	 * Returns the exception sensor types.
	 *
	 * @return Returns the exception sensor types.
	 *
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	ExceptionSensorTypeConfig getExceptionSensorType() throws StorageException;

	/**
	 * Returns a {@link List} of the {@link PlatformSensorTypeConfig} classes.
	 *
	 * @return A {@link List} of {@link PlatformSensorTypeConfig} classes.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	List<PlatformSensorTypeConfig> getPlatformSensorTypes() throws StorageException;

	/**
	 * Returns a {@link List} of the {@link UnregisteredJmxConfig} classes.
	 *
	 * @return A {@link List} of {@link UnregisteredJmxConfig} classes.
	 */
	List<UnregisteredJmxConfig> getUnregisteredJmxConfigs();

	/**
	 * Returns whether the {@link IExceptionSensor} is activated.
	 *
	 * @return Whether the {@link IExceptionSensor} is activated.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	boolean isExceptionSensorActivated() throws StorageException;

	/**
	 * Returns whether enhanced exception events are instrumented with try/catch.
	 *
	 * @return Whether enhanced exception events are instrumented.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	boolean isEnhancedExceptionSensorActivated() throws StorageException;

	/**
	 * Returns the patterns that denote the classes that should be ignored.
	 *
	 * @return Returns the patterns that denote the classes that should be ignored.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	Collection<IMatchPattern> getIgnoreClassesPatterns() throws StorageException;

	/**
	 * Returns if the class cache for the agent exist on the CMR. If this is set to
	 * <code>true</code> agent can use its internal sending classes cache, otherwise agent should
	 * send all the loaded classes to the CMR ignoring the sending cache state.
	 *
	 * @return Returns if the class cache for the agent exist on the CMR.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	boolean isClassCacheExistsOnCmr() throws StorageException;

	/**
	 * Set of known {@link InstrumentationDefinition} for the agent that can be used by the Agent right
	 * away. Each {@link InstrumentationDefinition} is mapped to the collection of the class hashes it
	 * relates to.
	 *
	 * @return Set of known {@link InstrumentationDefinition} for the agent that can be used by the
	 *         Agent right away. Each {@link InstrumentationDefinition} is mapped to the collection of
	 *         the class hashes it relates to.
	 * @throws StorageException
	 *             If agent configuration is not set.
	 */
	Map<Collection<String>, InstrumentationDefinition> getInitialInstrumentationResults() throws StorageException;

}