package info.novatec.inspectit.agent.connection;

import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.communication.DefaultData;

import java.net.ConnectException;
import java.util.List;

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
	 * Send the measurements to the server for further processing.
	 * 
	 * @param dataObjects
	 *            The measurements to send.
	 * @throws ServerUnavailableException
	 *             If the sending wasn't successful in any way, a {@link ServerUnavailableException}
	 *             exception is thrown.
	 */
	void sendDataObjects(List<? extends DefaultData> dataObjects) throws ServerUnavailableException;

	/**
	 * Registers the current platform (composed of the network interface with the Agent name) in the
	 * CMR and returns a unique value for this platform.
	 * 
	 * @param agentName
	 *            The name of the agent.
	 * @param version
	 *            The version of the agent.
	 * @return The unique id for this platform.
	 * @throws ServerUnavailableException
	 *             If the sending wasn't successful in any way, a {@link ServerUnavailableException}
	 *             exception is thrown.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 */
	long registerPlatform(String agentName, String version) throws ServerUnavailableException, RegistrationException;

	/**
	 * Unregisters the platform in the CMR by sending the agent name and the network interfaces
	 * defined by the machine.
	 * 
	 * @param agentName
	 *            Name of the Agent.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the un-registration process appears.
	 */
	void unregisterPlatform(String agentName) throws RegistrationException;

	/**
	 * Registers the specified parameters at the server and returns a unique identifier which will
	 * be used throughout the sensors.
	 * 
	 * @param platformId
	 *            The unique id for this platform.
	 * @param sensorConfig
	 *            The registered sensor configuration.
	 * 
	 * @return Returns the unique identifier.
	 * @throws ServerUnavailableException
	 *             If the sending wasn't successful in any way, a {@link ServerUnavailableException}
	 *             exception is thrown.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 */
	long registerMethod(long platformId, RegisteredSensorConfig sensorConfig) throws ServerUnavailableException, RegistrationException;

	/**
	 * Registers the specified method sensor type at the CMR.
	 * 
	 * @param platformId
	 *            The unique id for this platform.
	 * @param methodSensorTypeConfig
	 *            The unregistered sensor type configuration.
	 * 
	 * @return Returns the unique identifier.
	 * @throws ServerUnavailableException
	 *             If the sending wasn't successful in any way, a {@link ServerUnavailableException}
	 *             exception is thrown.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 */
	long registerMethodSensorType(long platformId, MethodSensorTypeConfig methodSensorTypeConfig) throws ServerUnavailableException, RegistrationException;

	/**
	 * Registers the specified platform sensor type at the CMR.
	 * 
	 * @param platformId
	 *            The unique id for this platform.
	 * @param platformSensorTypeConfig
	 *            The unregistered sensor type configuration.
	 * 
	 * @return Returns the unique identifier.
	 * @throws ServerUnavailableException
	 *             If the sending wasn't successful in any way, a {@link ServerUnavailableException}
	 *             exception is thrown.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 */
	long registerPlatformSensorType(long platformId, PlatformSensorTypeConfig platformSensorTypeConfig) throws ServerUnavailableException, RegistrationException;

	/**
	 * Adds a sensor type to an already registered sensor at the CMR.
	 * 
	 * @param sensorTypeId
	 *            The id of the sensor type.
	 * @param methodId
	 *            The id of the method.
	 * @throws ServerUnavailableException
	 *             If the sending wasn't successful in any way, a {@link ServerUnavailableException}
	 *             exception is thrown.
	 * @throws RegistrationException
	 *             This exception is thrown when a problem with the registration process appears.
	 */
	void addSensorTypeToMethod(long sensorTypeId, long methodId) throws ServerUnavailableException, RegistrationException;

}
