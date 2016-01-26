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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
			properties.put(ConfigurationStorage.AGENT_NAME_PROPERTY,  agentName);

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
