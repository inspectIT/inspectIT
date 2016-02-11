package info.novatec.inspectit.agent;

import info.novatec.inspectit.agent.analyzer.IByteCodeAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatchPattern;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.hooking.IHookDispatcher;
import info.novatec.inspectit.agent.logback.LogInitializer;
import info.novatec.inspectit.agent.spring.SpringConfiguration;
import info.novatec.inspectit.version.VersionService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
	private static final String CLASS_NAME_PREFIX = "info.novatec.inspectit";

	/**
	 * The hook dispatcher used by the instrumented methods.
	 */
	private IHookDispatcher hookDispatcher;

	/**
	 * Set to <code>true</code> if something happened while trying to initialize the pico container.
	 */
	private boolean initializationError = false;

	/**
	 * Created bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * Constructor initializing this agent.
	 * 
	 * @param inspectitJarLocation
	 *            location of inspectIT jar needed for proper logging
	 */
	public SpringAgent(String inspectitJarLocation) {
		LogInitializer.setInspectitJarLocation(inspectitJarLocation);
		LogInitializer.initLogging();
		this.initSpring();
	}

	/**
	 * Initializes the spring.
	 */
	private void initSpring() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Initializing Spring on inspectIT Agent...");
		}

		// set inspectIT class loader to be the context class loader
		// so that bean factory can use correct class loader for finding the classes
		ClassLoader inspectITClassLoader = this.getClass().getClassLoader();
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(inspectITClassLoader);

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(SpringConfiguration.class);
		ctx.refresh();
		beanFactory = ctx;

		if (LOG.isInfoEnabled()) {
			LOG.info("Spring successfully initialized");
		}

		// log version
		if (LOG.isInfoEnabled()) {
			VersionService versionService = beanFactory.getBean(VersionService.class);
			LOG.info("Using agent version " + versionService.getVersionAsString() + ".");
		}

		hookDispatcher = beanFactory.getBean(IHookDispatcher.class);

		// switch back to the original context class loader
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] inspectByteCode(byte[] byteCode, String className, ClassLoader classLoader) {
		// if an error in the init method was caught, we'll do nothing here.
		// This prevents further errors.
		if (initializationError) {
			return byteCode;
		}

		// ignore all classes which fit to the patterns in the configuration
		IConfigurationStorage configurationStorage = beanFactory.getBean(IConfigurationStorage.class);
		List<IMatchPattern> ignoreClassesPatterns = configurationStorage.getIgnoreClassesPatterns();
		for (IMatchPattern matchPattern : ignoreClassesPatterns) {
			if (matchPattern.match(className)) {
				return byteCode;
			}
		}

		IByteCodeAnalyzer byteCodeAnalyzer = beanFactory.getBean(IByteCodeAnalyzer.class);
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

}
