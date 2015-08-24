package info.novatec.inspectit.versioning;


/**
 * This service provides the current version of inspectIT.
 * 
 * @author Stefan Siegl
 */
public interface IVersioningService {

	/**
	 * Retrieves the version information of the current release.
	 * 
	 * @return the current version of this release
	 * @throws UnknownVersionException
	 *             If the version cannot be read.
	 */
	String getVersion() throws UnknownVersionException;

	/**
	 * Returns the major version of inspectIT (example: for 1.5.49 this is 1.5, for 1.6.2.49 this is
	 * 1.6).
	 * 
	 * @return the major version of inspectIT
	 * @throws UnknownVersionException
	 *             If the version cannot be read or it does not follow the inspectIT format.
	 */
	String getMajorVersion() throws UnknownVersionException;

	/**
	 * Returns the major version of inspectIT without dot separator (example: for 1.5.49 this is 15,
	 * for 1.6.2.49 this is 16).
	 * 
	 * @return the major version of inspectIT
	 * @throws UnknownVersionException
	 *             If the version cannot be read or it does not follow the inspectIT format.
	 */
	String getMajorVersionNoDots() throws UnknownVersionException;
}
