package info.novatec.inspectit.agent.sensor.platform.provider.sun;

import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.util.CpuUsageCalculator;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.sun.management.OperatingSystemMXBean;

/**
 * This class retrieves all the data as {@link OperatingSystemInfoProvider} from
 * {@link OperatingSystemMXBean} from Sun.
 * 
 * @see com.sun.management.OperatingSystemMXBean
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class SunOperatingSystemInfoProvider implements OperatingSystemInfoProvider {

	/**
	 * The managed bean to retrieve the OS information from.
	 */
	private OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	/**
	 * The managed bean to retrieve information about the uptime of the JVM.
	 */
	private RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

	/**
	 * The calculator used to calculate and retrieve the current CPU usage of the underlying JVM.
	 */
	private CpuUsageCalculator cpuCalculator = new CpuUsageCalculator();

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return osBean.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getArch() {
		return osBean.getArch();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return osBean.getVersion();
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
		return osBean.getCommittedVirtualMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreePhysicalMemorySize() {
		return osBean.getFreePhysicalMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreeSwapSpaceSize() {
		return osBean.getFreeSwapSpaceSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getProcessCpuTime() {
		return osBean.getProcessCpuTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalPhysicalMemorySize() {
		return osBean.getTotalPhysicalMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalSwapSpaceSize() {
		return osBean.getTotalSwapSpaceSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public float retrieveCpuUsage() {
		cpuCalculator.setUptime(runtimeBean.getUptime());
		cpuCalculator.setProcessCpuTime(this.getProcessCpuTime());
		cpuCalculator.setAvailableProcessors(this.getAvailableProcessors());
		cpuCalculator.updateCpuUsage();

		return cpuCalculator.getCpuUsage();
	}
}
