package info.novatec.inspectit.agent.core.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.config.impl.RepositoryConfig;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.RegistrationException;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.versioning.IVersioningService;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class IdManagerTest extends AbstractLogSupport {

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IConnection connection;

	@Mock
	private IVersioningService versioning;

	private IdManager idManager;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		idManager = new IdManager(configurationStorage, connection, versioning);
		idManager.log = LoggerFactory.getLogger(IdManager.class);
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under heavy load. There is
	 * no reliable way to make this test always successful.
	 */
	@Test
	public void startStop() throws ConnectException, InterruptedException, ServerUnavailableException, RegistrationException {
		String host = "localhost";
		int port = 1099;
		RepositoryConfig repositoryConfig = new RepositoryConfig(host, port);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		String agentName = "testagent";
		when(configurationStorage.getAgentName()).thenReturn(agentName);

		idManager.start();

		verify(configurationStorage, times(1)).getMethodSensorTypes();
		verify(configurationStorage, times(1)).getPlatformSensorTypes();

		idManager.stop();
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under heavy load. There is
	 * no reliable way to make this test always successful.
	 */
	@Test
	public void connected() throws InterruptedException, ServerUnavailableException, RegistrationException {
		when(connection.isConnected()).thenReturn(true);

		String host = "localhost";
		int port = 1099;
		String agentName = "testagent";
		when(configurationStorage.getAgentName()).thenReturn(agentName);
		RepositoryConfig repositoryConfig = new RepositoryConfig(host, port);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);

		idManager.start();

		verify(configurationStorage, times(1)).getMethodSensorTypes();
		verify(configurationStorage, times(1)).getPlatformSensorTypes();

		idManager.stop();
	}

	@Test
	public void connectAndRetrievePlatformId() throws ServerUnavailableException, RegistrationException, IdNotAvailableException, IOException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(versioning.getVersion()).thenReturn("dummyVersion");

		long fakePlatformId = 7L;
		when(connection.isConnected()).thenReturn(false);
		when(connection.registerPlatform("testAgent", "dummyVersion")).thenReturn(fakePlatformId);

		idManager.start();
		long platformId = idManager.getPlatformId();
		idManager.stop();

		assertThat(platformId, is(equalTo(fakePlatformId)));
	}

	@Test
	public void retrievePlatformId() throws IdNotAvailableException, ServerUnavailableException, RegistrationException, InterruptedException, IOException {
		long fakePlatformId = 3L;
		when(connection.isConnected()).thenReturn(true);
		when(connection.registerPlatform("testAgent", "dummyVersion")).thenReturn(fakePlatformId);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(versioning.getVersion()).thenReturn("dummyVersion");

		idManager.start();
		long platformId = idManager.getPlatformId();
		idManager.stop();

		assertThat(platformId, is(equalTo(fakePlatformId)));
	}

	@Test(expectedExceptions = { IdNotAvailableException.class })
	public void platformIdNotAvailable() throws ConnectException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(false);
		doThrow(new ConnectException("fake")).when(connection).connect(anyString(), anyInt());

		idManager.start();
		idManager.getPlatformId();
	}

	/**
	 * Tests that unregister of platform is executed if connection to the server is established and
	 * registration is performed.
	 */
	@Test
	public void unregisterPlatform() throws ServerUnavailableException, RegistrationException, IOException, IdNotAvailableException {
		// first simulate connect
		long fakePlatformId = 3L;
		when(connection.isConnected()).thenReturn(true);
		when(connection.registerPlatform("testAgent", "dummyVersion")).thenReturn(fakePlatformId);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(versioning.getVersion()).thenReturn("dummyVersion");

		idManager.start();
		idManager.getPlatformId();
		idManager.unregisterPlatform();

		verify(connection, times(1)).unregisterPlatform("testAgent");
	}

	/**
	 * Test that unregister will not be called if there is no active connection to the server and
	 * registration is not done at first place.
	 */
	@Test
	public void noUnregisterPlatform() throws RegistrationException {
		// no unregister if no connection
		when(connection.isConnected()).thenReturn(false);
		idManager.unregisterPlatform();

		// no unregister if registration is not done at the first place
		when(connection.isConnected()).thenReturn(true);
		idManager.unregisterPlatform();

		verify(connection, times(0)).unregisterPlatform(anyString());
	}

	/**
	 * If unregister is called with shutdown initialized marker every next call to getPlatformId
	 * should throw an exception
	 */
	@Test(expectedExceptions = { IdNotAvailableException.class })
	public void unregisterPlatformAndInitShutdown() throws ServerUnavailableException, RegistrationException, IOException, IdNotAvailableException {
		// first simulate connect
		long fakePlatformId = 3L;
		when(connection.isConnected()).thenReturn(true);
		when(connection.registerPlatform("testAgent", "dummyVersion")).thenReturn(fakePlatformId);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(versioning.getVersion()).thenReturn("dummyVersion");

		idManager.start();
		idManager.getPlatformId();
		idManager.unregisterPlatform();
		idManager.getPlatformId();
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under heavy load. There is
	 * no reliable way to make this test always successful.
	 */
	@Test
	public void registerMethodSensorTypes() throws InterruptedException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(connection.isConnected()).thenReturn(true);

		MethodSensorTypeConfig methodSensorType = mock(MethodSensorTypeConfig.class);
		List<MethodSensorTypeConfig> methodSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		methodSensorTypes.add(methodSensorType);
		when(configurationStorage.getMethodSensorTypes()).thenReturn(methodSensorTypes);

		idManager.start();
		assertThat(methodSensorType.getId(), is(0L));

		synchronized (this) {
			this.wait(2000L);
		}

		assertThat(idManager.getRegisteredSensorTypeId(methodSensorType.getId()), is(not(-1L)));

		idManager.stop();
	}

	/**
	 * This method could <b>fail</b> if the testing machine is currently under heavy load. There is
	 * no reliable way to make this test always successful.
	 */
	@Test
	public void registerPlatformSensorTypes() throws InterruptedException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(configurationStorage.getAgentName()).thenReturn("testAgent");
		when(connection.isConnected()).thenReturn(true);

		PlatformSensorTypeConfig platformSensorType = mock(PlatformSensorTypeConfig.class);
		List<PlatformSensorTypeConfig> platformSensorTypes = new ArrayList<PlatformSensorTypeConfig>();
		platformSensorTypes.add(platformSensorType);
		when(configurationStorage.getPlatformSensorTypes()).thenReturn(platformSensorTypes);

		idManager.start();
		assertThat(platformSensorType.getId(), is(0L));

		synchronized (this) {
			this.wait(2000L);
		}

		assertThat(idManager.getRegisteredSensorTypeId(platformSensorType.getId()), is(0L));

		idManager.stop();
	}

	@Test(expectedExceptions = { IdNotAvailableException.class })
	public void sensorTypeIdNotAvailable() throws InterruptedException, IdNotAvailableException, ConnectException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(false);
		doThrow(new ConnectException("fake")).when(connection).connect(anyString(), anyInt());

		MethodSensorTypeConfig methodSensorType = mock(MethodSensorTypeConfig.class);
		List<MethodSensorTypeConfig> methodSensorTypes = new ArrayList<MethodSensorTypeConfig>();
		methodSensorTypes.add(methodSensorType);
		when(configurationStorage.getMethodSensorTypes()).thenReturn(methodSensorTypes);

		idManager.start();
		assertThat(methodSensorType.getId(), is(0L));

		idManager.getRegisteredSensorTypeId(methodSensorType.getId());
	}

	@Test
	public void registerMethod() throws ConnectException, ServerUnavailableException, RegistrationException, IdNotAvailableException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(true);

		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);

		idManager.start();

		when(connection.registerMethod(anyInt(), eq(registeredSensorConfig))).thenReturn(7L).thenReturn(13L);
		long id = idManager.registerMethod(registeredSensorConfig);
		assertThat(id, is(greaterThanOrEqualTo(0L)));
		assertThat(idManager.getRegisteredMethodId(id), is(7L));

		id = idManager.registerMethod(registeredSensorConfig);
		assertThat(id, is(greaterThanOrEqualTo(0L)));
		assertThat(idManager.getRegisteredMethodId(id), is(13L));

		idManager.stop();
	}

	@Test
	public void testMapping() throws ServerUnavailableException, RegistrationException {
		RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
		when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
		when(connection.isConnected()).thenReturn(true);

		RegisteredSensorConfig registeredSensorConfig = mock(RegisteredSensorConfig.class);
		MethodSensorTypeConfig methodSensorType = mock(MethodSensorTypeConfig.class);

		idManager.start();

		when(connection.registerMethod(anyInt(), eq(registeredSensorConfig))).thenReturn(7L);
		when(connection.registerMethodSensorType(anyInt(), eq(methodSensorType))).thenReturn(5L);
		long methodId = idManager.registerMethod(registeredSensorConfig);
		long sensorTypeId = idManager.registerMethodSensorType(methodSensorType);
		idManager.addSensorTypeToMethod(sensorTypeId, methodId);

		idManager.stop();

		verify(connection).addSensorTypeToMethod(5L, 7L);
	}
}
