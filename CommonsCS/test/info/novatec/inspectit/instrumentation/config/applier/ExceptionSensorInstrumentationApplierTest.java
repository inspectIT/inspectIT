/**
 *
 */
package info.novatec.inspectit.instrumentation.config.applier;

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

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.MethodType.Character;
import info.novatec.inspectit.instrumentation.config.PriorityEnum;
import info.novatec.inspectit.instrumentation.config.filter.AssignmentFilterProvider;
import info.novatec.inspectit.instrumentation.config.filter.ClassSensorAssignmentFilter;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.ExceptionSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
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
			verify(registrationService, times(1)).addSensorTypeToMethod(sensorId, methodId);
			verifyNoMoreInteractions(registrationService);

			// check RSC and instrumentation config
			ArgumentCaptor<MethodInstrumentationConfig> captor = ArgumentCaptor.forClass(MethodInstrumentationConfig.class);
			verify(methodType, times(1)).setMethodInstrumentationConfig(captor.capture());

			MethodInstrumentationConfig instrumentationConfig = captor.getValue();
			RegisteredSensorConfig rsc = instrumentationConfig.getRegisteredSensorConfig();
			assertThat(instrumentationConfig.getTargetClassFqn(), is(packageName + '.' + className));
			assertThat(instrumentationConfig.getTargetMethodName(), is(methodName));
			assertThat(instrumentationConfig.getReturnType(), is(returnType));
			assertThat(instrumentationConfig.getParameterTypes(), is(parameters));
			assertThat(instrumentationConfig.getAllInstrumentationPoints(), hasSize(1));
			assertThat(rsc.getId(), is(methodId));
			assertThat(rsc.getSensorIds().length, is(1));
			assertThat(rsc.getSensorIds()[0], is(sensorId));
			assertThat(rsc.getTargetClassFqn(), is(packageName + '.' + className));
			assertThat(rsc.getTargetMethodName(), is(methodName));
			assertThat(rsc.getReturnType(), is(returnType));
			assertThat(rsc.getParameterTypes(), is(parameters));
			assertThat(rsc.getSettings(), is(settings));
		}

		@Test
		public void doesNotMatchClassFilter() throws Exception {
			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
			when(classType.isException()).thenReturn(true);
			when(classFilter.matches(assignment, classType)).thenReturn(false);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(registrationService, methodType);
		}

		@Test
		public void doesNotMatchExceptionClass() throws Exception {
			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
			when(classType.isException()).thenReturn(false);
			when(classFilter.matches(assignment, classType)).thenReturn(true);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(registrationService, methodType);
		}

		@Test
		public void doesNotMatchConstructorMethod() throws Exception {
			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
			when(classType.isException()).thenReturn(true);
			when(methodType.getName()).thenReturn("something else");

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verify(methodType).getName();
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
			when(methodType.getName()).thenReturn("<init>");

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
