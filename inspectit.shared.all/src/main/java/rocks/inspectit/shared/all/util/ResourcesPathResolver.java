package rocks.inspectit.shared.all.util;

import java.io.File;
import java.io.IOException;

/**
 * This class helps in finding resources during runtime.
 *
 * @see ResourcesPathResolver#getResourcePath(String)
 * @author Ivan Senic
 *
 */
public final class ResourcesPathResolver {

	/**
	 * Folder path to the resources.
	 */
	public static final String RESOURCES = "src" + File.separator + "main" + File.separator + "resources";

	/**
	 * Folder path to the external resources.
	 */
	public static final String EXT_RESOURCES = "src" + File.separator + "main" + File.separator + "external-resources";

	/**
	 * Private constructor.
	 */
	private ResourcesPathResolver() {
	}

	/**
	 * Find the resource file by looking in to the following locations:
	 * <ul>
	 * <li>working dir (assumed to be "")
	 * <li>src/main/resources
	 * <li>src/main/external-resources
	 * </ul>
	 * If file or directory can not be found in the given locations, exception will be thrown.
	 *
	 * @param resource
	 *            File or directory to locate.
	 * @return existing {@link File}
	 * @throws IOException
	 *             If resource can not be found.
	 */
	public static File getResourceFile(String resource) throws IOException {
		return getResourceFile(resource, new File(""));
	}

	/**
	 * Find the resource file by looking in to the following locations:
	 * <ul>
	 * <li>given working dir
	 * <li>src/main/resources
	 * <li>src/main/external-resources
	 * </ul>
	 * If file or directory can not be found in the given locations, exception will be thrown.
	 * <p>
	 * This method provide possibility to define workingDir
	 *
	 * @param resource
	 *            File or directory to locate.
	 * @param workingDir
	 *            Working dir to check.
	 * @return existing {@link File}
	 * @throws IOException
	 *             If resource can not be found.
	 */
	public static File getResourceFile(String resource, File workingDir) throws IOException {
		// by default search in the working directory
		File workingDirResourceFile = new File(workingDir.getAbsolutePath(), resource);
		if (workingDirResourceFile.exists()) {
			return workingDirResourceFile;
		}

		// if it does not exist then in the src/main/resources
		File mainResourcesFile = new File(workingDir.getAbsolutePath(), RESOURCES + File.separator + resource);
		if (mainResourcesFile.exists()) {
			return mainResourcesFile;
		}

		// if it does not exist then in the src/main/external-resources
		File extResourcesFile = new File(workingDir.getAbsolutePath(), EXT_RESOURCES + File.separator + resource);
		if (extResourcesFile.exists()) {
			return extResourcesFile;
		}

		// if there is no such resource throw exception
		throw new IOException("Resource " + resource + " can not be found in the working dir, main resources or external resources.");
	}
}
