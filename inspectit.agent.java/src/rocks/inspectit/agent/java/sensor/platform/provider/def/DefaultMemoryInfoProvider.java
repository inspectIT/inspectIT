package info.novatec.inspectit.agent.sensor.platform.provider.def;

import info.novatec.inspectit.agent.sensor.platform.provider.MemoryInfoProvider;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

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
	public MemoryUsage getHeapMemoryUsage() {
		return memoryBean.getHeapMemoryUsage();
	}

	/**
	 * {@inheritDoc}
	 */
	public MemoryUsage getNonHeapMemoryUsage() {
		return memoryBean.getNonHeapMemoryUsage();
	}

}
