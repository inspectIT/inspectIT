package rocks.inspectit.server.instrumentation.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.ConfigurationInterfaceManager;
import rocks.inspectit.server.instrumentation.config.applier.ExceptionSensorInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.MethodSensorInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.SpecialInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.TimerMethodSensorInstrumentationApplier;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.exclude.ExcludeRule;
import rocks.inspectit.shared.cs.ci.factory.SpecialMethodSensorAssignmentFactory;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

@SuppressWarnings("PMD")
public class ConfigurationResolverTest extends TestBase {

	final static String PROFILE_ID = "id";

	@InjectMocks
	ConfigurationResolver configurationResolver;

	@Mock
	ConfigurationInterfaceManager configurationInterfaceManager;

	@Mock
	IRegistrationService registrationService;

	@Mock
	SpecialMethodSensorAssignmentFactory functionalAssignmentFactory;

	@Mock
	Environment environment;

	@Mock
	Profile profile;

	@Mock
	Logger log;

	public static class GetEnvironmentForAgent extends ConfigurationResolverTest {

		@Mock
		private AgentMappings agentMappings;

		private final String agentName = "inspectit";

		private final List<String> definedIPs = Collections.singletonList("127.0.0.1");

		@BeforeMethod
		public void initMappings() {
			when(configurationInterfaceManager.getAgentMappings()).thenReturn(agentMappings);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void noMappings() throws BusinessException {
			when(agentMappings.getMappings()).thenReturn(Collections.<AgentMapping> emptyList());
			configurationResolver.getEnvironmentForAgent(definedIPs, agentName);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void noMatchingMappingsName() throws BusinessException {
			AgentMapping mapping = mock(AgentMapping.class);
			when(agentMappings.getMappings()).thenReturn(Collections.singletonList(mapping));

			when(mapping.getAgentName()).thenReturn("something else");
			when(mapping.isActive()).thenReturn(true);
			configurationResolver.getEnvironmentForAgent(definedIPs, agentName);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void noMatchingMappingsNameWildcard() throws BusinessException {
			AgentMapping mapping = mock(AgentMapping.class);
			when(agentMappings.getMappings()).thenReturn(Collections.singletonList(mapping));

			when(mapping.getAgentName()).thenReturn("ins*TT");
			when(mapping.isActive()).thenReturn(true);
			configurationResolver.getEnvironmentForAgent(definedIPs, agentName);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void noMatchingMappingsIp() throws BusinessException {
			AgentMapping mapping = mock(AgentMapping.class);
			when(agentMappings.getMappings()).thenReturn(Collections.singletonList(mapping));

			when(mapping.getAgentName()).thenReturn("*");
			when(mapping.getIpAddress()).thenReturn("128.0.0.1");
			when(mapping.isActive()).thenReturn(true);
			configurationResolver.getEnvironmentForAgent(definedIPs, agentName);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void noMatchingMappingsIpWildcard() throws BusinessException {
			AgentMapping mapping = mock(AgentMapping.class);
			when(agentMappings.getMappings()).thenReturn(Collections.singletonList(mapping));

			when(mapping.getAgentName()).thenReturn("*");
			when(mapping.getIpAddress()).thenReturn("127.*.2");
			when(mapping.isActive()).thenReturn(true);
			configurationResolver.getEnvironmentForAgent(definedIPs, agentName);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void twoMatchingMappings() throws BusinessException {
			AgentMapping mapping1 = mock(AgentMapping.class);
			AgentMapping mapping2 = mock(AgentMapping.class);
			List<AgentMapping> mappings = new ArrayList<>();
			mappings.add(mapping1);
			mappings.add(mapping2);
			when(agentMappings.getMappings()).thenReturn(mappings);

			when(mapping1.getAgentName()).thenReturn("*");
			when(mapping1.isActive()).thenReturn(true);
			when(mapping1.getIpAddress()).thenReturn("*");
			when(mapping2.getAgentName()).thenReturn("*");
			when(mapping2.getIpAddress()).thenReturn("*");
			when(mapping2.isActive()).thenReturn(true);
			configurationResolver.getEnvironmentForAgent(definedIPs, agentName);
		}

		@Test
		public void oneMatchingMapping() throws BusinessException {
			AgentMapping mapping1 = mock(AgentMapping.class);
			AgentMapping mapping2 = mock(AgentMapping.class);
			List<AgentMapping> mappings = new ArrayList<>();
			mappings.add(mapping1);
			mappings.add(mapping2);
			when(agentMappings.getMappings()).thenReturn(mappings);

			when(mapping1.getAgentName()).thenReturn("ins*");
			when(mapping1.isActive()).thenReturn(true);
			when(mapping1.getIpAddress()).thenReturn("*");
			when(mapping2.getAgentName()).thenReturn("something else");
			when(mapping2.isActive()).thenReturn(true);
			when(mapping1.getEnvironmentId()).thenReturn("env1");
			Environment environment = mock(Environment.class);
			when(configurationInterfaceManager.getEnvironment("env1")).thenReturn(environment);

			assertThat(configurationResolver.getEnvironmentForAgent(definedIPs, agentName), is(environment));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void inactiveMapping() throws BusinessException {
			AgentMapping mapping1 = mock(AgentMapping.class);
			when(agentMappings.getMappings()).thenReturn(Collections.singleton(mapping1));

			when(mapping1.getAgentName()).thenReturn("ins*");
			when(mapping1.isActive()).thenReturn(false);
			when(mapping1.getIpAddress()).thenReturn("*");
			when(mapping1.getEnvironmentId()).thenReturn("env1");
			Environment environment = mock(Environment.class);
			when(configurationInterfaceManager.getEnvironment("env1")).thenReturn(environment);

			configurationResolver.getEnvironmentForAgent(definedIPs, agentName);
		}
	}

	public static class GetInstrumentationAppliers extends ConfigurationResolverTest {

		@Test
		public void nullEnvironment() {
			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(null);

			assertThat(appliers, is(empty()));

			verifyZeroInteractions(functionalAssignmentFactory, configurationInterfaceManager);
		}

		@Test
		public void noProfile() {
			when(environment.getProfileIds()).thenReturn(Collections.<String> emptySet());

			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(environment);

			assertThat(appliers, is(empty()));

			verify(functionalAssignmentFactory).getSpecialAssignments(environment);
		}

		@Test
		public void profileDoesNotExists() throws BusinessException {
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenThrow(new BusinessException(null));

			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(environment);

			assertThat(appliers, is(empty()));

			verify(functionalAssignmentFactory).getSpecialAssignments(environment);
		}

		@Test
		public void profileNotActive() throws BusinessException {
			MethodSensorAssignment assignment = mock(MethodSensorAssignment.class);
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenReturn(profile);
			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(assignment));
			when(profile.isActive()).thenReturn(false);

			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(environment);

			assertThat(appliers, is(empty()));

			verify(functionalAssignmentFactory).getSpecialAssignments(environment);
			verifyZeroInteractions(assignment);
		}

		@Test
		public void methodSensorAssignment() throws BusinessException {
			MethodSensorAssignment assignment = mock(MethodSensorAssignment.class);
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenReturn(profile);
			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(assignment));
			when(profile.isActive()).thenReturn(true);

			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(environment);

			assertThat(appliers, hasSize(1));
			IInstrumentationApplier applier = appliers.iterator().next();
			assertThat(applier, is(instanceOf(MethodSensorInstrumentationApplier.class)));
			assertThat((MethodSensorAssignment) applier.getSensorAssignment(), is(assignment));

			verify(functionalAssignmentFactory).getSpecialAssignments(environment);
		}

		@Test
		public void timerSensorAssignment() throws BusinessException {
			TimerMethodSensorAssignment assignment = mock(TimerMethodSensorAssignment.class);
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenReturn(profile);
			when(profile.getMethodSensorAssignments()).thenReturn(Collections.<MethodSensorAssignment> singletonList(assignment));
			when(profile.isActive()).thenReturn(true);

			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(environment);

			assertThat(appliers, hasSize(1));
			IInstrumentationApplier applier = appliers.iterator().next();
			assertThat(applier, is(instanceOf(TimerMethodSensorInstrumentationApplier.class)));
			assertThat((TimerMethodSensorAssignment) applier.getSensorAssignment(), is(assignment));

			verify(functionalAssignmentFactory).getSpecialAssignments(environment);
		}

		@Test
		public void exceptionSensorAssignment() throws BusinessException {
			ExceptionSensorAssignment assignment = mock(ExceptionSensorAssignment.class);
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenReturn(profile);
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(assignment));
			when(profile.isActive()).thenReturn(true);

			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(environment);

			assertThat(appliers, hasSize(1));
			IInstrumentationApplier applier = appliers.iterator().next();
			assertThat(applier, is(instanceOf(ExceptionSensorInstrumentationApplier.class)));
			assertThat((ExceptionSensorAssignment) applier.getSensorAssignment(), is(assignment));

			verify(functionalAssignmentFactory).getSpecialAssignments(environment);
		}

		@Test
		public void specialAssignments() {
			SpecialMethodSensorAssignment assignment = mock(SpecialMethodSensorAssignment.class);
			when(functionalAssignmentFactory.getSpecialAssignments(environment)).thenReturn(Collections.singletonList(assignment));

			Collection<IInstrumentationApplier> appliers = configurationResolver.getInstrumentationAppliers(environment);

			assertThat(appliers, hasSize(1));
			IInstrumentationApplier applier = appliers.iterator().next();
			assertThat(applier, is(instanceOf(SpecialInstrumentationApplier.class)));
			assertThat((SpecialMethodSensorAssignment) applier.getSensorAssignment(), is(assignment));

			verify(functionalAssignmentFactory).getSpecialAssignments(environment);
		}

	}

	public static class GetInstrumentationApplier extends ConfigurationResolverTest {

		@Test
		public void methodSensorAssignment() throws BusinessException {
			MethodSensorAssignment assignment = mock(MethodSensorAssignment.class);

			IInstrumentationApplier applier = configurationResolver.getInstrumentationApplier(assignment, environment);

			assertThat(applier, is(instanceOf(MethodSensorInstrumentationApplier.class)));
			assertThat((MethodSensorAssignment) applier.getSensorAssignment(), is(assignment));
		}

		@Test
		public void timerSensorAssignment() throws BusinessException {
			TimerMethodSensorAssignment assignment = mock(TimerMethodSensorAssignment.class);

			IInstrumentationApplier applier = configurationResolver.getInstrumentationApplier(assignment, environment);

			assertThat(applier, is(instanceOf(TimerMethodSensorInstrumentationApplier.class)));
			assertThat((TimerMethodSensorAssignment) applier.getSensorAssignment(), is(assignment));
		}

		@Test
		public void exceptionSensorAssignment() throws BusinessException {
			ExceptionSensorAssignment assignment = mock(ExceptionSensorAssignment.class);

			IInstrumentationApplier applier = configurationResolver.getInstrumentationApplier(assignment, environment);

			assertThat(applier, is(instanceOf(ExceptionSensorInstrumentationApplier.class)));
			assertThat((ExceptionSensorAssignment) applier.getSensorAssignment(), is(assignment));
		}

		@Test
		public void specialAssignments() {
			SpecialMethodSensorAssignment assignment = mock(SpecialMethodSensorAssignment.class);

			IInstrumentationApplier applier = configurationResolver.getInstrumentationApplier(assignment, environment);

			assertThat(applier, is(instanceOf(SpecialInstrumentationApplier.class)));
			assertThat((SpecialMethodSensorAssignment) applier.getSensorAssignment(), is(assignment));
		}

	}

	public static class GetAllExcludeRules extends ConfigurationResolverTest {

		@Mock
		private ExcludeRule excludeRule;

		@Test
		public void nullEnvironment() {
			Collection<ExcludeRule> rules = configurationResolver.getAllExcludeRules(null);

			assertThat(rules, is(empty()));

			verifyZeroInteractions(configurationInterfaceManager);
		}

		@Test
		public void noProfile() {
			when(environment.getProfileIds()).thenReturn(Collections.<String> emptySet());

			Collection<ExcludeRule> rules = configurationResolver.getAllExcludeRules(environment);

			assertThat(rules, is(empty()));
		}

		@Test
		public void profileDoesNotExists() throws BusinessException {
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenThrow(new BusinessException(null));

			Collection<ExcludeRule> rules = configurationResolver.getAllExcludeRules(environment);

			assertThat(rules, is(empty()));
		}

		@Test
		public void profileNotActive() throws BusinessException {
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenReturn(profile);
			when(profile.getExcludeRules()).thenReturn(Collections.singletonList(excludeRule));
			when(profile.isActive()).thenReturn(false);

			Collection<ExcludeRule> rules = configurationResolver.getAllExcludeRules(environment);

			assertThat(rules, is(empty()));
		}

		@Test
		public void excludeRule() throws BusinessException {
			when(environment.getProfileIds()).thenReturn(Collections.singleton(PROFILE_ID));
			when(configurationInterfaceManager.getProfile(PROFILE_ID)).thenReturn(profile);
			when(profile.getExcludeRules()).thenReturn(Collections.singletonList(excludeRule));
			when(profile.isActive()).thenReturn(true);

			Collection<ExcludeRule> rules = configurationResolver.getAllExcludeRules(environment);

			assertThat(rules, hasSize(1));
			assertThat(rules, hasItem(excludeRule));
		}

	}

}
