package info.novatec.inspectit.agent.sensor.platform.provider.def;

import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;

import java.lang.management.ManagementFactory;

/**
 * Uses the {@link java.lang.management.OperatingSystemMXBean} in order to retrieve a subpart of the
 * needed information. Only the methods {@link #getArch()}, {@link #getAvailableProcessors()},
 * {@link #getName()}, {@link #getVersion()}, and {@link #getSystemLoadAverage()} provide data. All
 * other methods in this class return -1L values.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class DefaultOperatingSystemInfoProvider implements OperatingSystemInfoProvider {

	/**
	 * The operating system bean used to retrieve information about the underlying operating system.
	 */
	private java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		try {
			return osBean.getName();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getArch() {
		try {
			return osBean.getArch();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		try {
			return osBean.getVersion();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getAvailableProcessors() {
		return osBean.getAvailableProcessors();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getCommittedVirtualMemorySize() {
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreePhysicalMemorySize() {
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreeSwapSpaceSize() {
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getProcessCpuTime() {
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalPhysicalMemorySize() {
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalSwapSpaceSize() {
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 */
	public float retrieveCpuUsage() {
		return -1.0f;
	}
}
