package info.novatec.inspectit.version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides the inspectIT version. Uses a {@link VersionProvider} to lookup the version. Version
 * lookup is cached over calls.
 *
 * @author Stefan Siegl
 */
@Service
public class VersionService {

	/**
	 * Unknown version.
	 */
	public static final String UNKNOWN_VERSION = "unknown";

	/**
	 * Reads the version.
	 */
	@Autowired
	private VersionProvider provider;

	/**
	 * Caches the version.
	 */
	private Version cachedVersion;

	/**
	 * Retrieves the version information of the current release.
	 *
	 * @return the current version of this release
	 * @throws InvalidVersionException
	 *             If the version is invalid or cannot be read.
	 */
	public Version getVersion() throws InvalidVersionException {
		if (null == cachedVersion) {
			String readVersion = provider.readVersion();
			cachedVersion = Version.verifyAndCreate(readVersion);
		}
		return cachedVersion;
	}

	/**
	 * Returns the current version as string representation (calling
	 * <code> Version.toString() </code>). If the version cannot be found the String "unknown" will
	 * be returned. Use this method in favor of the <code>getVersion()</code> method if you just
	 * want to display the current version.
	 *
	 * @return the current version as string representation. If the version cannot be found the
	 *         String "unknown" will be returned.
	 */
	public String getVersionAsString() {
		try {
			Version version = getVersion();
			return version.toString();
		} catch (InvalidVersionException e) {
			return UNKNOWN_VERSION;
		}
	}

	/**
	 * Setter for Spring injection.
	 *
	 * @param provider
	 *            provider.
	 */
	public void setProvider(VersionProvider provider) {
		this.provider = provider;
	}
}
