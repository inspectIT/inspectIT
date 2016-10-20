package rocks.inspectit.agent.java.sensor.platform.provider.def;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import rocks.inspectit.agent.java.sensor.platform.provider.RuntimeInfoProvider;

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
	@Override
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
	@Override
	public long getTotalLoadedClassCount() {
		return classLoadingBean.getTotalLoadedClassCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLoadedClassCount() {
		return classLoadingBean.getLoadedClassCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getUnloadedClassCount() {
		return classLoadingBean.getUnloadedClassCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getJitCompilerName() {
		return compilationBean.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public long getUptime() {
		return runtimeBean.getUptime();
	}

}
