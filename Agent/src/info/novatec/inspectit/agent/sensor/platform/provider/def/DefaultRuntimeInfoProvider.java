package info.novatec.inspectit.agent.sensor.platform.provider.def;

import info.novatec.inspectit.agent.sensor.platform.provider.RuntimeInfoProvider;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Uses the {@link java.lang.management.CompilationMXBean},
 * {@link java.lang.management.ClassLoadingMXBean}, and {@link java.lang.management.RuntimeMXBean}
 * in order to retrieve all of the provided information.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class DefaultRuntimeInfoProvider implements RuntimeInfoProvider {

	/**
	 * The MXBean used to retrieve information from the compilation system.
	 */
	private CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();

	/**
	 * The MXBean used to retrieve information from the class loading system.
	 */
	private ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();

	/**
	 * The MXBean used to retrieve information from the runtime system of the underlying Virtual
	 * Machine.
	 */
	private RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

	/**
	 * {@inheritDoc}
	 */
	public long getTotalCompilationTime() {
		try {
			return compilationBean.getTotalCompilationTime();
		} catch (UnsupportedOperationException e) {
			return -1L;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalLoadedClassCount() {
		return classLoadingBean.getTotalLoadedClassCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLoadedClassCount() {
		return classLoadingBean.getLoadedClassCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getUnloadedClassCount() {
		return classLoadingBean.getUnloadedClassCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJitCompilerName() {
		return compilationBean.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVmName() {
		try {
			return runtimeBean.getVmName();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVmVendor() {
		try {
			return runtimeBean.getVmVendor();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVmVersion() {
		try {
			return runtimeBean.getVmVersion();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSpecName() {
		try {
			return runtimeBean.getSpecName();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getClassPath() {
		try {
			return runtimeBean.getClassPath();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLibraryPath() {
		try {
			return runtimeBean.getLibraryPath();
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBootClassPath() {
		try {
			return runtimeBean.getBootClassPath();
		} catch (UnsupportedOperationException e) {
			return "";
		} catch (SecurityException e) {
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long getUptime() {
		return runtimeBean.getUptime();
	}

}
