package rocks.inspectit.ui.rcp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import rocks.inspectit.shared.all.minlog.MinlogToSLF4JLogger;
import rocks.inspectit.shared.all.util.ResourcesPathResolver;
import rocks.inspectit.ui.rcp.ci.InspectITConfigurationInterfaceManager;
import rocks.inspectit.ui.rcp.log.LogListener;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryManager;
import rocks.inspectit.ui.rcp.storage.InspectITStorageManager;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

/**
 * The main plugin class to be used in the desktop.
 */
public class InspectIT extends AbstractUIPlugin {

	/**
	 * The id of this plugin.
	 */
	public static final String ID = "rocks.inspectit.ui.rcp";

	/**
	 * Default name of the log file.
	 */
	private static final String DEFAULT_LOG_FILE_NAME = "logging-config.xml";

	/**
	 * JVM property for the log file location.
	 */
	private static final String LOG_FILE_PROPERTY = "inspectit.logging.config";

	/**
	 * The shared instance.
	 */
	private static InspectIT plugin;

	/**
	 * The global repository management tool. It is used to create and save the connection to the
	 * CMR.
	 */
	private volatile CmrRepositoryManager cmrRepositoryManager;

	/**
	 * Preferences store for the plug-in.
	 */
	private volatile ScopedPreferenceStore preferenceStore;

	/**
	 * The global storage manager.
	 */
	private volatile InspectITStorageManager storageManager;

	/**
	 * The global configuration interface manager.
	 */
	private volatile InspectITConfigurationInterfaceManager configurationInterfaceManager;

	/**
	 * List of property change listener in the plug-in.
	 * <p>
	 * Currently the property change mechanism of Eclipse RCP is not used in inspectIT. However, it
	 * might be used in future.
	 */
	private List<IPropertyChangeListener> propertyChangeListeners = new ArrayList<IPropertyChangeListener>();

	/**
	 * Runtime directory of plug-in depending if we are in development or not.
	 */
	private Path runtimeDir;

	/**
	 * {@link ILogListener} used for logging.
	 */
	private ILogListener logListener;

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context
	 *            the Context.
	 * 
	 * @throws Exception
	 *             in case of error.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;

		locateRuntimeDir();
		initLogger();

		// add log listener once logger is initialized
		logListener = new LogListener();
		Platform.addLogListener(logListener);

		super.start(context);
	}

	/**
	 * Locates the runtime directory. It's needed for distinguish between development and runtime.
	 */
	private void locateRuntimeDir() {
		File bundleFile = null;
		try {
			bundleFile = FileLocator.getBundleFile(getBundle());
		} catch (IOException e) { // NOPMD //NOCHK
		}

		if (null != bundleFile && bundleFile.isDirectory()) {
			runtimeDir = Paths.get(bundleFile.getAbsolutePath());
			// in development bundle file is in src/main/resources
			if (runtimeDir.toString().endsWith(ResourcesPathResolver.RESOURCES)) {
				runtimeDir = runtimeDir.getParent().getParent().getParent().toAbsolutePath();
			}
		} else {
			runtimeDir = Paths.get("");
		}
	}

	/**
	 * Initializes the logger.
	 */
	private void initLogger() {
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
				Path logPath = ResourcesPathResolver.getResourceFile(DEFAULT_LOG_FILE_NAME, runtimeDir.toFile()).toPath().toAbsolutePath();
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
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            the Context.
	 * 
	 * @throws Exception
	 *             in case of error.
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		if (null != cmrRepositoryManager) {
			cmrRepositoryManager.cancelAllUpdateRepositoriesJobs();
		}

		// remove log listener
		Platform.removeLogListener(logListener);
		logListener = null; // NOPMD

		super.stop(context);
		plugin = null; // NOPMD
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Returns the shared instance.
	 */
	public static InspectIT getDefault() {
		return plugin;
	}

	/**
	 * Registers the {@link IPropertyChangeListener} with the plug-in. Has no effect if the listener
	 * is already registered.
	 * 
	 * @param propertyChangeListener
	 *            {@link IPropertyChangeListener} to add.
	 */
	public void addPropertyChangeListener(IPropertyChangeListener propertyChangeListener) {
		if (!propertyChangeListeners.contains(propertyChangeListener)) {
			propertyChangeListeners.add(propertyChangeListener);
		}
	}

	/**
	 * Unregisters the {@link IPropertyChangeListener} from the plug-in.
	 * 
	 * @param propertyChangeListener
	 *            {@link IPropertyChangeListener} to remove.
	 */
	public void removePropertyChangeListener(IPropertyChangeListener propertyChangeListener) {
		propertyChangeListeners.remove(propertyChangeListener);
	}

	/**
	 * Delegates the {@link PropertyChangeEvent} to all listeners.
	 * 
	 * @param event
	 *            Event to delegate.
	 */
	public void firePropertyChangeEvent(PropertyChangeEvent event) {
		for (IPropertyChangeListener listener : propertyChangeListeners) {
			listener.propertyChange(event);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		Field[] allFields = InspectITImages.class.getFields();
		for (Field field : allFields) {
			if (field.getName().startsWith("IMG") && String.class.equals(field.getType())) {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				try {
					String key = (String) field.get(null);
					URL url = getBundle().getEntry(key);
					
					if (null != reg.get(key)) {
						// if we already have an icon with the same key continue
						// can happen if two fields are pointing to same image
						continue;
					}
					
					if (null != url) {
						reg.put(key, ImageDescriptor.createFromURL(url));
						
						// try to get the image in order to be certain given URL is representing an image file
						reg.get(key);
					} else {
						// if image does not exists (url is null) show and log error
						Status status = new Status(Status.ERROR, ID, "Image with the key '" + field.getName() + "' does not exist on the disk. ");
						StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
					}
				} catch (Exception e) {
					Status status = new Status(Status.ERROR, ID, "Error loading image with the key'" + field.getName() + "'. ");
					StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
					continue;
				}
			}
		}
	}

	/**
	 * Returns an image from the image registry by resolving the passed image key.
	 * <p>
	 * <b>Images retrieved by this method should not be disposed, because they are shared resources
	 * in the plugin and will be disposed with the disposal of the display.</b>
	 * 
	 * @param imageKey
	 *            The key of the image to look for in the registry.
	 * @return The generated image.
	 */
	public Image getImage(String imageKey) {
		return getImageRegistry().get(imageKey);
	}

	/**
	 * Returns the image descriptor for the given key. The key can be one of the IMG_ definitions in
	 * {@link InspectITImages}.
	 * <p>
	 * <b>Every new image created with the given {@link ImageDescriptor} should be disposed by the
	 * caller.</b>
	 * 
	 * @param imageKey
	 *            The image key.
	 * @return The image descriptor for the given image key.
	 */
	public ImageDescriptor getImageDescriptor(String imageKey) {
		return getImageRegistry().getDescriptor(imageKey);
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
		ServiceReference<E> reference = getDefault().getBundle().getBundleContext().getServiceReference(clazz);
		if (null != reference) {
			return getDefault().getBundle().getBundleContext().getService(reference);
		}
		throw new RuntimeException("Requested service of the class " + clazz.getName() + " is not registered in the bundle.");
	}

	/**
	 * {@inheritDoc}
	 */
	public ScopedPreferenceStore getPreferenceStore() {
		if (null == preferenceStore) {
			synchronized (this) {
				if (null == preferenceStore) { // NOCHK: DCL works with volatile.
					preferenceStore = new ScopedPreferenceStore(ConfigurationScope.INSTANCE, ID);
				}
			}
		}
		return preferenceStore;
	}

	/**
	 * @return Returns the CMR repository manager.
	 */
	public CmrRepositoryManager getCmrRepositoryManager() {
		if (null == cmrRepositoryManager) {
			synchronized (this) {
				if (null == cmrRepositoryManager) { // NOCHK: DCL works with volatile.
					cmrRepositoryManager = new CmrRepositoryManager();
				}
			}
		}
		return cmrRepositoryManager;
	}

	/**
	 * 
	 * @return Returns the {@link InspectITStorageManager}.
	 */
	public InspectITStorageManager getInspectITStorageManager() {
		if (null == storageManager) {
			synchronized (this) {
				if (null == storageManager) { // NOCHK: DCL works with volatile.
					storageManager = getService(InspectITStorageManager.class);
					storageManager.startUp();
				}
			}
		}
		return storageManager;
	}

	/**
	 * 
	 * @return Returns the {@link InspectITConfigurationInterfaceManager}.
	 */
	public InspectITConfigurationInterfaceManager getInspectITConfigurationInterfaceManager() {
		if (null == configurationInterfaceManager) {
			synchronized (this) {
				if (null == configurationInterfaceManager) { // NOCHK: DCL works with volatile.
					configurationInterfaceManager = getService(InspectITConfigurationInterfaceManager.class);
				}
			}
		}
		return configurationInterfaceManager;
	}

	/**
	 * Gets {@link #runtimeDir}.
	 * 
	 * @return {@link #runtimeDir}
	 */
	public Path getRuntimeDir() {
		return runtimeDir;
	}

	/**
	 * Sets {@link #runtimeDir}.
	 * 
	 * @param runtimeDir
	 *            New value for {@link #runtimeDir}
	 */
	public void setRuntimeDir(Path runtimeDir) {
		this.runtimeDir = runtimeDir;
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
		IStatus status = new Status(IStatus.ERROR, ID, code, message, throwable);
		StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
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
		// Status sets exception to <code>null</code> internally.
		IStatus status = new Status(IStatus.ERROR, ID, code, message, null);
		StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
	}

	/**
	 * Creates a simple info dialog.
	 * 
	 * @param message
	 *            The message of the dialog.
	 * @param code
	 *            The code of the error. <b>-1</b> is a marker that the code has to be added later.
	 */
	public void createInfoDialog(String message, int code) {
		MessageDialog.openInformation(null, "Information", message);
	}

	/**
	 * Logs the message with given severity. Logging only means no dialog will be displayed to the
	 * user. Severity can be {@link IStatus#INFO}, {@link IStatus#WARN} or {@link IStatus#ERROR}
	 * which will define log level for the logger.
	 * 
	 * @param severity
	 *            {@link IStatus#INFO}, {@link IStatus#WARN} or {@link IStatus#ERROR}
	 * @param message
	 *            Message to log.
	 */
	public void log(int severity, String message) {
		log(severity, message, null);
	}

	/**
	 * Logs the message and throwbale with given severity. Logging only means no dialog will be
	 * displayed to the user. Severity can be {@link IStatus#INFO}, {@link IStatus#WARN} or
	 * {@link IStatus#ERROR} which will define log level for the logger.
	 * 
	 * @param severity
	 *            {@link IStatus#INFO}, {@link IStatus#WARN} or {@link IStatus#ERROR}
	 * @param message
	 *            Message to log.
	 * @param throwable
	 *            Throwable to log.
	 */
	public void log(int severity, String message, Throwable throwable) {
		IStatus status = new Status(severity, ID, 0, message, throwable);
		StatusManager.getManager().handle(status, StatusManager.LOG);
	}
}
