package rocks.inspectit.server.instrumentation.config.applier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
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
import rocks.inspectit.shared.cs.ci.assignment.impl.InvocationStartMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class InvocationStartMethodSensorInstrumentationApplierTest extends TestBase {

	protected InvocationStartMethodSensorInstrumentationApplier applier;

	@Mock
	protected InvocationStartMethodSensorAssignment assignment;

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
		applier = new InvocationStartMethodSensorInstrumentationApplier(assignment, environment, registrationService);
		applier.assignmentFilterProvider = filterProvider;

		// filters to true by default
		when(filterProvider.getClassSensorAssignmentFilter()).thenReturn(classFilter);
		when(filterProvider.getMethodSensorAssignmentFilter()).thenReturn(methodFilter);
		when(methodFilter.matches(Matchers.<MethodSensorAssignment> any(), Matchers.<MethodType> any())).thenReturn(true);
		when(classFilter.matches(Matchers.<AbstractClassSensorAssignment<?>> any(), Matchers.<ClassType> any(), Matchers.eq(false))).thenReturn(true);

		// class to return one method
		when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
	}

	public class AddInstrumentationPoints extends InvocationStartMethodSensorInstrumentationApplierTest {

		@Test
		public void add() throws Exception {
			long agentId = 13L;
			long sensorId = 15L;
			long methodId = 17L;
			long invocationSensorId = 19L;
			String sensorClassName = "sensorClassName";
			String invocClassName = "invocClassName";
			when(registrationService.registerMethodIdent(eq(agentId), anyString(), anyString(), anyString(), Matchers.<List<String>> any(), anyString(), anyInt())).thenReturn(methodId);

			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensorTypeConfig.getId()).thenReturn(sensorId);
			when(methodSensorTypeConfig.getPriority()).thenReturn(PriorityEnum.NORMAL);

			MethodSensorTypeConfig invocSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(invocSensorTypeConfig.getId()).thenReturn(invocationSensorId);
			when(invocSensorTypeConfig.getPriority()).thenReturn(PriorityEnum.INVOC);

			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(agentConfiguration.getPlatformId()).thenReturn(agentId);
			when(agentConfiguration.getMethodSensorTypeConfig(sensorClassName)).thenReturn(methodSensorTypeConfig);
			when(agentConfiguration.getMethodSensorTypeConfig(invocClassName)).thenReturn(invocSensorTypeConfig);

			when(assignment.isStartsInvocation()).thenReturn(true);

			IMethodSensorConfig methodSensorConfig = mock(IMethodSensorConfig.class);
			when(methodSensorConfig.getClassName()).thenReturn(sensorClassName);
			when(environment.getMethodSensorTypeConfig(Matchers.<Class<? extends IMethodSensorConfig>> any())).thenReturn(methodSensorConfig);

			IMethodSensorConfig invocSensorConfig = mock(IMethodSensorConfig.class);
			when(invocSensorConfig.getClassName()).thenReturn(invocClassName);
			when(environment.getMethodSensorTypeConfig(InvocationSequenceSensorConfig.class)).thenReturn(invocSensorConfig);

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

			boolean added = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(added, is(true));

			// verify registration service
			verify(registrationService, times(1)).registerMethodIdent(agentId, packageName, className, methodName, parameters, returnType, mod);
			verifyNoMoreInteractions(registrationService);

			// check RSC
			ArgumentCaptor<MethodInstrumentationConfig> captor = ArgumentCaptor.forClass(MethodInstrumentationConfig.class);
			verify(methodType, times(1)).setMethodInstrumentationConfig(captor.capture());

			MethodInstrumentationConfig instrumentationConfig = captor.getValue();
			SensorInstrumentationPoint rsc = instrumentationConfig.getSensorInstrumentationPoint();

			// just check related to the timer sensor stuff
			assertThat(rsc.getId(), is(methodId));
			assertThat(rsc.getSensorIds().length, is(2));
			assertThat(rsc.getSensorIds()[0], is(sensorId));
			assertThat(rsc.getSensorIds()[1], is(invocationSensorId));
			assertThat(rsc.isStartsInvocation(), is(true));
			assertThat(rsc.isPropertyAccess(), is(false));
		}
	}
}
