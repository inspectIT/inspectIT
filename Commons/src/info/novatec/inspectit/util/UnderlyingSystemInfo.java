package info.novatec.inspectit.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * Provides information about the System and JVM.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from
 * <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons Lang</a>
 * {@link org.apache.commons.lang.SystemUtils} class. License info can be found
 * <a href="http://www.apache.org/licenses/">here</a>.
 * 
 */
public final class UnderlyingSystemInfo {

	/**
	 * Amount of heap in bytes till the compressed oops are activated by default with 64bit Sun JVM.
	 */
	private static final long MAX_COMPRESSED_OOPS_JAVA7_MEMORY = 34359738368L;

	/**
	 * Enumeration for Java version.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum JavaVersion {

		/**
		 * Denotes Java version 1.1.
		 */
		JAVA_1_1,

		/**
		 * Denotes Java version 1.2.
		 */
		JAVA_1_2,

		/**
		 * Denotes Java version 1.3.
		 */
		JAVA_1_3,

		/**
		 * Denotes Java version 1.4.
		 */
		JAVA_1_4,

		/**
		 * Denotes Java version 1.5.
		 */
		JAVA_1_5,

		/**
		 * Denotes Java version 1.6.
		 */
		JAVA_1_6,

		/**
		 * Denotes Java version 1.7.
		 */
		JAVA_1_7,

		/**
		 * Denotes unknown java version.
		 */
		OTHER;
	}

	/**
	 * Enumeration for Java provider.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum JvmProvider {

		/**
		 * Denotes JVM from Sun.
		 */
		SUN,

		/**
		 * Denotes JVM from Oracle-Corporation.
		 */
		ORACLE,

		/**
		 * Denotes JVM from IBM.
		 */
		IBM,

		/**
		 * Denotes JVM from other provider.
		 */
		OTHER;
	}

	/**
	 * Java vendor.
	 */
	public static final String JAVA_VENDOR = getSystemProperty("java.vendor");

	/**
	 * Java version.
	 */
	public static final String JAVA_VERSION_FULL = getSystemProperty("java.version");

	/**
	 * Java version trimmed.
	 */
	public static final String JAVA_VERSION_TRIMMED = getJavaVersionTrimmed();

	/**
	 * Java version in {@link JavaVersion} enumeration.
	 */
	public static final JavaVersion JAVA_VERSION = getJavaVersion();

	/**
	 * JVM provider in {@link JvmProvider} enumeration.
	 */
	public static final JvmProvider JVM_PROVIDER = getJvmProvider();

	/**
	 * <p>
	 * Is <code>true</code> if the Java is 64bit. Works only if VM is IBM or Sun.
	 * </p>
	 * 
	 */
	public static final boolean IS_64BIT = is64Bit();

	/**
	 * <p>
	 * Is <code>true</code> if the Java is 64bit and compressed oops are used. Works only if VM is
	 * IBM or Sun.
	 * </p>
	 * 
	 */
	public static final boolean IS_COMPRESSED_OOPS = isCompressedOops();

	/**
	 * Private constructor.
	 */
	private UnderlyingSystemInfo() {
	}

	/**
	 * Returns the JVM provider.
	 * 
	 * @return Returns the JVM provider.
	 */
	private static JvmProvider getJvmProvider() {
		if (getJavaVendorMatches("Sun")) {
			return JvmProvider.SUN;
		} else if (getJavaVendorMatches("Oracle Corporation")) {
			return JvmProvider.ORACLE;
		} else if (getJavaVendorMatches("IBM")) {
			return JvmProvider.IBM;
		} else {
			return JvmProvider.OTHER;
		}
	}

	/**
	 * Returns the Java version.
	 * 
	 * @return Returns the Java version.
	 */
	private static JavaVersion getJavaVersion() {
		if (getJavaVersionMatches("1.1")) {
			return JavaVersion.JAVA_1_1;
		} else if (getJavaVersionMatches("1.2")) {
			return JavaVersion.JAVA_1_2;
		} else if (getJavaVersionMatches("1.3")) {
			return JavaVersion.JAVA_1_3;
		} else if (getJavaVersionMatches("1.4")) {
			return JavaVersion.JAVA_1_4;
		} else if (getJavaVersionMatches("1.5")) {
			return JavaVersion.JAVA_1_5;
		} else if (getJavaVersionMatches("1.6")) {
			return JavaVersion.JAVA_1_6;
		} else if (getJavaVersionMatches("1.7")) {
			return JavaVersion.JAVA_1_7;
		} else {
			return JavaVersion.OTHER;
		}
	}

	/**
	 * Matches the vendor name provided with the vendor of the JVM.
	 * 
	 * @param vendor
	 *            Vendor to check.
	 * @return True if the {@link #JAVA_VENDOR} is not <code>null</code> and vendor name matches.
	 */
	private static boolean getJavaVendorMatches(String vendor) {
		if (null != JAVA_VENDOR) {
			return JAVA_VENDOR.indexOf(vendor) != -1;
		}
		return false;
	}

	/**
	 * Returns if the JVM is a 64bit. Note that this method returns <code>false</code> if the JVM is
	 * not from Sun or IBM.
	 * 
	 * @return Returns if the JVM is a 64bit. Note that this method returns <code>false</code> if
	 *         the JVM is not from Sun or IBM.
	 */
	private static boolean is64Bit() {
		switch (JVM_PROVIDER) {
		case SUN:
			return System.getProperty("sun.arch.data.model").indexOf("64") != -1;
		case ORACLE:
			return System.getProperty("sun.arch.data.model").indexOf("64") != -1;
		case IBM:
			return System.getProperty("com.ibm.vm.bitmode").indexOf("64") != -1;
		default:
			return false;
		}
	}

	/**
	 * Tests if the compressed pointers are used. Note that this method will return true only if the
	 * JVM is 64bit, ignoring the fact that the 32bit JVM can also have
	 * <code>+UseCompressedOops</code> argument. This method has been tested with Sun & IBM virtual
	 * machine, and behavior with different providers is unknown.
	 * <p>
	 * IMPORTANT (SUN & ORACLE): Compressed oops is supported and enabled by default in Java SE 6u23
	 * and later. In Java SE 7, use of compressed oops is the default for 64-bit JVM processes when
	 * -Xmx isn't specified and for values of -Xmx less than 32 gigabytes. For JDK 6 before the 6u23
	 * release, use the -XX:+UseCompressedOops flag with the java command to enable the feature.
	 * <p>
	 * IMPORTANT (IBM): From Java 6 SR 5, 64-bit JVMs recognize the following Oracle JVM options:
	 * -XX:+UseCompressedOops This enables compressed references in 64-bit JVMs. It is identical to
	 * specifying the -Xcompressedrefs option. -XX:-UseCompressedOops This prevents use of
	 * compressed references in 64-bit JVMs.
	 * 
	 * @return True only if JVM is 64bit and compressed oops are used.
	 */
	private static boolean isCompressedOops() {
		if (IS_64BIT) {
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
			if (null != runtimeMXBean) {
				List<String> arguments = runtimeMXBean.getInputArguments();
				for (String argument : arguments) {
					if (argument.indexOf("+UseCompressedOops") != -1 || argument.indexOf("compressedrefs") != -1) {
						return true;
					} else if (argument.indexOf("-UseCompressedOops") != -1) {
						return false;
					}
				}
			}

			switch (getJvmProvider()) {
			case SUN:
			case ORACLE:
				if (getJavaVersion() == JavaVersion.JAVA_1_7) {
					long max = Runtime.getRuntime().maxMemory();
					return max == Long.MAX_VALUE || max < MAX_COMPRESSED_OOPS_JAVA7_MEMORY;
				} else if (getJavaVersion() == JavaVersion.JAVA_1_6) {
					try {
						int subversionIndexStart = JAVA_VERSION_TRIMMED.indexOf('_');
						boolean isAbove6u23 = Integer.parseInt(JAVA_VERSION_TRIMMED.substring(subversionIndexStart + 1)) >= 23;
						return isAbove6u23;
					} catch (NumberFormatException e) {
						break;
					}
				}
				break;

			default:
				break;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Decides if the Java version matches.
	 * </p>
	 * 
	 * @param versionPrefix
	 *            the prefix for the java version
	 * @return true if matches, or false if not or can't determine
	 */
	private static boolean getJavaVersionMatches(String versionPrefix) {
		return isJavaVersionMatch(JAVA_VERSION_TRIMMED, versionPrefix);
	}

	/**
	 * <p>
	 * Decides if the Java version matches.
	 * </p>
	 * <p>
	 * This method is package private instead of private to support unit test invocation.
	 * </p>
	 * 
	 * @param version
	 *            the actual Java version
	 * @param versionPrefix
	 *            the prefix for the expected Java version
	 * @return true if matches, or false if not or can't determine
	 */
	private static boolean isJavaVersionMatch(String version, String versionPrefix) {
		if (version == null) {
			return false;
		}
		return version.startsWith(versionPrefix);
	}

	/**
	 * Trims the text of the java version to start with numbers.
	 * 
	 * @return the trimmed java version
	 */
	private static String getJavaVersionTrimmed() {
		if (JAVA_VERSION_FULL != null) {
			for (int i = 0; i < JAVA_VERSION_FULL.length(); i++) {
				char ch = JAVA_VERSION_FULL.charAt(i);
				if (ch >= '0' && ch <= '9') {
					return JAVA_VERSION_FULL.substring(i);
				}
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Gets a System property, defaulting to <code>null</code> if the property cannot be read.
	 * </p>
	 * 
	 * <p>
	 * If a <code>SecurityException</code> is caught, the return value is <code>null</code> and a
	 * message is written to <code>System.err</code>.
	 * </p>
	 * 
	 * @param property
	 *            the system property name
	 * @return the system property value or <code>null</code> if a security problem occurs
	 */
	private static String getSystemProperty(String property) {
		try {
			return System.getProperty(property);
		} catch (SecurityException ex) {
			return null;
		}
	}
}
