package info.novatec.inspectit.cmr.test;

import info.novatec.inspectit.cmr.CMR;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.LoggerFactory;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * This abstract class provides general logging support for the test classes that need transactional
 * spring context.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public abstract class AbstractTransactionalTestNGLogSupport extends AbstractTransactionalTestNGSpringContextTests {

	/**
	 * Init logging.
	 */
	@BeforeSuite
	public void initLogging() throws IOException {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();

		Path logPath = Paths.get(CMR.DEFAULT_LOG_FILE_NAME).toAbsolutePath();
		try (InputStream is = Files.newInputStream(logPath, StandardOpenOption.READ)) {

			configurator.doConfigure(is);
		} catch (JoranException je) { // NOPMD StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

}
