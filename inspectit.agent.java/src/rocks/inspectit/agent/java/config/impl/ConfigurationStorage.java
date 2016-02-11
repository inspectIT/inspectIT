package info.novatec.inspectit.agent.config.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatchPattern;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.analyzer.impl.DirectMatcher;
import info.novatec.inspectit.agent.analyzer.impl.ModifierMatcher;
import info.novatec.inspectit.agent.analyzer.impl.SimpleMatchPattern;
import info.novatec.inspectit.agent.analyzer.impl.SuperclassMatcher;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.PriorityEnum;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.jrebel.JRebelUtil;
import info.novatec.inspectit.communication.data.ParameterContentType;
import info.novatec.inspectit.spring.logger.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javassist.Modifier;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The default configuration storage implementation which stores everything in the memory.
 * <p>
 * TODO: Event mechanism is needed so that new definitions can be added and other components are
 * notified that something has been added.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Alfred Krauss
 * 
 */
@Component
public class ConfigurationStorage implements IConfigurationStorage, InitializingBean {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The name of the property for the repository IP.
	 */
	private static final String REPOSITORY_PROPERTY = "inspectit.repository";

	/**
	 * The class pool analyzer.
	 */
	private final IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * The inheritance analyzer.
	 */
	private final IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * The repository configuration is used to store the needed information to connect to a remote
	 * CMR.
	 */
	private RepositoryConfig repository;

	/**
	 * The name of the agent.
	 */
	private String agentName;

	/**
	 * The used buffer strategy.
	 */
	private StrategyConfig bufferStrategy;

	/**
	 * The list of sending strategies. Default size is set to 1 as it's unlikely that more than one
	 * is defined.
	 */
	private List<StrategyConfig> sendingStrategies = new ArrayList<StrategyConfig>(1);

	/**
	 * The default size of the method sensor type list.
	 */
	private static final int METHOD_LIST_SIZE = 10;

	/**
	 * The list of method sensor types. Contains objects of type {@link MethodSensorTypeConfig}.
	 */
	private List<MethodSensorTypeConfig> methodSensorTypes = new ArrayList<MethodSensorTypeConfig>(METHOD_LIST_SIZE);

	/**
	 * The default size of the platform sensor type list.
	 */
	private static final int PLATFORM_LIST_SIZE = 10;

	/**
	 * The list of platform sensor types. Contains objects of type {@link PlatformSensorTypeConfig}.
	 */
	private List<PlatformSensorTypeConfig> platformSensorTypes = new ArrayList<PlatformSensorTypeConfig>(PLATFORM_LIST_SIZE);

	/**
	 * The default size of the jmx sensor type list.
	 */
	private static final int JMX_LIST_SIZE = 1;

	/**
	 * The list of jmx sensor types. Contains objects of type {@link JmxSensorTypeConfig}.
	 */
	private List<JmxSensorTypeConfig> jmxSensorTypes = new ArrayList<JmxSensorTypeConfig>(JMX_LIST_SIZE);

	/**
	 * A list containing all the sensor definitions from the configuration.
	 */
	private List<UnregisteredSensorConfig> unregisteredSensorConfigs = new ArrayList<UnregisteredSensorConfig>();

	/**
	 * A list containing all unregistered sensor definitions from the configuration.
	 */
	private List<UnregisteredJmxConfig> unregisteredJmxConfigs = new ArrayList<UnregisteredJmxConfig>();

	/**
	 * Indicates whether the exception sensor is activated or not.
	 */
	private boolean exceptionSensorActivated = false;

	/**
	 * Indicates whether try/catch instrumentation should be used to handle all exception events.
	 */
	private boolean enhancedExceptionSensorActivated = false;

	/**
	 * List of the ignore classes patterns. Classes matching these patterns should be ignored by the
	 * configuration.
	 */
	private List<IMatchPattern> ignoreClassesPatterns = new ArrayList<IMatchPattern>();

	/**
	 * The matchers that can be used to test if the ClassLoader class should be instrumented in the
	 * way that class loading is delegated if the class to be loaded is inspectIT class.
	 */
	private Collection<IMatcher> classLoaderDelegationMatchers;

	/**
	 * Default constructor which takes 2 parameter.
	 * 
	 * @param classPoolAnalyzer
	 *            The class pool analyzer used by the sensor configuration.
	 * @param inheritanceAnalyzer
	 *            The inheritance analyzer used by the sensor configuration.
	 */
	@Autowired
	public ConfigurationStorage(IClassPoolAnalyzer classPoolAnalyzer, IInheritanceAnalyzer inheritanceAnalyzer) {
		this.classPoolAnalyzer = classPoolAnalyzer;
		this.inheritanceAnalyzer = inheritanceAnalyzer;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setRepository(String host, int port) throws StorageException {
		if (null == host || "".equals(host)) {
			throw new StorageException("Repository host name cannot be null or empty!");
		}

		if (port < 1) {
			throw new StorageException("Repository port has to be greater than 0!");
		}

		// can not reset repository
		if (null == repository) {
			this.repository = new RepositoryConfig(host, port);
		}

		if (log.isInfoEnabled()) {
			log.info("Repository definition added. Host: " + host + " Port: " + port);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RepositoryConfig getRepositoryConfig() {
		return repository;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setAgentName(String name) throws StorageException {
		if (null == name || "".equals(name)) {
			throw new StorageException("Agent name cannot be null or empty!");
		}

		// don't allow reseting
		if (null == agentName) {
			agentName = name;
		}

		if (log.isInfoEnabled()) {
			log.info("Agent name set to: " + name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAgentName() {
		return agentName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBufferStrategy(String clazzName, Map<String, String> settings) throws StorageException {
		if (null == clazzName || "".equals(clazzName)) {
			throw new StorageException("Buffer strategy class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.emptyMap();
		}

		this.bufferStrategy = new StrategyConfig(clazzName, settings);

		if (log.isDebugEnabled()) {
			log.debug("Buffer strategy set to: " + clazzName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public StrategyConfig getBufferStrategyConfig() {
		return bufferStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSendingStrategy(String clazzName, Map<String, String> settings) throws StorageException {
		if (null == clazzName || "".equals(clazzName)) {
			throw new StorageException("Sending strategy class name cannot be null or empty!");
		}

		for (StrategyConfig config : sendingStrategies) {
			if (clazzName.equals(config.getClazzName())) {
				throw new StorageException("Sending strategy class is already registered!");
			}
		}

		if (null == settings) {
			settings = Collections.emptyMap();
		}

		sendingStrategies.add(new StrategyConfig(clazzName, settings));

		if (log.isDebugEnabled()) {
			log.debug("Sending strategy added: " + clazzName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<StrategyConfig> getSendingStrategyConfigs() {
		return Collections.unmodifiableList(sendingStrategies);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addMethodSensorType(String sensorTypeName, String sensorTypeClass, PriorityEnum priority, Map<String, Object> settings) throws StorageException {
		if (null == sensorTypeName || "".equals(sensorTypeName)) {
			throw new StorageException("Method sensor type name cannot be null or empty!");
		}

		if (null == sensorTypeClass || "".equals(sensorTypeClass)) {
			throw new StorageException("Method sensor type class name cannot be null or empty!");
		}

		if (null == priority) {
			throw new StorageException("Method sensor type priority cannot be null!");
		}

		if (null == settings) {
			settings = Collections.emptyMap();
		}

		MethodSensorTypeConfig sensorTypeConfig = new MethodSensorTypeConfig();
		sensorTypeConfig.setName(sensorTypeName);
		sensorTypeConfig.setClassName(sensorTypeClass);
		sensorTypeConfig.setPriority(priority);
		sensorTypeConfig.setParameters(settings);

		methodSensorTypes.add(sensorTypeConfig);

		if (log.isDebugEnabled()) {
			log.debug("Method sensor type added: " + sensorTypeName + " prio: " + priority);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MethodSensorTypeConfig> getMethodSensorTypes() {
		return Collections.unmodifiableList(methodSensorTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPlatformSensorType(String sensorTypeClass, Map<String, Object> settings) throws StorageException {
		if (null == sensorTypeClass || "".equals(sensorTypeClass)) {
			throw new StorageException("Platform sensor type class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.emptyMap();
		}

		PlatformSensorTypeConfig sensorTypeConfig = new PlatformSensorTypeConfig();
		sensorTypeConfig.setClassName(sensorTypeClass);
		sensorTypeConfig.setParameters(settings);

		platformSensorTypes.add(sensorTypeConfig);

		if (log.isInfoEnabled()) {
			log.info("Platform sensor type added: " + sensorTypeClass);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PlatformSensorTypeConfig> getPlatformSensorTypes() {
		return Collections.unmodifiableList(platformSensorTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensor(String sensorTypeName, String targetClassName, String targetMethodName, List<String> parameterList, boolean ignoreSignature, Map<String, Object> settings)
			throws StorageException {
		if (null == sensorTypeName || "".equals(sensorTypeName)) {
			throw new StorageException("Sensor type name for the sensor cannot be null or empty!");
		}

		if (null == targetClassName || "".equals(targetClassName)) {
			throw new StorageException("Target class name cannot be null or empty!");
		}

		if (null == targetMethodName || "".equals(targetMethodName)) {
			throw new StorageException("Target method name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.emptyMap();
		}

		UnregisteredSensorConfig sensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);
		sensorConfig.setTargetClassName(targetClassName);
		sensorConfig.setTargetMethodName(targetMethodName);
		if ("<init>".equals(targetMethodName)) {
			sensorConfig.setConstructor(true);
		}
		sensorConfig.setIgnoreSignature(ignoreSignature);
		sensorConfig.setParameterTypes(parameterList);
		sensorConfig.setSettings(settings);

		MethodSensorTypeConfig methodSensorTypeConfig = getMethodSensorTypeConfigForName(sensorTypeName);
		sensorConfig.setSensorTypeConfig(methodSensorTypeConfig);

		// check for a virtual definition
		if (ignoreSignature) {
			sensorConfig.setVirtual(true);
		}

		// check if we are dealing with a superclass definition
		if (settings.containsKey("superclass") && settings.get("superclass").equals("true")) {
			sensorConfig.setSuperclass(true);
		}

		// check if we are dealing with a interface definition
		if (settings.containsKey("interface") && settings.get("interface").equals("true")) {
			sensorConfig.setInterface(true);
		}

		// check for annotation
		if (settings.containsKey("annotation")) {
			sensorConfig.setAnnotationClassName((String) settings.get("annotation"));
		}

		// check if the modifiers are set
		if (settings.containsKey("modifiers")) {
			String modifiersString = (String) settings.get("modifiers");
			String separator = ",";
			StringTokenizer tokenizer = new StringTokenizer(modifiersString, separator);
			int modifiers = 0;
			while (tokenizer.hasMoreTokens()) {
				String modifier = tokenizer.nextToken().trim();
				if (modifier != null && modifier.startsWith("pub")) {
					modifiers |= Modifier.PUBLIC;
				} else if (modifier != null && modifier.startsWith("priv")) {
					modifiers |= Modifier.PRIVATE;
				} else if (modifier != null && modifier.startsWith("prot")) {
					modifiers |= Modifier.PROTECTED;
				} else if (modifier != null && modifier.startsWith("def")) {
					modifiers |= ModifierMatcher.DEFAULT;
				}
			}
			sensorConfig.setModifiers(modifiers);
		}

		if (settings.containsKey("field")) {
			@SuppressWarnings("unchecked")
			List<String> fieldAccessorList = (List<String>) settings.get("field");

			for (String fieldDefinition : fieldAccessorList) {
				String[] fieldDefinitionParts = fieldDefinition.split(";");
				String name = fieldDefinitionParts[0];
				PropertyAccessor.PropertyPathStart start = new PropertyAccessor.PropertyPathStart();
				start.setName(name);
				start.setContentType(ParameterContentType.FIELD);

				String[] steps = fieldDefinitionParts[1].split("\\.");
				PropertyAccessor.PropertyPath parentPath = start;
				for (String step : steps) {
					PropertyAccessor.PropertyPath path = new PropertyAccessor.PropertyPath();
					path.setName(step);
					parentPath.setPathToContinue(path);
					parentPath = path;
				}

				sensorConfig.getPropertyAccessorList().add(start);
			}
		}

		if (settings.containsKey("property")) {
			@SuppressWarnings("unchecked")
			List<String> propertyAccessorList = (List<String>) settings.get("property");

			for (String fieldDefinition : propertyAccessorList) {
				String[] fieldDefinitionParts = fieldDefinition.split(";");
				int position = Integer.parseInt(fieldDefinitionParts[0]);
				String name = fieldDefinitionParts[1];
				PropertyAccessor.PropertyPathStart start = new PropertyAccessor.PropertyPathStart();
				start.setName(name);
				start.setContentType(ParameterContentType.PARAM);
				start.setSignaturePosition(position);

				if (3 == fieldDefinitionParts.length) {
					String[] steps = fieldDefinitionParts[2].split("\\.");
					PropertyAccessor.PropertyPath parentPath = start;
					for (String step : steps) {
						PropertyAccessor.PropertyPath path = new PropertyAccessor.PropertyPath();
						path.setName(step);
						parentPath.setPathToContinue(path);
						parentPath = path;
					}
				}

				sensorConfig.getPropertyAccessorList().add(start);
			}
		}

		if (settings.containsKey("return") && !sensorConfig.isConstructor()) {
			@SuppressWarnings("unchecked")
			List<String> returnAccessorList = (List<String>) settings.get("return");

			for (String returnDefinition : returnAccessorList) {
				String[] returnDefinitionParts = returnDefinition.split(";");
				String name = returnDefinitionParts[0];
				PropertyAccessor.PropertyPathStart start = new PropertyAccessor.PropertyPathStart();
				start.setName(name);
				start.setContentType(ParameterContentType.RETURN);

				if (returnDefinitionParts.length > 1) {
					String[] steps = returnDefinitionParts[1].split("\\.");
					PropertyAccessor.PropertyPath parentPath = start;
					for (String step : steps) {
						PropertyAccessor.PropertyPath path = new PropertyAccessor.PropertyPath();
						path.setName(step);
						parentPath.setPathToContinue(path);
						parentPath = path;
					}
				}

				sensorConfig.getPropertyAccessorList().add(start);
			}
		}

		sensorConfig.setPropertyAccess(!sensorConfig.getPropertyAccessorList().isEmpty());

		sensorConfig.completeConfiguration();

		unregisteredSensorConfigs.add(sensorConfig);

		if (log.isDebugEnabled()) {
			log.debug("Sensor configuration added: " + sensorConfig.toString());
		}

		if (methodSensorTypeConfig.isJRebelActive()) {
			UnregisteredSensorConfig jRebelSensorConfig = JRebelUtil.getJRebelSensorConfiguration(sensorConfig, classPoolAnalyzer, inheritanceAnalyzer);
			jRebelSensorConfig.completeConfiguration();
			unregisteredSensorConfigs.add(jRebelSensorConfig);

			if (log.isDebugEnabled()) {
				log.debug("Sensor configuration for JRebel enhanced classes added: " + jRebelSensorConfig.toString());
			}
		}
	}

	/**
	 * Returns the matching {@link MethodSensorTypeConfig} for the passed name.
	 * 
	 * @param sensorTypeName
	 *            The name to look for.
	 * @return The {@link MethodSensorTypeConfig} which name is equal to the passed sensor type name
	 *         in the method parameter.
	 * @throws StorageException
	 *             Throws the storage exception if no method sensor type configuration can be found.
	 */
	private MethodSensorTypeConfig getMethodSensorTypeConfigForName(String sensorTypeName) throws StorageException {
		for (MethodSensorTypeConfig config : methodSensorTypes) {
			if (config.getName().equals(sensorTypeName)) {
				return config;
			}
		}

		throw new StorageException("Could not find method sensor type with name: " + sensorTypeName);
	}

	/**
	 * Returns the matching {@link MethodSensorTypeConfig} of the Exception Sensor for the passed
	 * name.
	 * 
	 * @param sensorTypeName
	 *            The name to look for.
	 * @return The {@link MethodSensorTypeConfig} which name is equal to the passed sensor type name
	 *         in the method parameter.
	 * @throws StorageException
	 *             Throws the storage exception if no method sensor type configuration can be found.
	 */
	private MethodSensorTypeConfig getExceptionSensorTypeConfigForName(String sensorTypeName) throws StorageException {
		for (MethodSensorTypeConfig config : methodSensorTypes) {
			if (config.getName().equals(sensorTypeName)) {
				return config;
			}
		}

		throw new StorageException("Could not find exception sensor type with name: " + sensorTypeName);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<UnregisteredSensorConfig> getUnregisteredSensorConfigs() {
		return Collections.unmodifiableList(unregisteredSensorConfigs);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MethodSensorTypeConfig> getExceptionSensorTypes() {
		// TODO ET: could also be improved by adding the configs directly to exceptionSensorTypes
		// when they are added to the methodSensorTypes
		List<MethodSensorTypeConfig> exceptionSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		for (MethodSensorTypeConfig config : methodSensorTypes) {
			if (config.getName().equals("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor")) {
				exceptionSensorTypes.add(config);
			}
		}

		return Collections.unmodifiableList(exceptionSensorTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addJmxSensorType(String sensorTypeClass, String sensorName) throws StorageException {
		if (StringUtils.isEmpty(sensorTypeClass)) {
			throw new StorageException("Jmx sensor type class name cannot be null or empty!");
		}
		JmxSensorTypeConfig sensorTypeConfig = new JmxSensorTypeConfig();
		sensorTypeConfig.setName(sensorName);
		sensorTypeConfig.setClassName(sensorTypeClass);

		jmxSensorTypes.add(sensorTypeConfig);

		if (log.isDebugEnabled()) {
			log.debug("Jmx sensor type added: " + sensorTypeClass + "Name: " + sensorName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<JmxSensorTypeConfig> getJmxSensorTypes() {
		return Collections.unmodifiableList(jmxSensorTypes);
	}

	/**
	 * Returns the matching {@link JmxSensorTypeConfig} for the passed name.
	 * 
	 * @param jmxSensorTypeConfigname
	 *            The name to look for.
	 * @return The {@link JmxSensorTypeConfig} which name is equal to the passed sensor type name in
	 *         the method parameter.
	 * @throws StorageException
	 *             Throws the storage exception if no method sensor type configuration can be found.
	 */
	private JmxSensorTypeConfig getJmxSensorTypeConfigForName(String jmxSensorTypeConfigname) throws StorageException {
		for (JmxSensorTypeConfig jmxSensorTypeConfig : jmxSensorTypes) {
			if (jmxSensorTypeConfig.getName().equals(jmxSensorTypeConfigname)) {
				return jmxSensorTypeConfig;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addUnregisteredJmxConfig(String jmxSensorTypeName, String mBeanName, String attributeName) throws StorageException {
		JmxSensorTypeConfig jstc = this.getJmxSensorTypeConfigForName(jmxSensorTypeName);
		UnregisteredJmxConfig ujc = new UnregisteredJmxConfig(jstc, mBeanName, attributeName);
		this.unregisteredJmxConfigs.add(ujc);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<UnregisteredJmxConfig> getUnregisteredJmxConfigs() {
		return this.unregisteredJmxConfigs;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorType(String sensorTypeClass, Map<String, Object> settings) throws StorageException {
		if (null == sensorTypeClass || "".equals(sensorTypeClass)) {
			throw new StorageException("Exception sensor type class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.emptyMap();
		}

		MethodSensorTypeConfig sensorTypeConfig = new MethodSensorTypeConfig();
		sensorTypeConfig.setName(sensorTypeClass);
		sensorTypeConfig.setClassName(sensorTypeClass);
		sensorTypeConfig.setParameters(settings);

		methodSensorTypes.add(sensorTypeConfig);

		if (log.isDebugEnabled()) {
			log.debug("Exception sensor type added: " + sensorTypeClass);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExceptionSensorTypeParameter(String sensorTypeName, String targetClassName, boolean isVirtual, Map<String, Object> settings) throws StorageException {
		if (null == sensorTypeName || "".equals(sensorTypeName)) {
			throw new StorageException("Sensor type name for the sensor cannot be null or empty!");
		}

		if (null == targetClassName || "".equals(targetClassName)) {
			throw new StorageException("Target class name cannot be null or empty!");
		}

		if (null == settings) {
			settings = Collections.emptyMap();
		}

		UnregisteredSensorConfig sensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);

		sensorConfig.setVirtual(isVirtual);

		// check if we are dealing with a superclass definition
		if (settings.containsKey("superclass") && settings.get("superclass").equals("true")) {
			sensorConfig.setSuperclass(true);
		}

		// check if we are dealing with a interface definition
		if (settings.containsKey("interface") && settings.get("interface").equals("true")) {
			sensorConfig.setInterface(true);
		}

		// Now set all the given parameters
		sensorConfig.setTargetClassName(targetClassName);
		sensorConfig.setSettings(settings);
		sensorConfig.setSensorTypeConfig(getExceptionSensorTypeConfigForName(sensorTypeName));
		sensorConfig.setTargetMethodName("");
		sensorConfig.setConstructor(true);
		sensorConfig.setExceptionSensorActivated(true);
		sensorConfig.setIgnoreSignature(true);
		sensorConfig.completeConfiguration();

		unregisteredSensorConfigs.add(sensorConfig);
		exceptionSensorActivated = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExceptionSensorActivated() {
		return exceptionSensorActivated;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnhancedExceptionSensorActivated(boolean isEnhanced) {
		this.enhancedExceptionSensorActivated = isEnhanced;
		if (log.isDebugEnabled()) {
			if (isEnhanced) {
				log.debug("Using enhanced exception sensor mode");
			} else {
				log.debug("Using simple exception sensor mode");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnhancedExceptionSensorActivated() {
		return enhancedExceptionSensorActivated;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IMatchPattern> getIgnoreClassesPatterns() {
		return ignoreClassesPatterns;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addIgnoreClassesPattern(String patternString) {
		ignoreClassesPatterns.add(new SimpleMatchPattern(patternString));
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<IMatcher> getClassLoaderDelegationMatchers() {
		return classLoaderDelegationMatchers;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		loadConfigurationFromJvmParameters();
		createClassLoaderDelegationMatcher();
	}

	/**
	 * Checks if the JVM parameters have the repository and agent information.
	 */
	private void loadConfigurationFromJvmParameters() {

		// check if the information about the repository and agent is provided with the JVM params
		String repositoryProperty = System.getProperty(REPOSITORY_PROPERTY);

		if (null == repositoryProperty) {
			return;
		}

		// expecting data in the form ip:port;name
		StringTokenizer tokenizer = new StringTokenizer(repositoryProperty, ";");
		if (tokenizer.countTokens() == 2) {
			// ip and host
			String[] repositoryIpHost = tokenizer.nextToken().split(":");
			if (repositoryIpHost.length == 2) {
				String repositoryIp = repositoryIpHost[0];
				String repositoryPort = repositoryIpHost[1];
				if (null != repositoryIp && !"".equals(repositoryIp) && null != repositoryPort && !"".equals(repositoryPort)) {
					log.info("Repository information found in the JVM parameters: IP=" + repositoryIp + " Port=" + repositoryPort);
					try {
						int port = Integer.parseInt(repositoryPort);
						setRepository(repositoryIp, port);
					} catch (Exception e) {
						log.warn("Repository could not be defined from the data in the JVM parameters", e);
					}
				}
			}

			// agent
			String agentName = tokenizer.nextToken();
			if (null != agentName && !"".equals(agentName)) {
				try {
					log.info("Agent name found in the JVM parameters: AgentName=" + agentName);
					setAgentName(agentName);
				} catch (Exception e) {
					log.warn("Agent name could not be defined from the data in the JVM parameters", e);
				}
			}
		}
	}

	/**
	 * Creates the {@link #classLoaderDelegationMatcher}.
	 */
	private void createClassLoaderDelegationMatcher() {
		UnregisteredSensorConfig superclassSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);
		superclassSensorConfig.setSuperclass(true);
		superclassSensorConfig.setTargetClassName("java.lang.ClassLoader");
		superclassSensorConfig.setTargetMethodName("loadClass");
		superclassSensorConfig.setParameterTypes(Collections.singletonList("java.lang.String"));
		superclassSensorConfig.setModifiers(Modifier.PUBLIC);

		UnregisteredSensorConfig directSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);
		directSensorConfig.setTargetClassName("java.lang.ClassLoader");
		directSensorConfig.setTargetMethodName("loadClass");
		directSensorConfig.setParameterTypes(Collections.singletonList("java.lang.String"));
		directSensorConfig.setModifiers(Modifier.PUBLIC);

		IMatcher superclassIMatcher = new SuperclassMatcher(inheritanceAnalyzer, classPoolAnalyzer, superclassSensorConfig);
		IMatcher directIMatcher = new DirectMatcher(classPoolAnalyzer, superclassSensorConfig);
		this.classLoaderDelegationMatchers = Arrays.<IMatcher> asList(superclassIMatcher, directIMatcher);
	}

}
