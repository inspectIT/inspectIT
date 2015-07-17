package info.novatec.inspectit.versioning;

import java.io.IOException;

/**
 * This service provides the current version of inspectIT.
 * 
 * The version is provided by the release process and is stored as a file in the classpath.
 * 
 * @author Stefan Siegl
 */
public interface IVersioningService {

	/**
	 * Retrieves the version information of the current release.
	 * 
	 * @return the current version of this release
	 * @throws IOException
	 *             In case the file could not be found or cannot be read
	 */
	String getVersion() throws IOException;
}
