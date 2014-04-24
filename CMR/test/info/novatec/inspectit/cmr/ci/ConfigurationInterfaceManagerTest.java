package info.novatec.inspectit.cmr.ci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.storage.util.DeleteFileVisitor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for the {@link ConfigurationInterfaceManager}.
 * 
 * @author Ivan Senic
 * 
 */
public class ConfigurationInterfaceManagerTest {

	/**
	 * What folder to use for testing.
	 */
	private static final String TEST_FOLDER = "testCi";

	/**
	 * Ci manager to test, altered paths for saving into the test folder.
	 */
	private ConfigurationInterfaceManager manager;

	/**
	 * Init with correct paths for testing.
	 */
	@BeforeMethod
	public void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		manager = new ConfigurationInterfaceManager() {

			protected Path getAgentMappingFilePath() {
				return Paths.get(TEST_FOLDER).resolve(super.getAgentMappingFilePath());
			}

			protected Path getEnvironmentFilePath(Environment environment) {
				return Paths.get(TEST_FOLDER).resolve(super.getEnvironmentFilePath(environment));
			};

			protected Path getEnvironmentPath() {
				return Paths.get(TEST_FOLDER).resolve(super.getEnvironmentPath());
			};

			protected Path getProfileFilePath(Profile profile) {
				return Paths.get(TEST_FOLDER).resolve(super.getProfileFilePath(profile));
			};

			protected Path getProfilesPath() {
				return Paths.get(TEST_FOLDER).resolve(super.getProfilesPath());
			};
		};

		Field field = ConfigurationInterfaceManager.class.getDeclaredField("log");
		field.setAccessible(true);
		field.set(manager, LoggerFactory.getLogger(ConfigurationInterfaceManager.class));

		manager.init();
	}

	@Test
	public void createProfile() throws Exception {
		Profile profile = new Profile();
		profile.setName("test");

		manager.createProfile(profile);

		assertThat(profile.getId(), is(not(nullValue())));
		assertThat(manager.getProfile(profile.getId()), is(equalTo(profile)));
		assertThat(manager.getAllProfiles(), hasItem(profile));
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
	public void createEnvironmentDefaultProfiles() throws Exception {
		Environment environment = new Environment();
		environment.setName("test");

		environment = manager.createEnvironment(environment);
		assertThat(environment.getId(), is(not(nullValue())));
		assertThat(manager.getEnvironment(environment.getId()), is(equalTo(environment)));
		assertThat(manager.getAllEnvironments(), hasItem(environment));
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

	@Test(expectedExceptions = { Exception.class })
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

		manager.setAgentMappings(mappings, false);
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

		manager.setAgentMappings(mappings, true);
		assertThat(mappings.getRevision(), is(2));
		assertThat(mappings.getMappings(), is(empty()));
	}

	@Test(expectedExceptions = { Exception.class })
	public void mappingsRevisionFails() throws Exception {
		AgentMappings mappings = new AgentMappings();
		mappings.setRevision(0);
		manager.setAgentMappings(mappings, false);
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
