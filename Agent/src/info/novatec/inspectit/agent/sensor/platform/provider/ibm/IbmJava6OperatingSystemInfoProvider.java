package info.novatec.inspectit.agent.sensor.platform.provider.ibm;

import info.novatec.inspectit.agent.sensor.platform.provider.def.DefaultOperatingSystemInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.util.CpuUsageCalculator;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide the information about the operating system when the
 * {@value #IBM_OPERATING_SYSTEM_MX_BEAN_CLASS} is available. The information provided by this class
 * is gathered using the reflection.
 * <p>
 * Note that the methods {@link #getFreePhysicalMemorySize()} and {@link #getTotalSwapSpaceSize()}
 * will not be able to provide different values than the default ones, because the IBM does not
 * provide the information about the OS swap size.
 * 
 * @author Ivan Senic
 * 
 */
public class IbmJava6OperatingSystemInfoProvider extends DefaultOperatingSystemInfoProvider {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(IbmJava6OperatingSystemInfoProvider.class);

	/**
	 * FQN name of the IBMs class that provides operating system management.
	 */
	private static final String IBM_OPERATING_SYSTEM_MX_BEAN_CLASS = "com.ibm.lang.management.OperatingSystemMXBeanImpl";

	/**
	 * The managed bean to retrieve information about the uptime of the JVM.
	 */
	private RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

	/**
	 * The calculator used to calculate and retrieve the current CPU usage of the underlying JVM.
	 */
	private CpuUsageCalculator cpuCalculator = new CpuUsageCalculator();

	/**
	 * Method for getting the committed virtual memory size.
	 */
	private Method committedVirtualMemorySizeMethod;

	/**
	 * Method for getting the free physical memory size.
	 */
	private Method freePhysicalMemorySizeMethod;

	/**
	 * Method for getting the process CPU time.
	 */
	private Method processCpuTimeMethod;

	/**
	 * Method for getting the total physical memory size.
	 */
	private Method totalPhysicalMemorySizeMethod;

	/**
	 * Instance to be used with reflection.
	 */
	private Object ibmOperatingSystemMxBeanInstance;

	/**
	 * Default constructor.
	 * 
	 * @throws Exception
	 *             If the initialization fails.
	 */
	public IbmJava6OperatingSystemInfoProvider() throws Exception {
		initAndCheckEnvironment();
	}

	/**
	 * Initializes the class and checks if every method needed to be accessed with reflection is
	 * available and is returning the correct value.
	 * 
	 * @throws Exception
	 *             If any exception occurs during the initialization.
	 */
	private void initAndCheckEnvironment() throws Exception {
		Class<?> ibmOperatingSystemMxBeanClass = null;
		ibmOperatingSystemMxBeanClass = Class.forName(IBM_OPERATING_SYSTEM_MX_BEAN_CLASS);

		if (null != ibmOperatingSystemMxBeanClass) {
			Method getInstanceMethod = ibmOperatingSystemMxBeanClass.getDeclaredMethod("getInstance");
			getInstanceMethod.setAccessible(true);
			if (null != getInstanceMethod) {
				ibmOperatingSystemMxBeanInstance = getInstanceMethod.invoke(null);
			}

			if (null != ibmOperatingSystemMxBeanInstance) {
				// getProcessVirtualMemorySize method
				committedVirtualMemorySizeMethod = getMethod(ibmOperatingSystemMxBeanClass, "getProcessVirtualMemorySize");
				Object result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, committedVirtualMemorySizeMethod);
				if (!(result instanceof Number)) {
					throw new Exception("Result of getProcessVirtualMemorySize() method invocation is not a number. Result was: " + result);
				}

				// getFreePhysicalMemorySize method
				freePhysicalMemorySizeMethod = getMethod(ibmOperatingSystemMxBeanClass, "getFreePhysicalMemorySize");
				result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, freePhysicalMemorySizeMethod);
				if (!(result instanceof Number)) {
					throw new Exception("Result of getFreePhysicalMemorySize() method invocation is not a number. Result was: " + result);
				}

				// getProcessCpuTime method
				processCpuTimeMethod = getMethod(ibmOperatingSystemMxBeanClass, "getProcessCpuTime");
				result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, processCpuTimeMethod);
				if (!(result instanceof Number)) {
					throw new Exception("Result of getProcessCpuTime() method invocation is not a number. Result was: " + result);
				}

				// getTotalPhysicalMemory method
				totalPhysicalMemorySizeMethod = getMethod(ibmOperatingSystemMxBeanClass, "getTotalPhysicalMemory");
				result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, totalPhysicalMemorySizeMethod);
				if (!(result instanceof Number)) {
					throw new Exception("Result of getTotalPhysicalMemory() method invocation is not a number. Result was: " + result);
				}

			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long getCommittedVirtualMemorySize() {
		Number result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, committedVirtualMemorySizeMethod);
		return result.longValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreePhysicalMemorySize() {
		Number result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, freePhysicalMemorySizeMethod);
		return result.longValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getProcessCpuTime() {
		Number result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, processCpuTimeMethod);
		// by IBM Documentation the process cpu time is in 100 ns units
		// since we need ns, we need to multiply the result by 100
		return result.longValue() * 100;

	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalPhysicalMemorySize() {
		Number result = getValueFromMethodInvocation(ibmOperatingSystemMxBeanInstance, totalPhysicalMemorySizeMethod);
		return result.longValue();

	}

	/**
	 * {@inheritDoc}
	 */
	public float retrieveCpuUsage() {
		cpuCalculator.setUptime(runtimeBean.getUptime());
		cpuCalculator.setProcessCpuTime(this.getProcessCpuTime());
		cpuCalculator.setAvailableProcessors(this.getAvailableProcessors());
		cpuCalculator.updateCpuUsage();

		return cpuCalculator.getCpuUsage();
	}

	/**
	 * Loads the wanted method from the class and makes it accessible.
	 * 
	 * @param clazz
	 *            Class
	 * @param methodName
	 *            Method name
	 * @param parameterTypes
	 *            Parameters.
	 * @return Returns method object.
	 * @throws Exception
	 *             If any {@link Exception} occurs during getting the method.
	 */
	private Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws Exception {
		Method m = clazz.getDeclaredMethod(methodName, parameterTypes);
		m.setAccessible(true);
		return m;

	}

	/**
	 * Invokes the given method on the given instance.
	 * 
	 * @param instance
	 *            instance to perform invocation on
	 * @param method
	 *            Method to invoke
	 * @param args
	 *            Arguments
	 * @param <V>
	 *            The result type.
	 * @return Returns method invocation result, or null if method invocation fails for any reason.
	 */
	@SuppressWarnings("unchecked")
	private <V> V getValueFromMethodInvocation(Object instance, Method method, Object... args) {
		try {
			return (V) method.invoke(instance, args);
		} catch (Exception e) {
			LOG.warn("Exception throw during method invocation.", e);
			return null;
		}
	}
}
