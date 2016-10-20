package rocks.inspectit.agent.java.sensor.platform.provider.ibm;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.PlatformSensorInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.ThreadInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.def.DefaultMemoryInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.def.DefaultRuntimeInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.def.DefaultThreadInfoProvider;

/**
 * {@link PlatformSensorInfoProvider} for IBM Java virtual machine. Only for Java version 1.6+.
 *
 * @author Ivan Senic
 *
 */
public class IbmJava6PlatformSensorInfoProvider implements PlatformSensorInfoProvider {

	/**
	 * {@link MemoryInfoProvider}.
	 */
	private static final MemoryInfoProvider MEMORY_INFO_PROVIDER = new DefaultMemoryInfoProvider();

	/**
	 * {@link OperatingSystemInfoProvider}.
	 */
	private static OperatingSystemInfoProvider operatingSystemInfoProvider;

	/**
	 * {@link RuntimeInfoProvider}.
	 */
	private static final RuntimeInfoProvider RUNTIME_INFO_PROVIDER = new DefaultRuntimeInfoProvider();

	/**
	 * {@link ThreadInfoProvider}.
	 */
	private static final ThreadInfoProvider THREAD_INFO_PROVIDER = new DefaultThreadInfoProvider();

	/**
	 * Default constructor.
	 *
	 * @throws Exception
	 *             If the SystemInformationProvider could not be created.
	 */
	public IbmJava6PlatformSensorInfoProvider() throws Exception {
		if (null == operatingSystemInfoProvider) {
			createOperatingSystemInfoProvider();
		}
	}

	/**
	 * Creates the OperatingSystemInfoProvider for IBM Java6.
	 *
	 * @throws Exception
	 *             on error.
	 */
	private static synchronized void createOperatingSystemInfoProvider() throws Exception {
		if (null == operatingSystemInfoProvider) {
			operatingSystemInfoProvider = new IbmJava6OperatingSystemInfoProvider();
		}
	}

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
		return operatingSystemInfoProvider;
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
