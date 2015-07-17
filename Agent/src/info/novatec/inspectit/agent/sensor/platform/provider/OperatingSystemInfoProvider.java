package info.novatec.inspectit.agent.sensor.platform.provider;

/**
 * The management interface for the underlying operating system on which the Java virtual machine is
 * executed.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface OperatingSystemInfoProvider {

	/**
	 * @return The name of the underlying operating system.
	 */
	String getName();

	/**
	 * @return The operating system architecture.
	 */
	String getArch();

	/**
	 * @return The operating system version.
	 */
	String getVersion();

	/**
	 * @return The number of processors available to the Java virtual machine.
	 */
	int getAvailableProcessors();

	/**
	 * @return The amount of virtual memory that is guaranteed to be available to the running
	 *         process in bytes, or -1 if this operation is not supported.
	 */
	long getCommittedVirtualMemorySize();

	/**
	 * @return The amount of free physical memory in bytes.
	 */
	long getFreePhysicalMemorySize();

	/**
	 * @return The amount of free swap space in bytes.
	 */
	long getFreeSwapSpaceSize();

	/**
	 * @return The CPU time used by the process on which the Java virtual machine is running in
	 *         nanoseconds. The returned value is of nanoseconds precision but not necessarily
	 *         nanoseconds accuracy. This method returns -1 if the the platform does not support
	 *         this operation.
	 */
	long getProcessCpuTime();

	/**
	 * @return The total amount of physical memory in bytes.
	 */
	long getTotalPhysicalMemorySize();

	/**
	 * @return The total amount of swap space in bytes.
	 */
	long getTotalSwapSpaceSize();

	/**
	 * @return The current cpu usage for the JVM process. This value is a float in the [0.0,99.0]
	 *         interval. A value of 0.0 means that none of the CPUs were running threads from the
	 *         JVM process during the recent period of time observed, while a value of 99.0 means
	 *         that all CPUs were actively running threads from the JVM 99% of the time during the
	 *         recent period being observed.
	 */
	float retrieveCpuUsage();
}
