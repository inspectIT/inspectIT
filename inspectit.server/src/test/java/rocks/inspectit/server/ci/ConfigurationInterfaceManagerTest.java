package rocks.inspectit.server.ci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.event.EnvironmentUpdateEvent;
import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.storage.util.DeleteFileVisitor;

/**
 * Test for the {@link ConfigurationInterfaceManager}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("all")
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
		doAnswer(new Answer<Path>() {
			@Override
			public Path answer(InvocationOnMock invocation) throws Throwable {
				return Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getEnvironmentPath()))
						.resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getEnvironmentFilePath((Environment) invocation.getArguments()[0])));
			}
		}).when(pathResolver).getEnvironmentFilePath(Mockito.<Environment> any());
		doAnswer(new Answer<Path>() {
			@Override
			public Path answer(InvocationOnMock invocation) throws Throwable {
				return Paths.get(TEST_FOLDER).resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getEnvironmentPath()))
						.resolve(EXT_RESOURCES_PATH.relativize(resolverHelper.getProfileFilePath((Profile) invocation.getArguments()[0])));
			}
		}).when(pathResolver).getProfileFilePath(Mockito.<Profile> any());

		manager.init();
	}

	public class CreateProfile extends ConfigurationInterfaceManagerTest {

		@Test
		public void createProfileCheckId() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");

			manager.createProfile(profile);

			assertThat(profile.getId(), is(not(nullValue())));
			assertThat(manager.getAllProfiles(), hasItem(profile));
		}

		@Test
		public void createProfileCheckExists() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");

			manager.createProfile(profile);

			assertThat(manager.getAllProfiles(), hasItem(profile));
		}
	}

	public class GetProfile extends ConfigurationInterfaceManagerTest {

		@Test
		public void get() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile = manager.createProfile(profile);

			Profile result = manager.getProfile(profile.getId());

			assertThat(result.getId(), is(profile.getId()));
		}

		@Test(expectedExceptions = BusinessException.class)
		public void getNotExisting() throws Exception {
			Profile result = manager.getProfile("someId");
		}
	}

	public class DeleteProfile extends ConfigurationInterfaceManagerTest {

		@Test
		public void deleteProfile() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile = manager.createProfile(profile);

			manager.deleteProfile(profile);

			assertThat(manager.getAllProfiles(), is(empty()));
		}

		@Test
		public void deleteProfileRemovedFromEnvironment() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
			profile = manager.createProfile(profile);
			Environment environment = new Environment();
			environment.setName("Test");
			environment = manager.createEnvironment(environment);
			environment.setProfileIds(new HashSet<>(Collections.singleton(profile.getId())));
			environment = manager.updateEnvironment(environment, true);

			manager.deleteProfile(profile);

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
			profile = manager.createProfile(profile);
			profile.setName("new");

			Profile updated = manager.updateProfile(profile);

			assertThat(updated.getName(), is("new"));
			assertThat(updated.getRevision(), is(2));

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
			profile.setMethodSensorAssignments(Collections.singletonList(methodSensorAssignment1));
			Profile created = manager.createProfile(profile);
			assertThat(profile.getMethodSensorAssignments(), hasItem(methodSensorAssignment1));

			profile = new Profile();
			profile.setId(created.getId());
			profile.setRevision(created.getRevision());
			profile.setName(created.getName());
			profile.setMethodSensorAssignments(Collections.singletonList(methodSensorAssignment2));

			Profile updated = manager.updateProfile(profile);

			assertThat(profile.getMethodSensorAssignments(), hasItem(methodSensorAssignment2));
			assertThat(updated.getRevision(), is(2));

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(ProfileUpdateEvent.class)));
		}

		@Test
		public void updateProfileExceptionAssignmentsChange() throws Exception {
			ExceptionSensorAssignment exceptionSensorAssignment1 = mock(ExceptionSensorAssignment.class);
			ExceptionSensorAssignment exceptionSensorAssignment2 = mock(ExceptionSensorAssignment.class);

			Profile profile = new Profile();
			profile.setName("test");
			profile.setExceptionSensorAssignments(Collections.singletonList(exceptionSensorAssignment1));
			Profile created = manager.createProfile(profile);
			assertThat(profile.getExceptionSensorAssignments(), hasItem(exceptionSensorAssignment1));

			profile = new Profile();
			profile.setId(created.getId());
			profile.setRevision(created.getRevision());
			profile.setName(created.getName());
			profile.setExceptionSensorAssignments(Collections.singletonList(exceptionSensorAssignment2));

			Profile updated = manager.updateProfile(profile);

			assertThat(profile.getExceptionSensorAssignments(), hasItem(exceptionSensorAssignment2));
			assertThat(updated.getRevision(), is(2));

			ArgumentCaptor<Collection> addedSensorsCaptor = ArgumentCaptor.forClass(Collection.class);
			ArgumentCaptor<Collection> removedSensorsCaptor = ArgumentCaptor.forClass(Collection.class);

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(ProfileUpdateEvent.class)));
		}

		@Test(expectedExceptions = { Exception.class })
		public void updateProfileRevisionFails() throws Exception {
			Profile profile = new Profile();
			profile.setName("test");
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

			ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
			verify(eventPublisher).publishEvent(captor.capture());
			assertThat(captor.getValue(), is(instanceOf(EnvironmentUpdateEvent.class)));
		}

		@Test
		public void updateEnvironmentProfileUpdate() throws Exception {
			Profile profile1 = new Profile();
			profile1.setName("profile1");
			profile1 = manager.createProfile(profile1);
			Profile profile2 = new Profile();
			profile2.setName("profile2");
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
