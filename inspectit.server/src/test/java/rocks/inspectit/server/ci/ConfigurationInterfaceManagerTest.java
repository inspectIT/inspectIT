package rocks.inspectit.server.ci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.storage.util.DeleteFileVisitor;

/**
 * Test for the {@link ConfigurationInterfaceManager}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ConfigurationInterfaceManagerTest {

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
	private ConfigurationInterfaceManager manager;

	@Mock
	private ConfigurationInterfacePathResolver pathResolver;

	@Mock
	private Logger logger;

	/**
	 * Init with correct paths for testing.
	 */
	@BeforeMethod
	public void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

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

	@Test
	public void importProfile() throws Exception {
		Profile profile = new Profile();
		profile.setId("myId");
		profile.setName("test");

		manager.importProfile(profile);

		assertThat(profile.getId(), is("myId"));
		assertThat(manager.getAllProfiles(), hasItem(profile));
	}

	@Test
	public void importProfileExists() throws Exception {
		Profile profile = new Profile();
		profile.setName("test");
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

	@Test
	public void deleteProfile() throws Exception {
		Profile profile = new Profile();
		profile.setName("test");

		profile = manager.createProfile(profile);
		manager.deleteProfile(profile);

		assertThat(manager.getAllProfiles(), is(empty()));
	}

	@Test
	public void updateProfile() throws Exception {
		Profile profile = new Profile();
		profile.setName("test");

		profile = manager.createProfile(profile);
		profile.setName("new");
		Profile updated = manager.updateProfile(profile);

		assertThat(updated.getName(), is("new"));
		assertThat(updated.getRevision(), is(2));
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
	public void importEnvironment() throws Exception {
		Environment environment = new Environment();
		environment.setId("myId");
		environment.setName("test");

		environment = manager.importEnvironment(environment);

		assertThat(environment.getId(), is("myId"));
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

	@Test
	public void deleteEnvironment() throws Exception {
		Environment environment = new Environment();
		environment.setName("test");

		environment = manager.createEnvironment(environment);
		manager.deleteEnvironment(environment);

		assertThat(manager.getAllEnvironments(), is(empty()));
	}

	@Test
	public void updateEnvironment() throws Exception {
		Environment environment = new Environment();
		environment.setName("test");

		environment = manager.createEnvironment(environment);
		environment.setName("new");
		Environment updated = manager.updateEnvironment(environment, false);

		assertThat(updated.getName(), is("new"));
		assertThat(updated.getRevision(), is(2));
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

	@Test
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
