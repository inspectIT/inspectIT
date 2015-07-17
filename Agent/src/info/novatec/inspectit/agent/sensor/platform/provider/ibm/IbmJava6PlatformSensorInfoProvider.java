package info.novatec.inspectit.agent.sensor.platform.provider.ibm;

import info.novatec.inspectit.agent.sensor.platform.provider.MemoryInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.PlatformSensorInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.RuntimeInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.ThreadInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.def.DefaultMemoryInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.def.DefaultRuntimeInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.def.DefaultThreadInfoProvider;

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
	public MemoryInfoProvider getMemoryInfoProvider() {
		return MEMORY_INFO_PROVIDER;
	}

	/**
	 * {@inheritDoc}
	 */
	public OperatingSystemInfoProvider getOperatingSystemInfoProvider() {
		return operatingSystemInfoProvider;
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
