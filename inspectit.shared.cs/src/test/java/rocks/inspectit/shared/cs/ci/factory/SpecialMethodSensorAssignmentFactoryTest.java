package rocks.inspectit.shared.cs.ci.factory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ClassLoadingDelegationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.MBeanServerInterceptorSensorConfig;

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
				assertThat(assignment.getSpecialMethodSensorConfig(), is((AbstractSpecialMethodSensorConfig) ClassLoadingDelegationSensorConfig.INSTANCE));
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
				assertThat(assignment.getSpecialMethodSensorConfig(), is((AbstractSpecialMethodSensorConfig) MBeanServerInterceptorSensorConfig.INSTANCE));
			}
		}
	}
}
