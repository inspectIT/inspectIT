package rocks.inspectit.server.instrumentation.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentEndUserMonitoringConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.RetransformationStrategy;
import rocks.inspectit.shared.all.instrumentation.config.impl.StrategyConfig;
import rocks.inspectit.shared.all.pattern.EqualsMatchPattern;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.WildcardMatchPattern;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.eum.EndUserMonitoringConfig;
import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;
import rocks.inspectit.shared.cs.ci.sensor.exception.IExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ClassLoadingDelegationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.CloseableHttpAsyncClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ExecutorIntercepterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.HttpClientBuilderSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.MBeanServerInterceptorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.strategy.IStrategyConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

@SuppressWarnings("PMD")
public class ConfigurationCreatorTest extends TestBase {

	@InjectMocks
	ConfigurationCreator creator;

	@Mock
	Environment environment;

	@Mock
	IRegistrationService registrationService;

	@Mock
	ConfigurationResolver configurationResolver;

	public ExecutorIntercepterSensorConfig eisc = ExecutorIntercepterSensorConfig.INSTANCE;

	public HttpClientBuilderSensorConfig httpClientBuilder = HttpClientBuilderSensorConfig.INSTANCE;

	public CloseableHttpAsyncClientSensorConfig closeableHttpAsyncCLient = CloseableHttpAsyncClientSensorConfig.INSTANCE;

	@BeforeMethod
	public void setup() {
		// mock strategies
		when(environment.getDisruptorStrategyConfig()).thenReturn(mock(IStrategyConfig.class));
		when(environment.getEumConfig()).thenReturn(mock(EndUserMonitoringConfig.class));
	}

	public class EnvironmentToConfiguration extends ConfigurationCreatorTest {

		@Test
		public void configureAgent() throws Exception {
			long agentId = 13L;

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			assertThat(agentConfiguration.getPlatformId(), is(agentId));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
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
			when(platformSensorConfig.isActive()).thenReturn(true);
			when(environment.getPlatformSensorConfigs()).thenReturn(Collections.singletonList(platformSensorConfig));
			when(registrationService.registerPlatformSensorTypeIdent(agentId, className)).thenReturn(sensorId);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<PlatformSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getPlatformSensorTypeConfigs();
			assertThat(sensorTypeConfigs, hasSize(1));
			PlatformSensorTypeConfig sensorTypeConfig = sensorTypeConfigs.iterator().next();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getClassName(), is(className));
			assertThat(sensorTypeConfig.getParameters(), is(parameters));

			verify(registrationService).registerPlatformSensorTypeIdent(agentId, className);
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void configurePlatformSensorNotActive() throws Exception {
			long agentId = 13L;
			IPlatformSensorConfig platformSensorConfig = mock(IPlatformSensorConfig.class);
			when(platformSensorConfig.isActive()).thenReturn(false);
			when(environment.getPlatformSensorConfigs()).thenReturn(Collections.singletonList(platformSensorConfig));

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<PlatformSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getPlatformSensorTypeConfigs();
			assertThat(sensorTypeConfigs, is(empty()));

			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
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

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<MethodSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getMethodSensorTypeConfigs();
			assertThat(sensorTypeConfigs, hasSize(1));
			MethodSensorTypeConfig sensorTypeConfig = sensorTypeConfigs.iterator().next();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getName(), is(sensorName));
			assertThat(sensorTypeConfig.getClassName(), is(className));
			assertThat(sensorTypeConfig.getParameters(), is(parameters));
			assertThat(sensorTypeConfig.getPriority(), is(PriorityEnum.MAX));

			verify(registrationService).registerMethodSensorTypeIdent(agentId, className, parameters);
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
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

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			ExceptionSensorTypeConfig sensorTypeConfig = agentConfiguration.getExceptionSensorTypeConfig();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getName(), is(sensorName));
			assertThat(sensorTypeConfig.getClassName(), is(className));
			assertThat(sensorTypeConfig.getParameters(), is(parameters));
			assertThat(sensorTypeConfig.getPriority(), is(PriorityEnum.NORMAL)); // default priority

			verify(registrationService).registerMethodSensorTypeIdent(agentId, className, parameters);
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
			verifyNoMoreInteractions(registrationService);
		}

		@SuppressWarnings("unchecked")
		@Test
		public void configureJmxSensor() throws Exception {
			long agentId = 13L;
			long sensorId = 17L;
			String className = "className";
			Map<String, Object> parameters = Collections.<String, Object> singletonMap("key", "value");
			JmxSensorConfig jmxSensorConfig = mock(JmxSensorConfig.class);
			when(jmxSensorConfig.getClassName()).thenReturn(className);
			when(jmxSensorConfig.getParameters()).thenReturn(parameters);
			when(jmxSensorConfig.isActive()).thenReturn(true);
			when(environment.getJmxSensorConfig()).thenReturn(jmxSensorConfig);
			when(registrationService.registerJmxSensorTypeIdent(agentId, className)).thenReturn(sensorId);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			JmxSensorTypeConfig sensorTypeConfig = agentConfiguration.getJmxSensorTypeConfig();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getClassName(), is(className));
			assertThat(sensorTypeConfig.getParameters(), is(parameters));

			verify(registrationService).registerJmxSensorTypeIdent(agentId, className);
			// needed because of the intercepting server sensor
			verify(registrationService, times(4)).registerMethodSensorTypeIdent(anyLong(), anyString(), anyMap());
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void configureJmxSensorNotActive() throws Exception {
			long agentId = 13L;
			JmxSensorConfig jmxSensorConfig = mock(JmxSensorConfig.class);
			when(jmxSensorConfig.isActive()).thenReturn(false);
			when(environment.getJmxSensorConfig()).thenReturn(jmxSensorConfig);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			JmxSensorTypeConfig sensorTypeConfig = agentConfiguration.getJmxSensorTypeConfig();
			assertThat(sensorTypeConfig, is(nullValue()));

			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void excludeRules() throws Exception {
			ExcludeRule er1 = new ExcludeRule("excludeRule1");
			ExcludeRule er2 = new ExcludeRule("wildCard*");
			when(configurationResolver.getAllExcludeRules(environment)).thenReturn(Arrays.asList(new ExcludeRule[] { er1, er2 }));

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, 0);

			Collection<IMatchPattern> excludeClassesPatterns = agentConfiguration.getExcludeClassesPatterns();
			assertThat(excludeClassesPatterns, hasSize(2));
			assertThat(excludeClassesPatterns, hasItem(new EqualsMatchPattern(er1.getClassName())));
			assertThat(excludeClassesPatterns, hasItem(new WildcardMatchPattern(er2.getClassName())));
		}

		@Test
		public void retransformationStrategy() throws Exception {
			RetransformationStrategy retransformationStrategy = RetransformationStrategy.ALWAYS;
			when(environment.getRetransformationStrategy()).thenReturn(retransformationStrategy);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, 0);

			assertThat(agentConfiguration.getRetransformationStrategy(), is(retransformationStrategy));
		}

		// special method sensor

		@Test
		public void noSpecialSensors() throws Exception {
			long agentId = 13L;
			when(environment.isClassLoadingDelegation()).thenReturn(false);
			when(environment.getJmxSensorConfig()).thenReturn(null);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<MethodSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getSpecialMethodSensorTypeConfigs();
			assertThat(Iterators.get(sensorTypeConfigs.iterator(), 0).getClassName(), is(equalTo(eisc.getClassName())));
			assertThat(Iterators.get(sensorTypeConfigs.iterator(), 1).getClassName(), is(equalTo(httpClientBuilder.getClassName())));
			assertThat(Iterators.get(sensorTypeConfigs.iterator(), 2).getClassName(), is(equalTo(closeableHttpAsyncCLient.getClassName())));
		}

		@Test
		public void classLoadingDelegation() throws Exception {
			long agentId = 13L;
			long sensorId = 17L;
			when(environment.isClassLoadingDelegation()).thenReturn(true);
			ClassLoadingDelegationSensorConfig cldConfig = ClassLoadingDelegationSensorConfig.INSTANCE;
			when(registrationService.registerMethodSensorTypeIdent(agentId, cldConfig.getClassName(), cldConfig.getParameters())).thenReturn(sensorId);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<MethodSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getSpecialMethodSensorTypeConfigs();
			assertThat(sensorTypeConfigs, hasSize(4));
			// first element will be class loading config
			MethodSensorTypeConfig sensorTypeConfig = sensorTypeConfigs.iterator().next();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getName(), is(cldConfig.getName()));
			assertThat(sensorTypeConfig.getClassName(), is(cldConfig.getClassName()));
			assertThat(sensorTypeConfig.getParameters(), is(cldConfig.getParameters()));
			assertThat(sensorTypeConfig.getPriority(), is(cldConfig.getPriority()));

			verify(registrationService).registerMethodSensorTypeIdent(agentId, cldConfig.getClassName(), cldConfig.getParameters());
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void disruptorStrategy() throws Exception {
			String className = "className";
			Map<String, String> settings = Collections.singletonMap("key", "value");
			IStrategyConfig config = mock(IStrategyConfig.class);
			when(config.getClassName()).thenReturn(className);
			when(config.getSettings()).thenReturn(settings);
			when(environment.getDisruptorStrategyConfig()).thenReturn(config);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, 0);

			StrategyConfig strategyConfig = agentConfiguration.getDisruptorStrategyConfig();
			assertThat(strategyConfig.getClassName(), is(className));
			assertThat(strategyConfig.getSettings(), is(settings));
		}

		@Test
		public void eumConfig() throws Exception {

			EndUserMonitoringConfig config = mock(EndUserMonitoringConfig.class);
			when(config.isEumEnabled()).thenReturn(true);
			String url = "/base/url";
			when(config.getScriptBaseUrl()).thenReturn(url);
			String modules = "12a";
			when(config.getActiveModules()).thenReturn(modules);
			when(config.isListenerInstrumentationAllowed()).thenReturn(false);
			when(config.isAgentMinificationEnabled()).thenReturn(false);

			when(environment.getEumConfig()).thenReturn(config);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, 0);

			AgentEndUserMonitoringConfig eumConfig = agentConfiguration.getEumConfig();
			assertThat(eumConfig.isEnabled(), is(true));
			assertThat(eumConfig.getActiveModules(), is(modules));
			assertThat(eumConfig.getScriptBaseUrl(), is(url));
			assertThat(eumConfig.isListenerInstrumentationAllowed(), is(false));
			assertThat(eumConfig.isAgentMinificationEnabled(), is(false));
		}

		@Test
		public void mbeanServerInterceptor() throws Exception {
			long agentId = 13L;
			long sensorId = 17L;
			JmxSensorConfig jmxSensorConfig = mock(JmxSensorConfig.class);
			when(jmxSensorConfig.isActive()).thenReturn(true);
			when(environment.getJmxSensorConfig()).thenReturn(jmxSensorConfig);
			MBeanServerInterceptorSensorConfig msiConfig = MBeanServerInterceptorSensorConfig.INSTANCE;
			when(registrationService.registerMethodSensorTypeIdent(agentId, msiConfig.getClassName(), msiConfig.getParameters())).thenReturn(sensorId);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<MethodSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getSpecialMethodSensorTypeConfigs();
			assertThat(sensorTypeConfigs, hasSize(4));
			// first element will be mbean server interceptor config
			MethodSensorTypeConfig sensorTypeConfig = sensorTypeConfigs.iterator().next();
			assertThat(sensorTypeConfig.getId(), is(sensorId));
			assertThat(sensorTypeConfig.getName(), is(msiConfig.getName()));
			assertThat(sensorTypeConfig.getClassName(), is(msiConfig.getClassName()));
			assertThat(sensorTypeConfig.getParameters(), is(msiConfig.getParameters()));
			assertThat(sensorTypeConfig.getPriority(), is(msiConfig.getPriority()));

			verify(registrationService).registerMethodSensorTypeIdent(agentId, msiConfig.getClassName(), msiConfig.getParameters());
			// needed because jmx sensor will be also registered
			verify(registrationService).registerJmxSensorTypeIdent(anyLong(), anyString());
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
			verifyNoMoreInteractions(registrationService);
		}

		@Test
		public void mbeanServerInterceptorJmxNotActive() throws Exception {
			long agentId = 13L;
			JmxSensorConfig jmxSensorConfig = mock(JmxSensorConfig.class);
			when(jmxSensorConfig.isActive()).thenReturn(false);
			when(environment.getJmxSensorConfig()).thenReturn(jmxSensorConfig);

			AgentConfig agentConfiguration = creator.environmentToConfiguration(environment, agentId);

			Collection<MethodSensorTypeConfig> sensorTypeConfigs = agentConfiguration.getSpecialMethodSensorTypeConfigs();
			assertThat(sensorTypeConfigs, hasSize(3));
			assertThat(sensorTypeConfigs.iterator().next().getClassName(), is(equalTo(eisc.getClassName())));

			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(eisc.getClassName()), eq(eisc.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(httpClientBuilder.getClassName()), eq(httpClientBuilder.getParameters()));
			verify(registrationService).registerMethodSensorTypeIdent(anyLong(), eq(closeableHttpAsyncCLient.getClassName()), eq(closeableHttpAsyncCLient.getParameters()));
			verifyNoMoreInteractions(registrationService);
		}
	}
}
