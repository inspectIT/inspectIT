package rocks.inspectit.agent.java.logback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import rocks.inspectit.agent.java.SpringAgent;
import rocks.inspectit.shared.all.minlog.MinlogToSLF4JLogger;

/**
 * The component responsible for log initializations.
 *
 * @author Ivan Senic
 *
 */
public final class LogInitializer extends PropertyDefinerBase {

	/**
	 * Default name of the log file.
	 */
	public static final String DEFAULT_LOG_FILE_NAME = "logging-config.xml";

	/**
	 * JVM property for the log file location.
	 */
	private static final String LOG_FILE_PROPERTY = "inspectit.logging.config";

	/**
	 * Location of logs.
	 */
	private static String logDirLocation;

	/**
	 * Initializes the logging.
	 */
	public static void initLogging() {
		// set the location of logs
		File agentJar = SpringAgent.getInspectitJarFile();
		if (null == agentJar) {
			return;
		}

		initLogDirLocation();

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();

		InputStream is = null;

		try {
			// first check if it's supplied as parameter
			String logFileLocation = System.getProperty(LOG_FILE_PROPERTY);
			if (null != logFileLocation) {
				File logFile = new File(logFileLocation).getAbsoluteFile();
				if (logFile.exists()) {
					is = new FileInputStream(logFile);
				}
			}

			// then fail to default if none is specified
			if (null == is && null != agentJar) {
				String logPath = agentJar.getParent() + File.separator + File.separator + DEFAULT_LOG_FILE_NAME;
				File logFile = new File(logPath);
				if (logFile.exists()) {
					is = new FileInputStream(logFile);
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

		// initialize out minlog bridge to the slf4j
		// not sure if we can do this, this would also bridge application logging if they use
		// minlong
		MinlogToSLF4JLogger.init();
	}

	/**
	 * Initializes log directory location.
	 */
	private static synchronized void initLogDirLocation() {
		if (null == logDirLocation) {
			// set the location of logs to just [agent-path]/logs/startup for start
			File agentJar = SpringAgent.getInspectitJarFile();
			logDirLocation = agentJar.getParent() + File.separator + "logs" + File.separator + "startup"; // NOPMD
		}
	}

	/**
	 * Sets the agent name and re-initializes the logging so that the agentName is used as folder
	 * for logging.
	 *
	 * @param agentName
	 *            Agent name.
	 */
	public static void setAgentNameAndInitLogging(String agentName) {
		File agentJar = SpringAgent.getInspectitJarFile();
		if (null == agentJar) {
			return;
		}

		// set the location of logs based to agent name
		logDirLocation = agentJar.getParent() + File.separator + "logs" + File.separator + agentName;
		initLogging();
	}

	/**
	 * {@inheritDoc}
	 * <P>
	 * Returns {@link #logDirLocation} if one is set.
	 */
	public String getPropertyValue() {
		return logDirLocation;
	}

}
