package rocks.inspectit.shared.cs.ci.factory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.config.SpecialInstrumentationType;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpecialMethodSensorAssignmentFactoryTest extends TestBase {

	@InjectMocks
	SpecialMethodSensorAssignmentFactory factory;

	@Mock
	Environment environment;

	public class GetSpecialAssignments extends SpecialMethodSensorAssignmentFactoryTest {

		@BeforeMethod
		public void setup() {
			factory.init();
		}

		@Test
		public void noSpecialAssignment() {
			when(environment.isClassLoadingDelegation()).thenReturn(false);
			when(environment.getJmxSensorConfig()).thenReturn(null);

			Collection<SpecialMethodSensorAssignment> assignments = factory.getSpecialAssignments(environment);

			assertThat(assignments, is(empty()));
		}

		@Test
		public void jmxSensorNotActive() {
			JmxSensorConfig jmxSensorConfig = mock(JmxSensorConfig.class);
			when(environment.getJmxSensorConfig()).thenReturn(jmxSensorConfig);
			when(jmxSensorConfig.isActive()).thenReturn(false);

			Collection<SpecialMethodSensorAssignment> assignments = factory.getSpecialAssignments(environment);

			assertThat(assignments, is(empty()));
		}

		@Test
		public void classLoadingDelegation() {
			when(environment.isClassLoadingDelegation()).thenReturn(true);
			when(environment.getJmxSensorConfig()).thenReturn(null);

			Collection<SpecialMethodSensorAssignment> assignments = factory.getSpecialAssignments(environment);

			assertThat(assignments, is(not(empty())));
			for (SpecialMethodSensorAssignment assignment : assignments) {
				assertThat(assignment.getInstrumentationType(), is(SpecialInstrumentationType.CLASS_LOADING_DELEGATION));
			}
		}

		@Test
		public void mbeanServer() {
			JmxSensorConfig jmxSensorConfig = mock(JmxSensorConfig.class);
			when(environment.isClassLoadingDelegation()).thenReturn(false);
			when(environment.getJmxSensorConfig()).thenReturn(jmxSensorConfig);
			when(jmxSensorConfig.isActive()).thenReturn(true);

			Collection<SpecialMethodSensorAssignment> assignments = factory.getSpecialAssignments(environment);

			assertThat(assignments, is(not(empty())));
			for (SpecialMethodSensorAssignment assignment : assignments) {
				assertThat(assignment.getInstrumentationType(), is(anyOf(equalTo(SpecialInstrumentationType.MBEAN_SERVER_ADD), equalTo(SpecialInstrumentationType.MBEAN_SERVER_REMOVE))));
			}
		}
	}
}
