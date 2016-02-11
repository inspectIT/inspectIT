package rocks.inspectit.agent.java.sensor.platform.provider.sun;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.PlatformSensorInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.ThreadInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.def.DefaultMemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.def.DefaultRuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.def.DefaultThreadInfoProvider;

/**
 * Special {@link PlatformSensorInfoProvider} for SunVM..
 * 
 * @author Ivan Senic
 * 
 */
public class SunPlatformSensorInfoProvider implements PlatformSensorInfoProvider {

	/**
	 * {@link MemoryInfoProvider}.
	 */
	private static final MemoryInfoProvider MEMORY_INFO_PROVIDER = new DefaultMemoryInfoProvider();

	/**
	 * {@link OperatingSystemInfoProvider}.
	 */
	private static final OperatingSystemInfoProvider OPERATING_SYSTEM_INFO_PROVIDER = new SunOperatingSystemInfoProvider();

	/**
	 * {@link RuntimeInfoProvider}.
	 */
	private static final RuntimeInfoProvider RUNTIME_INFO_PROVIDER = new DefaultRuntimeInfoProvider();

	/**
	 * {@link ThreadInfoProvider}.
	 */
	private static final ThreadInfoProvider THREAD_INFO_PROVIDER = new DefaultThreadInfoProvider();

	/**
	 * {@inheritDoc}
	 */
	public MemoryInfoProvider getMemoryInfoProvider() {
		return MEMORY_INFO_PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	public OperatingSystemInfoProvider getOperatingSystemInfoProvider() {
		return OPERATING_SYSTEM_INFO_PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	public RuntimeInfoProvider getRuntimeInfoProvider() {
		return RUNTIME_INFO_PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	public ThreadInfoProvider getThreadInfoProvider() {
		return THREAD_INFO_PROVIDER;
	}

}
