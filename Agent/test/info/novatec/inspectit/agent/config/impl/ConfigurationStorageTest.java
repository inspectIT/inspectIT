package info.novatec.inspectit.agent.config.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyZeroInteractions;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatchPattern;
import info.novatec.inspectit.agent.analyzer.impl.DirectMatcher;
import info.novatec.inspectit.agent.analyzer.impl.IndirectMatcher;
import info.novatec.inspectit.agent.analyzer.impl.InterfaceMatcher;
import info.novatec.inspectit.agent.analyzer.impl.SuperclassMatcher;
import info.novatec.inspectit.agent.analyzer.impl.ThrowableMatcher;
import info.novatec.inspectit.agent.config.PriorityEnum;
import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPath;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPathStart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.Modifier;

import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ConfigurationStorageTest extends AbstractLogSupport {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	private ConfigurationStorage configurationStorage;

	/**
	 * This method will be executed before every method is executed in here. This ensures that some
	 * tests don't modify the contents of the configuration storage.
	 */
	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws StorageException {
		configurationStorage = new ConfigurationStorage(classPoolAnalyzer, inheritanceAnalyzer);
		configurationStorage.log = LoggerFactory.getLogger(ConfigurationStorage.class);

		// name and repository
		configurationStorage.setAgentName("UnitTestAgent");
		configurationStorage.setRepository("localhost", 1099);

		// method sensor types
		Map<String, Object> settings = new HashMap<String, Object>(1);
		settings.put("mode", "optimized");
		configurationStorage.addMethodSensorType("timer", "info.novatec.inspectit.agent.sensor.method.timer.TimerSensor", PriorityEnum.MAX, settings);
		configurationStorage.addMethodSensorType("isequence", "info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceSensor", PriorityEnum.INVOC, null);

		// platform sensor types
		configurationStorage.addPlatformSensorType("info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation", null);
		configurationStorage.addPlatformSensorType("info.novatec.inspectit.agent.sensor.platform.CompilationInformation", null);
		configurationStorage.addPlatformSensorType("info.novatec.inspectit.agent.sensor.platform.RuntimeInformation", null);

		// exception sensor
		configurationStorage.addExceptionSensorType("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", null);

		// exception sensor parameters
		settings = new HashMap<String, Object>();
		settings.put("superclass", "true");
		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "java.lang.Throwable", false, settings);

		settings = new HashMap<String, Object>();
		settings.put("interface", "true");
		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "info.novatec.inspectit.agent.analyzer.test.classes.IException", false,
				settings);

		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "info.novatec.inspectit.agent.analyzer.test.classes.My*Exception", true,
				Collections.<String, Object> emptyMap());
		configurationStorage.addExceptionSensorTypeParameter("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor", "info.novatec.inspectit.agent.analyzer.test.classes.MyException", false,
				Collections.<String, Object> emptyMap());

		// sending strategies
		Map<String, String> sendingSettings = new HashMap<String, String>(1);
		sendingSettings.put("time", "5000");
		configurationStorage.addSendingStrategy("info.novatec.inspectit.agent.sending.impl.TimeStrategy", sendingSettings);

		sendingSettings = new HashMap<String, String>(1);
		sendingSettings.put("size", "10");
		configurationStorage.addSendingStrategy("info.novatec.inspectit.agent.sending.impl.ListSizeStrategy", sendingSettings);

		// buffer strategy
		configurationStorage.setBufferStrategy("info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy", null);

		// sensor definitions
		configurationStorage.addSensor("timer", "*", "*", null, true, null);

		configurationStorage.addSensor("isequence", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", null, true, null);

		List<String> parameterList = new ArrayList<String>();
		parameterList.add("java.lang.String");
		configurationStorage.addSensor("timer", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", parameterList, false, null);

		settings = new HashMap<String, Object>();
		settings.put("interface", "true");
		configurationStorage.addSensor("timer", "info.novatec.IService", "*Service", null, true, settings);

		settings = new HashMap<String, Object>();
		settings.put("superclass", "true");
		configurationStorage.addSensor("isequence", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", null, true, settings);

		Map<String, Object> fieldSettings = new HashMap<String, Object>();
		List<String> list = new ArrayList<String>();
		list.add("LastOutput;jlbOutput.text");
		fieldSettings.put("field", list);
		configurationStorage.addSensor("timer", "*", "*", null, true, fieldSettings);

		fieldSettings = new HashMap<String, Object>();
		list = new ArrayList<String>();
		list.add("0;Source;msg");
		fieldSettings.put("property", list);
		configurationStorage.addSensor("timer", "*", "*", null, true, fieldSettings);

		settings = new HashMap<String, Object>();
		settings.put("annotation", "javax.ejb.StatelessBean");
		configurationStorage.addSensor("isequence", "info.novatec.inspectitsamples.calculator.Calculator", "actionPerformed", null, false, settings);

		settings = new HashMap<String, Object>();
		settings.put("modifiers", "pub,prot");
		configurationStorage.addSensor("timer", "*", "*", null, true, settings);

		configurationStorage.addIgnoreClassesPattern("info.novatec.*");
	}

	@Test()
	public void agentNameCheck() {
		assertThat(configurationStorage.getAgentName(), is(equalTo("UnitTestAgent")));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setNullAgentName() throws StorageException {
		configurationStorage.setAgentName(null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setEmptyAgentName() throws StorageException {
		configurationStorage.setAgentName("");

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void repositoryCheck() {
		assertThat(configurationStorage.getRepositoryConfig().getHost(), is(equalTo("localhost")));
		assertThat(configurationStorage.getRepositoryConfig().getPort(), is(equalTo(1099)));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setNullRepositoryHost() throws StorageException {
		configurationStorage.setRepository(null, 1099);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setEmptyRepositoryHost() throws StorageException {
		configurationStorage.setRepository("", 1099);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void resetRepositoryNotAllowed() throws StorageException {
		configurationStorage.setRepository("localhost1", 1200);

		assertThat(configurationStorage.getRepositoryConfig().getHost(), is(equalTo("localhost")));
		assertThat(configurationStorage.getRepositoryConfig().getPort(), is(equalTo(1099)));
	}

	@Test
	public void resetAgentnameNotAllowed() throws StorageException {
		configurationStorage.setAgentName("agent1");

		assertThat(configurationStorage.getAgentName(), is(equalTo("UnitTestAgent")));
	}

	@Test
	public void methodSensorTypesCheck() {
		List<MethodSensorTypeConfig> configs = configurationStorage.getMethodSensorTypes();
		assertThat(configs, is(notNullValue()));
		assertThat(configs, hasSize(3));

		// first
		MethodSensorTypeConfig config = configs.get(0);
		assertThat(config.getClassName(), is(equalTo("info.novatec.inspectit.agent.sensor.method.timer.TimerSensor")));
		assertThat(config.getName(), is(equalTo("timer")));
		assertThat(config.getParameters(), is(notNullValue()));
		Map<String, Object> settings = config.getParameters();
		assertThat(settings.size(), is(1));
		assertThat(settings, hasKey("mode"));
		assertThat(settings, hasEntry("mode", (Object) "optimized"));
		assertThat(config.getPriority(), is(equalTo(PriorityEnum.MAX)));
		assertThat(config.getSensorType(), is(nullValue()));

		// second
		config = configs.get(1);
		assertThat(config.getClassName(), is(equalTo("info.novatec.inspectit.agent.sensor.method.invocationsequence.InvocationSequenceSensor")));
		assertThat(config.getName(), is(equalTo("isequence")));
		assertThat(config.getParameters(), is(notNullValue()));
		assertThat(config.getParameters().size(), is(0));
		assertThat(config.getPriority(), is(equalTo(PriorityEnum.INVOC)));
		assertThat(config.getSensorType(), is(nullValue()));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullMethodSensorTypeName() throws StorageException {
		configurationStorage.addMethodSensorType(null, "xxx", PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyMethodSensorTypeName() throws StorageException {
		configurationStorage.addMethodSensorType("", "xxx", PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullMethodSensorTypeClass() throws StorageException {
		configurationStorage.addMethodSensorType("xxx", null, PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyMethodSensorTypeClass() throws StorageException {
		configurationStorage.addMethodSensorType("xxx", "", PriorityEnum.NORMAL, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullMethodSensorTypePriority() throws StorageException {
		configurationStorage.addMethodSensorType("xxx", "xxx", null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void platformSensorTypeCheck() {
		List<PlatformSensorTypeConfig> configs = configurationStorage.getPlatformSensorTypes();
		assertThat(configs, is(notNullValue()));
		assertThat(configs, hasSize(3));

		// first
		PlatformSensorTypeConfig config = configs.get(0);
		assertThat(config.getClassName(), is(equalTo("info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation")));
		assertThat(config.getParameters(), is(notNullValue()));
		assertThat(config.getParameters().size(), is(0));
		assertThat(config.getSensorType(), is(nullValue()));

		// second
		config = configs.get(1);
		assertThat(config.getClassName(), is(equalTo("info.novatec.inspectit.agent.sensor.platform.CompilationInformation")));
		assertThat(config.getParameters(), is(notNullValue()));
		assertThat(config.getParameters().size(), is(0));
		assertThat(config.getSensorType(), is(nullValue()));

		// third
		config = configs.get(2);
		assertThat(config.getClassName(), is(equalTo("info.novatec.inspectit.agent.sensor.platform.RuntimeInformation")));
		assertThat(config.getParameters(), is(notNullValue()));
		assertThat(config.getParameters().size(), is(0));
		assertThat(config.getSensorType(), is(nullValue()));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullPlatformSensorTypeClass() throws StorageException {
		configurationStorage.addPlatformSensorType(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyPlatformSensorTypeClass() throws StorageException {
		configurationStorage.addPlatformSensorType("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void exceptionSensorCheck() {
		List<MethodSensorTypeConfig> configs = configurationStorage.getExceptionSensorTypes();
		assertThat(configs, is(notNullValue()));
		assertThat(configs, hasSize(1));

		MethodSensorTypeConfig config = configs.get(0);
		assertThat(config.getClassName(), is(equalTo("info.novatec.inspectit.agent.sensor.exception.ExceptionSensor")));
		assertThat(config.getParameters(), is(notNullValue()));
		assertThat(config.getParameters().size(), is(0));
		assertThat(config.getSensorType(), is(nullValue()));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullExceptionSensor() throws StorageException {
		configurationStorage.addExceptionSensorType(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);

	}

	@Test
	public void exceptionSensorParameterCheck() {
		List<UnregisteredSensorConfig> configs = configurationStorage.getUnregisteredSensorConfigs();
		assertThat(configs, is(notNullValue()));
		assertThat(configs, hasSize(13));

		// first
		UnregisteredSensorConfig config = configs.get(0);
		assertThat(config.getTargetClassName(), is(equalTo("java.lang.Throwable")));
		assertThat(config.getTargetMethodName(), is(equalTo("")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.isConstructor(), is(true));
		assertThat(config.isIgnoreSignature(), is(true));
		assertThat(config.isInterface(), is(false));
		assertThat(config.isVirtual(), is(false));
		assertThat(config.isSuperclass(), is(true));
		assertThat(config.getMatcher(), is(instanceOf(ThrowableMatcher.class)));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(1));

		// second
		config = configs.get(1);
		assertThat(config.getTargetClassName(), is(equalTo("info.novatec.inspectit.agent.analyzer.test.classes.IException")));
		assertThat(config.getTargetMethodName(), is(equalTo("")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.isConstructor(), is(true));
		assertThat(config.isIgnoreSignature(), is(true));
		assertThat(config.isInterface(), is(true));
		assertThat(config.isVirtual(), is(false));
		assertThat(config.isSuperclass(), is(false));
		assertThat(config.getMatcher(), is(instanceOf(ThrowableMatcher.class)));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(1));

		// third
		config = configs.get(2);
		assertThat(config.getTargetClassName(), is(equalTo("info.novatec.inspectit.agent.analyzer.test.classes.My*Exception")));
		assertThat(config.getTargetMethodName(), is(equalTo("")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.isConstructor(), is(true));
		assertThat(config.isIgnoreSignature(), is(true));
		assertThat(config.isInterface(), is(false));
		assertThat(config.isVirtual(), is(true));
		assertThat(config.isSuperclass(), is(false));
		assertThat(config.getMatcher(), is(instanceOf(ThrowableMatcher.class)));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(0));

		// fourth
		config = configs.get(3);
		assertThat(config.getTargetClassName(), is(equalTo("info.novatec.inspectit.agent.analyzer.test.classes.MyException")));
		assertThat(config.getTargetMethodName(), is(equalTo("")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.isConstructor(), is(true));
		assertThat(config.isIgnoreSignature(), is(true));
		assertThat(config.isInterface(), is(false));
		assertThat(config.isVirtual(), is(false));
		assertThat(config.isSuperclass(), is(false));
		assertThat(config.getMatcher(), is(instanceOf(ThrowableMatcher.class)));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(0));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);

	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptyExceptionSensor() throws StorageException {
		configurationStorage.addExceptionSensorType("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);

	}

	@Test
	public void sendingStrategiesCheck() {
		List<StrategyConfig> strategies = configurationStorage.getSendingStrategyConfigs();
		assertThat(strategies, is(notNullValue()));
		assertThat(strategies, hasSize(2));

		// first
		StrategyConfig config = strategies.get(0);
		assertThat(config.getClazzName(), is(equalTo("info.novatec.inspectit.agent.sending.impl.TimeStrategy")));
		assertThat(config.getSettings(), is(notNullValue()));
		Map<String, String> settings = config.getSettings();
		assertThat(settings.size(), is(1));
		assertThat(settings, hasKey("time"));
		assertThat(settings, hasEntry("time", "5000"));

		// second
		config = strategies.get(1);
		assertThat(config.getClazzName(), is(equalTo("info.novatec.inspectit.agent.sending.impl.ListSizeStrategy")));
		assertThat(config.getSettings(), is(notNullValue()));
		settings = config.getSettings();
		assertThat(settings.size(), is(1));
		assertThat(settings, hasKey("size"));
		assertThat(settings, hasEntry("size", "10"));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSendingStrategy() throws StorageException {
		configurationStorage.addSendingStrategy(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySendingStrategy() throws StorageException {
		configurationStorage.addSendingStrategy("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void bufferStrategyCheck() {
		StrategyConfig config = configurationStorage.getBufferStrategyConfig();
		assertThat(config, is(notNullValue()));

		assertThat(config.getClazzName(), is(equalTo("info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy")));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(0));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setNullBufferStrategy() throws StorageException {
		configurationStorage.setBufferStrategy(null, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void setEmptyBufferStrategy() throws StorageException {
		configurationStorage.setBufferStrategy("", null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void sensorCheck() {
		List<UnregisteredSensorConfig> configs = configurationStorage.getUnregisteredSensorConfigs();
		assertThat(configs, is(notNullValue()));
		assertThat(configs, hasSize(13));

		// the first 4 configs are the ones from the exception sensor
		// first
		UnregisteredSensorConfig config = configs.get(4);
		assertThat(config.getSensorTypeConfig().getName(), is(equalTo("timer")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.getTargetClassName(), is(equalTo("*")));
		assertThat(config.getTargetMethodName(), is(equalTo("*")));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(0));
		assertThat(config.getPropertyAccessorList(), is(notNullValue()));
		assertThat(config.getPropertyAccessorList(), is(empty()));
		assertThat(config.getMatcher(), is(instanceOf(IndirectMatcher.class)));

		// second
		config = configs.get(5);
		assertThat(config.getSensorTypeConfig().getName(), is(equalTo("isequence")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.getTargetClassName(), is(equalTo("info.novatec.inspectitsamples.calculator.Calculator")));
		assertThat(config.getTargetMethodName(), is(equalTo("actionPerformed")));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(0));
		assertThat(config.getPropertyAccessorList(), is(notNullValue()));
		assertThat(config.getPropertyAccessorList(), is(empty()));
		assertThat(config.getMatcher(), is(instanceOf(IndirectMatcher.class)));

		// third
		config = configs.get(6);
		assertThat(config.getSensorTypeConfig().getName(), is(equalTo("timer")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.getTargetClassName(), is(equalTo("info.novatec.inspectitsamples.calculator.Calculator")));
		assertThat(config.getTargetMethodName(), is(equalTo("actionPerformed")));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), hasSize(1));
		assertThat(config.getParameterTypes(), hasItem("java.lang.String"));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(0));
		assertThat(config.getPropertyAccessorList(), is(notNullValue()));
		assertThat(config.getPropertyAccessorList(), is(empty()));
		assertThat(config.getMatcher(), is(instanceOf(DirectMatcher.class)));

		// fourth
		config = configs.get(7);
		assertThat(config.getSensorTypeConfig().getName(), is(equalTo("timer")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.getTargetClassName(), is(equalTo("info.novatec.IService")));
		assertThat(config.getTargetMethodName(), is(equalTo("*Service")));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(1));
		assertThat(config.getSettings(), hasKey("interface"));
		assertThat(config.getSettings(), hasEntry("interface", (Object) "true"));
		assertThat(config.getPropertyAccessorList(), is(notNullValue()));
		assertThat(config.getPropertyAccessorList(), is(empty()));
		assertThat(config.getMatcher(), is(instanceOf(InterfaceMatcher.class)));

		// fifth
		config = configs.get(8);
		assertThat(config.getSensorTypeConfig().getName(), is(equalTo("isequence")));
		assertThat(config.getTargetPackageName(), is(nullValue()));
		assertThat(config.getTargetClassName(), is(equalTo("info.novatec.inspectitsamples.calculator.Calculator")));
		assertThat(config.getTargetMethodName(), is(equalTo("actionPerformed")));
		assertThat(config.getParameterTypes(), is(notNullValue()));
		assertThat(config.getParameterTypes(), is(empty()));
		assertThat(config.getSettings(), is(notNullValue()));
		assertThat(config.getSettings().size(), is(1));
		assertThat(config.getSettings(), hasKey("superclass"));
		assertThat(config.getSettings(), hasEntry("superclass", (Object) "true"));
		assertThat(config.getPropertyAccessorList(), is(notNullValue()));
		assertThat(config.getPropertyAccessorList(), is(empty()));
		assertThat(config.getMatcher(), is(instanceOf(SuperclassMatcher.class)));

		// sixth
		config = configs.get(9);
		assertThat(config.getPropertyAccessorList(), hasSize(1));
		assertThat(config.getPropertyAccessorList().get(0), is(instanceOf(PropertyPathStart.class)));
		PropertyPathStart start = (PropertyPathStart) config.getPropertyAccessorList().get(0);
		assertThat(start.getName(), is(equalTo("LastOutput")));
		assertThat(start.getSignaturePosition(), is(-1));
		assertThat(start.getPathToContinue(), is(instanceOf(PropertyPath.class)));
		assertThat(start.getPathToContinue().getName(), is(equalTo("jlbOutput")));
		assertThat(start.getPathToContinue().getPathToContinue(), is(instanceOf(PropertyPath.class)));
		assertThat(start.getPathToContinue().getPathToContinue().getName(), is(equalTo("text")));
		assertThat(start.getPathToContinue().getPathToContinue().getPathToContinue(), is(nullValue()));

		// seventh
		config = configs.get(10);
		assertThat(config.getPropertyAccessorList(), hasSize(1));
		assertThat(config.getPropertyAccessorList().get(0), is(instanceOf(PropertyPathStart.class)));
		start = (PropertyPathStart) config.getPropertyAccessorList().get(0);
		assertThat(start.getName(), is(equalTo("Source")));
		assertThat(start.getSignaturePosition(), is(0));
		assertThat(start.getPathToContinue(), is(instanceOf(PropertyPath.class)));
		assertThat(start.getPathToContinue().getName(), is(equalTo("msg")));
		assertThat(start.getPathToContinue().getPathToContinue(), is(nullValue()));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSensorTypeName() throws StorageException {
		configurationStorage.addSensor(null, "xxx", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySensorTypeName() throws StorageException {
		configurationStorage.addSensor("", "xxx", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSensorTargetClassName() throws StorageException {
		configurationStorage.addSensor("xxx", null, "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySensorTargetClassName() throws StorageException {
		configurationStorage.addSensor("xxx", "", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addNullSensorTargetMethodName() throws StorageException {
		configurationStorage.addSensor("xxx", "xxx", null, null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addEmptySensorTargetMethodName() throws StorageException {
		configurationStorage.addSensor("xxx", "xxx", "", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test(expectedExceptions = { StorageException.class })
	public void addSensorInvalidSensorTypeName() throws StorageException {
		configurationStorage.addSensor("xxx", "xxx", "xxx", null, false, null);

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void annotationCheck() {
		List<UnregisteredSensorConfig> configs = configurationStorage.getUnregisteredSensorConfigs();
		assertThat(configs, is(notNullValue()));

		UnregisteredSensorConfig annotationConfig = configs.get(11);
		assertThat(annotationConfig.getAnnotationClassName(), is(notNullValue()));
		assertThat(annotationConfig.getAnnotationClassName(), is(equalTo("javax.ejb.StatelessBean")));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void modifiersCheck() {
		List<UnregisteredSensorConfig> configs = configurationStorage.getUnregisteredSensorConfigs();
		assertThat(configs, is(notNullValue()));

		// 11 is index of config with modifiers
		UnregisteredSensorConfig configWithModifiers = configs.get(12);
		assertThat(configWithModifiers.getSettings(), hasKey("modifiers"));
		assertThat(configWithModifiers.getModifiers(), is(not(0)));
		assertThat(Modifier.isPublic(configWithModifiers.getModifiers()), is(true));
		assertThat(Modifier.isProtected(configWithModifiers.getModifiers()), is(true));

		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}

	@Test
	public void ignoreClassesCheck() {
		List<IMatchPattern> ignorePatterns = configurationStorage.getIgnoreClassesPatterns();
		assertThat(ignorePatterns, is(notNullValue()));
		assertThat(ignorePatterns, is(not(empty())));
		verifyZeroInteractions(classPoolAnalyzer, inheritanceAnalyzer);
	}
}
