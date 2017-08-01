package rocks.inspectit.agent.java.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class can be used to dynamically get a reference to a {@link Class} based on a
 * FQN. It will be only tried once to load the desired class.
 *
 * @author Marius Oehler
 *
 */
public class ClassReference {

	/**
	 * Logger of this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ClassReference.class);

	/**
	 * The loaded class.
	 */
	private final Class<?> loadedClass;

	/**
	 * Constructor which also tries to load the class. Loading the class will be only tried once! If
	 * the class could not be loaded in the constructor, this instance will always return
	 * <code>null</code> when calling {@link #get()}.
	 *
	 * @param fqnClassName
	 *            the FQN of the class to load
	 * @param classLoader
	 *            the class loader which should be used for loading the class
	 */
	public ClassReference(String fqnClassName, ClassLoader classLoader) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(fqnClassName, false, classLoader);
		} catch (ClassNotFoundException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Failed loading of '" + fqnClassName + "' class.", e);
			}
		} finally {
			loadedClass = clazz;
		}
	}

	/**
	 * Returns the class specified by the FQN given to the constructor.
	 *
	 * @return The loaded class or <code>null</code> if the class could not be loaded.
	 */
	public Class<?> get() {
		return loadedClass;
	}
}
