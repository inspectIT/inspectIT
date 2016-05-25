package rocks.inspectit.agent.java.javaagent;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rocks.inspectit.agent.java.Agent;
import rocks.inspectit.agent.java.IAgent;
import rocks.inspectit.agent.java.hooking.IHookDispatcher;

/**
 * The JavaAgent is used since Java 5.0 to instrument classes before they are actually loaded by the
 * VM.
 *
 * This method is used by specifying the -javaagent attribute on the command line. Example:
 * <code>-javaagent:inspectit-agent.jar</code>
 *
 * @author Patrice Bouillet
 */
public class JavaAgent implements ClassFileTransformer {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(JavaAgent.class.getName());

	/**
	 * The reference to the instrumentation class.
	 */
	private static Instrumentation instrumentation;

	/**
	 * Defines if we can instrument core classes.
	 */
	private static boolean instrumentCoreClasses = false;

	/**
	 * Defines the class of our current real agent to use.
	 */
	private static final String INSPECTIT_AGENT = "rocks.inspectit.agent.java.SpringAgent";

	/**
	 * Defines the self first classes which should be loaded by this class loader instead of
	 * delegating the loading to the parent.
	 */
	private static Set<String> selfFirstClasses = new HashSet<String>();

	/**
	 * The premain method will be executed before anything else.
	 *
	 * @param agentArgs
	 *            Some arguments.
	 * @param inst
	 *            The instrumentation instance is used to add a transformer which will do the actual
	 *            instrumentation.
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;

		LOGGER.info("inspectIT Agent: Starting initialization...");
		checkForCorrectSetup();

		// Starting up the real agent
		try {
			// now we load the PicoAgent via our own classloader
			@SuppressWarnings("resource")
			InspectItClassLoader classLoader = new InspectItClassLoader(new URL[0], JavaAgent.class.getClassLoader());
			Class<?> agentClazz = classLoader.loadClass(INSPECTIT_AGENT);
			Constructor<?> constructor = agentClazz.getConstructor(String.class);
			Object realAgent = constructor.newInstance(getInspectItAgentJarFileLocation());

			// we can reference the Agent now here because it should have been added to the
			// bootclasspath and thus available from anywhere in the application
			Agent.agent = (IAgent) realAgent;

			// we need to preload some classes due to the minimal possibility of classcircularity
			// errors etc.
			preloadClasses();

			LOGGER.info("inspectIT Agent: Initialization complete...");

			// now we are analysing the already loaded classes by the jvm to instrument those
			// classes, too
			analyzeAlreadyLoadedClasses();
			inst.addTransformer(new JavaAgent());
		} catch (Exception e) {
			LOGGER.severe("Something unexpected happened while trying to initialize the Agent, aborting!");
			e.printStackTrace(); // NOPMD
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain pd, byte[] data) throws IllegalClassFormatException {
		try {
			if (null != classLoader && InspectItClassLoader.class.getCanonicalName().equals(classLoader.getClass().getCanonicalName())) {
				// return if the classloader to load the class is our own, we don't want to
				// instrument these classes.
				return data;
			}
			// early return if some conditions fail
			if (null == data || data.length == 0 || null == className || "".equals(className)) {
				// - no data = we cannot construct the class and analyze it
				// - no class name = we don't know how the name of the class is and so the whole
				// analysis will fail
				return data;
			}

			// skip analyzing if we cannot instrument core classes.
			if (!instrumentCoreClasses & null == classLoader) {
				return data;
			}

			// now the real inspectit agent will handle this class
			String modifiedClassName = className.replaceAll("/", ".");
			byte[] instrumentedData = Agent.agent.inspectByteCode(data, modifiedClassName, classLoader);
			return instrumentedData;
		} catch (Throwable ex) { // NOPMD
			LOGGER.severe("Error occurred while dealing with class: " + className + " " + ex.getMessage());
			ex.printStackTrace(); // NOPMD
			return null;
		}
	}

	/**
	 * Checks for the correct setup of the jvm parameters or tries to append the inspectit agent to
	 * the bootstrap class loader search automatically (Java 6+ required).
	 */
	private static void checkForCorrectSetup() {
		try {
			// we can utilize the mechanism to add the inspectit-agent to the bootstrap classloader
			// through the instrumentation api.
			Method append = instrumentation.getClass().getDeclaredMethod("appendToBootstrapClassLoaderSearch", JarFile.class);
			append.setAccessible(true);
			append.invoke(instrumentation, new JarFile(getInspectItAgentJarFileLocation()));

			instrumentCoreClasses = true;
		} catch (NoSuchMethodException e) {
			LOGGER.info("inspectIT Agent: Advanced instrumentation capabilities not detected...");
		} catch (SecurityException e) {
			LOGGER.info("inspectIT Agent: Advanced instrumentation capabilities not detected due to security constraints...");
		} catch (Exception e) {
			LOGGER.severe("Something unexpected happened while trying to get advanced instrumentation capabilities!");
			e.printStackTrace(); // NOPMD
		}

		if (!instrumentCoreClasses) {
			// 2. try
			// find out if the bootclasspath option is set
			List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
			for (String arg : inputArgs) {
				if (arg.contains("Xbootclasspath") && arg.contains("inspectit-agent.jar")) {
					instrumentCoreClasses = true;
					LOGGER.info("inspectIT Agent: Xbootclasspath setting found, activating core class instrumentation...");
					break;
				}
			}
		}
	}

	/**
	 * Analyzes all the classes which are already loaded by the jvm. This only works if the
	 * -Xbootclasspath option is being set in addition as we are instrumenting core classes which
	 * are directly connected to the bootstrap classloader.
	 */
	private static void analyzeAlreadyLoadedClasses() {
		try {
			if (instrumentation.isRedefineClassesSupported()) {
				if (instrumentCoreClasses) {
					for (Class<?> loadedClass : instrumentation.getAllLoadedClasses()) {
						String clazzName = loadedClass.getCanonicalName();
						if (null != clazzName && !selfFirstClasses.contains(clazzName)) {
							if (null == loadedClass.getClassLoader() || !InspectItClassLoader.class.getCanonicalName().equals(loadedClass.getClassLoader().getClass().getCanonicalName())) {
								try {
									clazzName = getClassNameForJavassist(loadedClass);
									byte[] modified = Agent.agent.inspectByteCode(null, clazzName, loadedClass.getClassLoader());
									if (null != modified) {
										ClassDefinition classDefinition = new ClassDefinition(loadedClass, modified);
										instrumentation.redefineClasses(new ClassDefinition[] { classDefinition });
									}
								} catch (ClassNotFoundException e) {
									LOGGER.severe(e.getMessage());
								} catch (UnmodifiableClassException e) {
									LOGGER.severe(e.getMessage());
								}
							}
						}
					}
					LOGGER.info("inspectIT Agent: Instrumentation of core classes finished...");
				} else {
					LOGGER.info("inspectIT Agent: Core classes cannot be instrumented, please add -Xbootclasspath/a:<path_to_agent.jar> to the JVM parameters!");
				}
			} else {
				LOGGER.info("Redefinition of Classes is not supported in this JVM!");
			}
		} catch (Throwable t) { // NOPMD
			t.printStackTrace(); // NOPMD
			LOGGER.severe("The process of class redefinitions produced an error: " + t.getMessage());
			LOGGER.severe("If you are running on an IBM JVM, please ignore this error as the JVM does not support this feature!");
			LOGGER.throwing(JavaAgent.class.getCanonicalName(), "analyzeAlreadyLoadedClasses", t);
		}
	}

	/**
	 * Preload some classes to prevent errors in the running application.
	 */
	private static void preloadClasses() {
		LOGGER.info("Preloading classes ...");

		StringIndexOutOfBoundsException.class.getClass();
		LinkedBlockingQueue.class.getClass();

		LOGGER.info("Preloading classes complete...");
	}

	/**
	 * See ClassPool#get(String) why it is needed to replace the '.' with '$' for inner class.
	 *
	 * @param clazz
	 *            The class to get the name from.
	 * @return the name to be passed to javassist.
	 */
	private static String getClassNameForJavassist(Class<?> clazz) {
		String clazzName = clazz.getCanonicalName();
		while (null != clazz.getEnclosingClass()) {
			clazz = clazz.getEnclosingClass();
		}

		if (!clazzName.equals(clazz.getCanonicalName())) {
			String enclosingClasses = clazzName.substring(clazz.getCanonicalName().length());
			enclosingClasses = enclosingClasses.replaceAll("\\.", "\\$");
			clazzName = clazz.getCanonicalName() + enclosingClasses;
		}

		return clazzName;
	}

	/**
	 * Returns the path to the inspectit-agent.jar file.
	 *
	 * @return the path to the jar file.
	 */
	public static String getInspectItAgentJarFileLocation() {
		CodeSource cs = JavaAgent.class.getProtectionDomain().getCodeSource();
		if (null != cs) {
			// no bootstrap definition for the inspectit agent is in place, thus we can use this
			// mechanism
			return cs.getLocation().getFile();
		} else {
			List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
			for (String arg : inputArgs) {
				if (arg.contains("javaagent") && arg.contains("inspectit-agent.jar")) {
					// -javaagent:c:/.../inspectit-agent.jar
					// -javaagent:/home/.../inspectit-agent.jar
					Pattern pattern = Pattern.compile("-javaagent:(.*\\.jar)");
					Matcher matcher = pattern.matcher(arg);
					boolean matches = matcher.matches();
					if (matches) {
						String path = matcher.group(1);
						// for multiple javaagent definitions, this will fail, but we won't include
						// this right now.
						return path;
					} else {
						break;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Self first class loader handling the boundaries of our needed dependency classes and
	 * inspectit classes so we don't mess up with the target.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	public static class InspectItClassLoader extends URLClassLoader {

		/**
		 * We need to ignore some of the self first classes so that they are accessible from this
		 * class (different class loader) and from the SUD.
		 */
		private final Set<String> ignoreClasses = new HashSet<String>();

		/**
		 * Default constructor initialized with the urls of the dependency jars etc. and the parent
		 * classloader.
		 *
		 * @param urls
		 *            the urls to search for the classes for.
		 * @param parent
		 *            the parent class loader.
		 */
		public InspectItClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);

			try {
				String agentFile = getInspectItAgentJarFileLocation();
				if (isJar(agentFile)) {
					addJarResource(new File(agentFile));
				} else {
					LOGGER.severe("There was a problem in retrieving the root jar name!");
					throw new RuntimeException("There was a problem in retrieving the root jar name!");
				}
			} catch (IOException e) {
				LOGGER.severe("There was a problem in extracting needed libs for the inspectIT agent: " + e.getMessage());
				LOGGER.throwing(InspectItClassLoader.class.getCanonicalName(), "InspectItClassLoader", e);
			}

			// ignore IAgent because this is the interface for the SUD to access the real agent
			ignoreClasses.add(IAgent.class.getCanonicalName());
			ignoreClasses.add(Agent.class.getCanonicalName());

			// ignore hook dispatcher because it is defined in the IAgent interface and thus must be
			// available in the standard classloader.
			ignoreClasses.add(IHookDispatcher.class.getCanonicalName());

			// ignore the following classes because they are used in the JavaAgent class
			ignoreClasses.add(JavaAgent.class.getCanonicalName());
			ignoreClasses.add(InspectItClassLoader.class.getCanonicalName());
		}

		/**
		 * Analyze this jar file for containing jar files and classes to be used in our own
		 * classloader.
		 *
		 * @param file
		 *            the file to analyze
		 * @throws IOException
		 *             if something happens on file access.
		 */
		private void addJarResource(File file) throws IOException {
			JarFile jarFile = new JarFile(file);
			addURL(file.toURI().toURL());
			analyzeFile(file);
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (!jarEntry.isDirectory() && isJar(jarEntry.getName())) {
					addJarResource(jarEntryAsFile(jarFile, jarEntry));
				}
			}
		}

		/**
		 * If the file name denotes a jar file.
		 *
		 * @param fileName
		 *            the file name to define if it is a jar file.
		 * @return <b>true</b> if the file name denotes a jar file, <b>false</b> otherwise.
		 */
		private boolean isJar(String fileName) {
			return fileName != null && fileName.toLowerCase().endsWith(".jar");
		}

		/**
		 * The entry in the jar file is a jar file and needs to be extracted into a temporary file
		 * and analyzed/added to our valid classes.
		 *
		 * @param jarFile
		 *            the jar file to get the input stream from.
		 * @param jarEntry
		 *            the specific jar entry which denotes a jar file.
		 * @return the temporary file.
		 * @throws IOException
		 *             if some problems appear while accessing or writing the files.
		 */
		private File jarEntryAsFile(JarFile jarFile, JarEntry jarEntry) throws IOException {
			InputStream input = null;
			OutputStream output = null;
			try {
				String name = jarEntry.getName().replace('/', '_');
				int i = name.lastIndexOf('.');
				String extension = i > -1 ? name.substring(i) : "";
				File file = File.createTempFile(name.substring(0, name.length() - extension.length()) + ".", extension);
				file.deleteOnExit();
				input = jarFile.getInputStream(jarEntry);
				output = new FileOutputStream(file);

				byte[] buffer = new byte[4096];
				int readCount = input.read(buffer);
				while (readCount != -1) {
					output.write(buffer, 0, readCount);
					readCount = input.read(buffer);
				}

				return file;
			} finally {
				close(input);
				close(output);
			}
		}

		/**
		 * Analyzes the (jar) file to get the contained classes for our single first approach.
		 *
		 * @param file
		 *            the file to analyze.
		 * @throws IOException
		 *             if there are problems on accessing the file.
		 */
		private void analyzeFile(File file) throws IOException {
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (jarEntry.getName().endsWith(".class")) {
					selfFirstClasses.add(jarEntry.getName().replaceAll("/", "\\.").replace(".class", ""));
				}
			}
			if (null != jarFile) {
				jarFile.close();
			}
		}

		/**
		 * Convenience method to close the Closeable object and ignore the exception being thrown if
		 * there is one.
		 *
		 * @param closeable
		 *            the Closeable object.
		 */
		private static void close(Closeable closeable) {
			if (closeable != null) {
				try {
					closeable.close();

				} catch (IOException e) { // NOPMD NOCHK
					// don't care about this one
				}
			}
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * We need this method due to the class loading delegation. As we are instrumenting the
		 * {@link ClassLoader#loadClass(String)} with delegation byte code, without this method we
		 * would enter the loop if any our class is loaded via this class loader. Thus, I delegate
		 * this to our {@link #loadClass(String, boolean)} method with <code>false</code> resolve.
		 * Thus, escaping the further delegation checks.
		 */
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return loadClass(name, false);
		}

		/** {@inheritDoc} */
		@Override
		public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			Class<?> result = findLoadedClass(name);
			if (null != result) {
				if (resolve) {
					resolveClass(result);
				}
				return result;
			}

			boolean selfFirst = false;
			if (!ignoreClasses.contains(name)) {
				if (selfFirstClasses.contains(name)) {
					selfFirst = true;
				}
			}

			// Override parent-first behavior into self-first only for specified classes
			if (selfFirst) {
				Class<?> c = findClass(name);
				if (resolve) {
					resolveClass(c);
				}
				return c;
			} else {
				return super.loadClass(name, resolve);
			}
		}

		/** {@inheritDoc} */
		@Override
		protected PermissionCollection getPermissions(CodeSource codesource) {
			// apply the all permission policy to all of our classes and packages.
			AllPermission allPerm = new AllPermission();
			PermissionCollection pc = allPerm.newPermissionCollection();
			pc.add(allPerm);
			return pc;
		}

	}

}
