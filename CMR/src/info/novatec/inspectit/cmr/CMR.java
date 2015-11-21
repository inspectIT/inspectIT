package info.novatec.inspectit.cmr;

import info.novatec.inspectit.cmr.security.SecurityInitialization;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.minlog.MinlogToSLF4JLogger;
import info.novatec.inspectit.versioning.IVersioningService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Main class of the Central Measurement Repository. The main method is used to start the
 * application.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class CMR {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CMR.class);

	/**
	 * Default name of the log file.
	 */
	public static final String DEFAULT_LOG_FILE_NAME = "logging-config.xml";

	/**
	 * JVM property for the log file location.
	 */
	private static final String LOG_FILE_PROPERTY = "inspectit.logging.config";

	/**
	 * The spring bean factory to get the registered beans.
	 */
	private static BeanFactory beanFactory; // NOPMD

	/**
	 * startedAsService holds the value if CMR was started as a Windows Service.
	 */
	private static boolean startedAsService;

	/**
	 * This class will start the Repository.
	 */
	private CMR() {
	}

	/**
	 * Pseudo main method of class.
	 */
	private static void startCMR() {
		initLogger();

		long startTime = System.nanoTime();
		LOGGER.info("Central Measurement Repository is starting up!");
		LOGGER.info("==============================================");

		startRepository();

		LOGGER.info("CMR started in " + Converter.nanoToMilliseconds(System.nanoTime() - startTime) + " ms");
				
		SecurityInitialization databaseStartup = (SecurityInitialization) beanFactory.getBean("SecurityInitialization");
		
		databaseStartup.start();
		
		LOGGER.info("User-Database initialised.");
		
	}

	/**
	 * This class will start the Repository.
	 */
	private static void startRepository() {
		LOGGER.info("Initializing Spring...");

		BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("ctx");
		beanFactory = beanFactoryReference.getFactory();

		if (beanFactory instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) beanFactory).registerShutdownHook();
		}

		LOGGER.info("Spring successfully initialized");

		if (LOGGER.isInfoEnabled()) {
			IVersioningService versioning = (IVersioningService) getBeanFactory().getBean("versioning");
			String currentVersion = "n/a";
			try {
				currentVersion = versioning.getVersion();
			} catch (IOException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Versioning information could not be read");
				}
			}
			LOGGER.info("Starting CMR in version " + currentVersion);
		}
	}

	/**
	 * Start function. Needed by Procrun.
	 * 
	 * @param args
	 *            The arguments.
	 */
	public static void start(String[] args) {
		startedAsService = true;
		startCMR();
	}

	/**
	 * Stop function. Needed by Procrun.
	 * 
	 * @param args
	 *            The arguments.
	 */
	public static void stop(String[] args) {
		System.exit(0);
	}

	/**
	 * Initializes the logger.
	 */
	private static void initLogger() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();

		InputStream is = null;

		try {
			// first check if it's supplied as parameter
			String logFileLocation = System.getProperty(LOG_FILE_PROPERTY);
			if (null != logFileLocation) {
				Path logPath = Paths.get(logFileLocation).toAbsolutePath();
				if (Files.exists(logPath)) {
					is = Files.newInputStream(logPath, StandardOpenOption.READ);
				}
			}

			// then fail to default if none is specified
			if (null == is) {
				Path logPath = Paths.get(DEFAULT_LOG_FILE_NAME).toAbsolutePath();
				if (Files.exists(logPath)) {
					is = Files.newInputStream(logPath, StandardOpenOption.READ);
				}
			}

			if (null != is) {
				try {
					configurator.doConfigure(is);
				} catch (JoranException e) { // NOPMD NOCHK StatusPrinter will handle this
				} finally {
					is.close();
				}
			}
		} catch (IOException e) { // NOPMD NOCHK StatusPrinter will handle this
		}

		StatusPrinter.printInCaseOfErrorsOrWarnings(context);

		// use sysout-over-slf4j to redirect out and err calls to logger
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();

		// initialize out minlog bridge to the slf4j
		MinlogToSLF4JLogger.init();
	}

	/**
	 * Main method of class.
	 * 
	 * @param args
	 *            The arguments.
	 */
	public static void main(String[] args) {
		// Start Apache Procrun only if it's a Windows operating system.
		if (args.length == 1 && SystemUtils.IS_OS_WINDOWS) {
			switch (args[0]) {
			case "start":
				start(args);
				break;
			case "stop":
				stop(args);
				break;
			default:
				startCMR();
			}
		} else {
			startCMR();
		}
	}

	/**
	 * Returns the spring bean factory.
	 * 
	 * @return The spring bean factory.
	 */
	public static BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Getter method for property <code>startedAsService</code>.
	 * 
	 * @return startedAsService.
	 */
	public static boolean isStartedAsService() {
		return startedAsService;
	}
}
