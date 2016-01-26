package info.novatec.inspectit.cmr.instrumentation.config.job;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCache;
import info.novatec.inspectit.cmr.instrumentation.classcache.IClassCacheInstrumentation;
import info.novatec.inspectit.cmr.instrumentation.config.AgentCacheEntry;
import info.novatec.inspectit.cmr.instrumentation.config.ConfigurationHolder;
import info.novatec.inspectit.cmr.instrumentation.config.job.MappingUpdateJob;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;

import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings({ "PMD" })
public class MappingUpdateJobTest {

	private static final long PLATFORM_ID = 10L;

	private MappingUpdateJob job;

	@Mock
	private AgentCacheEntry agentCacheEntry;

	@Mock
	private ClassCache classCache;

	@Mock
	private ConfigurationHolder configurationHolder;

	@Mock
	private IInstrumentationApplier holdedInstrumentationApplier;

	@Mock
	private Environment environment;

	@Mock
	private Environment updateEnvironment;

	@Mock
	private AgentConfiguration updateConfiguration;

	@Mock
	private IClassCacheInstrumentation instrumentationService;

	@BeforeMethod
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		job = new MappingUpdateJob();
		job.log = LoggerFactory.getLogger(MappingUpdateJob.class);
		job.setAgentCacheEntry(agentCacheEntry);

		when(configurationHolder.getEnvironment()).thenReturn(environment);
		when(configurationHolder.getInstrumentationAppliers()).thenReturn(Collections.singletonList(holdedInstrumentationApplier));

		when(agentCacheEntry.getConfigurationHolder()).thenReturn(configurationHolder);
		when(agentCacheEntry.getClassCache()).thenReturn(classCache);
		when(agentCacheEntry.getId()).thenReturn(PLATFORM_ID);

		when(classCache.getInstrumentationService()).thenReturn(instrumentationService);
	}

	@Test
	public void noEnvironment() {
		when(configurationHolder.isInitialized()).thenReturn(false);

		job.setEnvironment(null);
		job.run();

		verify(instrumentationService, times(1)).removeInstrumentationPoints();
		verify(configurationHolder, times(1)).update(null, PLATFORM_ID);

		verifyNoMoreInteractions(instrumentationService);
	}

	@Test
	public void yesEnvironment() {
		when(configurationHolder.isInitialized()).thenReturn(true);
		when(configurationHolder.getAgentConfiguration()).thenReturn(updateConfiguration);

		job.setEnvironment(updateEnvironment);
		job.run();

		verify(instrumentationService, times(1)).removeInstrumentationPoints();
		verify(configurationHolder, times(1)).update(updateEnvironment, PLATFORM_ID);

		Collection<IInstrumentationApplier> appliers = configurationHolder.getInstrumentationAppliers();
		verify(instrumentationService, times(1)).addInstrumentationPoints(updateConfiguration, appliers);

		verifyNoMoreInteractions(instrumentationService, updateConfiguration, updateEnvironment);
	}
}
