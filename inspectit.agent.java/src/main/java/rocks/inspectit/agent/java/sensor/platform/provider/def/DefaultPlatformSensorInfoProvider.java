package rocks.inspectit.agent.java.sensor.platform.provider.def;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.PlatformSensorInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.ThreadInfoProvider;

/**
 * Default {@link PlatformSensorInfoProvider}.
 *
 * @author Ivan Senic
 *
 */
public class DefaultPlatformSensorInfoProvider implements PlatformSensorInfoProvider {

	/**
	 * {@link MemoryInfoProvider}.
	 */
	private static final MemoryInfoProvider MEMORY_INFO_PROVIDER = new DefaultMemoryInfoProvider();

	/**
	 * {@link OperatingSystemInfoProvider}.
	 */
	private static final OperatingSystemInfoProvider OPERATING_SYSTEM_INFO_PROVIDER = new DefaultOperatingSystemInfoProvider();

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
	@Override
	public MemoryInfoProvider getMemoryInfoProvider() {
		return MEMORY_INFO_PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperatingSystemInfoProvider getOperatingSystemInfoProvider() {
		return OPERATING_SYSTEM_INFO_PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RuntimeInfoProvider getRuntimeInfoProvider() {
		return RUNTIME_INFO_PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ThreadInfoProvider getThreadInfoProvider() {
		return THREAD_INFO_PROVIDER;
	}

}
