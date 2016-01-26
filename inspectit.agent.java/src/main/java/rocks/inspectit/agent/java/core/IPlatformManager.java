package rocks.inspectit.agent.java.core;

/**
 * The platform Manager is used to register and un-register the platform.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 *
 */
public interface IPlatformManager {

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
	 * this method {@link IPlatformManager} will omit any further calls to register the platform since it
	 * will assume that the shutdown of the JVM has been started.
	 */
	void unregisterPlatform();

}
