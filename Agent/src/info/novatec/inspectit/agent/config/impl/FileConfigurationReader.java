package info.novatec.inspectit.agent.config.impl;

import info.novatec.inspectit.agent.analyzer.IMatchPattern;
import info.novatec.inspectit.agent.config.IConfigurationReader;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.ParserException;
import info.novatec.inspectit.agent.config.PriorityEnum;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.logback.LogInitializer;
import info.novatec.inspectit.spring.logger.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This config reader class reads simple config files. Simple in the way as you don't need any
 * additional java libraries and every statement is in one line.
 * 
 * @author Patrice Bouillet
 * @author Alfred Krauss
 * 
 */
@Component("configurationReader")
public class FileConfigurationReader implements IConfigurationReader, InitializingBean {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * Default ignore classes patterns. These will be used if no patterns is supplied by the user.
	 */
	private static final String[] DEFAULT_IGNORE_PATTERNS = new String[] { "java.security.SecureClassLoader", "info.novatec.inspectit.*", "$Proxy*", "sun.*", "java.lang.ThreadLocal",
			"java.lang.ref.Reference", "*_WLStub", "*[]" };

	/**
	 * The configuration storage implementation. Used to store the information to.
	 */
	private final IConfigurationStorage configurationStorage;

	/**
	 * The name of the property (directory) of the configuration.
	 */
	private static final String CONFIGURATION_PROPERTY = "inspectit.config";

	/**
	 * The name of the configuration file.
	 */
	private static final String CONFIGURATION_FILE = "inspectit-agent.cfg";

	// The available strings to look for a valid beginning
	/** Starting characters to mark the line a comment. */
	private static final String CONFIG_COMMENT = "#";
	/** Keyword to mark the repository definition. */
	private static final String CONFIG_REPOSITORY = "repository";
	/** Keyword to mark the send strategy definition. */
	private static final String CONFIG_SEND_STRATEGY = "send-strategy";
	/** Keyword to mark the buffer strategy definition. */
	private static final String CONFIG_BUFFER_STRATEGY = "buffer-strategy";
	/** Keyword to mark the method sensor definition. */
	private static final String CONFIG_METHOD_SENSOR_TYPE = "method-sensor-type";
	/** Keyword to mark the platform sensor definition. */
	private static final String CONFIG_PLATFORM_SENSOR_TYPE = "platform-sensor-type";
	/** Keyword to mark the assignment of a sensor. */
	private static final String CONFIG_SENSOR = "sensor";
	/** Keyword to configure the exception sensor. */
	private static final String CONFIG_EXCEPTION_SENSOR = "exception-sensor";
	/** Keyword to define the exception sensor type. */
	private static final String CONFIG_EXCEPTION_SENSOR_TYPE = "exception-sensor-type";
	/** Keyword to configure the jmx sensor. */
	private static final String CONFIG_JMX_SENSOR = "jmx-sensor";
	/** Keyword to define the jmx sensor type. */
	private static final String CONFIG_JMX_SENSOR_TYPE = "jmx-sensor-type";
	/** Keyword to include additional configuration files. */
	private static final String CONFIG_INCLUDE_FILE = "$include";
	/** Keyword to exclude certain classes from instrumentation. */
	private static final String CONFIG_EXCLUDE_CLASS = "exclude-class";
	/** Keyword to detect the Name of a MBean. */
	private static final String MBEAN_NAME_IDENTIFIER = "mbeanname=";
	/** Keyword to detect the Attributename of attribute of a MBean. */
	private static final String MBEAN_ATTRIBUTENAME_IDENTIFIER = "attributename=";

	/**
	 * Regular expression pattern to find the signatures of the method definitions in the
	 * configuration file. Pre-compiled for faster execution.
	 */
	private final Pattern methodSignature = Pattern.compile(".*\\((.+)\\)");

	/**
	 * Regular expression pattern to find empty signatures -> '()' . Pre-compiled for faster
	 * execution.
	 */
	private final Pattern emptyMethodSignature = Pattern.compile(".*\\(\\)");

	/**
	 * Default constructor which accepts one parameter.
	 * 
	 * @param configurationStorage
	 *            The configuration storage implementation.
	 */
	@Autowired
	public FileConfigurationReader(IConfigurationStorage configurationStorage) {
		this.configurationStorage = configurationStorage;
	}

	/**
	 * The default location for this reader is the path in the system variable 'inspectit.config'.
	 * <p>
	 * {@inheritDoc}
	 */
	public void load() throws ParserException {
		String pathToConfig = System.getProperty(CONFIGURATION_PROPERTY) + File.separator + CONFIGURATION_FILE;

		// Fallback to the standard location of the inspectit configuration
		// file when no system property is specified.
		if ("".equals(pathToConfig)) {
			pathToConfig = System.getProperty("user.dir") + File.separator + "inspectit" + File.separator + CONFIGURATION_PROPERTY;
		}

		// Load and parse the file
		try {
			File configFile = new File(pathToConfig);
			if (log.isDebugEnabled()) {
				log.debug("Agent Configuration file found at: " + configFile.getAbsolutePath());
			}
			InputStream is = new FileInputStream(configFile);
			InputStreamReader reader = new InputStreamReader(is);
			this.parse(reader, pathToConfig);

			// check if the exclude class patterns were supplied
			// if not add the default ones to the configuration
			List<IMatchPattern> ignorePatterns = configurationStorage.getIgnoreClassesPatterns();
			if (null == ignorePatterns || ignorePatterns.isEmpty()) {
				for (String ignorePatternString : DEFAULT_IGNORE_PATTERNS) {
					configurationStorage.addIgnoreClassesPattern(ignorePatternString);
				}
			}

		} catch (FileNotFoundException e) {
			log.info("Agent Configuration file not found at " + pathToConfig + ", aborting!");
			throw new ParserException("Agent Configuration file not found at " + pathToConfig, e);
		}
	}

	/**
	 * Parses the given file.
	 * 
	 * @param reader
	 *            The reader to open and parse.
	 * @param pathToConfig
	 *            The path to the configuration file.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	void parse(Reader reader, String pathToConfig) throws ParserException {
		// check for a valid Reader object
		if (null == reader) {
			throw new ParserException("Input is null! Aborting parsing.");
		}

		BufferedReader br = new BufferedReader(reader);

		String line = null;
		try {
			while ((line = br.readLine()) != null) { // NOPMD
				// Skip empty and comment lines
				if (line.trim().equals("") || line.startsWith(CONFIG_COMMENT)) {
					continue;
				}

				// Split the line into tokens
				StringTokenizer tokenizer = new StringTokenizer(line, " ");
				String discriminator = tokenizer.nextToken();

				// check for the repository
				if (discriminator.equalsIgnoreCase(CONFIG_REPOSITORY)) {
					processRepositoryLine(tokenizer);
					continue;
				}

				// check for a sending strategy
				if (discriminator.equalsIgnoreCase(CONFIG_SEND_STRATEGY)) {
					processSendStrategyLine(tokenizer);
					continue;
				}

				// check for a buffer strategy
				if (discriminator.equalsIgnoreCase(CONFIG_BUFFER_STRATEGY)) {
					processBufferStrategyLine(tokenizer);
					continue;
				}

				// Check for the method sensor type
				if (discriminator.equalsIgnoreCase(CONFIG_METHOD_SENSOR_TYPE)) {
					processMethodSensorTypeLine(tokenizer);
					continue;
				}

				// Check for the platform sensor type
				if (discriminator.equalsIgnoreCase(CONFIG_PLATFORM_SENSOR_TYPE)) {
					processPlatformSensorTypeLine(tokenizer);
					continue;
				}

				// check for exception sensor type line
				if (discriminator.equalsIgnoreCase(CONFIG_EXCEPTION_SENSOR_TYPE)) {
					processExceptionSensorTypeLine(tokenizer);
					continue;
				}

				// check for exception sensor line
				if (discriminator.equalsIgnoreCase(CONFIG_EXCEPTION_SENSOR)) {
					processExceptionSensorLine(tokenizer);
					continue;
				}

				// check for exception sensor type line
				if (discriminator.equalsIgnoreCase(CONFIG_JMX_SENSOR_TYPE)) {
					processJmxSensorTypeLine(tokenizer);
					continue;
				}

				// check for exception sensor line
				if (discriminator.equalsIgnoreCase(CONFIG_JMX_SENSOR)) {
					processJmxSensorLine(tokenizer);
					continue;
				}

				// check for a sensor
				if (discriminator.equalsIgnoreCase(CONFIG_SENSOR)) {
					processSensorLine(tokenizer);
					continue;
				}

				// check for a file include
				if (discriminator.equalsIgnoreCase(CONFIG_INCLUDE_FILE)) {
					processIncludeFileLine(tokenizer, pathToConfig);
					continue;
				}

				// check for exclude classes
				if (discriminator.equalsIgnoreCase(CONFIG_EXCLUDE_CLASS)) {
					processExcludeClassLine(tokenizer);
				}
			}
		} catch (Throwable throwable) { // NOPMD
			log.error("Error reading config on line : " + line);
			throw new ParserException("Error reading config on line : " + line, throwable);
		}
	}

	/**
	 * Process the jmx sensor type line.
	 * 
	 * @param tokenizer
	 *            {@link StringTokenizer} holding rest of the line.
	 * @throws ParserException
	 *             If Exception Sensor Type can not be added to the configuration storage.
	 */
	private void processJmxSensorTypeLine(StringTokenizer tokenizer) throws ParserException {
		String sensorName = tokenizer.nextToken();
		String sensorTypeClass = tokenizer.nextToken();
		try {
			configurationStorage.addJmxSensorType(sensorTypeClass, sensorName);
		} catch (StorageException e) {
			throw new ParserException("Could not add the jmx sensor type to the storage", e);
		}
	}

	/**
	 * Processes an jmx sensor line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor type.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processJmxSensorLine(StringTokenizer tokenizer) throws ParserException {
		// Adding values to unregisteredJmxSensorConfigs for further processing with a single JMX
		// sensor
		String jmxSensorTypeName = tokenizer.nextToken();
		String mBeanName = "";
		String attributeName = "";
		boolean marker = true;

		// Due to the fact that the name of a MBean can contain white spaces the remaining token
		// have to be checked as well
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.contains(MBEAN_NAME_IDENTIFIER)) {
				mBeanName = token.substring(MBEAN_NAME_IDENTIFIER.length());
			} else if (token.contains(MBEAN_ATTRIBUTENAME_IDENTIFIER)) {
				marker = false;
				attributeName = token.substring(MBEAN_ATTRIBUTENAME_IDENTIFIER.length());
			} else {
				if (marker) {
					mBeanName += " " + token;
				} else {
					attributeName += " " + token;
				}
			}
		}

		try {
			configurationStorage.addUnregisteredJmxConfig(jmxSensorTypeName, mBeanName, attributeName);
		} catch (StorageException e) {
			throw new ParserException("Could not add the jmx sensor type to the storage", e);
		}
	}

	/**
	 * Process the exception sensor type line.
	 * 
	 * @param tokenizer
	 *            {@link StringTokenizer} holding rest of the line.
	 * @throws ParserException
	 *             If Exception Sensor Type can not be added to the configuration storage.
	 */
	private void processExceptionSensorTypeLine(StringTokenizer tokenizer) throws ParserException {
		String sensorTypeClass = tokenizer.nextToken();

		Map<String, Object> settings = new HashMap<String, Object>();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}

		Object mode = settings.get("mode");
		if (null != mode && "enhanced".equals(mode)) {
			configurationStorage.setEnhancedExceptionSensorActivated(true);
		} else {
			configurationStorage.setEnhancedExceptionSensorActivated(false);
		}

		try {
			configurationStorage.addExceptionSensorType(sensorTypeClass, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the exception sensor type to the storage", e);
		}
	}

	/**
	 * Processes an exception sensor line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor type.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processExceptionSensorLine(StringTokenizer tokenizer) throws ParserException {
		// the sensor name is hardcoded here, because we don't define the
		// fully-qualified name of the exception sensor in the config file.
		String sensorTypeClass = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String targetClassName = tokenizer.nextToken();
		boolean isVirtual = false;

		if (targetClassName.indexOf('*') > -1) {
			isVirtual = true;
		}

		Map<String, Object> settings = new HashMap<String, Object>();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}

		try {
			configurationStorage.addExceptionSensorTypeParameter(sensorTypeClass, targetClassName, isVirtual, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the exception sensor type parameter to the storage", e);
		}
	}

	/**
	 * Processes a method sensor type line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor type.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processMethodSensorTypeLine(StringTokenizer tokenizer) throws ParserException {
		String sensorTypeName = tokenizer.nextToken();
		String sensorTypeClass = tokenizer.nextToken();
		String priorityString = tokenizer.nextToken();
		PriorityEnum priority = PriorityEnum.valueOf(priorityString);

		Map<String, Object> settings = new HashMap<String, Object>();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}

		try {
			configurationStorage.addMethodSensorType(sensorTypeName, sensorTypeClass, priority, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the method sensor type to the storage", e);
		}
	}

	/**
	 * Processes a platform sensor type line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor type.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processPlatformSensorTypeLine(StringTokenizer tokenizer) throws ParserException {
		String sensorTypeClass = tokenizer.nextToken();

		Map<String, Object> settings = new HashMap<String, Object>();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}
		try {
			configurationStorage.addPlatformSensorType(sensorTypeClass, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the platform sensor type to the storage", e);
		}
	}

	/**
	 * Processes a sensor line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sensor.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processSensorLine(StringTokenizer tokenizer) throws ParserException {
		String sensorTypeName = tokenizer.nextToken();
		String targetClassName = tokenizer.nextToken();
		String targetMethodName = tokenizer.nextToken();
		boolean ignoreSignature = false;

		// Trying to match the parameter types (if there are any)
		Matcher m = methodSignature.matcher(targetMethodName);
		List<String> parameterList = Collections.emptyList();
		if (m.matches()) {
			String[] classes = m.group(1).split(",");
			parameterList = new ArrayList<String>(classes.length);

			for (String clazz : classes) {
				parameterList.add(clazz.trim());
			}
			targetMethodName = targetMethodName.split("\\(")[0];
		} else if (emptyMethodSignature.matcher(targetMethodName).matches()) {
			targetMethodName = targetMethodName.replaceAll("\\(\\)", "");
		} else {
			ignoreSignature = true;
		}

		Map<String, Object> settings = new HashMap<String, Object>();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			if (parameterToken.charAt(0) == '@') {
				settings.put("annotation", parameterToken.substring(1));
			} else {
				StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
				String leftSide = parameterTokenizer.nextToken();
				String rightSide = parameterTokenizer.nextToken();

				if ("property".equals(leftSide) || "p".equals(leftSide)) {
					@SuppressWarnings("unchecked")
					List<String> propertyAccessorList = (List<String>) settings.get("property");
					if (null == propertyAccessorList) {
						propertyAccessorList = new ArrayList<String>();
						settings.put("property", propertyAccessorList);
					}
					propertyAccessorList.add(rightSide);
				} else if ("field".equals(leftSide) || "f".equals(leftSide)) {
					@SuppressWarnings("unchecked")
					List<String> propertyAccessorList = (List<String>) settings.get("field");
					if (null == propertyAccessorList) {
						propertyAccessorList = new ArrayList<String>();
						settings.put("field", propertyAccessorList);
					}
					propertyAccessorList.add(rightSide);
				} else if ("return".equals(leftSide) || "r".equals(leftSide)) {
					@SuppressWarnings("unchecked")
					List<String> propertyAccessorList = (List<String>) settings.get("return");
					if (null == propertyAccessorList) {
						propertyAccessorList = new ArrayList<String>();
						settings.put("return", propertyAccessorList);
					}
					propertyAccessorList.add(rightSide);
				} else {
					settings.put(leftSide, rightSide);
				}
			}
		}

		try {
			configurationStorage.addSensor(sensorTypeName, targetClassName, targetMethodName, parameterList, ignoreSignature, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the sensor to the storage", e);
		}
	}

	/**
	 * Processes a repository line and initializes it afterwards.
	 * 
	 * @param tokenizer
	 *            The string tokenizer which contains the definition of the repository.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processRepositoryLine(StringTokenizer tokenizer) throws ParserException {
		String host = tokenizer.nextToken();
		int port = Integer.parseInt(tokenizer.nextToken());
		String name = tokenizer.nextToken();

		// when we know the name init the logging
		LogInitializer.setAgentNameAndInitLogging(name);

		try {
			configurationStorage.setAgentName(name);
			configurationStorage.setRepository(host, port);
		} catch (StorageException e) {
			throw new ParserException("Could net set the agent name or repository", e);
		}
	}

	/**
	 * Processes a send strategy line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a sending strategy.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processSendStrategyLine(StringTokenizer tokenizer) throws ParserException {
		String sendStrategyClass = tokenizer.nextToken();

		Map<String, String> settings = new HashMap<String, String>();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}
		try {
			configurationStorage.addSendingStrategy(sendStrategyClass, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not add the sending strategy to the storage", e);
		}
	}

	/**
	 * Processes a buffer strategy line.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the strings to create a buffer strategy.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processBufferStrategyLine(StringTokenizer tokenizer) throws ParserException {
		String bufferStrategyClass = tokenizer.nextToken();

		Map<String, String> settings = new HashMap<String, String>();
		while (tokenizer.hasMoreTokens()) {
			String parameterToken = tokenizer.nextToken();
			StringTokenizer parameterTokenizer = new StringTokenizer(parameterToken, "=");
			String leftSide = parameterTokenizer.nextToken();
			String rightSide = parameterTokenizer.nextToken();
			settings.put(leftSide, rightSide);
		}
		try {
			configurationStorage.setBufferStrategy(bufferStrategyClass, settings);
		} catch (StorageException e) {
			throw new ParserException("Could not set the buffer strategy in the storage", e);
		}
	}

	/**
	 * Process an additional configuration file.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the path to an additional configuration file.
	 * @param pathToParentFile
	 *            path to the parent file.
	 * @throws ParserException
	 *             Thrown if there was an exception caught by parsing the config file.
	 */
	private void processIncludeFileLine(StringTokenizer tokenizer, String pathToParentFile) throws ParserException {
		String fileName = tokenizer.nextToken();

		File file = new File(fileName);
		if (!file.isAbsolute()) {
			// if the file does not denote an absolute file, we have to prepend
			// the folder in which the current configuration is loaded.
			file = new File(new File(pathToParentFile).getParent() + File.separator + fileName);
		}
		if (file.isDirectory()) {
			log.info("Specified additional configuration is a folder: " + file.getAbsolutePath() + ", aborting!");
			throw new ParserException("Specified additional configuration is a folder: " + file.getAbsolutePath());
		}

		try {
			log.info("Additional agent configuration file found at: " + file.getAbsolutePath());
			InputStream is = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(is);
			this.parse(reader, file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			log.info("Additional agent configuration file not found at " + file.getAbsolutePath() + ", aborting!");
			throw new ParserException("Additional agent Configuration file not found at " + file.getAbsolutePath(), e);
		}
	}

	/**
	 * Process a line for the exclude classes configuration.
	 * 
	 * @param tokenizer
	 *            The tokenizer which contains the path to an additional configuration file.
	 */
	private void processExcludeClassLine(StringTokenizer tokenizer) {
		while (tokenizer.hasMoreTokens()) {
			String patternString = tokenizer.nextToken();
			configurationStorage.addIgnoreClassesPattern(patternString);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		load();
	}

}
