package info.novatec.inspectit.agent.sensor.platform.provider;

/**
 * The management interface for the thread system of the Java virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface ThreadInfoProvider {

	/**
	 * Returns the current number of live threads including both daemon and non-daemon threads.
	 * 
	 * @return the current number of live threads.
	 */
	int getThreadCount();

	/**
	 * Returns the peak live thread count since the Java virtual machine started or peak was reset.
	 * 
	 * @return the peak live thread count.
	 */
	int getPeakThreadCount();

	/**
	 * Returns the total number of threads created and also started since the Java virtual machine
	 * started.
	 * 
	 * @return the total number of threads started.
	 */
	long getTotalStartedThreadCount();

	/**
	 * Returns the current number of live daemon threads.
	 * 
	 * @return the current number of live daemon threads.
	 */
	int getDaemonThreadCount();

}
