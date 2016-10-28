package rocks.inspectit.server.instrumentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.event.AgentRegisteredEvent;
import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheInstrumentation;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheLookup;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheModification;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheModificationException;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.JmxMonitoringApplier;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings({ "PMD", "unchecked" })
public class NextGenInstrumentationManagerTest extends TestBase {

	@InjectMocks
	NextGenInstrumentationManager manager;

	@Mock
	Logger log;

	@Mock
	ObjectFactory<ClassCache> classCacheFactory;

	@Mock
	ObjectFactory<ConfigurationHolder> configurationHolderFactory;

	@Mock
	IRegistrationService registrationService;

	@Mock
	ConfigurationResolver configurationResolver;

	@Mock
	ExecutorService executor;

	@Mock
	ClassCache classCache;

	@Mock
	ConfigurationHolder configurationHolder;

	@Mock
	ClassCacheInstrumentation instrumentationService;

	@Mock
	ClassCacheLookup lookupService;

	@Mock
	ClassCacheModification modificationService;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	PlatformIdentDao platformIdentDao;

	@BeforeMethod
	public void setup() {
		when(classCacheFactory.getObject()).thenReturn(classCache);
		when(configurationHolderFactory.getObject()).thenReturn(configurationHolder);
		when(classCache.getInstrumentationService()).thenReturn(instrumentationService);
		when(classCache.getLookupService()).thenReturn(lookupService);
		when(classCache.getModificationService()).thenReturn(modificationService);
		when(platformIdentDao.load(any(Long.class))).thenReturn(mock(PlatformIdent.class));
		doAnswer(new Answer<Future<?>>() {
			@Override
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				Runnable runnable = (Runnable) invocation.getArguments()[0];
				runnable.run();
				return null;
			}
		}).when(executor).submit(any(Runnable.class));
	}

	public class Register extends NextGenInstrumentationManagerTest {

		@Test(expectedExceptions = BusinessException.class)
		public void noMappingsForAgent() throws BusinessException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(configurationResolver.getEnvironmentForAgent(definedIPs, agentName)).thenThrow(new BusinessException(null));

			manager.register(definedIPs, agentName, version);
		}

		@Test
		public void newAgent() throws BusinessException {
			long id = 10;
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			final AgentConfig configuration = mock(AgentConfig.class);
			Environment environment = mock(Environment.class);
			when(configurationResolver.getEnvironmentForAgent(definedIPs, agentName)).thenReturn(environment);
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(id);
			doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
					return null;
				}
			}).when(configurationHolder).update(environment, id);

			AgentConfig result = manager.register(definedIPs, agentName, version);

			assertThat(result, is(configuration));

			ArgumentCaptor<AgentRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(AgentRegisteredEvent.class);
			verify(eventPublisher).publishEvent(eventCaptor.capture());
			assertThat(eventCaptor.getValue().getPlatformIdent(), is(notNullValue()));
			verify(configurationResolver).getEnvironmentForAgent(definedIPs, agentName);
			verify(registrationService).registerPlatformIdent(definedIPs, agentName, version);
			verify(configurationHolder).update(environment, id);
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).getAgentConfiguration();
			verify(configurationHolder).isInitialized();
			verify(executor).submit(any(Runnable.class));
			verifyNoMoreInteractions(configurationResolver, registrationService, configurationHolder, eventPublisher, executor);
			verifyZeroInteractions(classCache);
		}

		@Test
		public void existingAgent() throws BusinessException {
			long id = 10;
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			final AgentConfig configuration = mock(AgentConfig.class);
			Environment environment = mock(Environment.class);
			when(configurationResolver.getEnvironmentForAgent(definedIPs, agentName)).thenReturn(environment);
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(id);
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(configurationHolder.getEnvironment()).thenReturn(environment);
			when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
			Map<Collection<String>, InstrumentationDefinition> initialInstrumentations = mock(Map.class);
			when(instrumentationService.getInstrumentationResultsWithHashes()).thenReturn(initialInstrumentations);

			AgentConfig result = manager.register(definedIPs, agentName, version);

			assertThat(result, is(configuration));

			verify(configurationResolver).getEnvironmentForAgent(definedIPs, agentName);
			verify(registrationService).registerPlatformIdent(definedIPs, agentName, version);
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).getAgentConfiguration();
			verify(configurationHolder).isInitialized();
			verify(configuration).setInitialInstrumentationResults(initialInstrumentations);
			verify(configuration).setClassCacheExistsOnCmr(true);
			verify(classCache).getInstrumentationService();
			verify(instrumentationService).getInstrumentationResultsWithHashes();
			verifyNoMoreInteractions(configurationResolver, registrationService, configurationHolder, instrumentationService, classCache);
		}

		@Test
		public void existingAgentNoEnvironment() throws BusinessException {
			long id = 10;
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			final AgentConfig configuration = mock(AgentConfig.class);
			Environment environment = mock(Environment.class);
			when(configurationResolver.getEnvironmentForAgent(definedIPs, agentName)).thenReturn(environment);
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(id);
			when(configurationHolder.isInitialized()).thenReturn(false);
			when(configurationHolder.getEnvironment()).thenReturn(environment);
			doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
					return null;
				}
			}).when(configurationHolder).update(environment, id);

			AgentConfig result = manager.register(definedIPs, agentName, version);

			assertThat(result, is(configuration));

			verify(configurationResolver).getEnvironmentForAgent(definedIPs, agentName);
			verify(registrationService).registerPlatformIdent(definedIPs, agentName, version);
			verify(configurationHolder).update(environment, id);
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).getAgentConfiguration();
			verify(configurationHolder).isInitialized();
			verifyNoMoreInteractions(configurationResolver, registrationService, configurationHolder);
			verifyZeroInteractions(classCache);
		}

		@Test
		public void existingAgentDifferentEnvironment() throws BusinessException {
			long id = 10;
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			final AgentConfig configuration = mock(AgentConfig.class);
			Environment environment = mock(Environment.class);
			Environment newEnvironment = mock(Environment.class);
			when(configurationResolver.getEnvironmentForAgent(definedIPs, agentName)).thenReturn(newEnvironment);
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(id);
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(configurationHolder.getEnvironment()).thenReturn(environment);
			doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
					return null;
				}
			}).when(configurationHolder).update(newEnvironment, id);

			AgentConfig result = manager.register(definedIPs, agentName, version);

			assertThat(result, is(configuration));

			verify(configurationResolver).getEnvironmentForAgent(definedIPs, agentName);
			verify(registrationService).registerPlatformIdent(definedIPs, agentName, version);
			verify(configurationHolder).update(newEnvironment, id);
			verify(configurationHolder).getEnvironment();
			verify(configurationHolder).getAgentConfiguration();
			verify(configurationHolder).isInitialized();
			verifyNoMoreInteractions(configurationResolver, registrationService, configurationHolder);
			verifyZeroInteractions(classCache);
		}
	}

	public class Unregister extends NextGenInstrumentationManagerTest {

		@Test
		public void unregister() throws Exception {
			long platformId = 10L;

			manager.unregister(platformId);

			verify(registrationService).unregisterPlatformIdent(platformId);
		}
	}

	public class Analyze extends NextGenInstrumentationManagerTest {

		@Mock
		private Type type;

		private final static String HASH = "hash";

		private final static long ID = 10;

		@Test(expectedExceptions = BusinessException.class)
		public void agentNotRegistered() throws BusinessException {
			manager.analyze(ID, HASH, type);
		}

		@Test
		public void nonExistingNonClassType() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			ImmutableType typeFromClassCache = mock(ImmutableType.class);
			when(typeFromClassCache.isClass()).thenReturn(false);
			when(lookupService.findByHash(HASH)).thenReturn(null, typeFromClassCache);

			InstrumentationDefinition result = manager.analyze(ID, HASH, type);

			assertThat(result, is(nullValue()));

			verify(modificationService).merge(type);
			verifyNoMoreInteractions(modificationService);
			verifyZeroInteractions(instrumentationService);
		}

		@Test
		public void existingNonClassType() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			ImmutableType typeFromClassCache = mock(ImmutableType.class);
			when(typeFromClassCache.isClass()).thenReturn(false);
			when(lookupService.findByHash(HASH)).thenReturn(typeFromClassCache);

			InstrumentationDefinition result = manager.analyze(ID, HASH, type);

			assertThat(result, is(nullValue()));

			verifyZeroInteractions(modificationService, instrumentationService);
		}

		@Test
		public void existingClassTypeConfigurationNotInitialized() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			ClassType classType = mock(ClassType.class);
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(lookupService.findByHash(HASH)).thenReturn(classType);
			when(configurationHolder.isInitialized()).thenReturn(false);

			InstrumentationDefinition result = manager.analyze(ID, HASH, type);

			assertThat(result, is(nullValue()));

			verifyZeroInteractions(modificationService, instrumentationService);
		}

		@Test
		public void existingClassTypeInstrumented() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			ClassType classType = mock(ClassType.class);
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(lookupService.findByHash(HASH)).thenReturn(classType);
			when(configurationHolder.isInitialized()).thenReturn(true);
			AgentConfig configuration = mock(AgentConfig.class);
			Collection<IInstrumentationApplier> appliers = mock(Collection.class);
			InstrumentationDefinition instrumentationResult = mock(InstrumentationDefinition.class);
			when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
			when(configurationHolder.getInstrumentationAppliers()).thenReturn(appliers);
			when(instrumentationService.addAndGetInstrumentationResult(classType, configuration, appliers)).thenReturn(instrumentationResult);

			InstrumentationDefinition result = manager.analyze(ID, HASH, type);

			assertThat(result, is(instrumentationResult));

			verify(instrumentationService).addAndGetInstrumentationResult(classType, configuration, appliers);
			verifyNoMoreInteractions(instrumentationService);
			verifyZeroInteractions(modificationService);
		}

	}

	public class AnalyzeJmxAttributes extends NextGenInstrumentationManagerTest {

		private final static long ID = 10;

		@Mock
		JmxAttributeDescriptor descriptor;

		@Mock
		JmxMonitoringApplier applier;

		@Test(expectedExceptions = BusinessException.class)
		public void agentNotRegistered() throws BusinessException {
			manager.analyzeJmxAttributes(ID, Collections.singleton(descriptor));
		}

		@Test
		public void nothingSent() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			Collection<JmxAttributeDescriptor> toMonitor = manager.analyzeJmxAttributes(ID, Collections.<JmxAttributeDescriptor> emptyList());

			assertThat(toMonitor, is(empty()));
		}

		@Test
		public void configurationHolderNotInitialized() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			when(configurationHolder.isInitialized()).thenReturn(false);

			Collection<JmxAttributeDescriptor> toMonitor = manager.analyzeJmxAttributes(ID, Collections.singleton(descriptor));

			assertThat(toMonitor, is(empty()));
		}

		@Test
		public void configurationHolderHasNoAppliers() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			when(configurationHolder.isInitialized()).thenReturn(true);
			when(configurationHolder.getJmxMonitoringAppliers()).thenReturn(null);

			Collection<JmxAttributeDescriptor> toMonitor = manager.analyzeJmxAttributes(ID, Collections.singleton(descriptor));

			assertThat(toMonitor, is(empty()));
		}

		@Test
		public void applierDoesNothing() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			AgentConfig configuration = mock(AgentConfig.class);
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
			when(configurationHolder.getJmxMonitoringAppliers()).thenReturn(Collections.singleton(applier));
			when(applier.addMonitoringPoint(configuration, descriptor)).thenReturn(false);

			Collection<JmxAttributeDescriptor> toMonitor = manager.analyzeJmxAttributes(ID, Collections.singleton(descriptor));

			assertThat(toMonitor, is(empty()));

			verify(applier).addMonitoringPoint(configuration, descriptor);
		}

		@Test
		public void applierAddsMonitoringPoint() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			AgentConfig configuration = mock(AgentConfig.class);
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
			when(configurationHolder.getJmxMonitoringAppliers()).thenReturn(Collections.singleton(applier));
			when(applier.addMonitoringPoint(configuration, descriptor)).thenReturn(true);

			Collection<JmxAttributeDescriptor> toMonitor = manager.analyzeJmxAttributes(ID, Collections.singleton(descriptor));

			assertThat(toMonitor, hasSize(1));
			assertThat(toMonitor, hasItem(descriptor));

			verify(applier).addMonitoringPoint(configuration, descriptor);
		}

		@Test
		public void ensureNoDoubleRegistration() throws BusinessException, ClassCacheModificationException {
			List<String> definedIPs = mock(List.class);
			String agentName = "agentName";
			String version = "v1";
			when(registrationService.registerPlatformIdent(definedIPs, agentName, version)).thenReturn(ID);

			manager.register(definedIPs, agentName, version);

			AgentConfig configuration = mock(AgentConfig.class);
			when(configurationHolder.isInitialized()).thenReturn(true);
			when(configurationHolder.getAgentConfiguration()).thenReturn(configuration);
			List<JmxMonitoringApplier> appliers = new ArrayList<>();
			appliers.add(applier);
			appliers.add(applier);
			when(configurationHolder.getJmxMonitoringAppliers()).thenReturn(appliers);
			when(applier.addMonitoringPoint(configuration, descriptor)).thenReturn(true);

			Collection<JmxAttributeDescriptor> toMonitor = manager.analyzeJmxAttributes(ID, Collections.singleton(descriptor));

			assertThat(toMonitor, hasSize(1));
			assertThat(toMonitor, hasItem(descriptor));

			verify(applier).addMonitoringPoint(configuration, descriptor);
		}

	}

}
