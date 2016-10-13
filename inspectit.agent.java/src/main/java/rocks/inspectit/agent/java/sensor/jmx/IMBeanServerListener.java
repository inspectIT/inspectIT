package rocks.inspectit.agent.java.sensor.jmx;

import javax.management.MBeanServer;

/**
 * Generic listener for adding/removing of the {@link MBeanServer}s.
 *
 * @author Ivan Senic
 *
 */
public interface IMBeanServerListener {

	/**
	 * Informs the listener that the {@link MBeanServer} has been added.
	 *
	 * @param server
	 *            {@link MBeanServer}
	 */
	void mbeanServerAdded(MBeanServer server);

	/**
	 * Informs the listener that the {@link MBeanServer} has been removed.
	 *
	 * @param server
	 *            {@link MBeanServer}
	 */
	void mbeanServerRemoved(MBeanServer server);

}
