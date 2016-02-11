package info.novatec.inspectit.version;

/**
 * Provider for the version string in inspectIT.
 *
 * @author Stefan Siegl
 */
public interface VersionProvider {

	/**
	 * Reads the current version of inspectIT as String.
	 *
	 * @return the current version of inspectIT. Return value may <b>not</b> be null or empty. In
	 *         this case an exception is raised.
	 * @throws InvalidVersionException
	 *             in case the version cannot be read.
	 */
	String readVersion() throws InvalidVersionException;
}
