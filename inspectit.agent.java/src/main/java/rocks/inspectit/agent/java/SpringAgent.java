package rocks.inspectit.agent.java;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import rocks.inspectit.agent.java.analyzer.IByteCodeAnalyzer;
import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.hooking.IHookDispatcher;
import rocks.inspectit.agent.java.instrumentation.IInstrumentationAware;
import rocks.inspectit.agent.java.logback.LogInitializer;
import rocks.inspectit.agent.java.spring.SpringConfiguration;
import rocks.inspectit.shared.all.instrumentation.config.impl.RetransformationStrategy;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.util.UnderlyingSystemInfo;
import rocks.inspectit.shared.all.util.UnderlyingSystemInfo.JvmProvider;
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
	 * inspectIT jar file.
	 */
	private static File inspectitJarFile;

	/**
	 * The hook dispatcher used by the instrumented methods.
	 */
	IHookDispatcher hookDispatcher;

	/**
	 * The configuration storage.
	 */
	IConfigurationStorage configurationStorage;

	/**
	 * The byte code analyzer.
	 */
	IByteCodeAnalyzer byteCodeAnalyzer;

	/**
	 * The thread transform helper.
	 */
	IThreadTransformHelper threadTransformHelper;

	/**
	 * Set to <code>true</code> if something happened and we need to disable further
	 * instrumentation.
	 */
	volatile boolean disableInstrumentation = false;

	/**
	 * Created bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * Ignore classes patterns.
	 */
	Collection<IMatchPattern> ignoreClassesPatterns;

	/**
	 * The used {@link Instrumentation}.
	 */
	private final Instrumentation instrumentation;

	/**
	 * Specifies whether retransformation is used.
	 */
	private Boolean usingRetransformation = null;

	/**
	 * Constructor initializing this agent.
	 *
	 * @param inspectitJarFile
	 *            The inspectIT jar file needed for proper logging
	 * @param instrumentation
	 *            The {@link Instrumentation} to use
	 */
	public SpringAgent(File inspectitJarFile, Instrumentation instrumentation) {
		this.instrumentation = instrumentation;

		setInspectITJarFile(inspectitJarFile);

		// init logging
		LogInitializer.initLogging();

		// init spring
		this.initSpring();
	}

	/**
	 * Sets {@link #inspectitJarFile} in synch mode.
	 *
	 * @param inspectitJarFile
	 *            The inspectIT jar file.
	 */
	private synchronized void setInspectITJarFile(File inspectitJarFile) {
		SpringAgent.inspectitJarFile = inspectitJarFile;
		LOG.info("Location of inspectit-agent.jar set to: " + inspectitJarFile.getAbsolutePath());
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
			threadTransformHelper = beanFactory.getBean(IThreadTransformHelper.class);

			// load ignore patterns only once
			ignoreClassesPatterns = configurationStorage.getIgnoreClassesPatterns();

			// injecting instrumentation in instrumentation aware beans
			Map<String, IInstrumentationAware> instrumentationAwareBeans = ctx.getBeansOfType(IInstrumentationAware.class);
			for (IInstrumentationAware instrumentationAwareBean : instrumentationAwareBeans.values()) {
				instrumentationAwareBean.setInstrumentation(instrumentation);
			}
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
	@Override
	public byte[] inspectByteCode(byte[] byteCode, String className, ClassLoader classLoader) {
		// if an error in the init method was caught, we'll do nothing here.
		// This prevents further errors.
		if (disableInstrumentation) {
			return byteCode;
		}

		boolean threadTransformDisabled = threadTransformHelper.isThreadTransformDisabled();
		if (threadTransformDisabled) {
			// if transform is currently disabled for thread trying to transform the class do
			// nothing
			return byteCode;
		}

		// check if it should be ignored
		if (shouldClassBeIgnored(className)) {
			return byteCode;
		}

		try {
			// set transform disabled from this point for this thread
			threadTransformHelper.setThreadTransformDisabled(true);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);
			return instrumentedByteCode;
		} catch (Throwable throwable) { // NOPMD
			LOG.error("Something unexpected happened while trying to analyze or instrument the bytecode with the class name: " + className, throwable);
			return byteCode;
		} finally {
			// reset the state of transform disabled if we set it originally
			threadTransformHelper.setThreadTransformDisabled(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHookDispatcher getHookDispatcher() {
		return hookDispatcher;
	}

	/**
	 * Returns absolute file to the inspectit jar if the {@link #inspectitJarFile} is set, otherwise
	 * returns <code>null</code>.
	 *
	 * @return Returns absolute file to the inspectit jar if the {@link #inspectitJarFile} is set,
	 *         otherwise returns <code>null</code>.
	 */
	public static File getInspectitJarFile() {
		if (null != inspectitJarFile) {
			return inspectitJarFile.getAbsoluteFile();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldClassBeIgnored(String className) {
		// if we are in disable instrumentation mode ignore all
		if (disableInstrumentation) {
			return true;
		}

		// ignore all classes which fit to the patterns in the configuration
		for (IMatchPattern matchPattern : ignoreClassesPatterns) {
			if (matchPattern.match(className)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUsingRetransformation() {
		if (usingRetransformation == null) {
			RetransformationStrategy retransformationStrategy;
			try {
				retransformationStrategy = configurationStorage.getRetransformStrategy();
			} catch (StorageException e) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Class retransformation strategy could be read. Using the default retransformation stretegy: " + RetransformationStrategy.DISABLE_ON_IBM_JVM);
				}
				retransformationStrategy = RetransformationStrategy.DISABLE_ON_IBM_JVM;
			}

			usingRetransformation = (retransformationStrategy == RetransformationStrategy.ALWAYS)
					|| ((retransformationStrategy == RetransformationStrategy.DISABLE_ON_IBM_JVM) && (UnderlyingSystemInfo.JVM_PROVIDER != JvmProvider.IBM));
		}

		return usingRetransformation.booleanValue();
	}

}
