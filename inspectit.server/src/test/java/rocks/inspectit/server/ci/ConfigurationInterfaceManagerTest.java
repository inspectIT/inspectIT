package rocks.inspectit.server.ci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.event.AbstractAlertingDefinitionEvent;
import rocks.inspectit.server.ci.event.BusinessContextDefinitionUpdateEvent;
import rocks.inspectit.server.ci.event.EnvironmentUpdateEvent;
import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.BusinessContextDefinition;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;
import rocks.inspectit.shared.cs.storage.util.DeleteFileVisitor;

/**
 * Test for the {@link ConfigurationInterfaceManager}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings({ "all", "unchecked" })
public class ConfigurationInterfaceManagerTest extends TestBase {

	/**
	 * Path to the external resources folder.
	 */
	private static final Path EXT_RESOURCES_PATH = Paths.get("src", "main", "external-resources").toAbsolutePath();

	/**
	 * What folder to use for testing.
	 */
	private static final String TEST_FOLDER = "testCi";

	/**
	 * Ci manager to test, altered paths for saving into the test folder.
	 */
	@InjectMocks
	ConfigurationInterfaceManager manager;

	@Mock
	ConfigurationInterfacePathResolver pathResolver;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	Logger logger;

	/**
	 * Init with correct paths for testing.
	 */
	@BeforeMethod
	public void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final ConfigurationInterfacePathResolver resolverHelper = new ConfigurationInterfacePathResolver();
		resolverHelper.init();
		when(pathResolver.getDefaultCiPath()).thenReturn(Paths.get(TEST_FOLDER));
		when(pathResolver.getAgentMappingFilePath()).thenReturn(Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getAgentMappingFilePath())));
		when(pathResolver.getEnvironmentPath()).thenReturn(Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getEnvironmentPath())));
		when(pathResolver.getProfilesPath()).thenReturn(Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getProfilesPath())));
		when(pathResolver.getSchemaPath()).thenReturn(Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getSchemaPath())));
		when(pathResolver.getBusinessContextFilePath()).thenReturn(Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getBusinessContextFilePath())));
		when(pathResolver.getAlertingDefinitionsPath()).thenReturn(Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getAlertingDefinitionsPath())));
		doAnswer(new Answer<Path>() {
			@Override
			public Path answer(InvocationOnMock invocation) throws Throwable {
				return Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getEnvironmentPath()))
						.resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getEnvironmentFilePath((Environment) invocation.getArguments()[0])));
			}
		}).when(pathResolver).getEnvironmentFilePath(Matchers.<Environment> any());
		doAnswer(new Answer<Path>() {
			@Override
			public Path answer(InvocationOnMock invocation) throws Throwable {
				return Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getEnvironmentPath()))
						.resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getProfileFilePath((Profile) invocation.getArguments()[0])));
			}
		}).when(pathResolver).getProfileFilePath(Matchers.<Profile> any());
		doAnswer(new Answer<Path>() {
			@Override
			public Path answer(InvocationOnMock invocation) throws Throwable {
				return Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getAlertingDefinitionsPath()))
						.resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getAlertingDefinitionFilePath((AlertingDefinition) invocation.getArguments()[0])));
			}
		}).when(pathResolver).getAlertingDefinitionFilePath(Matchers.<AlertingDefinition> any());

		manager.init();
	}

	public class CreateProfile extends ConfigurationInterfaceManagerTest {

		@Test
		public void createProfileCheckId() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());

			manager.createProfile(profile);

			assertThat(profile.getId(), is(not(nullValue())));
			assertThat(profile.getCreatedDate(), is(not(nullValue())));
			assertThat(manager.getAllProfiles(), hasItem(profile));
		}

		@Test
		public void createProfileCheckExists() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());

			manager.createProfile(profile);

			assertThat(manager.getAllProfiles(), hasItem(profile));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void createProfileNoProfileData() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");

			manager.createProfile(profile);
		}
	}

	public class GetProfile extends ConfigurationInterfaceManagerTest {
		@Test
		public void get() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());
			profile = manager.createProfile(profile);

			Profile result = manager.getProfile(profile.getId());

			assertThat(result.getId(), is(profile.getId()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void getNotExisting() throws Exception {
			Profile result = manager.getProfile("someId");
		}
	}

	public class ImportProfile extends ConfigurationInterfaceManagerTest {

		@Test
		public void importProfile() throws Exception {
			Profile profile = new Profile();
			profile.setId("myId");
			profile.setName("test");

			manager.importProfile(profile);

			assertThat(profile.getId(), is("myId"));
			assertThat(profile.getImportDate(), is(not(nullValue())));
			assertThat(manager.getAllProfiles(), hasItem(profile));
		}

		@Test
		public void importProfileExists() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());
			manager.createProfile(profile);
			Profile importProfile = new Profile();
			importProfile.setId(profile.getId());
			importProfile.setName("imported");

			manager.importProfile(importProfile);

			assertThat(manager.getProfile(profile.getId()), is(importProfile));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void importProfileNoId() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");

			manager.importProfile(profile);
		}

	}

	public class DeleteProfile extends ConfigurationInterfaceManagerTest {

		@Mock
		SerializationManager serializationManager;

		@Test
		public void deleteProfile() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());
			profile = manager.createProfile(profile);

			manager.deleteProfile(profile);

			assertThat(manager.getAllProfiles(), is(empty()));
		}

		@Test
		public void deleteProfileRemovedFromEnvironment() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());
			profile = manager.createProfile(profile);
			Environment environment = new Environment();
			environment.setName("Test");
			environment = manager.createEnvironment(environment);
			environment.setProfileIds(new HashSet<>(Collections.singleton(profile.getId())));
			environment = manager.updateEnvironment(environment, true);
			when(serializationManager.copy(any(Environment.class))).thenAnswer(new Answer<Environment>() {
				@Override
				public Environment answer(InvocationOnMock invocation) throws Throwable {
					Environment env = (Environment) invocation.getArguments()[0];
					Environment copy = new Environment();
					copy.setId(env.getId());
					copy.setName(env.getName());
					copy.setRevision(env.getRevision());
					copy.setProfileIds(new HashSet<>(env.getProfileIds()));
					return copy;
				}
			});

			manager.deleteProfile(profile);

			assertThat(manager.getEnvironment(environment.getId()).getProfileIds(), is(not(equalTo(environment.getProfileIds()))));
			assertThat(manager.getEnvironment(environment.getId()).getProfileIds(), is(empty()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void deleteCommonProfile() throws Exception {
			Profile profile = new Profile();
			Field f = Profile.class.getDeclaredField("commonProfile");
			f.setAccessible(true);
			f.set(profile, true);

			manager.deleteProfile(profile);
		}
	}

	public class UpdateProfile extends ConfigurationInterfaceManagerTest {

		@Test
		public void updateProfile() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());
			profile = manager.createProfile(profile);
			profile.setName("new");

			Profile updated = manager.updateProfile(profile);

			assertThat(updated.getName(), is("new"));
			assertThat(updated.getRevision(), is(2));
			assertThat(updated.getUpdatedDate(), is(greaterThanOrEqualTo(updated.getCreatedDate())));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(ProfileUpdateEvent.class)));
		}

		@Test
		public void updateProfileMethodAssignmentsChange() throws Exception {
			MethodSensorAssignment methodSensorAssignment1 = mock(MethodSensorAssignment.class);
			MethodSensorAssignment methodSensorAssignment2 = mock(MethodSensorAssignment.class);

			Profile profile = new Profile();
			profile.setName("test");
			SensorAssignmentProfileData profileData = new SensorAssignmentProfileData();
			profileData.setMethodSensorAssignments(Collections.singletonList(methodSensorAssignment1));
			profile.setProfileData(profileData);
			Profile created = manager.createProfile(profile);
			assertThat((Collection<MethodSensorAssignment>) profile.getProfileData().getData(SensorAssignmentProfileData.class), hasItem(methodSensorAssignment1));

			profile = new Profile();
			profile.setId(created.getId());
			profile.setRevision(created.getRevision());
			profile.setName(created.getName());
			profileData = new SensorAssignmentProfileData();
			profileData.setMethodSensorAssignments(Collections.singletonList(methodSensorAssignment2));
			profile.setProfileData(profileData);

			Profile updated = manager.updateProfile(profile);

			assertThat((Collection<MethodSensorAssignment>) profile.getProfileData().getData(SensorAssignmentProfileData.class), hasItem(methodSensorAssignment2));
			assertThat(updated.getRevision(), is(2));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(ProfileUpdateEvent.class)));
			ProfileUpdateEvent profileUpdateEvent = (ProfileUpdateEvent) captor.getValue();
			assertThat(profileUpdateEvent.getRemovedSensorAssignments(), hasItem(methodSensorAssignment1));
			assertThat(profileUpdateEvent.getAddedSensorAssignments(), hasItem(methodSensorAssignment2));
		}

		@Test
		public void updateProfileExceptionAssignmentsChange() throws Exception {
			ExceptionSensorAssignment exceptionSensorAssignment1 = mock(ExceptionSensorAssignment.class);
			ExceptionSensorAssignment exceptionSensorAssignment2 = mock(ExceptionSensorAssignment.class);

			Profile profile = new Profile();
			profile.setName("test");
			SensorAssignmentProfileData profileData = new SensorAssignmentProfileData();
			profileData.setExceptionSensorAssignments(Collections.singletonList(exceptionSensorAssignment1));
			profile.setProfileData(profileData);
			Profile created = manager.createProfile(profile);
			assertThat((Collection<ExceptionSensorAssignment>) profile.getProfileData().getData(SensorAssignmentProfileData.class), hasItem(exceptionSensorAssignment1));

			profile = new Profile();
			profile.setId(created.getId());
			profile.setRevision(created.getRevision());
			profile.setName(created.getName());
			profileData = new SensorAssignmentProfileData();
			profileData.setExceptionSensorAssignments(Collections.singletonList(exceptionSensorAssignment2));
			profile.setProfileData(profileData);

			Profile updated = manager.updateProfile(profile);

			assertThat((Collection<ExceptionSensorAssignment>) profile.getProfileData().getData(SensorAssignmentProfileData.class), hasItem(exceptionSensorAssignment2));
			assertThat(updated.getRevision(), is(2));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(ProfileUpdateEvent.class)));
			ProfileUpdateEvent profileUpdateEvent = (ProfileUpdateEvent) captor.getValue();
			assertThat(profileUpdateEvent.getRemovedSensorAssignments(), hasItem(exceptionSensorAssignment1));
			assertThat(profileUpdateEvent.getAddedSensorAssignments(), hasItem(exceptionSensorAssignment2));
		}

		@Test(expectedExceptions = { Exception.class })
		public void updateProfileRevisionFails() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setProfileData(new SensorAssignmentProfileData());
			profile = manager.createProfile(profile);

			Profile clone = new Profile();
			clone.setId(profile.getId());

			manager.updateProfile(profile);
			manager.updateProfile(clone);
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateCommonProfile() throws Exception {
			Profile profile = new Profile();
			Field f = Profile.class.getDeclaredField("commonProfile");
			f.setAccessible(true);
			f.set(profile, true);

			manager.updateProfile(profile);
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateProfileNotExisting() throws Exception {
			Profile profile = new Profile();
			profile.setId("non-existing");

			manager.updateProfile(profile);
		}
	}

	public class CreateEnvironment extends ConfigurationInterfaceManagerTest {

		@Test
		public void createEnvironmentCheckId() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");

			environment = manager.createEnvironment(environment);

			assertThat(environment.getId(), is(not(nullValue())));
			assertThat(environment.getCreatedDate(), is(not(nullValue())));
			assertThat(manager.getEnvironment(environment.getId()), is(equalTo(environment)));
		}

		@Test
		public void createEnvironmentCheckExists() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");

			environment = manager.createEnvironment(environment);

			assertThat(manager.getAllEnvironments(), hasItem(environment));
		}

		@Test
		public void createEnvironmentIncludeDefaults() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile.setDefaultProfile(true);
			profile.setProfileData(new SensorAssignmentProfileData());
			profile = manager.createProfile(profile);
			Environment environment = new Environment();
			environment.setName("test");

			environment = manager.createEnvironment(environment);

			assertThat(environment.getProfileIds(), hasItem(profile.getId()));
		}
	}

	public class GetEnvironment extends ConfigurationInterfaceManagerTest {

		@Test
		public void get() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");
			environment = manager.createEnvironment(environment);

			Environment result = manager.getEnvironment(environment.getId());

			assertThat(result.getId(), is(environment.getId()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void getNotExisting() throws Exception {
			Environment result = manager.getEnvironment("someId");
		}
	}

	public class ImportEnvironment extends ConfigurationInterfaceManagerTest {

		@Test
		public void importEnvironment() throws Exception {
			Environment environment = new Environment();
			environment.setId("myId");
			environment.setName("test");

			environment = manager.importEnvironment(environment);

			assertThat(environment.getId(), is("myId"));
			assertThat(environment.getImportDate(), is(not(nullValue())));
			assertThat(manager.getEnvironment(environment.getId()), is(equalTo(environment)));
		}

		@Test
		public void importEnvironemtExists() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");
			manager.createEnvironment(environment);
			Environment importEnvironment = new Environment();
			importEnvironment.setId(environment.getId());
			importEnvironment.setName("imported");

			manager.importEnvironment(importEnvironment);

			assertThat(manager.getEnvironment(environment.getId()), is(equalTo(importEnvironment)));
		}

		@Test
		public void importEnvironmentProfileDoesNotExists() throws Exception {
			Environment environment = new Environment();
			environment.setId("myId");
			environment.setName("test");
			Set<String> profiles = new HashSet<>();
			profiles.add("whatever");
			environment.setProfileIds(profiles);

			environment = manager.importEnvironment(environment);

			assertThat(environment.getId(), is("myId"));
			assertThat(environment.getProfileIds(), is(empty()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void importEnvironmentNoId() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");

			environment = manager.importEnvironment(environment);
		}
	}

	public class DeleteEnvironment extends ConfigurationInterfaceManagerTest {

		@Test
		public void deleteEnvironment() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");
			environment = manager.createEnvironment(environment);

			manager.deleteEnvironment(environment);

			assertThat(manager.getAllEnvironments(), not(hasItem(environment)));
		}

		@Test
		public void deleteEnvironmentUpdateMappings() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");
			environment = manager.createEnvironment(environment);
			AgentMappings mappings = manager.getAgentMappings();
			List<AgentMapping> list = new ArrayList<>();
			AgentMapping mapping = new AgentMapping("test", "test");
			mapping.setEnvironmentId(environment.getId());
			list.add(mapping);
			mappings.setMappings(list);
			manager.saveAgentMappings(mappings, true);

			manager.deleteEnvironment(environment);

			assertThat(manager.getAgentMappings().getMappings(), is(empty()));
		}
	}

	public class UpdateEnvironment extends ConfigurationInterfaceManagerTest {

		@Test
		public void updateEnvironment() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");
			environment = manager.createEnvironment(environment);
			environment.setName("new");

			Environment updated = manager.updateEnvironment(environment, false);

			assertThat(updated.getName(), is("new"));
			assertThat(updated.getRevision(), is(2));
			assertThat(environment.getUpdatedDate(), is(greaterThanOrEqualTo(environment.getCreatedDate())));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(EnvironmentUpdateEvent.class)));
		}

		@Test
		public void updateEnvironmentProfileUpdate() throws Exception {
			Profile profile1 = new Profile();
			profile1.setName("profile1");
			profile1.setProfileData(new SensorAssignmentProfileData());
			profile1 = manager.createProfile(profile1);
			Profile profile2 = new Profile();
			profile2.setName("profile2");
			profile2.setProfileData(new SensorAssignmentProfileData());
			profile2 = manager.createProfile(profile2);

			Environment environment = new Environment();
			environment.setName("test");
			environment = manager.createEnvironment(environment);

			environment.setProfileIds(Collections.singleton(profile1.getId()));
			Environment updated = manager.updateEnvironment(environment, true);
			assertThat(updated.getRevision(), is(2));
			assertThat(updated.getProfileIds(), hasItem(profile1.getId()));

			environment = new Environment();
			environment.setId(updated.getId());
			environment.setRevision(updated.getRevision());
			environment.setName(updated.getName());
			environment.setProfileIds(Collections.singleton(profile2.getId()));

			updated = manager.updateEnvironment(environment, false);

			assertThat(updated.getRevision(), is(3));
			assertThat(updated.getProfileIds(), hasItem(profile2.getId()));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher, times(2)).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(EnvironmentUpdateEvent.class)));
			EnvironmentUpdateEvent event = (EnvironmentUpdateEvent) captor.getValue();

			assertThat(event.getAddedProfiles(), hasSize(1));
			assertThat(event.getAddedProfiles(), hasItem(profile2));

			assertThat(event.getRemovedProfiles(), hasSize(1));
			assertThat(event.getRemovedProfiles(), hasItem(profile1));

		}

		@Test
		public void updateEnvironmentCheckProfiles() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");
			environment = manager.createEnvironment(environment);
			environment.setName("new");
			Set<String> profiles = new HashSet<>();
			profiles.add("non-existing-profile-id");
			environment.setProfileIds(profiles);

			Environment updated = manager.updateEnvironment(environment, true);

			assertThat(updated.getName(), is("new"));
			assertThat(updated.getRevision(), is(2));
			assertThat(updated.getProfileIds(), is(empty()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateEnvironmentRevisionFails() throws Exception {
			Environment environment = new Environment();
			environment.setName("test");
			environment = manager.createEnvironment(environment);
			Environment clone = new Environment();
			clone.setId(environment.getId());

			manager.updateEnvironment(environment, false);
			manager.updateEnvironment(clone, false);
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateEnvironmentNotExisting() throws Exception {
			Environment environment = new Environment();
			environment.setId("non-existing");

			manager.updateEnvironment(environment, false);
		}
	}

	public class SaveAgentMappings extends ConfigurationInterfaceManagerTest {

		@Test
		public void mappings() throws Exception {
			AgentMappings mappings = manager.getAgentMappings();
			List<AgentMapping> list = new ArrayList<>();
			AgentMapping mapping = new AgentMapping("test", "test");
			list.add(mapping);
			mappings.setMappings(list);

			manager.saveAgentMappings(mappings, false);

			assertThat(mappings.getRevision(), is(2));
		}

		@Test
		public void mappingsCheckEnvironments() throws Exception {
			AgentMappings mappings = manager.getAgentMappings();
			List<AgentMapping> list = new ArrayList<>();
			AgentMapping mapping = new AgentMapping("test", "test");
			mapping.setEnvironmentId("non-existing-env-id");
			list.add(mapping);
			mappings.setMappings(list);

			manager.saveAgentMappings(mappings, true);

			assertThat(mappings.getRevision(), is(2));
			assertThat(mappings.getMappings(), is(empty()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void mappingsRevisionFails() throws Exception {
			AgentMappings mappings = new AgentMappings();
			mappings.setRevision(0);

			manager.saveAgentMappings(mappings, false);
		}
	}

	public class UpdateBusinessContext extends ConfigurationInterfaceManagerTest {

		@Test
		public void updateBusinessContext() throws Exception {
			assertThat(manager.getBusinessconContextDefinition().getApplicationDefinitions(), hasSize(1));

			BusinessContextDefinition businessCtxDefinition = manager.getBusinessconContextDefinition();
			businessCtxDefinition.addApplicationDefinition(new ApplicationDefinition(1, "newApplication", null));
			manager.updateBusinessContextDefinition(businessCtxDefinition);
			BusinessContextDefinition updated = manager.getBusinessconContextDefinition();

			assertThat(updated.getApplicationDefinitions(), hasSize(2));
			assertThat(updated.getRevision(), is(2));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(BusinessContextDefinitionUpdateEvent.class)));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateBusinessContextFails() throws Exception {
			BusinessContextDefinition businessCtxDefinition = new BusinessContextDefinition();
			businessCtxDefinition.setRevision(0);
			businessCtxDefinition.addApplicationDefinition(new ApplicationDefinition(1, "newApplication", null));
			manager.updateBusinessContextDefinition(businessCtxDefinition);
		}
	}

	/**
	 * Tests the {@link ConfigurationInterfaceManager#createAlertingDefinition(AlertingDefinition)}
	 * method.
	 */
	public class CreateAlertingDefinition extends ConfigurationInterfaceManagerTest {

		@Test
		public void createAlertingDefinitionCheckId() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");

			alertingDefinition = manager.createAlertingDefinition(alertingDefinition);

			assertThat(alertingDefinition.getId(), is(not(nullValue())));
			assertThat(alertingDefinition.getCreatedDate(), is(not(nullValue())));
		}

		@Test
		public void createAlertingDefinitionExists() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");

			alertingDefinition = manager.createAlertingDefinition(alertingDefinition);

			assertThat(manager.getAlertingDefinitions(), hasItem(alertingDefinition));
		}
	}

	/**
	 * Tests the {@link ConfigurationInterfaceManager#getAlertingDefinitions()} and
	 * {@link ConfigurationInterfaceManager#getAlertingDefinition(String)} methods.
	 */
	public class GetAlertingDefinition extends ConfigurationInterfaceManagerTest {
		@Test
		public void getAlertingDefinitions() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");

			AlertingDefinition returnedDefinition = manager.createAlertingDefinition(alertingDefinition);

			List<AlertingDefinition> alertingDefinitions = manager.getAlertingDefinitions();

			assertThat(alertingDefinitions, hasItem(returnedDefinition));
		}

		@Test
		public void getAlertingDefinition() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");

			manager.createAlertingDefinition(alertingDefinition);

			AlertingDefinition returnedDefinition = manager.getAlertingDefinition(alertingDefinition.getId());

			assertThat(alertingDefinition.getId(), equalTo(returnedDefinition.getId()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void getAlertingDefinitionNotExisting() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");

			manager.createAlertingDefinition(alertingDefinition);

			AlertingDefinition returnedDefinition = manager.getAlertingDefinition("unknown_id");
		}
	}

	/**
	 * Tests the {@link ConfigurationInterfaceManager#updateAlertingDefinition(AlertingDefinition)}
	 * method.
	 */
	public class UpdateAlertingDefinition extends ConfigurationInterfaceManagerTest {
		@Test
		public void updateAlertingDefinition() throws BusinessException, JAXBException, IOException {
			String newName = "newName";

			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");
			manager.createAlertingDefinition(alertingDefinition);

			alertingDefinition.setName(newName);
			AlertingDefinition updated = manager.updateAlertingDefinition(alertingDefinition);

			assertThat(updated.getName(), is(newName));
			assertThat(updated.getRevision(), is(2));
			assertThat(alertingDefinition.getUpdatedDate(), is(greaterThanOrEqualTo(alertingDefinition.getCreatedDate())));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher, times(2)).publishEvent(captor.capture());

			assertThat(captor.getAllValues().get(0), is(instanceOf(AbstractAlertingDefinitionEvent.AlertingDefinitionCreatedEvent.class)));
			assertThat(captor.getAllValues().get(1), is(instanceOf(AbstractAlertingDefinitionEvent.AlertingDefinitionUpdateEvent.class)));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateAlertingDefinitionRevisionFailed() throws BusinessException, JAXBException, IOException {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");
			manager.createAlertingDefinition(alertingDefinition);

			AlertingDefinition clone = new AlertingDefinition();
			clone.setId(alertingDefinition.getId());

			manager.updateAlertingDefinition(alertingDefinition);
			manager.updateAlertingDefinition(clone);
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateAlertingDefinitionNotExisting() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setId("not-existing");

			manager.updateAlertingDefinition(alertingDefinition);
		}

		@Test(expectedExceptions = BusinessException.class)
		public void updateAlertingDefinitionNoId() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();

			manager.updateAlertingDefinition(alertingDefinition);
		}
	}

	/**
	 * Tests the {@link ConfigurationInterfaceManager#deleteAlertingDefinition(AlertingDefinition)}
	 * method.
	 */
	public class DeleteAlertingDefinition extends ConfigurationInterfaceManagerTest {

		@Test
		public void deleteAlertingDefinition() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setName("test");
			manager.createAlertingDefinition(alertingDefinition);

			manager.deleteAlertingDefinition(alertingDefinition);

			assertThat(manager.getAlertingDefinitions(), not(hasItem(alertingDefinition)));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher, times(2)).publishEvent(captor.capture());
			assertThat(captor.getAllValues().get(0), is(instanceOf(AbstractAlertingDefinitionEvent.AlertingDefinitionCreatedEvent.class)));
			assertThat(captor.getAllValues().get(1), is(instanceOf(AbstractAlertingDefinitionEvent.AlertingDefinitionDeletedEvent.class)));
		}

		@Test
		public void deleteAlertingDefinitionNotExisting() throws Exception {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setId("not-existing");

			manager.deleteAlertingDefinition(alertingDefinition);

			verify(eventPublisher, never()).publishEvent(any(ApplicationEvent.class));
		}
	}

	/**
	 * Clean test folder after each test.
	 */
	@AfterMethod
	public void cleanUp() throws IOException {
		if (Files.exists(Paths.get(TEST_FOLDER))) {
			Files.walkFileTree(Paths.get(TEST_FOLDER), new DeleteFileVisitor());
			Files.deleteIfExists(Paths.get(TEST_FOLDER));
		}
	}
}
