package info.novatec.inspectit.agent;

import java.io.IOException;
import java.util.logging.Level;

import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * This abstract class is used if the logging level needs to be changed. The default of
 * {@link Level#INFO} is most of the time not used.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class AbstractLogSupport extends TestBase {

	/**
	 * Init logging.
	 */
	@BeforeSuite
	public void initLogging() throws IOException {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		// don't print anything
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();

		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

}
