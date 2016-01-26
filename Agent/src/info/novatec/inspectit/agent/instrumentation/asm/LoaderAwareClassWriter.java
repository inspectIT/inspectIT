package info.novatec.inspectit.agent.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;

/**
 * Extension to the {@link ClassWriter} that has a specific class loader to use when resolving
 * common super class is called.
 * <p>
 * This was suggested by asm: http://mail-archive.ow2.org/asm/2008-08/msg00018.html.
 *
 * @author Ivan Senic
 *
 */
public class LoaderAwareClassWriter extends ClassWriter {

	/**
	 * {@link ClassLoader} to use when types need to be resolved.
	 */
	private final ClassLoader classLoader;

	/**
	 * Default constructor.
	 *
	 * @see ClassWriter#ClassWriter(int);
	 * @param flags
	 *            option flags that can be used to modify the default behavior of this class. See
	 *            {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
	 * @param classLoader
	 *            Class loader to be used in order to resolve the common super class. If
	 *            <code>null</code> is passed the default implementation of the {@link ClassWriter}
	 *            will be used (using Class.forName). If class loader is provided then this class
	 *            loader will be used in order to find required types.
	 */
	public LoaderAwareClassWriter(int flags, ClassLoader classLoader) {
		super(flags);
		this.classLoader = classLoader;
	}

	/**
	 * Second constructor.
	 *
	 * @see ClassWriter#ClassWriter(ClassReader, int)
	 * @param classReader
	 *            the {@link ClassReader} used to read the original class. It will be used to copy
	 *            the entire constant pool from the original class and also to copy other fragments
	 *            of original bytecode where applicable.
	 * @param flags
	 *            option flags that can be used to modify the default behavior of this class. See
	 *            {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
	 * @param classLoader
	 *            Class loader to be used in order to resolve the common super class. If
	 *            <code>null</code> is passed the default implementation of the {@link ClassWriter}
	 *            will be used (using Class.forName). If class loader is provided then this class
	 *            loader will be used in order to find required types.
	 */
	public LoaderAwareClassWriter(ClassReader classReader, int flags, ClassLoader classLoader) {
		super(classReader, flags);
		this.classLoader = classLoader;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		// if we don't have a class loader specified, just use super
		if (null == classLoader) {
			return super.getCommonSuperClass(type1, type2);
		}

		// otherwise use same code as in ClassWriter, but with provided class loader
		Class<?> c, d;
		try {
			c = Class.forName(type1.replace('/', '.'), false, classLoader);
			d = Class.forName(type2.replace('/', '.'), false, classLoader);
		} catch (Exception e) {
			throw new RuntimeException(e.toString(), e);
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	};

}
