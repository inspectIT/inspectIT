package rocks.inspectit.server.instrumentation.config.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.event.ClassInstrumentationChangedEvent;
import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheInstrumentation;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheLookup;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.cs.ci.Environment;

@SuppressWarnings({ "PMD" })
public class EnvironmentMappingUpdateJobTest {

	private static final long PLATFORM_ID = 10L;

	@InjectMocks
	private EnvironmentMappingUpdateJob job;

	@Mock
	protected Logger log;

	@Mock
	private AgentCacheEntry agentCacheEntry;

	@Mock
	private ClassCache classCache;

	@Mock
	private ClassCacheLookup lookupService;

	@Mock
	private ConfigurationHolder configurationHolder;

	@Mock
	private IInstrumentationApplier holdedInstrumentationApplier;

	@Mock
	private Environment environment;

	@Mock
	private Environment updateEnvironment;

	@Mock
	private AgentConfig updateConfiguration;

	@Mock
	private ClassCacheInstrumentation instrumentationService;

	@Mock
	protected ApplicationEventPublisher eventPublisher;

	@Mock
	protected ClassType classType;

	@Mock
	protected ImmutableClassType immutableClassType;

	@BeforeMethod
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		job.log = LoggerFactory.getLogger(EnvironmentMappingUpdateJob.class);
		job.setAgentCacheEntry(agentCacheEntry);

		when(configurationHolder.getEnvironment()).thenReturn(environment);
		when(configurationHolder.getInstrumentationAppliers()).thenReturn(Collections.singletonList(holdedInstrumentationApplier));

		when(agentCacheEntry.getConfigurationHolder()).thenReturn(configurationHolder);
		when(agentCacheEntry.getClassCache()).thenReturn(classCache);
		when(agentCacheEntry.getId()).thenReturn(PLATFORM_ID);

		when(classCache.getInstrumentationService()).thenReturn(instrumentationService);
		when(classCache.getLookupService()).thenReturn(lookupService);

		when(classType.isClass()).thenReturn(true);
		when(classType.castToClass()).thenReturn(immutableClassType);
	}

	@Test
	public void noEnvironment() {
		when(configurationHolder.isInitialized()).thenReturn(false);

		job.setEnvironment(null);
		job.run();

		verify(instrumentationService, times(1)).removeInstrumentationPoints();
		verify(configurationHolder, times(1)).update(null, PLATFORM_ID);

		verifyNoMoreInteractions(instrumentationService, eventPublisher);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void yesEnvironment() {
		when(configurationHolder.isInitialized()).thenReturn(true);
		when(configurationHolder.getAgentConfiguration()).thenReturn(updateConfiguration);
		doReturn(Collections.singletonList(classType)).when(instrumentationService).addInstrumentationPoints(any(AgentConfig.class), any(Collection.class));

		job.setEnvironment(updateEnvironment);
		job.run();

		verify(instrumentationService, times(1)).removeInstrumentationPoints();
		verify(configurationHolder, times(1)).update(updateEnvironment, PLATFORM_ID);

		Collection<IInstrumentationApplier> appliers = configurationHolder.getInstrumentationAppliers();
		verify(instrumentationService, times(1)).addInstrumentationPoints(updateConfiguration, appliers);

		ArgumentCaptor<Collection> typeCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(instrumentationService).getInstrumentationResults(typeCaptor.capture());
		assertThat((Collection<Type>) typeCaptor.getValue(), hasItems((Type) classType));

		verify(eventPublisher).publishEvent(any(ClassInstrumentationChangedEvent.class));

		verifyNoMoreInteractions(instrumentationService, updateConfiguration, updateEnvironment, eventPublisher);
	}
}
