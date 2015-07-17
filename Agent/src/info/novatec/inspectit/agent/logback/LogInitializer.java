package info.novatec.inspectit.agent.logback;

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
	 * Location of inspectIT jar file.
	 */
	private static String inspectitJarLocation;

	/**
	 * Initializes the logging.
	 */
	public static void initLogging() {
		if (null == inspectitJarLocation) {
			return;
		}

		// set the location of logs
		File agentJar = new File(inspectitJarLocation).getAbsoluteFile();

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
	}

	/**
	 * Sets the agent name and re-initializes the logging so that the agentName is used as folder
	 * for logging.
	 * 
	 * @param agentName
	 *            Agent name.
	 */
	public static void setAgentNameAndInitLogging(String agentName) {
		if (null == inspectitJarLocation) {
			return;
		}

		// set the location of logs based to agent name
		File agentJar = new File(inspectitJarLocation).getAbsoluteFile();
		logDirLocation = agentJar.getParent() + File.separator + "logs" + File.separator + agentName;

		initLogging();
	}

	/**
	 * Sets {@link #inspectitJarLocation}.
	 * 
	 * @param inspectitJarLoc
	 *            New value for {@link #inspectitJarLocation}
	 */
	public static void setInspectitJarLocation(String inspectitJarLoc) {
		inspectitJarLocation = inspectitJarLoc;

		// set the location of logs to just [agent-path]/logs/startup for start
		File agentJar = new File(inspectitJarLocation).getAbsoluteFile();
		logDirLocation = agentJar.getParent() + File.separator + "logs" + File.separator + "startup";
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
