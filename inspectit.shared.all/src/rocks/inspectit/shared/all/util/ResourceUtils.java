package info.novatec.inspectit.util;

import java.io.InputStream;

import org.springframework.stereotype.Component;

/**
 * Small utility to read the content of a file using classloaders.
 *
 * @author Stefan Siegl
 */
@Component
public class ResourceUtils {

	/**
	 * Reads the content of the file given as resource name.
	 *
	 * @param resource
	 *            the name of the resource.
	 * @return the content of the file or <code>null</code> if the file is not found.
	 */
	public InputStream getAsStream(String resource) {
		ClassLoader classLoader = ResourceUtils.class.getClassLoader();
		if (null == classLoader) {
			// this means inspectIT was started using the XBootclasspath option and thus all classes
			// are in fact loaded by the system classloader, so we need to use the system
			// classloader
			classLoader = ClassLoader.getSystemClassLoader();
		}

		return classLoader.getResourceAsStream(resource);
	}
}
