package rocks.inspectit.server.instrumentation.config.job;

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

import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheInstrumentation;
import rocks.inspectit.server.instrumentation.config.AgentCacheEntry;
import rocks.inspectit.server.instrumentation.config.ClassCacheSearchNarrower;
import rocks.inspectit.server.instrumentation.config.ConfigurationHolder;
import rocks.inspectit.server.instrumentation.config.ConfigurationResolver;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

@SuppressWarnings({ "all", "unchecked" })
public class ProfileUpdateJobTest extends TestBase {

	@InjectMocks
	protected ProfileUpdateJob job;

	@Mock
	protected Logger log;

	@Mock
	protected ClassCacheSearchNarrower classCacheSearchNarrower;

	@Mock
	protected AgentCacheEntry agentCacheEntry;

	@Mock
	protected ClassCache classCache;

	@Mock
	protected ConfigurationHolder configurationHolder;

	@Mock
	protected AgentConfig agentConfiguration;

	@Mock
	protected Environment environment;

	@Mock
	protected ConfigurationResolver configurationResolver;

	@Mock
	protected ClassCacheInstrumentation instrumentationService;

	@Mock
	protected AbstractClassSensorAssignment<?> sensorAssignment;

	@Mock
	protected IInstrumentationApplier holdedInstrumentationApplier;

	@Mock
	protected IInstrumentationApplier instrumentationApplier;

	@Mock
	protected ClassType classType;

	@Mock
	protected ProfileUpdateEvent event;

	@BeforeMethod
	public void setup() throws Exception {
		when(configurationHolder.getAgentConfiguration()).thenReturn(agentConfiguration);
		when(configurationHolder.getEnvironment()).thenReturn(environment);
		when(configurationHolder.getInstrumentationAppliers()).thenReturn(Collections.singletonList(holdedInstrumentationApplier));

		when(agentCacheEntry.getConfigurationHolder()).thenReturn(configurationHolder);
		when(agentCacheEntry.getClassCache()).thenReturn(classCache);

		when(classCache.getInstrumentationService()).thenReturn(instrumentationService);
	}

	public class Run extends ProfileUpdateJobTest {

		@Test
		public void noChanges() {
			job.setProfileUpdateEvent(event);

			job.run();

			verifyZeroInteractions(classCache, environment, classCacheSearchNarrower, agentConfiguration, instrumentationService);
		}

		@Test
		public void addedAssignment() throws RemoteException {
			Collection<ClassType> types = Collections.singleton(classType);

			doReturn(instrumentationApplier).when(configurationResolver).getInstrumentationApplier(sensorAssignment, environment);
			doReturn(types).when(classCacheSearchNarrower).narrowByClassSensorAssignment(classCache, sensorAssignment);
			doReturn(types).when(instrumentationService).addInstrumentationPoints(eq(types), eq(agentConfiguration), Mockito.<Collection<IInstrumentationApplier>> any());
			doReturn(Collections.singleton(sensorAssignment)).when(event).getAddedSensorAssignments();
			job.setProfileUpdateEvent(event);

			job.run();

			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			verify(instrumentationService, times(1)).addInstrumentationPoints(eq(types), eq(agentConfiguration), captor.capture());

			assertThat((Collection<IInstrumentationApplier>) captor.getValue(), hasSize(1));
			assertThat(((Collection<IInstrumentationApplier>) captor.getValue()).iterator().next(), is(instrumentationApplier));

			verifyNoMoreInteractions(instrumentationService);
			verifyZeroInteractions(environment);
		}

		@Test
		public void removedAssignment() throws RemoteException {
			Collection<ClassType> types = Collections.singleton(classType);

			doReturn(instrumentationApplier).when(configurationResolver).getInstrumentationApplier(sensorAssignment, environment);
			doReturn(types).when(classCacheSearchNarrower).narrowByClassSensorAssignment(classCache, sensorAssignment);
			doReturn(types).when(instrumentationService).removeInstrumentationPoints(eq(types), Mockito.<Collection<IInstrumentationApplier>> any());
			doReturn(Collections.singleton(sensorAssignment)).when(event).getRemovedSensorAssignments();
			job.setProfileUpdateEvent(event);

			job.run();

			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			verify(instrumentationService, times(1)).removeInstrumentationPoints(eq(types), captor.capture());

			assertThat((Collection<IInstrumentationApplier>) captor.getValue(), hasSize(1));
			assertThat(((Collection<IInstrumentationApplier>) captor.getValue()).iterator().next(), is(instrumentationApplier));

			Collection<IInstrumentationApplier> appliers = configurationHolder.getInstrumentationAppliers();
			verify(instrumentationService, times(1)).addInstrumentationPoints(captor.capture(), eq(agentConfiguration), eq(appliers));
			assertThat((Collection<ClassType>) captor.getValue(), hasSize(1));
			assertThat(((Collection<ClassType>) captor.getValue()).iterator().next(), is(classType));

			verifyNoMoreInteractions(instrumentationService);
			verifyZeroInteractions(environment);
		}

		@Test
		public void removedAssignmentNoChange() throws RemoteException {
			Collection<ClassType> types = Collections.singleton(classType);

			doReturn(instrumentationApplier).when(configurationResolver).getInstrumentationApplier(sensorAssignment, environment);
			doReturn(types).when(classCacheSearchNarrower).narrowByClassSensorAssignment(classCache, sensorAssignment);
			doReturn(Collections.emptyList()).when(instrumentationService).removeInstrumentationPoints(eq(types), Mockito.<Collection<IInstrumentationApplier>> any());
			doReturn(Collections.singleton(sensorAssignment)).when(event).getRemovedSensorAssignments();
			job.setProfileUpdateEvent(event);

			job.run();

			ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
			verify(instrumentationService, times(1)).removeInstrumentationPoints(eq(types), captor.capture());

			assertThat((Collection<IInstrumentationApplier>) captor.getValue(), hasSize(1));
			assertThat(((Collection<IInstrumentationApplier>) captor.getValue()).iterator().next(), is(instrumentationApplier));

			verifyNoMoreInteractions(instrumentationService);
			verifyZeroInteractions(environment);
		}
	}

}
