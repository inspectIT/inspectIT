package rocks.inspectit.shared.all.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.exception.enumeration.VersioningErrorCodeEnum;
import rocks.inspectit.shared.all.util.ResourceUtils;

/**
 * Provides the version by reading a version file that is automatically generated by the release
 * build.
 *
 * @author Stefan Siegl
 */
@Component
public class FileBasedVersionReader implements VersionProvider {

	/**
	 * The name of the file containing the version information.
	 */
	public static final String VERSION_LOG_NAME = "version.log";

	/**
	 * Utility to retrieve the stream of a file.
	 */
	@Autowired
	private ResourceUtils resourceUtils;

	/**
	 * {@inheritDoc}
	 */
	public String readVersion() throws InvalidVersionException {
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = resourceUtils.getAsStream(VERSION_LOG_NAME);

			if (null == inputStream) {
				throw new InvalidVersionException(VersioningErrorCodeEnum.VERSION_FILE_IO, null);
			}

			reader = new BufferedReader(new InputStreamReader(inputStream));
			try {
				String version = reader.readLine();
				if (StringUtils.isEmpty(version)) {
					throw new InvalidVersionException(VersioningErrorCodeEnum.VERSION_NOT_IN_FILE, null);
				}
				return version;
			} catch (IOException e) {
				throw new InvalidVersionException(VersioningErrorCodeEnum.VERSION_FILE_IO, e);
			}
		} finally {
			try {
				if (null != reader) {
					reader.close();
					inputStream.close();
				}
			} catch (IOException e) { // NOPMD NOCHK
				// ignore
			}
		}
	}

	/**
	 * Setter. Necessary for injection on the user interface.
	 *
	 * @param resourceUtils
	 *            the resource utils.
	 */
	public void setResourceUtils(ResourceUtils resourceUtils) {
		this.resourceUtils = resourceUtils;
	}
}
