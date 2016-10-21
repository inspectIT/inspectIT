package rocks.inspectit.server.instrumentation.config.applier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SubstitutionDescriptor;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpecialInstrumentationApplierTest extends TestBase {

	protected SpecialInstrumentationApplier applier;

	@Mock
	protected SpecialMethodSensorAssignment assignment;

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
		applier = new SpecialInstrumentationApplier(assignment, environment, registrationService);
		applier.assignmentFilterProvider = filterProvider;

		// filters to true by default
		when(filterProvider.getClassSensorAssignmentFilter()).thenReturn(classFilter);
		when(filterProvider.getMethodSensorAssignmentFilter()).thenReturn(methodFilter);
		when(methodFilter.matches(Matchers.<MethodSensorAssignment> any(), Matchers.<MethodType> any())).thenReturn(true);
		when(classFilter.matches(Matchers.<AbstractClassSensorAssignment<?>> any(), Matchers.<ClassType> any(), Matchers.eq(false))).thenReturn(true);

		// class to return one method
		when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
		when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
	}

	public class AddInstrumentationPoints extends SpecialInstrumentationApplierTest {

		@Test
		public void add() throws Exception {
			long agentId = 13L;
			long sensorId = 15L;
			long methodId = 17L;
			String sensorClassName = "sensorClassName";
			when(registrationService.registerMethodIdent(eq(agentId), anyString(), anyString(), anyString(), Matchers.<List<String>> any(), anyString(), anyInt())).thenReturn(methodId);

			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensorTypeConfig.getId()).thenReturn(sensorId);
			when(methodSensorTypeConfig.getPriority()).thenReturn(PriorityEnum.NORMAL);

			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(agentConfiguration.getPlatformId()).thenReturn(agentId);
			when(agentConfiguration.getSpecialMethodSensorTypeConfig(sensorClassName)).thenReturn(methodSensorTypeConfig);

			AbstractSpecialMethodSensorConfig methodSensorConfig = mock(AbstractSpecialMethodSensorConfig.class);
			when(methodSensorConfig.getClassName()).thenReturn(sensorClassName);
			when(assignment.getSpecialMethodSensorConfig()).thenReturn(methodSensorConfig);
			SubstitutionDescriptor substitutionDescriptor = mock(SubstitutionDescriptor.class);
			when(methodSensorConfig.getSubstitutionDescriptor()).thenReturn(substitutionDescriptor);

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

			// check instrumentation config
			ArgumentCaptor<MethodInstrumentationConfig> captor = ArgumentCaptor.forClass(MethodInstrumentationConfig.class);
			verify(methodType, times(1)).setMethodInstrumentationConfig(captor.capture());

			MethodInstrumentationConfig instrumentationConfig = captor.getValue();
			assertThat(instrumentationConfig.getTargetClassFqn(), is(packageName + '.' + className));
			assertThat(instrumentationConfig.getTargetMethodName(), is(methodName));
			assertThat(instrumentationConfig.getReturnType(), is(returnType));
			assertThat(instrumentationConfig.getParameterTypes(), is(parameters));
			assertThat(instrumentationConfig.getAllInstrumentationPoints(), hasSize(1));
			SpecialInstrumentationPoint ssc = instrumentationConfig.getSpecialInstrumentationPoint();
			assertThat(ssc.getId(), is(methodId));
			assertThat(ssc.getSensorId(), is(sensorId));
			assertThat(ssc.getSubstitutionDescriptor(), is(substitutionDescriptor));
			assertThat(instrumentationConfig.getSensorInstrumentationPoint(), is(nullValue()));
		}

		@Test
		public void doesNotMatchClassFilter() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(classFilter.matches(assignment, classType, false)).thenReturn(false);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));
			verifyZeroInteractions(methodType);
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
			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
		}

		@Test
		public void doesNotInstrumentConstructor() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(methodType.getMethodCharacter()).thenReturn(Character.CONSTRUCTOR);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));
			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
		}

		@Test
		public void doesNotInstrumentStaticConstructor() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(methodType.getMethodCharacter()).thenReturn(Character.STATIC_CONSTRUCTOR);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));
			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
		}
	}

	public class RemoveInstrumentationPoints extends SpecialInstrumentationApplierTest {

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
		public void doesNotMatchClassFilter() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType, false)).thenReturn(false);
			when(methodFilter.matches(assignment, methodType)).thenReturn(true);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));
			verifyZeroInteractions(methodType);
		}

		@Test
		public void doesNotMatchMethodFilter() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType, false)).thenReturn(true);
			when(methodFilter.matches(assignment, methodType)).thenReturn(false);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));
			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
		}

		@Test
		public void doesNotMatchConstructor() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType, false)).thenReturn(true);
			when(methodFilter.matches(assignment, methodType)).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.CONSTRUCTOR);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));
			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
		}

		@Test
		public void doesNotMatchStaticConstructor() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType, false)).thenReturn(true);
			when(methodFilter.matches(assignment, methodType)).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.STATIC_CONSTRUCTOR);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));
			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
		}
	}

}
