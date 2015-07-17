package info.novatec.inspectit.rcp;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Overwrites the <code>InspectIT</code> class from the inspectIT project. This allows us to re-use
 * most of the code of the inspectIT project to access the CMR services.
 * 
 * Please note that this class must "overwrite" the old inspectIT class by prepending it to the
 * classpath.
 * 
 * @author Stefan Siegl
 */
// NOCHKALL
@SuppressWarnings("PMD")
public class InspectIT {

	public static BeanFactory beanFactory;

	static {
		initLogger();

		BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("ctx");
		beanFactory = beanFactoryReference.getFactory();
	}

	/**
	 * The shared instance.
	 */
	private static InspectIT plugin = new InspectIT();

	/**
	 * Initializes the logger.
	 */
	private static void initLogger() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure(InspectIT.class.getResourceAsStream("/config/logback.xml"));
		} catch (JoranException je) { // NOPMD NOCHK StatusPrinter will handle this NOCHK
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

	/**
	 * Returns a service, if one is registered with the bundle context.
	 * 
	 * @param clazz
	 *            Class of service.
	 * @param <E>
	 *            Type
	 * @return Service or <code>null</code> is service is not registered at the moment.
	 */
	public static <E> E getService(Class<E> clazz) {
		return beanFactory.getBean(clazz);
	}

	public static InspectIT getDefault() {
		return plugin;
	}

	/**
	 * Creates a simple error dialog.
	 * 
	 * @param message
	 *            The message of the dialog.
	 * @param throwable
	 *            The exception to display
	 * @param code
	 *            The code of the error. <b>-1</b> is a marker that the code has to be added later.
	 */
	public void createErrorDialog(String message, Throwable throwable, int code) {
		System.out.println(message);
		throwable.printStackTrace();
	}

	/**
	 * Creates a simple error dialog without exception.
	 * 
	 * @param message
	 *            The message of the dialog.
	 * @param code
	 *            The code of the error. <b>-1</b> is a marker that the code has to be added later.
	 */
	public void createErrorDialog(String message, int code) {
		System.out.println(message);
	}

}
