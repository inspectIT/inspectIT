package rocks.inspectit.agent.java.core.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.impl.RepositoryConfig;
import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.version.VersionService;

@SuppressWarnings("PMD")
public class PlatformManagerTest extends TestBase {

	@InjectMocks
	PlatformManager platformManager;

	@Mock
	Logger log;

	@Mock
	IConfigurationStorage configurationStorage;

	@Mock
	IConnection connection;

	@Mock
	VersionService versionService;

	@Mock
	AgentConfig agentConfiguration;

	public class AfterPropertiesSet extends PlatformManagerTest {

		@Test
		public void connectAndRetrievePlatformId() throws Exception {
			String host = "localhost";
			int port = 1099;
			RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
			when(repositoryConfig.getHost()).thenReturn(host);
			when(repositoryConfig.getPort()).thenReturn(port);

			when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
			when(configurationStorage.getAgentName()).thenReturn("testAgent");
			when(versionService.getVersionAsString()).thenReturn("dummyVersion");

			long fakePlatformId = 7L;
			when(connection.isConnected()).thenReturn(false);
			when(connection.register("testAgent", "dummyVersion")).thenReturn(agentConfiguration);
			when(agentConfiguration.getPlatformId()).thenReturn(fakePlatformId);

			platformManager.afterPropertiesSet();
			long platformId = platformManager.getPlatformId();

			assertThat(platformId, is(equalTo(fakePlatformId)));
			verify(connection, times(1)).connect(host, port);
		}

		@Test
		public void retrievePlatformId() throws Exception {
			long fakePlatformId = 3L;
			when(connection.isConnected()).thenReturn(true);
			when(configurationStorage.getAgentName()).thenReturn("testAgent");
			when(versionService.getVersionAsString()).thenReturn("dummyVersion");
			when(connection.register("testAgent", "dummyVersion")).thenReturn(agentConfiguration);
			when(agentConfiguration.getPlatformId()).thenReturn(fakePlatformId);

			platformManager.afterPropertiesSet();
			long platformId = platformManager.getPlatformId();

			assertThat(platformId, is(equalTo(fakePlatformId)));
			verify(connection, times(0)).connect(anyString(), anyInt());
		}

		@Test(expectedExceptions = { IdNotAvailableException.class })
		public void platformIdNotAvailable() throws Exception {
			RepositoryConfig repositoryConfig = mock(RepositoryConfig.class);
			when(configurationStorage.getRepositoryConfig()).thenReturn(repositoryConfig);
			when(connection.isConnected()).thenReturn(false);
			doThrow(new ConnectException("fake")).when(connection).connect(anyString(), anyInt());

			platformManager.afterPropertiesSet();
			platformManager.getPlatformId();
		}
	}

	public class UnregisterPlatform extends PlatformManagerTest {

		/**
		 * Tests that unregister of platform is executed if connection to the server is established
		 * and registration is performed.
		 */
		@Test
		public void unregisterPlatform() throws Exception {
			// first simulate connect
			long fakePlatformId = 3L;
			when(connection.isConnected()).thenReturn(true);
			when(configurationStorage.getAgentName()).thenReturn("testAgent");
			when(versionService.getVersionAsString()).thenReturn("dummyVersion");
			when(connection.register("testAgent", "dummyVersion")).thenReturn(agentConfiguration);
			when(agentConfiguration.getPlatformId()).thenReturn(fakePlatformId);

			platformManager.afterPropertiesSet();
			platformManager.getPlatformId();
			platformManager.unregisterPlatform();

			verify(connection, times(1)).unregister(fakePlatformId);
			verify(connection, times(1)).disconnect();
		}

		/**
		 * Test that unregister will not be called if there is no active connection to the server
		 * and registration is not done at first place.
		 */
		@Test
		public void noUnregisterPlatform() throws Exception {
			// no unregister if no connection
			when(connection.isConnected()).thenReturn(false);
			platformManager.unregisterPlatform();

			// no unregister if registration is not done at the first place
			when(connection.isConnected()).thenReturn(true);
			platformManager.unregisterPlatform();

			verify(connection, times(0)).unregister(anyLong());
		}

		/**
		 * If unregister is called with shutdown initialized marker every next call to getPlatformId
		 * should throw an exception
		 */
		@Test(expectedExceptions = { IdNotAvailableException.class })
		public void unregisterPlatformAndInitShutdown() throws Exception {
			// first simulate connect
			long fakePlatformId = 3L;
			when(connection.isConnected()).thenReturn(true);
			when(configurationStorage.getAgentName()).thenReturn("testAgent");
			when(versionService.getVersionAsString()).thenReturn("dummyVersion");
			when(connection.register("testAgent", "dummyVersion")).thenReturn(agentConfiguration);
			when(agentConfiguration.getPlatformId()).thenReturn(fakePlatformId);

			platformManager.afterPropertiesSet();
			platformManager.getPlatformId();
			platformManager.unregisterPlatform();
			platformManager.getPlatformId();
		}
	}

}
