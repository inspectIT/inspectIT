package info.novatec.inspectit.cmr.instrumentation.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.exclude.ExcludeRule;
import info.novatec.inspectit.ci.sensor.exception.IExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.IPlatformSensorConfig;
import info.novatec.inspectit.ci.strategy.IStrategyConfig;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.config.PriorityEnum;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.ExceptionSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.StrategyConfig;
import info.novatec.inspectit.pattern.EqualsMatchPattern;
import info.novatec.inspectit.pattern.IMatchPattern;
import info.novatec.inspectit.pattern.WildcardMatchPattern;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ConfigurationCreatorTest extends TestBase {

	@InjectMocks
	protected ConfigurationCreator creator;

	@Mock
	protected Environment environment;

	@Mock
	protected IRegistrationService registrationService;

	@Mock
	protected ConfigurationResolver configurationResolver;

	@BeforeMethod
	public void setup() {
		// mock strategies
		when(environment.getSendingStrategyConfig()).thenReturn(mock(IStrategyConfig.class));
		when(environment.getBufferStrategyConfig()).thenReturn(mock(IStrategyConfig.class));
	}

	public class EnvironmentToConfiguration extends ConfigurationCreatorTest {

		@Test
		public void configureAgent() throws Exception {
			long agentId = 13L;

			AgentConfiguration agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			assertThat(agentConfiguration.getPlatformId(), is(agentId));

			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void configurePlatformSensor() throws Exception {
			long agentId = 13L;
			long sensorId = 17L;
			String className = "className";
			Map<String, Object> parameters = Collections.<String, Object> singletonMap("key", "value");
			IPlatformSensorConfig platformSensorConfig = mock(IPlatformSensorConfig.class);
			when(platformSensorConfig.getClassName()).thenReturn(className);
			when(platformSensorConfig.getParameters()).thenReturn(parameters);
			when(environment.getPlatformSensorConfigs()).thenReturn(Collections.singletonList(platformSensorConfig));
			when(registrationService.registerPlatformSensorTypeIdent(agentId, className)).thenReturn(sensorId);

			AgentConfiguration agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<PlatformSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getPlatformSensorTypeConfigs();
			assertThat(sensorTypeConfigs, hasSize(1));
			PlatformSensorTypeConfig sensorTypeConfig = sensorTypeConfigs.iterator().next();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getClassName(), is(className));
			assertThat(sensorTypeConfig.getParameters(), is(parameters));

			verify(registrationService, times(1)).registerPlatformSensorTypeIdent(agentId, className);
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void configureMethodSensor() throws Exception {
			long agentId = 13L;
			long sensorId = 17L;
			String sensorName = "sensorName";
			String className = "className";
			Map<String, Object> parameters = Collections.<String, Object> singletonMap("key", "value");
			IMethodSensorConfig methodSensorConfig = mock(IMethodSensorConfig.class);
			when(methodSensorConfig.getName()).thenReturn(sensorName);
			when(methodSensorConfig.getClassName()).thenReturn(className);
			when(methodSensorConfig.getParameters()).thenReturn(parameters);
			when(methodSensorConfig.getPriority()).thenReturn(PriorityEnum.MAX);
			when(environment.getMethodSensorConfigs()).thenReturn(Collections.singletonList(methodSensorConfig));
			when(registrationService.registerMethodSensorTypeIdent(agentId, className, parameters)).thenReturn(sensorId);

			AgentConfiguration agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<MethodSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getMethodSensorTypeConfigs();
			assertThat(sensorTypeConfigs, hasSize(1));
			MethodSensorTypeConfig sensorTypeConfig = sensorTypeConfigs.iterator().next();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getName(), is(sensorName));
			assertThat(sensorTypeConfig.getClassName(), is(className));
			assertThat(sensorTypeConfig.getParameters(), is(parameters));
			assertThat(sensorTypeConfig.getPriority(), is(PriorityEnum.MAX));

			verify(registrationService, times(1)).registerMethodSensorTypeIdent(agentId, className, parameters);
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void configureExceptionSensor() throws Exception {
			long agentId = 13L;
			long sensorId = 17L;
			String sensorName = "sensorName";
			String className = "className";
			Map<String, Object> parameters = Collections.<String, Object> singletonMap("key", "value");
			IExceptionSensorConfig exceptionSensorConfig = mock(IExceptionSensorConfig.class);
			when(exceptionSensorConfig.getName()).thenReturn(sensorName);
			when(exceptionSensorConfig.getClassName()).thenReturn(className);
			when(exceptionSensorConfig.getParameters()).thenReturn(parameters);
			when(environment.getExceptionSensorConfig()).thenReturn(exceptionSensorConfig);
			when(registrationService.registerMethodSensorTypeIdent(agentId, className, parameters)).thenReturn(sensorId);

			AgentConfiguration agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			ExceptionSensorTypeConfig sensorTypeConfig = agentConfiguration.getExceptionSensorTypeConfig();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getName(), is(sensorName));
			assertThat(sensorTypeConfig.getClassName(), is(className));
			assertThat(sensorTypeConfig.getParameters(), is(parameters));
			assertThat(sensorTypeConfig.getPriority(), is(PriorityEnum.NORMAL)); // default priority

			verify(registrationService, times(1)).registerMethodSensorTypeIdent(agentId, className, parameters);
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void excludeRules() throws Exception {
			ExcludeRule er1 = new ExcludeRule("excludeRule1");
			ExcludeRule er2 = new ExcludeRule("wildCard*");
			when(configurationResolver.getAllExcludeRules(environment)).thenReturn(Arrays.asList(new ExcludeRule[] { er1, er2 }));

			AgentConfiguration agentConfiguration = creator.environmentToConfiguration(environment, 0);

			Collection<IMatchPattern> excludeClassesPatterns = agentConfiguration.getExcludeClassesPatterns();
			assertThat(excludeClassesPatterns, hasSize(2));
			assertThat(excludeClassesPatterns, hasItem(new EqualsMatchPattern(er1.getClassName())));
			assertThat(excludeClassesPatterns, hasItem(new WildcardMatchPattern(er2.getClassName())));
		}

		@Test
		public void sendingStrategy() throws Exception {
			String className = "className";
			Map<String, String> settings = Collections.singletonMap("key", "value");
			IStrategyConfig config = mock(IStrategyConfig.class);
			when(config.getClassName()).thenReturn(className);
			when(config.getSettings()).thenReturn(settings);
			when(environment.getSendingStrategyConfig()).thenReturn(config);

			AgentConfiguration agentConfiguration = creator.environmentToConfiguration(environment, 0);

			StrategyConfig strategyConfig = agentConfiguration.getSendingStrategyConfig();
			assertThat(strategyConfig.getClazzName(), is(className));
			assertThat(strategyConfig.getSettings(), is(settings));
		}

		@Test
		public void bufferStrategy() throws Exception {
			String className = "className";
			Map<String, String> settings = Collections.singletonMap("key", "value");
			IStrategyConfig config = mock(IStrategyConfig.class);
			when(config.getClassName()).thenReturn(className);
			when(config.getSettings()).thenReturn(settings);
			when(environment.getBufferStrategyConfig()).thenReturn(config);

			AgentConfiguration agentConfiguration = creator.environmentToConfiguration(environment, 0);

			StrategyConfig strategyConfig = agentConfiguration.getBufferStrategyConfig();
			assertThat(strategyConfig.getClazzName(), is(className));
			assertThat(strategyConfig.getSettings(), is(settings));
		}

	}

}
