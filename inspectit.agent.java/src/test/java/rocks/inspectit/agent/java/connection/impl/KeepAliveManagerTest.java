package rocks.inspectit.agent.java.connection.impl;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.ConnectException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class KeepAliveManagerTest extends TestBase {

	@InjectMocks
	KeepAliveManager manager;

	@Mock
	IConnection connection;

	@Mock
	IPlatformManager platformManager;

	@Mock
	Logger log;

	public static class SendKeepAlive extends KeepAliveManagerTest {

		static final long PLATFORM_ID = 1L;

		@Test
		public void connected() throws Exception {
			when(connection.isConnected()).thenReturn(true);
			when(platformManager.getPlatformId()).thenReturn(PLATFORM_ID);

			manager.sendKeepAlive();

			verify(connection).isConnected();
			verify(connection).sendKeepAlive(PLATFORM_ID);
			verifyNoMoreInteractions(connection);
		}

		@Test
		public void connectedThenDisconnected() throws Exception {
			when(connection.isConnected()).thenReturn(true).thenReturn(false);
			when(platformManager.getPlatformId()).thenReturn(PLATFORM_ID);

			manager.sendKeepAlive();
			manager.sendKeepAlive();

			verify(connection, times(2)).isConnected();
			verify(connection).sendKeepAlive(PLATFORM_ID);
			// no reconnect at first disconnect
			verifyNoMoreInteractions(connection);
		}

		@Test
		public void reconnect() throws Exception {
			when(connection.isConnected()).thenReturn(false);
			doThrow(ConnectException.class).when(connection).reconnect();

			manager.sendKeepAlive();
			manager.sendKeepAlive();

			verify(connection, times(2)).isConnected();
			verify(connection).reconnect();
			verifyNoMoreInteractions(connection);
		}

		@Test
		public void reconnectExponential() throws Exception {
			when(connection.isConnected()).thenReturn(false);
			doThrow(ConnectException.class).when(connection).reconnect();

			manager.sendKeepAlive();
			manager.sendKeepAlive();
			verify(connection, times(1)).reconnect();

			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			verify(connection, times(2)).reconnect();

			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			manager.sendKeepAlive();
			verify(connection, times(3)).reconnect();

			verify(connection, atLeast(1)).isConnected();
			verifyNoMoreInteractions(connection);
		}

		@Test
		public void reconnectKeepAliveReconnect() throws Exception {
			when(connection.isConnected()).thenReturn(false);
			when(platformManager.getPlatformId()).thenReturn(PLATFORM_ID);
			doThrow(ConnectException.class).when(connection).reconnect();

			// first make sure we fire one reconnect
			manager.sendKeepAlive();
			manager.sendKeepAlive();

			verify(connection, times(1)).reconnect();

			// then switch to connected
			when(connection.isConnected()).thenReturn(true);

			manager.sendKeepAlive();

			verify(connection, times(1)).reconnect();
			verify(connection, times(1)).sendKeepAlive(PLATFORM_ID);

			// then back to reconnect, but not called on first call
			when(connection.isConnected()).thenReturn(false);

			manager.sendKeepAlive();

			verify(connection, times(1)).reconnect();

			verify(connection, atLeast(1)).isConnected();
			verifyNoMoreInteractions(connection);
		}

	}

}
