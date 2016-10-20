package rocks.inspectit.agent.java.sensor.platform.provider.def;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import rocks.inspectit.agent.java.sensor.platform.provider.MemoryInfoProvider;

/**
 * Uses the {@link java.lang.management.MemoryMXBean} in order to retrieve all information that are
 * provided here.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class DefaultMemoryInfoProvider implements MemoryInfoProvider {

	/**
	 * The MXBean used to retrieve heap memory information.
	 */
	private MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemoryUsage getHeapMemoryUsage() {
		return memoryBean.getHeapMemoryUsage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemoryUsage getNonHeapMemoryUsage() {
		return memoryBean.getNonHeapMemoryUsage();
	}

}
