package info.novatec.inspectit.agent.sensor.platform.provider.util;

/**
 * This class is used to calculate the cpu usage of the underlying Virtual Machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class CpuUsageCalculator {

	/**
	 * The uptime.
	 */
	private long uptime = -1L;

	/**
	 * The process cpu time.
	 */
	private long processCpuTime = -1L;

	/**
	 * The available processors.
	 */
	private int availableProcessors = 0;

	/**
	 * The previous uptime of the Virtual Machine.
	 */
	private long prevUptime = 0L;

	/**
	 * The previous processCpuTime.
	 */
	private long prevProcessCpuTime = 0L;

	/**
	 * The cpu usage.
	 */
	private float cpuUsage = 0.0F;

	/**
	 * Gets {@link #cpuUsage}.
	 * 
	 * @return {@link #cpuUsage}
	 */
	public float getCpuUsage() {
		return cpuUsage;
	}

	/**
	 * Sets {@link #uptime}.
	 * 
	 * @param uptime
	 *            New value for {@link #uptime}
	 */
	public void setUptime(long uptime) {
		this.uptime = uptime;
	}

	/**
	 * Sets {@link #processCpuTime}.
	 * 
	 * @param processCpuTime
	 *            New value for {@link #processCpuTime}
	 */
	public void setProcessCpuTime(long processCpuTime) {
		this.processCpuTime = processCpuTime;
	}

	/**
	 * Sets {@link #availableProcessors}.
	 * 
	 * @param availableProcessors
	 *            New value for {@link #availableProcessors}
	 */
	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	/**
	 * Calculates the current cpuUsage in percent.
	 * 
	 * elapsedCpu is in ns and elapsedTime is in ms. cpuUsage could go higher than 100% because
	 * elapsedTime and elapsedCpu are not fetched simultaneously. Limit to 99% to avoid showing a
	 * scale from 0% to 200%.
	 * 
	 */
	public void updateCpuUsage() {
		if (prevUptime > 0L && this.uptime > prevUptime) {
			long elapsedCpu = this.processCpuTime - prevProcessCpuTime;
			long elapsedTime = this.uptime - prevUptime;

			cpuUsage = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * this.availableProcessors));
		}
		this.prevUptime = this.uptime;
		this.prevProcessCpuTime = this.processCpuTime;
	}
}