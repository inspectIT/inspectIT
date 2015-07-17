package info.novatec.inspectit.agent.sensor.platform.provider.factory;

import info.novatec.inspectit.agent.sensor.platform.provider.PlatformSensorInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.def.DefaultPlatformSensorInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.ibm.IbmJava6PlatformSensorInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.sun.SunPlatformSensorInfoProvider;
import info.novatec.inspectit.util.UnderlyingSystemInfo;
import info.novatec.inspectit.util.UnderlyingSystemInfo.JavaVersion;
import info.novatec.inspectit.util.UnderlyingSystemInfo.JvmProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class decides which {@link PlatformSensorInfoProvider} will be used.
 * 
 * @author Ivan Senic
 * 
 */
public final class PlatformSensorInfoProviderFactory {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PlatformSensorInfoProviderFactory.class);

	/**
	 * {@link PlatformSensorInfoProvider} when the agent is running on the Sun JVM.
	 */
	private static volatile PlatformSensorInfoProvider platformSensorInfoProvider;

	/**
	 * Because the factory should always return same implementation of the
	 * {@link PlatformSensorInfoProvider} for one virtual machine, we can initialize the correct one
	 * in the static class initialization, and then simply always return the one we have created.
	 */
	static {
		if (UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.SUN || UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.ORACLE) {
			if (null == platformSensorInfoProvider) {
				createSunPlatformSensorInfoProvider();
			}
		} else if (UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.IBM && (UnderlyingSystemInfo.JAVA_VERSION == JavaVersion.JAVA_1_6 || UnderlyingSystemInfo.JAVA_VERSION == JavaVersion.JAVA_1_7)) {
			if (null == platformSensorInfoProvider) {
				try {
					createIbmJava6PlatformSensorInfoProvider();
				} catch (Exception e) {
					LOG.warn("Creation of the Platform Sensor Info Provider for IBM virtual machine failed.", e);
				}
			}
		}

		// if nothing is returned, the go for the default
		if (null == platformSensorInfoProvider) {
			createDefaultPlatformSensorInfoProvider();
		}
	}

	/**
	 * Private constructor.
	 */
	private PlatformSensorInfoProviderFactory() {
	}

	/**
	 * Returns the correct {@link PlatformSensorInfoProvider} based on the JVM vendor.
	 * 
	 * @return {@link PlatformSensorInfoProvider}
	 * @see UnderlyingSystemInfo
	 */
	public static PlatformSensorInfoProvider getPlatformSensorInfoProvider() {
		return platformSensorInfoProvider;
	}

	/**
	 * Creates the {@link IbmJava6PlatformSensorInfoProvider}.
	 * 
	 * @throws Exception
	 *             If the IBM platform sensor creation throws an exception.
	 */
	private static synchronized void createIbmJava6PlatformSensorInfoProvider() throws Exception {
		if (null == platformSensorInfoProvider) {
			platformSensorInfoProvider = new IbmJava6PlatformSensorInfoProvider();
		}
	}

	/**
	 * Creates the {@link SunPlatformSensorInfoProvider}.
	 */
	private static synchronized void createSunPlatformSensorInfoProvider() {
		if (null == platformSensorInfoProvider) {
			platformSensorInfoProvider = new SunPlatformSensorInfoProvider();
		}
	}

	/**
	 * Creates the {@link DefaultPlatformSensorInfoProvider}.
	 */
	private static synchronized void createDefaultPlatformSensorInfoProvider() {
		if (null == platformSensorInfoProvider) {
			platformSensorInfoProvider = new DefaultPlatformSensorInfoProvider();
		}
	}
}
