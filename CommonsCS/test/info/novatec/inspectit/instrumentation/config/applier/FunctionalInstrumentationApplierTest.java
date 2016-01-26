/**
 *
 */
package info.novatec.inspectit.instrumentation.config.applier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.FunctionalMethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.MethodType.Character;
import info.novatec.inspectit.instrumentation.config.FunctionalInstrumentationType;
import info.novatec.inspectit.instrumentation.config.filter.AssignmentFilterProvider;
import info.novatec.inspectit.instrumentation.config.filter.ClassSensorAssignmentFilter;
import info.novatec.inspectit.instrumentation.config.filter.MethodSensorAssignmentFilter;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class FunctionalInstrumentationApplierTest extends TestBase {

	protected FunctionalInstrumentationApplier applier;

	@Mock
	protected FunctionalMethodSensorAssignment assignment;

	@Mock
	protected AssignmentFilterProvider filterProvider;

	@Mock
	protected Environment environment;

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
		applier = new FunctionalInstrumentationApplier(assignment, environment);
		applier.assignmentFilterProvider = filterProvider;

		// filters to true by default
		when(filterProvider.getClassSensorAssignmentFilter()).thenReturn(classFilter);
		when(filterProvider.getMethodSensorAssignmentFilter()).thenReturn(methodFilter);
		when(methodFilter.matches(Mockito.<MethodSensorAssignment> any(), Mockito.<MethodType> any())).thenReturn(true);
		when(classFilter.matches(Mockito.<AbstractClassSensorAssignment<?>> any(), Mockito.<ClassType> any())).thenReturn(true);

		// class to return one method
		when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
	}

	public class AddInstrumentationPoints extends FunctionalInstrumentationApplierTest {

		@Test
		public void add() throws Exception {
			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
			FunctionalInstrumentationType instrumentationType = FunctionalInstrumentationType.CLASS_LOADING_DELEGATION;
			when(assignment.getInstrumentationType()).thenReturn(instrumentationType);

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

			// check instrumentation config
			ArgumentCaptor<MethodInstrumentationConfig> captor = ArgumentCaptor.forClass(MethodInstrumentationConfig.class);
			verify(methodType, times(1)).setMethodInstrumentationConfig(captor.capture());

			MethodInstrumentationConfig instrumentationConfig = captor.getValue();
			assertThat(instrumentationConfig.getTargetClassFqn(), is(packageName + '.' + className));
			assertThat(instrumentationConfig.getTargetMethodName(), is(methodName));
			assertThat(instrumentationConfig.getReturnType(), is(returnType));
			assertThat(instrumentationConfig.getParameterTypes(), is(parameters));
			assertThat(instrumentationConfig.getAllInstrumentationPoints(), hasSize(1));
		}

		@Test
		public void doesNotMatchClassFilter() throws Exception {
			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
			when(classFilter.matches(assignment, classType)).thenReturn(false);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(methodType);
		}

		@Test
		public void doesNotMatchMethodFilter() throws Exception {
			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
			when(methodFilter.matches(assignment, methodType)).thenReturn(false);

			// test direct and via collection
			// we expect two calls to everything
			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(false));

			verifyZeroInteractions(methodType);
		}
	}

	public class RemoveInstrumentationPoints extends FunctionalInstrumentationApplierTest {

		@Test
		public void remove() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType)).thenReturn(true);
			when(methodFilter.matches(assignment, methodType)).thenReturn(true);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(true));

			verify(methodType, times(1)).setMethodInstrumentationConfig(null);
		}

		@Test
		public void doesNotmatchClassFilter() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType)).thenReturn(false);
			when(methodFilter.matches(assignment, methodType)).thenReturn(true);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));

			verifyZeroInteractions(methodType);
		}

		@Test
		public void doesNotmatchMethodFilter() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classFilter.matches(assignment, classType)).thenReturn(true);
			when(methodFilter.matches(assignment, methodType)).thenReturn(false);

			boolean removed = applier.removeInstrumentationPoints(classType);

			assertThat(removed, is(false));

			verifyZeroInteractions(methodType);
		}
	}

}
