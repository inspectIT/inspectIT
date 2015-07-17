package info.novatec.inspectit.agent.core;

import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;

/**
 * The ID Manager is used to correlate between the local and global IDs (from the server).
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public interface IIdManager {

	/**
	 * Starts the ID Manager so that the registration will occur after a constant amount of time.
	 */
	void start();

	/**
	 * Stops this ID Manager, no registrations are executed any more.
	 */
	void stop();

	/**
	 * Returns if this platform is registered with the CMR, is equal to the fact that calling the
	 * {@link #getPlatformId()} method results in no {@link IdNotAvailableException}.
	 * 
	 * @return If this platform is registered.
	 */
	boolean isPlatformRegistered();

	/**
	 * Returns the platform id. This is unique for this java agent in this virtual machine as long
	 * as a different agent name is set on the same machine.
	 * 
	 * @return The unique platform id.
	 * @throws IdNotAvailableException
	 *             This exception is thrown if no ID can be retrieved from this manager.
	 */
	long getPlatformId() throws IdNotAvailableException;

	/**
	 * Un-registers the platform if the agent is currently connected and registered.After calling
	 * this method {@link IIdManager} will omit any further calls to register the platform since it
	 * will assume that the shutdown of the JVM has been started.
	 */
	void unregisterPlatform();

	/**
	 * Returns the ID of the server for this method. Needed for the data objects so they can be
	 * persisted properly.
	 * 
	 * @param methodId
	 *            The method ID used locally.
	 * @return The ID used at the server.
	 * @throws IdNotAvailableException
	 *             This exception is thrown if no ID can be retrieved from this manager.
	 */
	long getRegisteredMethodId(long methodId) throws IdNotAvailableException;

	/**
	 * Returns the ID of the server for this sensor type. Needed for the data objects so they can be
	 * persisted properly.
	 * 
	 * @param sensorTypeId
	 *            The sensor type ID used locally.
	 * @return The ID used at the server.
	 * @throws IdNotAvailableException
	 *             This exception is thrown if no ID can be retrieved from this manager.
	 */
	long getRegisteredSensorTypeId(long sensorTypeId) throws IdNotAvailableException;

	/**
	 * Registers a method and returns a unique id for it.
	 * 
	 * @param registeredSensorConfig
	 *            The method sensor configuration which contains all the needed information for
	 *            registering.
	 * @return The unique method id.
	 */
	long registerMethod(RegisteredSensorConfig registeredSensorConfig);

	/**
	 * Registers a method sensor type and returns a unique id for it.
	 * 
	 * @param methodSensorTypeConfig
	 *            The method sensor type configuration which contains all the needed information for
	 *            registering.
	 * @return The unique method sensor type id.
	 */
	long registerMethodSensorType(MethodSensorTypeConfig methodSensorTypeConfig);

	/**
	 * Adds a sensor type to a method.
	 * 
	 * @param sensorTypeId
	 *            The id of the sensor type.
	 * @param methodId
	 *            The id of the method.
	 */
	void addSensorTypeToMethod(long sensorTypeId, long methodId);

	/**
	 * Registers a platform sensor type and returns a unique id for it.
	 * 
	 * @param platformSensorTypeConfig
	 *            The platform sensor type configuration which contains all the needed information
	 *            for registering.
	 * @return The unique method sensor type id.
	 */
	long registerPlatformSensorType(PlatformSensorTypeConfig platformSensorTypeConfig);

}
