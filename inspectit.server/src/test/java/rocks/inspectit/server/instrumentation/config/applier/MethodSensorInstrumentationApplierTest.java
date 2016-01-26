package rocks.inspectit.server.instrumentation.config.applier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.instrumentation.config.filter.AssignmentFilterProvider;
import rocks.inspectit.server.instrumentation.config.filter.ClassSensorAssignmentFilter;
import rocks.inspectit.server.instrumentation.config.filter.MethodSensorAssignmentFilter;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType.Character;
import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class MethodSensorInstrumentationApplierTest extends TestBase {

	protected MethodSensorInstrumentationApplier applier;

	@Mock
	protected MethodSensorAssignment assignment;

	@Mock
	protected AssignmentFilterProvider filterProvider;

	@Mock
	protected Environment environment;

	@Mock
	protected IRegistrationService registrationService;

	@Mock
	protected ClassType classType;

	@Mock
	protected MethodType methodType;

	@Mock
	protected MethodSensorAssignmentFilter methodFilter;

	@Mock
	protected ClassSensorAssignmentFilter classFilter;

	@BeforeMethod
	public void setup() {
		applier = new MethodSensorInstrumentationApplier(assignment, environment, registrationService);
		applier.assignmentFilterProvider = filterProvider;

		// filters to true by default
		when(filterProvider.getClassSensorAssignmentFilter()).thenReturn(classFilter);
		when(filterProvider.getMethodSensorAssignmentFilter()).thenReturn(methodFilter);
		when(methodFilter.matches(Mockito.<MethodSensorAssignment> any(), Mockito.<MethodType> any())).thenReturn(true);
		when(classFilter.matches(Mockito.<AbstractClassSensorAssignment<?>> any(), Mockito.<ClassType> any(), Mockito.eq(false))).thenReturn(true);

		// class to return one method
		when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
	}

	public class AddInstrumentationPoints extends MethodSensorInstrumentationApplierTest {

		@Test
		public void add() throws Exception {
			long agentId = 13L;
			long sensorId = 15L;
			long methodId = 17L;
			String sensorClassName = "sensorClassName";
			when(registrationService.registerMethodIdent(eq(agentId), anyString(), anyString(), anyString(), Mockito.<List<String>> any(), anyString(), anyInt())).thenReturn(methodId);

			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensorTypeConfig.getId()).thenReturn(sensorId);
			when(methodSensorTypeConfig.getPriority()).thenReturn(PriorityEnum.NORMAL);

			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(agentConfiguration.getPlatformId()).thenReturn(agentId);
			when(agentConfiguration.getMethodSensorTypeConfig(sensorClassName)).thenReturn(methodSensorTypeConfig);

			Map<String, Object> settings = Collections.<String, Object> singletonMap("key", "value");
			when(assignment.getSettings()).thenReturn(settings);

			IMethodSensorConfig methodSensorConfig = mock(IMethodSensorConfig.class);
			when(methodSensorConfig.getClassName()).thenReturn(sensorClassName);
			when(environment.getMethodSensorTypeConfig(Mockito.<Class<? extends IMethodSensorConfig>> any())).thenReturn(methodSensorConfig);

			String packageName = "my.favorite.package";
			String className = "ClassName";
			String methodName = "methodName";
			String returnType = "returnType";
			List<String> parameters = Arrays.asList(new String[] { "p1", "p2" });
			int mod = 10;
			when(classType.getFQN()).thenReturn(packageName + '.' + className);
			when(methodType.getClassOrInterfaceType()).thenReturn(classType);
			when(methodType.getName()).thenReturn(methodName);
			when(methodType.getParameters()).thenReturn(parameters);
			when(methodType.getReturnType()).thenReturn(returnType);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getModifiers()).thenReturn(mod);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(true));

			// verify registration service
			verify(registrationService, times(1)).registerMethodIdent(agentId, packageName, className, methodName, parameters, returnType, mod);
			verifyNoMoreInteractions(registrationService);

			// check RSC and instrumentation config
			ArgumentCaptor<MethodInstrumentationConfig> captor = ArgumentCaptor.forClass(MethodInstrumentationConfig.class);
			verify(methodType, times(1)).setMethodInstrumentationConfig(captor.capture());

			MethodInstrumentationConfig instrumentationConfig = captor.getValue();
			SensorInstrumentationPoint rsc = instrumentationConfig.getSensorInstrumentationPoint();
			assertThat(instrumentationConfig.getTargetClassFqn(), is(packageName + '.' + className));
			assertThat(instrumentationConfig.getTargetMethodName(), is(methodName));
			assertThat(instrumentationConfig.getReturnType(), is(returnType));
			assertThat(instrumentationConfig.getParameterTypes(), is(parameters));
			assertThat(instrumentationConfig.getAllInstrumentationPoints(), hasSize(1));
			assertThat(rsc.getId(), is(methodId));
			assertThat(rsc.getSensorIds().length, is(1));
			assertThat(rsc.getSensorIds()[0], is(sensorId));
			assertThat(rsc.getSettings(), is(settings));
		}

		@Test
		public void instrumentationExist() throws Exception {
			long agentId = 13L;
			long sensorId = 15L;
			String sensorClassName = "sensorClassName";

			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensorTypeConfig.getId()).thenReturn(sensorId);
			when(methodSensorTypeConfig.getPriority()).thenReturn(PriorityEnum.NORMAL);

			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(agentConfiguration.getPlatformId()).thenReturn(agentId);
			when(agentConfiguration.getMethodSensorTypeConfig(sensorClassName)).thenReturn(methodSensorTypeConfig);

			Map<String, Object> settings = Collections.<String, Object> singletonMap("key", "value");
			when(assignment.getSettings()).thenReturn(settings);

			IMethodSensorConfig methodSensorConfig = mock(IMethodSensorConfig.class);
			when(methodSensorConfig.getClassName()).thenReturn(sensorClassName);
			when(environment.getMethodSensorTypeConfig(Mockito.<Class<? extends IMethodSensorConfig>> any())).thenReturn(methodSensorConfig);

			SensorInstrumentationPoint rsc = mock(SensorInstrumentationPoint.class);
			when(rsc.getSensorIds()).thenReturn(new long[] { sensorId });
			MethodInstrumentationConfig instrumentationConfig = mock(MethodInstrumentationConfig.class);
			when(instrumentationConfig.getSensorInstrumentationPoint()).thenReturn(rsc);
			when(methodType.getMethodInstrumentationConfig()).thenReturn(instrumentationConfig);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(true));

			// verify no interaction
			verifyZeroInteractions(registrationService);
			verify(instrumentationConfig, times(0)).setSensorInstrumentationPoint(Mockito.<SensorInstrumentationPoint> any());
			verify(methodType, times(0)).setMethodInstrumentationConfig(Mockito.<MethodInstrumentationConfig> any());
		}

		@Test
		public void doesNotMatchClassFilter() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(classFilter.matches(assignment, classType, false)).thenReturn(false);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(registrationService, methodType);
		}

		@Test
		public void doesNotMatchMethodFilter() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(methodFilter.matches(assignment, methodType)).thenReturn(false);

			// test direct and via collection
			// we expect two calls to everything
			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(registrationService, methodType);
		}

	}

	public class RemoveInstrumentationPoints extends MethodSensorInstrumentationApplierTest {

		@Test
		public void remove() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType, false)).thenReturn(true);
			when(methodFilter.matches(assignment, methodType)).thenReturn(true);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(true));

			verify(methodType, times(1)).setMethodInstrumentationConfig(null);
		}

		@Test
		public void doesNotmatchClassFilter() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType, false)).thenReturn(false);
			when(methodFilter.matches(assignment, methodType)).thenReturn(true);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));

			verifyZeroInteractions(methodType);
		}

		@Test
		public void doesNotmatchMethodFilter() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType, false)).thenReturn(true);
			when(methodFilter.matches(assignment, methodType)).thenReturn(false);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));

			verifyZeroInteractions(methodType);
		}
	}

}
