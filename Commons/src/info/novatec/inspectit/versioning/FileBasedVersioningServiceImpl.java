package info.novatec.inspectit.versioning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

/**
 * This utility searches for the version file and provides the current version of this release for
 * display.
 * 
 * The version is provided by the release process and is stored as a file in the classpath.
 * 
 * @author Stefan Siegl
 */
@Component
public class FileBasedVersioningServiceImpl implements IVersioningService {

	/**
	 * The name of the file containing the version information.
	 */
	public static final String VERSION_LOG_NAME = "version.log";

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() throws UnknownVersionException {

		// Get a classloader to find the version file
		ClassLoader classLoader = FileBasedVersioningServiceImpl.class.getClassLoader();
		if (null == classLoader) {
			// this means inspectIT was started using the XBootclasspath option and thus all classes
			// are in fact loaded by the system classloader, so we need to use the system
			// classloader
			classLoader = ClassLoader.getSystemClassLoader();
		}

		InputStream s = classLoader.getResourceAsStream(VERSION_LOG_NAME);

		if (null == s) {
			throw new UnknownVersionException("The version information file \"" + VERSION_LOG_NAME + "\" cannot be found in the classpath");
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(s));

			String versionString;
			try {
				versionString = reader.readLine();
			} catch (IOException e) {
				throw new UnknownVersionException("Problem while reading the version file.", e);
			}
			String version = "n/a";
			if (null != versionString && !"".equals(versionString.trim())) {
				version = versionString;
			}

			return version;

		} finally {
			try {
				if (null != reader) {
					reader.close();
					reader = null; // NOPMD
					s.close();
					s = null; // NOPMD
				}
			} catch (IOException e) { // NOPMD NOCHK
				// ignore
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMajorVersion() throws UnknownVersionException {
		String version = getVersion();

		if (null == version) {
			throw new UnknownVersionException("version was null");
		}

		int secondDotSeparator = version.indexOf(".", version.indexOf(".") + 1);
		
		if (-1 == secondDotSeparator) {
			// this is not a valid version, perhaps we are in development
			throw new UnknownVersionException("The version \"" + version + "\" is an invalid version.");
		} else {
			return version.substring(0, secondDotSeparator);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMajorVersionNoDots() throws UnknownVersionException {
		return getMajorVersion().replaceAll("\\.", "");
	}

	// public static void main(String[] args) throws Exception {
	// System.out.println(VersionUtil.getVersion());
	// }
}
