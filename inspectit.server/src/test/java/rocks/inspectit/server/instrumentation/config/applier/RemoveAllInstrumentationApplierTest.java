package rocks.inspectit.server.instrumentation.config.applier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

/**
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class RemoveAllInstrumentationApplierTest extends TestBase {

	@InjectMocks
	RemoveAllInstrumentationApplier applier;

	/**
	 * Tests the {@link RemoveAllInstrumentationApplier#getSensorAssignment()} method.
	 */
	public static class GetSensorAssignment extends RemoveAllInstrumentationApplierTest {

		@Test
		public void returnNull() {
			AbstractClassSensorAssignment<?> result = applier.getSensorAssignment();

			assertThat(result, is(nullValue()));
		}
	}

	/**
	 * Tests the
	 * {@link RemoveAllInstrumentationApplier#addInstrumentationPoints(AgentConfig, ClassType)}
	 * method.
	 */
	public static class AddInstrumentationPoints extends RemoveAllInstrumentationApplierTest {

		@Mock
		AgentConfig agentConfiguration;

		@Mock
		ClassType classType;

		@Test
		public void returnFalse() {
			boolean result = applier.addInstrumentationPoints(agentConfiguration, classType);

			assertThat(result, is(false));
			verifyZeroInteractions(agentConfiguration, classType);
		}
	}

	/**
	 * Tests the {@link RemoveAllInstrumentationApplier#removeInstrumentationPoints(ClassType)}
	 * method.
	 */
	public static class RemoveInstrumentationPoints extends RemoveAllInstrumentationApplierTest {

		@Mock
		ClassType classType;

		@Mock
		MethodType methodType;

		@Test
		public void removeInstrumentationPoints() {
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Sets.newHashSet(methodType));

			boolean result = applier.removeInstrumentationPoints(classType);

			assertThat(result, is(true));
			verify(classType).hasInstrumentationPoints();
			verify(classType).getMethods();
			verify(methodType).setMethodInstrumentationConfig(null);
			verifyNoMoreInteractions(classType, methodType);
		}

		@Test
		public void noInstrumentationPoints() {
			when(classType.hasInstrumentationPoints()).thenReturn(false);

			boolean result = applier.removeInstrumentationPoints(classType);

			assertThat(result, is(false));
			verify(classType).hasInstrumentationPoints();
			verifyNoMoreInteractions(classType);
		}


		@Test(expectedExceptions = NullPointerException.class)
		public void nullClass() {
			try {
				when(classType.hasInstrumentationPoints()).thenReturn(false);

				applier.removeInstrumentationPoints(null);
			} finally {
				verifyZeroInteractions(classType, methodType);
			}
		}
	}

	/**
	 * Tests {@link RemoveAllInstrumentationApplier#getInstance()} method.
	 */
	public static class GetInstance extends RemoveAllInstrumentationApplierTest {

		@Test
		public void notNull() {
			RemoveAllInstrumentationApplier instance = RemoveAllInstrumentationApplier.getInstance();

			assertThat(instance, is(not(nullValue())));
		}
	}
}
