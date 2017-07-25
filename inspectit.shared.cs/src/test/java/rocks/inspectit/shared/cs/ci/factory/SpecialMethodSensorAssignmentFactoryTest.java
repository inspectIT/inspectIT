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
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.eum.EndUserMonitoringConfig;
import rocks.inspectit.shared.cs.ci.profile.data.AbstractProfileData;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ExecutorClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ClassLoadingDelegationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.EUMInstrumentationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.ExecutorIntercepterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.special.impl.MBeanServerInterceptorSensorConfig;
import rocks.inspectit.shared.cs.cmr.service.IConfigurationInterfaceService;

/**
 * Tests the {@link SpecialMethodSensorAssignmentFactory} class.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings({ "PMD", "unchecked", "all" })
public class SpecialMethodSensorAssignmentFactoryTest extends TestBase {

	@InjectMocks
	SpecialMethodSensorAssignmentFactory factory;

	@Mock
	Environment environment;

	@Mock
	IConfigurationInterfaceService ciService;

	@Mock
	Logger log;

	@BeforeMethod
	public void setup() {
		factory.init();
	}

	/**
	 * Tests the {@link SpecialMethodSensorAssignmentFactory#getSpecialAssignments(Environment)}
	 * method.
	 *
	 * @author Marius Oehler
	 *
	 */
	public class GetSpecialAssignments extends SpecialMethodSensorAssignmentFactoryTest {

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

		@Test
		public void endUserMonitoring() {

			EndUserMonitoringConfig eumConf = new EndUserMonitoringConfig();
			eumConf.setActiveModules("" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier());
			eumConf.setScriptBaseUrl("/");
			eumConf.setEumEnabled(true);

			when(environment.getEumConfig()).thenReturn(eumConf);

			Collection<SpecialMethodSensorAssignment> assignments = factory.getSpecialAssignments(environment);

			assertThat(assignments, is(not(empty())));
			for (SpecialMethodSensorAssignment assignment : assignments) {
				assertThat(assignment.getSpecialMethodSensorConfig(), is((AbstractSpecialMethodSensorConfig) EUMInstrumentationSensorConfig.INSTANCE));
			}
		}

		@Test
		public void executorClient() throws BusinessException {
			Profile profile = mock(Profile.class);
			when(profile.isActive()).thenReturn(true);
			when(environment.getProfileIds()).thenReturn(Sets.newHashSet("profile-id"));
			when(ciService.getProfile("profile-id")).thenReturn(profile);
			SensorAssignmentProfileData sapData = mock(SensorAssignmentProfileData.class);
			when(profile.getProfileData()).thenReturn((AbstractProfileData) sapData);
			MethodSensorAssignment msAssignment = mock(MethodSensorAssignment.class);
			when(sapData.getMethodSensorAssignments()).thenReturn(Lists.newArrayList(msAssignment));
			when(msAssignment.getSensorConfigClass()).thenReturn((Class) ExecutorClientSensorConfig.class);

			Collection<SpecialMethodSensorAssignment> assignments = factory.getSpecialAssignments(environment);

			assertThat(assignments, is(not(empty())));
			for (SpecialMethodSensorAssignment assignment : assignments) {
				assertThat(assignment.getSpecialMethodSensorConfig(), is((AbstractSpecialMethodSensorConfig) ExecutorIntercepterSensorConfig.INSTANCE));
			}
		}

		@Test
		public void executorClientProfilesNotLoaded() {
			when(environment.getProfileIds()).thenReturn(Sets.newHashSet("profile-id"));

			Collection<SpecialMethodSensorAssignment> assignments = factory.getSpecialAssignments(environment);

			assertThat(assignments, is(empty()));
		}
	}
}
