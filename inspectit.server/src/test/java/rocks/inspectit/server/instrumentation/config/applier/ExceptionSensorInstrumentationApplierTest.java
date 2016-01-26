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

import rocks.inspectit.server.instrumentation.config.applier.ExceptionSensorInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.filter.AssignmentFilterProvider;
import rocks.inspectit.server.instrumentation.config.filter.ClassSensorAssignmentFilter;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType.Character;
import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ExceptionSensorInstrumentationApplierTest extends TestBase {

	protected ExceptionSensorInstrumentationApplier applier;

	@Mock
	protected ExceptionSensorAssignment assignment;

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
	protected ClassSensorAssignmentFilter classFilter;

	@BeforeMethod
	public void setup() {
		applier = new ExceptionSensorInstrumentationApplier(assignment, environment, registrationService);
		applier.assignmentFilterProvider = filterProvider;

		// filters to true by default
		when(filterProvider.getClassSensorAssignmentFilter()).thenReturn(classFilter);
		when(classFilter.matches(Mockito.<AbstractClassSensorAssignment<?>> any(), Mockito.<ClassType> any())).thenReturn(true);

		// class to return one method
		when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
	}

	public class AddInstrumentationPoints extends ExceptionSensorInstrumentationApplierTest {

		@Test
		public void add() throws Exception {
			long agentId = 13L;
			long sensorId = 15L;
			long methodId = 17L;
			when(registrationService.registerMethodIdent(eq(agentId), anyString(), anyString(), anyString(), Mockito.<List<String>> any(), anyString(), anyInt())).thenReturn(methodId);

			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(exceptionSensorTypeConfig.getId()).thenReturn(sensorId);
			when(exceptionSensorTypeConfig.getPriority()).thenReturn(PriorityEnum.NORMAL);

			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(agentConfiguration.getPlatformId()).thenReturn(agentId);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);

			Map<String, Object> settings = Collections.<String, Object> singletonMap("key", "value");
			when(assignment.getSettings()).thenReturn(settings);

			String packageName = "my.favorite.package";
			String className = "ClassName";
			String methodName = "<init>";
			String returnType = "returnType";
			List<String> parameters = Arrays.asList(new String[] {});
			int mod = 10;
			when(classType.getFQN()).thenReturn(packageName + '.' + className);
			when(methodType.getClassOrInterfaceType()).thenReturn(classType);
			when(methodType.getName()).thenReturn(methodName);
			when(methodType.getParameters()).thenReturn(parameters);
			when(methodType.getReturnType()).thenReturn(returnType);
			when(methodType.getMethodCharacter()).thenReturn(Character.CONSTRUCTOR);
			when(methodType.getModifiers()).thenReturn(mod);
			when(classType.isException()).thenReturn(true);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(true));

			// verify registration service
			// for constructors the registered method name is class name
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
		public void doesNotMatchClassFilter() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(classType.isException()).thenReturn(true);
			when(classFilter.matches(assignment, classType)).thenReturn(false);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(registrationService, methodType);
		}

		@Test
		public void doesNotMatchExceptionClass() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(classType.isException()).thenReturn(false);
			when(classFilter.matches(assignment, classType)).thenReturn(true);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(registrationService, methodType);
		}

		@Test
		public void doesNotMatchMethod() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(classType.isException()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
			verifyZeroInteractions(registrationService);
		}

		@Test
		public void doesNotMatchStaticConstructor() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(classType.isException()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.STATIC_CONSTRUCTOR);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verify(methodType).getMethodCharacter();
			verifyNoMoreInteractions(methodType);
			verifyZeroInteractions(registrationService);
		}
	}

	public class RemoveInstrumentationPoints extends ExceptionSensorInstrumentationApplierTest {

		@Test
		public void remove() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.isException()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType)).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.CONSTRUCTOR);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(true));

			verify(methodType, times(1)).setMethodInstrumentationConfig(Mockito.<MethodInstrumentationConfig> any());
		}

		@Test
		public void doesNotMatchClassFilter() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.isException()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType)).thenReturn(false);
			when(methodType.getName()).thenReturn("<init>");

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));

			verify(methodType, times(0)).setMethodInstrumentationConfig(Mockito.<MethodInstrumentationConfig> any());
		}

		@Test
		public void doesNotMatchConstructorMethod() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.isException()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType)).thenReturn(true);
			when(methodType.getName()).thenReturn("whatever");

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));

			verify(methodType, times(0)).setMethodInstrumentationConfig(Mockito.<MethodInstrumentationConfig> any());
		}
	}

}
