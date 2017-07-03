package rocks.inspectit.agent.java.config.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.spring.SpringConfiguration;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.StrategyConfig;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class ConfigurationStorageTest extends TestBase {

	@InjectMocks
	ConfigurationStorage configurationStorage;

	@Mock
	SpringConfiguration springConfiguration;

	@Mock
	AgentConfig agentConfiguration;

	@Mock
	Logger log;

	public class AfterPropertiesSet extends ConfigurationStorageTest {

		@Test
		public void repositoryAndAgentNameSet() throws Exception {
			String agentName = "agentName";
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, agentName);

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getRepositoryConfig().getHost(), is("localhost"));
			assertThat(configurationStorage.getRepositoryConfig().getPort(), is(8000));
			assertThat(configurationStorage.getAgentName(), is(agentName));
		}

		@Test(expectedExceptions = BeanInitializationException.class)
		public void nothingSet() throws Exception {
			Properties properties = System.getProperties();
			properties.remove(ConfigurationStorage.AGENT_NAME_PROPERTY);
			properties.remove(ConfigurationStorage.REPOSITORY_PROPERTY);

			configurationStorage.afterPropertiesSet();
		}

		@Test(expectedExceptions = BeanInitializationException.class)
		public void agentOnlySet() throws Exception {
			String agentName = "agentName";
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, agentName);
			properties.remove(ConfigurationStorage.REPOSITORY_PROPERTY);

			configurationStorage.afterPropertiesSet();
		}

		@Test
		public void agentEmptySet() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(not("")));
		}

		@Test
		public void repositoryOnlySet() throws Exception {
			Properties properties = System.getProperties();
			properties.remove(ConfigurationStorage.AGENT_NAME_PROPERTY);
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			configurationStorage.afterPropertiesSet();
			assertThat(configurationStorage.getRepositoryConfig().getHost(), is("localhost"));
			assertThat(configurationStorage.getRepositoryConfig().getPort(), is(8000));
			assertThat(configurationStorage.getAgentName(), is(not(nullValue())));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsAtTheEndOfTheNameAndItIsNotRecognize() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "agentName_$[test]");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("agentName_NA")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsAtTheEndOfTheNameAndItIsFromSystemProperties() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "agentName_$[systemProperty]");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");
			properties.put("systemProperty", "systemPropertyValue");

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("agentName_systemPropertyValue")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsAtTheEndOfTheNameAndItIsFromSystemEnvironment() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "agentName_$[environmentVariable]");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			try {
				Class[] classes = Collections.class.getDeclaredClasses();
				Map<String, String> environment = System.getenv();
				for (Class cl : classes) {
					if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
						Field field = cl.getDeclaredField("m");
						field.setAccessible(true);
						Object environmentObject = field.get(environment);
						Map<String, String> map = (Map<String, String>) environmentObject;
						map.put("environmentVariable", "environmentVariableValue");
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("agentName_environmentVariableValue")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsInTheMiddleOfTheNameAndItIsNotRecognize() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "agent_$[test]_Name");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("agent_NA_Name")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsInTheMiddleOfTheNameAndItIsFromSystemProperties() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "agent_$[systemProperty]_Name");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");
			properties.put("systemProperty", "systemPropertyValue");

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("agent_systemPropertyValue_Name")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsInTheMiddleOfTheNameAndItIsFromSystemEnvironment() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "agent_$[environmentVariable]_Name");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			try {
				Class[] classes = Collections.class.getDeclaredClasses();
				Map<String, String> environment = System.getenv();
				for (Class cl : classes) {
					if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
						Field field = cl.getDeclaredField("m");
						field.setAccessible(true);
						Object environmentObject = field.get(environment);
						Map<String, String> map = (Map<String, String>) environmentObject;
						map.put("environmentVariable", "environmentVariableValue");
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("agent_environmentVariableValue_Name")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsAtTheBegginingOfTheNameAndItIsNotRecognize() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "$[test]_agentName");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("NA_agentName")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsAtTheBegginingOfTheNameAndItIsFromSystemProperties() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "$[systemProperty]_agentName");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");
			properties.put("systemProperty", "systemPropertyValue");

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("systemPropertyValue_agentName")));
		}

		@Test
		public void agentNameIsCorrectIfThePatternIsAtTheBegginingOfTheNameAndItIsFromSystemEnvironment() throws Exception {
			Properties properties = System.getProperties();
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY, "$[environmentVariable]_agentName");
			properties.put(ConfigurationStorage.REPOSITORY_PROPERTY, "localhost:8000");

			try {
				Class[] classes = Collections.class.getDeclaredClasses();
				Map<String, String> environment = System.getenv();
				for (Class cl : classes) {
					if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
						Field field = cl.getDeclaredField("m");
						field.setAccessible(true);
						Object environmentObject = field.get(environment);
						Map<String, String> map = (Map<String, String>) environmentObject;
						map.put("environmentVariable", "environmentVariableValue");
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			configurationStorage.afterPropertiesSet();

			assertThat(configurationStorage.getAgentName(), is(("environmentVariableValue_agentName")));
		}
	}

	public class GetAgentName extends ConfigurationStorageTest {

		@Test()
		public void check() throws StorageException {
			configurationStorage.setAgentName("UnitTestAgent");

			String agentName = configurationStorage.getAgentName();

			assertThat(agentName, is(equalTo("UnitTestAgent")));
		}

		@Test(expectedExceptions = { StorageException.class })
		public void setNullName() throws StorageException {
			configurationStorage.setAgentName(null);
		}

		@Test(expectedExceptions = { StorageException.class })
		public void setEmptyName() throws StorageException {
			configurationStorage.setAgentName("");
		}

		@Test
		public void resetNameNotAllowed() throws StorageException {
			configurationStorage.setAgentName("UnitTestAgent");
			configurationStorage.setAgentName("agent1");

			String agentName = configurationStorage.getAgentName();

			assertThat(agentName, is(equalTo("UnitTestAgent")));
		}
	}

	public class GetRepositoryConfig extends ConfigurationStorageTest {

		@Test
		public void check() throws StorageException {
			configurationStorage.setRepository("localhost", 1099);

			String host = configurationStorage.getRepositoryConfig().getHost();
			int port = configurationStorage.getRepositoryConfig().getPort();

			assertThat(host, is(equalTo("localhost")));
			assertThat(port, is(equalTo(1099)));
		}

		@Test(expectedExceptions = { StorageException.class })
		public void setNullHost() throws StorageException {
			configurationStorage.setRepository(null, 1099);
		}

		@Test(expectedExceptions = { StorageException.class })
		public void setEmptyHost() throws StorageException {
			configurationStorage.setRepository("", 1099);
		}

		@Test
		public void resetNotAllowed() throws StorageException {
			configurationStorage.setRepository("localhost", 1099);
			configurationStorage.setRepository("localhost1", 1200);

			String host = configurationStorage.getRepositoryConfig().getHost();
			int port = configurationStorage.getRepositoryConfig().getPort();

			assertThat(host, is(equalTo("localhost")));
			assertThat(port, is(equalTo(1099)));
		}
	}

	public class GetSendingStrategyConfig extends ConfigurationStorageTest {

		@Test
		public void strategyCheck() throws StorageException {
			StrategyConfig strategyConfig = mock(StrategyConfig.class);
			when(agentConfiguration.getSendingStrategyConfig()).thenReturn(strategyConfig);

			StrategyConfig config = configurationStorage.getSendingStrategyConfig();

			assertThat(config, is(strategyConfig));
		}

		@Test(expectedExceptions = { StorageException.class })
		public void strategyNotDefined() throws StorageException {
			when(agentConfiguration.getSendingStrategyConfig()).thenReturn(null);

			configurationStorage.getSendingStrategyConfig();
		}
	}

	public class GetBufferStrategyConfig extends ConfigurationStorageTest {

		@Test
		public void strategyCheck() throws StorageException {
			StrategyConfig strategyConfig = mock(StrategyConfig.class);
			when(agentConfiguration.getBufferStrategyConfig()).thenReturn(strategyConfig);

			StrategyConfig config = configurationStorage.getBufferStrategyConfig();

			assertThat(config, is(strategyConfig));
		}

		@Test(expectedExceptions = { StorageException.class })
		public void strategyNotDefined() throws StorageException {
			when(agentConfiguration.getBufferStrategyConfig()).thenReturn(null);

			configurationStorage.getBufferStrategyConfig();
		}
	}

	public class GetMethodSensorTypes extends ConfigurationStorageTest {

		@Test
		public void defined() throws StorageException {
			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(agentConfiguration.getMethodSensorTypeConfigs()).thenReturn(Collections.singletonList(methodSensorTypeConfig));

			List<MethodSensorTypeConfig> methodSensorTypes = configurationStorage.getMethodSensorTypes();

			assertThat(methodSensorTypes, hasSize(1));
			assertThat(methodSensorTypes, hasItem(methodSensorTypeConfig));
		}

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getMethodSensorTypeConfigs()).thenReturn(null);

			List<MethodSensorTypeConfig> methodSensorTypes = configurationStorage.getMethodSensorTypes();

			assertThat(methodSensorTypes, is(empty()));
		}

		@Test
		public void includesExceptionSensor() throws StorageException {
			when(agentConfiguration.getMethodSensorTypeConfigs()).thenReturn(null);
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);

			List<MethodSensorTypeConfig> methodSensorTypes = configurationStorage.getMethodSensorTypes();

			assertThat(methodSensorTypes, hasSize(1));
			assertThat(methodSensorTypes, hasItem(exceptionSensorTypeConfig));
		}

		@Test
		public void includesSpecialSensors() throws StorageException {
			when(agentConfiguration.getMethodSensorTypeConfigs()).thenReturn(null);
			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(agentConfiguration.getSpecialMethodSensorTypeConfigs()).thenReturn(Collections.singletonList(methodSensorTypeConfig));

			List<MethodSensorTypeConfig> methodSensorTypes = configurationStorage.getMethodSensorTypes();

			assertThat(methodSensorTypes, hasSize(1));
			assertThat(methodSensorTypes, hasItem(methodSensorTypeConfig));
		}
	}

	public class GetExceptionSensorType extends ConfigurationStorageTest {

		@Test
		public void defined() throws StorageException {
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);

			ExceptionSensorTypeConfig config = configurationStorage.getExceptionSensorType();

			assertThat(config, is(exceptionSensorTypeConfig));
		}

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(null);

			ExceptionSensorTypeConfig config = configurationStorage.getExceptionSensorType();

			assertThat(config, is(nullValue()));
		}
	}

	public class GetJmxSensorTypes extends ConfigurationStorageTest {

		@Test
		public void defined() throws StorageException {
			JmxSensorTypeConfig jmxSensorTypeConfig = mock(JmxSensorTypeConfig.class);
			when(agentConfiguration.getJmxSensorTypeConfig()).thenReturn(jmxSensorTypeConfig);

			List<JmxSensorTypeConfig> jmxSensorTypes = configurationStorage.getJmxSensorTypes();

			assertThat(jmxSensorTypes, hasSize(1));
			assertThat(jmxSensorTypes, hasItem(jmxSensorTypeConfig));
		}

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getJmxSensorTypeConfig()).thenReturn(null);

			List<JmxSensorTypeConfig> jmxSensorTypes = configurationStorage.getJmxSensorTypes();

			assertThat(jmxSensorTypes, is(empty()));
		}
	}

	public class GetPlatformSensorTypes extends ConfigurationStorageTest {

		@Test
		public void defined() throws StorageException {
			PlatformSensorTypeConfig platformSensorTypeConfig = mock(PlatformSensorTypeConfig.class);
			when(agentConfiguration.getPlatformSensorTypeConfigs()).thenReturn(Collections.singletonList(platformSensorTypeConfig));

			List<PlatformSensorTypeConfig> platformSensorTypes = configurationStorage.getPlatformSensorTypes();

			assertThat(platformSensorTypes, hasSize(1));
			assertThat(platformSensorTypes, hasItem(platformSensorTypeConfig));
		}

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getPlatformSensorTypeConfigs()).thenReturn(null);

			List<PlatformSensorTypeConfig> platformSensorTypes = configurationStorage.getPlatformSensorTypes();

			assertThat(platformSensorTypes, is(empty()));
		}
	}

	public class IsExceptionSensorActivated extends ConfigurationStorageTest {

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(null);

			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();

			assertThat(exceptionSensorActivated, is(false));
		}

		@Test
		public void defined() throws StorageException {
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);

			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();

			assertThat(exceptionSensorActivated, is(true));
		}

	}

	public class IsEnhancedExceptionSensorActivated extends ConfigurationStorageTest {

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(null);

			boolean enhancedExceptionSensorActivated = configurationStorage.isEnhancedExceptionSensorActivated();

			assertThat(enhancedExceptionSensorActivated, is(false));
		}

		@Test
		public void notEnchanced() throws StorageException {
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);
			when(exceptionSensorTypeConfig.isEnhanced()).thenReturn(false);

			boolean enhancedExceptionSensorActivated = configurationStorage.isEnhancedExceptionSensorActivated();

			assertThat(enhancedExceptionSensorActivated, is(false));
		}

		@Test
		public void enchanced() throws StorageException {
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);
			when(exceptionSensorTypeConfig.isEnhanced()).thenReturn(true);

			boolean enhancedExceptionSensorActivated = configurationStorage.isEnhancedExceptionSensorActivated();

			assertThat(enhancedExceptionSensorActivated, is(true));
		}
	}

	public class GetIgnoreClassesPatterns extends ConfigurationStorageTest {

		@Test
		public void check() throws StorageException {
			IMatchPattern pattern = mock(IMatchPattern.class);
			when(agentConfiguration.getExcludeClassesPatterns()).thenReturn(Collections.singleton(pattern));

			Collection<IMatchPattern> ignorePatterns = configurationStorage.getIgnoreClassesPatterns();

			assertThat(ignorePatterns, is(notNullValue()));
			assertThat(ignorePatterns, hasItem(pattern));
		}
	}
}
