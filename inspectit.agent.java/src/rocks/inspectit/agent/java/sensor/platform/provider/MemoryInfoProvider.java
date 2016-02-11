package info.novatec.inspectit.agent.sensor.platform.provider;

import java.lang.management.MemoryUsage;

/**
 * The management interface for the memory system of the Java virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface MemoryInfoProvider {

	/**
	 * Returns the current memory usage of the heap that is used for object allocation. The heap
	 * consists of one or more memory pools. The <tt>used</tt> and <tt>committed</tt> size of the
	 * returned memory usage is the sum of those values of all heap memory pools whereas the
	 * <tt>init</tt> and <tt>max</tt> size of the returned memory usage represents the setting of
	 * the heap memory which may not be the sum of those of all heap memory pools.
	 * <p>
	 * The amount of used memory in the returned memory usage is the amount of memory occupied by
	 * both live objects and garbage objects that have not been collected, if any.
	 * 
	 * @return a {@link MemoryUsage} object representing the heap memory usage.
	 */
	MemoryUsage getHeapMemoryUsage();

	/**
	 * Returns the current memory usage of non-heap memory that is used by the Java virtual machine.
	 * The non-heap memory consists of one or more memory pools. The <tt>used</tt> and
	 * <tt>committed</tt> size of the returned memory usage is the sum of those values of all
	 * non-heap memory pools whereas the <tt>init</tt> and <tt>max</tt> size of the returned memory
	 * usage represents the setting of the non-heap memory which may not be the sum of those of all
	 * non-heap memory pools.
	 * 
	 * @return a {@link MemoryUsage} object representing the non-heap memory usage.
	 */
	MemoryUsage getNonHeapMemoryUsage();
}
