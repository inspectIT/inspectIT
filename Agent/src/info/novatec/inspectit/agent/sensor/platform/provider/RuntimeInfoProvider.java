package info.novatec.inspectit.agent.sensor.platform.provider;

/**
 * The management interface for the runtime system of the Java virtual machine.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface RuntimeInfoProvider {

	/**
	 * Returns the name of the Just-in-time (JIT) compiler.
	 * 
	 * @return the name of the JIT compiler.
	 */
	String getJitCompilerName();

	/**
	 * Returns the approximate accumlated elapsed time (in milliseconds) spent in compilation. If
	 * multiple threads are used for compilation, this value is summation of the approximate time
	 * that each thread spent in compilation.
	 * 
	 * <p>
	 * This method is optionally supported by the platform. A Java virtual machine implementation
	 * may not support the compilation time monitoring. The
	 * {@link #isCompilationTimeMonitoringSupported} method can be used to determine if the Java
	 * virtual machine supports this operation.
	 * 
	 * <p>
	 * This value does not indicate the level of performance of the Java virtual machine and is not
	 * intended for performance comparisons of different virtual machine implementations. The
	 * implementations may have different definitions and different measurements of the compilation
	 * time.
	 * 
	 * @return Compilation time in milliseconds
	 */
	long getTotalCompilationTime();

	/**
	 * Returns the total number of classes that have been loaded since the Java virtual machine has
	 * started execution.
	 * 
	 * @return the total number of classes loaded.
	 * 
	 */
	long getTotalLoadedClassCount();

	/**
	 * Returns the number of classes that are currently loaded in the Java virtual machine.
	 * 
	 * @return the number of currently loaded classes.
	 */
	int getLoadedClassCount();

	/**
	 * Returns the total number of classes unloaded since the Java virtual machine has started
	 * execution.
	 * 
	 * @return the total number of unloaded classes.
	 */
	long getUnloadedClassCount();

	/**
	 * Returns the Java virtual machine implementation name. This method is equivalent to
	 * {@link System#getProperty System.getProperty("java.vm.name")}.
	 * 
	 * @return the Java virtual machine implementation name.
	 */
	String getVmName();

	/**
	 * Returns the Java virtual machine implementation vendor. This method is equivalent to
	 * {@link System#getProperty System.getProperty("java.vm.vendor")}.
	 * 
	 * @return the Java virtual machine implementation vendor.
	 */
	String getVmVendor();

	/**
	 * Returns the Java virtual machine implementation version. This method is equivalent to
	 * {@link System#getProperty System.getProperty("java.vm.version")}.
	 * 
	 * @return the Java virtual machine implementation version.
	 */
	String getVmVersion();

	/**
	 * Returns the Java virtual machine specification name. This method is equivalent to
	 * {@link System#getProperty System.getProperty("java.vm.specification.name")}.
	 * 
	 * @return the Java virtual machine specification name.
	 */
	String getSpecName();

	/**
	 * Returns the Java class path that is used by the system class loader to search for class
	 * files. This method is equivalent to {@link System#getProperty
	 * System.getProperty("java.class.path")}.
	 * 
	 * <p>
	 * Multiple paths in the Java class path are separated by the path separator character of the
	 * platform of the Java virtual machine being monitored.
	 * 
	 * @return the Java class path.
	 */
	String getClassPath();

	/**
	 * Returns the Java library path. This method is equivalent to {@link System#getProperty
	 * System.getProperty("java.library.path")}.
	 * 
	 * <p>
	 * Multiple paths in the Java library path are separated by the path separator character of the
	 * platform of the Java virtual machine being monitored.
	 * 
	 * @return the Java library path.
	 */
	String getLibraryPath();

	/**
	 * Returns the boot class path that is used by the bootstrap class loader to search for class
	 * files.
	 * 
	 * <p>
	 * Multiple paths in the boot class path are separated by the path separator character of the
	 * platform on which the Java virtual machine is running.
	 * 
	 * <p>
	 * A Java virtual machine implementation may not support the boot class path mechanism for the
	 * bootstrap class loader to search for class files. The {@link #isBootClassPathSupported}
	 * method can be used to determine if the Java virtual machine supports this method.
	 * 
	 * @return the boot class path.
	 */
	String getBootClassPath();

	/**
	 * Returns the uptime of the Java virtual machine in milliseconds.
	 * 
	 * @return uptime of the Java virtual machine in milliseconds.
	 */
	long getUptime();

}
