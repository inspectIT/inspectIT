package info.novatec.inspectit.cmr.instrumentation.config.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.factory.FunctionalMethodSensorAssignmentFactory;
import info.novatec.inspectit.cmr.ci.event.EnvironmentUpdateEvent;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCache;
import info.novatec.inspectit.cmr.instrumentation.classcache.IClassCacheInstrumentation;
import info.novatec.inspectit.cmr.instrumentation.config.AgentCacheEntry;
import info.novatec.inspectit.cmr.instrumentation.config.ClassCacheSearchNarrower;
import info.novatec.inspectit.cmr.instrumentation.config.ConfigurationHolder;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.testbase.TestBase;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings({ "all", "unchecked" })
public class EnvironmentUpdateJobTest extends TestBase {

	protected static final long PLATFORM_ID = 10L;

	protected static final String ENVIRONMENT_ID = "env";

	@InjectMocks
	protected EnvironmentUpdateJob job;

	@Mock
	protected Logger log;

	@Mock
	protected ClassCacheSearchNarrower classCacheSearchNarrower;

	@Mock
	protected AgentCacheEntry agentCacheEntry;

	@Mock
	protected ConfigurationHolder configurationHolder;

	@Mock
	protected AgentConfiguration agentConfiguration;

	@Mock
	protected Environment environment;

	@Mock
	protected Environment updateEnvironment;

	@Mock
	protected IRegistrationService registrationService;

	@Mock
	protected ClassCache classCache;

	@Mock
	protected IClassCacheInstrumentation instrumentationService;

	@Mock
	protected MethodSensorAssignment sensorAssignment;

	@Mock
	protected IInstrumentationApplier instrumentationApplier;

	@Mock
	protected IInstrumentationApplier holdedInstrumentationApplier;

	@Mock
	protected ClassType classType;

	@Mock
	protected FunctionalMethodSensorAssignmentFactory functionalAssignmentFactory;

	@Mock
	protected EnvironmentUpdateEvent event;

	@BeforeMethod
	public void setup() throws Exception {
		when(configurationHolder.getAgentConfiguration()).thenReturn(agentConfiguration);
		when(configurationHolder.getEnvironment()).thenReturn(environment);
		when(configurationHolder.getInstrumentationAppliers()).thenReturn(Collections.singletonList(holdedInstrumentationApplier));

		when(agentCacheEntry.getConfigurationHolder()).thenReturn(configurationHolder);
		when(agentCacheEntry.getClassCache()).thenReturn(classCache);
		when(agentCacheEntry.getId()).thenReturn(PLATFORM_ID);

		when(classCache.getInstrumentationService()).thenReturn(instrumentationService);

		when(event.getAfter()).thenReturn(updateEnvironment);
	}

	public class Run extends EnvironmentUpdateJobTest {

		@Test
		public void onlyConfigurationUpdate() throws RemoteException, BusinessException {
			job.setEnvironmentUpdateEvent(event);

			job.run();

			verify(configurationHolder, times(1)).update(updateEnvironment, PLATFORM_ID);

			verify(agentConfiguration, times(0)).setInitialInstrumentationResults(Mockito.anyMap());
			verify(agentConfiguration, times(0)).setClassCacheExistsOnCmr(Mockito.anyBoolean());

			verifyZeroInteractions(classCache, environment, classCacheSearchNarrower, agentConfiguration, instrumentationService);
		}

		@Test
		public void addedAssignment() throws RemoteException, BusinessException {
			Collection<ClassType> types = Collections.singleton(classType);

			doReturn(instrumentationApplier).when(sensorAssignment).getInstrumentationApplier(environment, registrationService);
			doReturn(types).when(classCacheSearchNarrower).narrowByClassSensorAssignment(classCache, sensorAssignment);
			doReturn(types).when(instrumentationService).addInstrumentationPoints(eq(types), eq(agentConfiguration), Mockito.<Collection<IInstrumentationApplier>> any());
			doReturn(Collections.singletonList(sensorAssignment)).when(event).getAddedSensorAssignments(functionalAssignmentFactory);
			job.setEnvironmentUpdateEvent(event);

			job.run();

			verify(configurationHolder, times(1)).update(updateEnvironment, PLATFORM_ID);

			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			verify(instrumentationService, times(1)).addInstrumentationPoints(eq(types), eq(agentConfiguration), captor.capture());

			assertThat((Collection<IInstrumentationApplier>) captor.getValue(), hasSize(1));
			assertThat(((Collection<IInstrumentationApplier>) captor.getValue()).iterator().next(), is(instrumentationApplier));

			verifyZeroInteractions(environment, agentConfiguration);
		}

		@Test
		public void removedAssignment() throws RemoteException, BusinessException {
			Collection<ClassType> types = Collections.singleton(classType);

			doReturn(instrumentationApplier).when(sensorAssignment).getInstrumentationApplier(environment, registrationService);
			doReturn(types).when(classCacheSearchNarrower).narrowByClassSensorAssignment(classCache, sensorAssignment);
			doReturn(types).when(instrumentationService).removeInstrumentationPoints(eq(types), Mockito.<Collection<IInstrumentationApplier>> any());
			doReturn(Collections.singletonList(sensorAssignment)).when(event).getRemovedSensorAssignments(functionalAssignmentFactory);
			job.setEnvironmentUpdateEvent(event);

			job.run();

			verify(configurationHolder, times(1)).update(updateEnvironment, PLATFORM_ID);

			verify(instrumentationService, times(1)).removeInstrumentationPoints(types, Collections.singleton(instrumentationApplier));

			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			Collection<IInstrumentationApplier> appliers = configurationHolder.getInstrumentationAppliers();
			verify(instrumentationService, times(1)).addInstrumentationPoints(captor.capture(), eq(agentConfiguration), eq(appliers));
			assertThat((Collection<ClassType>) captor.getValue(), hasSize(1));
			assertThat(((Collection<ClassType>) captor.getValue()).iterator().next(), is(classType));

			verifyZeroInteractions(environment, agentConfiguration);
		}

		@Test
		public void removedAssignmentNoChange() {
			Collection<ClassType> types = Collections.singleton(classType);

			doReturn(instrumentationApplier).when(sensorAssignment).getInstrumentationApplier(environment, registrationService);
			doReturn(types).when(classCacheSearchNarrower).narrowByClassSensorAssignment(classCache, sensorAssignment);
			doReturn(Collections.emptyList()).when(instrumentationService).removeInstrumentationPoints(eq(types), Mockito.<Collection<IInstrumentationApplier>> any());
			doReturn(Collections.singletonList(sensorAssignment)).when(event).getRemovedSensorAssignments(functionalAssignmentFactory);
			job.setEnvironmentUpdateEvent(event);

			job.run();

			verify(configurationHolder, times(1)).update(updateEnvironment, PLATFORM_ID);

			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			verify(instrumentationService, times(1)).removeInstrumentationPoints(types, Collections.singleton(instrumentationApplier));

			verifyNoMoreInteractions(instrumentationService);
			verifyZeroInteractions(environment, agentConfiguration);
		}
	}
}
