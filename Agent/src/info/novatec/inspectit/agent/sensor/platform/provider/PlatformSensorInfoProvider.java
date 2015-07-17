package info.novatec.inspectit.agent.sensor.platform.provider;

/**
 * Interface for a provider platform sensor infos.
 * 
 * @author Ivan Senic
 * 
 */
public interface PlatformSensorInfoProvider {

	/**
	 * Returns the {@link MemoryInfoProvider}.
	 * 
	 * @return Returns the {@link MemoryInfoProvider}.
	 */
	MemoryInfoProvider getMemoryInfoProvider();

	/**
	 * Returns the {@link OperatingSystemInfoProvider}.
	 * 
	 * @return Returns the {@link OperatingSystemInfoProvider}.
	 */
	OperatingSystemInfoProvider getOperatingSystemInfoProvider();

	/**
	 * Returns the {@link RuntimeInfoProvider}.
	 * 
	 * @return Returns the {@link RuntimeInfoProvider}.
	 */
	RuntimeInfoProvider getRuntimeInfoProvider();

	/**
	 * Returns the {@link ThreadInfoProvider}.
	 * 
	 * @return Returns the {@link ThreadInfoProvider}.
	 */
	ThreadInfoProvider getThreadInfoProvider();

}
