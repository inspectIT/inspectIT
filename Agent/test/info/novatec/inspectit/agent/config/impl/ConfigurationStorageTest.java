package info.novatec.inspectit.agent.config.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.config.StorageException;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.ExceptionSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.StrategyConfig;
import info.novatec.inspectit.pattern.IMatchPattern;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ConfigurationStorageTest extends TestBase {

	@InjectMocks
	protected ConfigurationStorage configurationStorage;

	@Mock
	protected AgentConfiguration agentConfiguration;

	@Mock
	protected Logger log;

	public class AgentName extends ConfigurationStorageTest {

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

	public class Repository extends ConfigurationStorageTest {

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

	public class SendingStrategy extends ConfigurationStorageTest {

		@Test
		public void strategyCheck() throws StorageException {
			StrategyConfig strategyConfig = mock(StrategyConfig.class);
			when(agentConfiguration.getSendingStrategyConfig()).thenReturn(strategyConfig);

			List<StrategyConfig> config = configurationStorage.getSendingStrategyConfigs();

			assertThat(config, hasSize(1));
			assertThat(config, hasItem(strategyConfig));
		}

		@Test(expectedExceptions = { StorageException.class })
		public void strategyNotDefined() throws StorageException {
			when(agentConfiguration.getSendingStrategyConfig()).thenReturn(null);

			configurationStorage.getSendingStrategyConfigs();
		}
	}

	public class BufferStrategy extends ConfigurationStorageTest {

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

	public class MethodSensorTypes extends ConfigurationStorageTest {

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

	public class ExceptionSensorTypes extends ConfigurationStorageTest {

		@Test
		public void defined() throws StorageException {
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);

			List<MethodSensorTypeConfig> exceptionSensorTypes = configurationStorage.getExceptionSensorTypes();

			assertThat(exceptionSensorTypes, hasSize(1));
			assertThat(exceptionSensorTypes, hasItem(exceptionSensorTypeConfig));
		}

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(null);

			List<MethodSensorTypeConfig> exceptionSensorTypes = configurationStorage.getExceptionSensorTypes();

			assertThat(exceptionSensorTypes, is(empty()));
		}
	}

	public class PlatformSensorTypes extends ConfigurationStorageTest {

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

	public class ExceptionSensorInfo extends ConfigurationStorageTest {

		@Test
		public void notDefined() throws StorageException {
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(null);

			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();
			boolean enhancedExceptionSensorActivated = configurationStorage.isEnhancedExceptionSensorActivated();

			assertThat(exceptionSensorActivated, is(false));
			assertThat(enhancedExceptionSensorActivated, is(false));
		}

		@Test
		public void notEnchanced() throws StorageException {
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);
			when(exceptionSensorTypeConfig.isEnhanced()).thenReturn(false);

			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();
			boolean enhancedExceptionSensorActivated = configurationStorage.isEnhancedExceptionSensorActivated();

			assertThat(exceptionSensorActivated, is(true));
			assertThat(enhancedExceptionSensorActivated, is(false));
		}

		@Test
		public void enchanced() throws StorageException {
			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);
			when(exceptionSensorTypeConfig.isEnhanced()).thenReturn(true);

			boolean exceptionSensorActivated = configurationStorage.isExceptionSensorActivated();
			boolean enhancedExceptionSensorActivated = configurationStorage.isEnhancedExceptionSensorActivated();

			assertThat(exceptionSensorActivated, is(true));
			assertThat(enhancedExceptionSensorActivated, is(true));
		}
	}

	public class IgnoreClasses extends ConfigurationStorageTest {

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
