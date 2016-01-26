package info.novatec.inspectit.agent.connection;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationResult;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;

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
	 * Registers the agent with the CMR. The CMR will answer with the {@link AgentConfiguration}
	 * containing all necessary information for the agent initialization.
	 *
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent. Can be <code>null</code>.
	 * @param version
	 *            The version the agent is currently running with.
	 * @return {@link AgentConfiguration}.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 */
	AgentConfiguration register(String agentName, String version) throws ServerUnavailableException, RegistrationException;

	/**
	 * Unregisters the platform in the CMR by sending the agent name and the network interfaces
	 * defined by the machine.
	 *
	 * @param agentName
	 *            Name of the Agent.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the un-registration process appears.
	 */
	void unregister(String agentName) throws ServerUnavailableException, RegistrationException;

	/**
	 * Analyzes and instruments the given byte code if necessary, returning the byte code to use on
	 * the Agent.
	 *
	 * @param platformIdent
	 *            Id of the agent.
	 * @param hash
	 *            Class hash code.
	 * @param type
	 *            Type that has been parsed from the byte-code.
	 * @return Instrumentation result containing modified byte code or <code>null</code> if nothing
	 *         was instrumented or any kind of exception occurred.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 * @throws BusinessException
	 *             If {@link BusinessException} is thrown on the server.
	 */
	InstrumentationResult analyzeAndInstrument(long platformIdent, String hash, Type type) throws ServerUnavailableException, BusinessException;

	/**
	 * Informs the CMR that the methods have been instrumented on the agent.
	 *
	 * @param methodToSensorMap
	 *            Map containing method id as key and applied sensor IDs.
	 * @throws ServerUnavailableException
	 *             If server to send the request to is unavailable.
	 */
	void instrumentationApplied(Map<Long, long[]> methodToSensorMap) throws ServerUnavailableException;
}
