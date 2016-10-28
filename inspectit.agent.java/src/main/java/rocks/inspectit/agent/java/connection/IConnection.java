package rocks.inspectit.agent.java.connection;

import java.net.ConnectException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;

/**
 * The connection interface to implement different connection types, like RMI, Corba, etc.
 *
 * @author Patrice Bouillet
 *
 */
public interface IConnection {

	/**
	 * Establish the connection to the server.
	 *
	 * @param host
	 *            The host / ip of the server.
	 * @param port
	 *            The port of the server.
	 * @exception ConnectException
	 *                Throws a ConnectException if there was a problem connecting to the repository.
	 */
	void connect(String host, int port) throws ConnectException;

	/**
	 * Tries to reconnect to the server. Note that this method can be called only if the connection
	 * has been already connected before and it's currently disconnected. Otherwise has no effect.
	 *
	 * @throws ConnectException
	 *             Throws a ConnectException if there was a problem reconnecting to the repository.
	 */
	void reconnect() throws ConnectException;

	/**
	 * Disconnect from the server if possible.
	 */
	void disconnect();

	/**
	 * Returns if the connection is initialized and ready.
	 *
	 * @return Is the connection initialized and ready to use.
	 */
	boolean isConnected();

	/**
	 * Sends a keep-alive signal to give a sign of life.
	 *
	 * @param platformId
	 *            The unique id for this platform.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 */
	void sendKeepAlive(long platformId) throws ServerUnavailableException;

	/**
	 * Send the measurements to the server for further processing.
	 *
	 * @param dataObjects
	 *            The measurements to send.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 */
	void sendDataObjects(List<? extends DefaultData> dataObjects) throws ServerUnavailableException;

	/**
	 * Registers the agent with the CMR. The CMR will answer with the {@link AgentConfig} containing
	 * all necessary information for the agent initialization.
	 *
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent. Can not be <code>null</code>.
	 * @param version
	 *            The version the agent is currently running with.
	 * @return {@link AgentConfig}.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs on the server.
	 */
	AgentConfig register(String agentName, String version) throws ServerUnavailableException, RegistrationException, BusinessException;

	/**
	 * Unregisters the platform in the CMR by sending the agent name and the network interfaces
	 * defined by the machine.
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the un-registration process appears.
	 * @throws BusinessException
	 *             If {@link BusinessException} occurs on the server.
	 */
	void unregister(long platformIdent) throws ServerUnavailableException, RegistrationException, BusinessException;

	/**
	 * Analyzes the given byte code, returning the {@link InstrumentationDefinition} if the one
	 * given type should be instrumented on the Agent.
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 * @param hash
	 *            Class hash code.
	 * @param type
	 *            Type that has been parsed from the byte-code.
	 * @return Instrumentation definition containing method instrumentation points that should be
	 *         added for the given type.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 * @throws BusinessException
	 *             If {@link BusinessException} is thrown on the server.
	 */
	InstrumentationDefinition analyze(long platformIdent, String hash, Type type) throws ServerUnavailableException, BusinessException;

	/**
	 * Informs the CMR that the methods have been instrumented on the agent.
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 * @param methodToSensorMap
	 *            Map containing method id as key and applied sensor IDs.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 */
	void instrumentationApplied(long platformIdent, Map<Long, long[]> methodToSensorMap) throws ServerUnavailableException;

	/**
	 * Sends the given {@link JmxAttributeDescriptor} to the CMR, returning the ones that will be
	 * monitored, based on the current configuration on the server.
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 * @param attributeDescriptors
	 *            {@link JmxAttributeDescriptor}s that are available on the agent for monitoring.
	 * @return Collection of {@link JmxAttributeDescriptor} to be monitored with their correctly set
	 *         IDs.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 */
	Collection<JmxAttributeDescriptor> analyzeJmxAttributes(long platformIdent, Collection<JmxAttributeDescriptor> attributeDescriptors) throws ServerUnavailableException;

	/**
	 * Fetches all {@link IAgentMessage} which are available at the CMR. The returned list is
	 * an ordered list, ordered by time (ascending -> index 0 is the oldest).
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 * @return List of {@link IAgentMessage}s.
	 * @throws ServerUnavailableException
	 *             If agent with specified id does not exist.
	 */
	List<IAgentMessage<?>> fetchAgentMessages(long platformIdent) throws ServerUnavailableException;
}
