package rocks.inspectit.agent.java;

import java.io.File;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import rocks.inspectit.agent.java.analyzer.IByteCodeAnalyzer;
import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.hooking.IHookDispatcher;
import rocks.inspectit.agent.java.logback.LogInitializer;
import rocks.inspectit.agent.java.spring.SpringConfiguration;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.version.VersionService;

/**
 * The {@link SpringAgent} is used by the javaagent to analyze the passed bytecode if its needed to
 * be instrumented. The {@link #getInstance()} method returns the singleton instance of this class.
 * <p>
 * The {@link #inspectByteCode(byte[], String, ClassLoader)} is the method which should be called by
 * the javaagent. The method returns null if nothing has to be changed or something happened
 * unexpectedly.
 * <p>
 * This class is named <b>Spring</b>Agent as its using the Spring to handle the different components
 * in the Agent.
 *
 * @author Patrice Bouillet
 *
 */
public class SpringAgent implements IAgent {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SpringAgent.class);

	/**
	 * Our class start with {@value #CLASS_NAME_PREFIX}.
	 */
	private static final String CLASS_NAME_PREFIX = "rocks.inspectit.shared.all";

	/**
	 * Location of inspectIT jar file.
	 */
	private static String inspectitJarLocation;

	/**
	 * The hook dispatcher used by the instrumented methods.
	 */
	private IHookDispatcher hookDispatcher;

	/**
	 * The configuration storage.
	 */
	private IConfigurationStorage configurationStorage;

	/**
	 * The byte code analyzer.
	 */
	private IByteCodeAnalyzer byteCodeAnalyzer;

	/**
	 * Set to <code>true</code> if something happened and we need to disable further
	 * instrumentation.
	 */
	private volatile boolean disableInstrumentation = false;

	/**
	 * Created bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * Ignore classes patterns.
	 */
	private Collection<IMatchPattern> ignoreClassesPatterns;

	/**
	 * Constructor initializing this agent.
	 *
	 * @param inspectitJarLocation
	 *            location of inspectIT jar needed for proper logging
	 */
	public SpringAgent(String inspectitJarLocation) {
		setInspectITJarLocation(inspectitJarLocation);

		// init logging
		LogInitializer.initLogging();

		// init spring
		this.initSpring();
	}

	/**
	 * Sets {@link #inspectitJarLocation} in synch mode.
	 *
	 * @param inspectitJarLocation
	 *            Location of the inspectIT jar.
	 */
	private synchronized void setInspectITJarLocation(String inspectitJarLocation) {
		SpringAgent.inspectitJarLocation = inspectitJarLocation;
		LOG.info("Location of inspectit-agent.jar set to: " + inspectitJarLocation);
	}

	/**
	 * Initializes the spring.
	 */
	private void initSpring() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Initializing Spring on inspectIT Agent...");
		}

		// first add shutdown hook so we are informed when shutdown is initialized to stop
		// instrumenting classes on the shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				disableInstrumentation = true;
			};
		});

		// set inspectIT class loader to be the context class loader
		// so that bean factory can use correct class loader for finding the classes
		ClassLoader inspectITClassLoader = this.getClass().getClassLoader();
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(inspectITClassLoader);

		// load spring context in try block, catch exception and set init error to true
		try {
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.register(SpringConfiguration.class);
			ctx.refresh();
			beanFactory = ctx;

			if (beanFactory instanceof ConfigurableApplicationContext) {
				((ConfigurableApplicationContext) beanFactory).registerShutdownHook();
			}

			if (LOG.isInfoEnabled()) {
				LOG.info("Spring successfully initialized");
			}

			// log version
			if (LOG.isInfoEnabled()) {
				VersionService versionService = beanFactory.getBean(VersionService.class);
				LOG.info("Using agent version " + versionService.getVersionAsString() + ".");
			}

			// load all necessary beans right away
			hookDispatcher = beanFactory.getBean(IHookDispatcher.class);
			configurationStorage = beanFactory.getBean(IConfigurationStorage.class);
			byteCodeAnalyzer = beanFactory.getBean(IByteCodeAnalyzer.class);

			// load ignore patterns only once
			ignoreClassesPatterns = configurationStorage.getIgnoreClassesPatterns();

		} catch (Throwable throwable) { // NOPMD
			disableInstrumentation = true;
			LOG.error("inspectIT agent initialization failed. Agent will not be active.", throwable);
		}

		// switch back to the original context class loader
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] inspectByteCode(byte[] byteCode, String className, ClassLoader classLoader) {
		// if an error in the init method was caught, we'll do nothing here.
		// This prevents further errors.
		if (disableInstrumentation) {
			return byteCode;
		}

		// ignore all classes which fit to the patterns in the configuration
		for (IMatchPattern matchPattern : ignoreClassesPatterns) {
			if (matchPattern.match(className)) {
				return byteCode;
			}
		}

		try {
			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
			return instrumentedByteCode;
		} catch (Throwable throwable) { // NOPMD
			LOG.error("Something unexpected happened while trying to analyze or instrument the bytecode with the class name: " + className, throwable);
			return byteCode;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> loadClass(Object[] params) {
		try {
			if (null != params && params.length == 1) {
				Object p = params[0];
				if (p instanceof String) {
					return loadClass((String) p);
				}
			}
			return null;
		} catch (Throwable e) { // NOPMD
			return null;
		}
	}

	/**
	 * Delegates the class loading to the {@link #inspectItClassLoader} if the class name starts
	 * with {@value #CLASS_NAME_PREFIX}. Otherwise loads the class with the target class loader. If
	 * the inspectIT class loader throws {@link ClassNotFoundException}, the target class loader
	 * will be used.
	 *
	 * @param className
	 *            Class name.
	 * @return Loaded class or <code>null</code> if it can not be found with inspectIT class loader.
	 */
	private Class<?> loadClass(String className) {
		if (loadWithInspectItClassLoader(className)) {
			try {
				return getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Defines if the class should be loaded with our class loader.
	 *
	 * @param className
	 *            Name of the class to load.
	 * @return True if class name starts with {@value #CLASS_NAME_PREFIX}.
	 */
	private boolean loadWithInspectItClassLoader(String className) {
		return className.startsWith(CLASS_NAME_PREFIX);
	}

	/**
	 * {@inheritDoc}
	 */
	public IHookDispatcher getHookDispatcher() {
		return hookDispatcher;
	}

	/**
	 * Returns absolute file to the inspectit jar if the {@link #inspectitJarLocation} is set,
	 * otherwise returns <code>null</code>.
	 *
	 * @return Returns absolute file to the inspectit jar if the {@link #inspectitJarLocation} is
	 *         set, otherwise returns <code>null</code>.
	 */
	public static File getInspectitJarFile() {
		if (null != inspectitJarLocation) {
			return new File(inspectitJarLocation).getAbsoluteFile();
		}
		return null;
	}

}
