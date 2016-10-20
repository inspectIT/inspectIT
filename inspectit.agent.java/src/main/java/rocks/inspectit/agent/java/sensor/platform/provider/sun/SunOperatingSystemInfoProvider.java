package rocks.inspectit.agent.java.sensor.platform.provider.sun;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.sun.management.OperatingSystemMXBean;

import rocks.inspectit.agent.java.sensor.platform.provider.OperatingSystemInfoProvider;
import rocks.inspectit.agent.java.sensor.platform.provider.util.CpuUsageCalculator;

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
	@Override
	public String getName() {
		return osBean.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getArch() {
		return osBean.getArch();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVersion() {
		return osBean.getVersion();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAvailableProcessors() {
		return osBean.getAvailableProcessors();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCommittedVirtualMemorySize() {
		return osBean.getCommittedVirtualMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getFreePhysicalMemorySize() {
		return osBean.getFreePhysicalMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getFreeSwapSpaceSize() {
		return osBean.getFreeSwapSpaceSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getProcessCpuTime() {
		return osBean.getProcessCpuTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTotalPhysicalMemorySize() {
		return osBean.getTotalPhysicalMemorySize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTotalSwapSpaceSize() {
		return osBean.getTotalSwapSpaceSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float retrieveCpuUsage() {
		cpuCalculator.setUptime(runtimeBean.getUptime());
		cpuCalculator.setProcessCpuTime(this.getProcessCpuTime());
		cpuCalculator.setAvailableProcessors(this.getAvailableProcessors());
		cpuCalculator.updateCpuUsage();

		return cpuCalculator.getCpuUsage();
	}
}
