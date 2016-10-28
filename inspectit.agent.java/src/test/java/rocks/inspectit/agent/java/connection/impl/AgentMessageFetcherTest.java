package rocks.inspectit.agent.java.connection.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.event.AgentMessagesReceivedEvent;
import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AgentMessageFetcher} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AgentMessageFetcherTest extends TestBase {

	@InjectMocks
	AgentMessageFetcher messageFetcher;

	@Mock
	Logger log;

	/**
	 * Test the {@link AgentMessageFetcher#postConstruct()} method.
	 */
	public static class PostConstruct extends AgentMessageFetcherTest {

		@Mock
		ScheduledExecutorService executorService;

		@Test
		public void successful() {
			messageFetcher.postConstruct();

			verify(executorService).scheduleAtFixedRate(messageFetcher, 30L, 30L, TimeUnit.SECONDS);
			verifyNoMoreInteractions(executorService);
		}
	}

	/**
	 * Test the {@link AgentMessageFetcher#run()} method.
	 */
	public static class Run extends AgentMessageFetcherTest {

		@Mock
		ApplicationEventPublisher eventPublisher;

		@Mock
		IPlatformManager platformManager;

		@Mock
		IConnection connection;

		@Test
		public void successful() throws IdNotAvailableException, ServerUnavailableException {
			when(connection.isConnected()).thenReturn(true);
			when(platformManager.getPlatformId()).thenReturn(10L);
			List<IAgentMessage<?>> messages = Arrays.<IAgentMessage<?>> asList(mock(IAgentMessage.class));
			when(connection.fetchAgentMessages(10L)).thenReturn(messages);

			messageFetcher.run();

			verify(connection).isConnected();
			verify(connection).fetchAgentMessages(10L);
			verify(platformManager).getPlatformId();
			ArgumentCaptor<AgentMessagesReceivedEvent> eventCaptor = ArgumentCaptor.forClass(AgentMessagesReceivedEvent.class);
			verify(eventPublisher).publishEvent(eventCaptor.capture());
			verifyNoMoreInteractions(connection, eventPublisher, platformManager);
			assertThat(eventCaptor.getValue().getAgentMessages(), is(equalTo(messages)));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void successfulNoMessages() throws IdNotAvailableException, ServerUnavailableException {
			when(connection.isConnected()).thenReturn(true);
			when(platformManager.getPlatformId()).thenReturn(10L);
			when(connection.fetchAgentMessages(10L)).thenReturn(Collections.EMPTY_LIST);

			messageFetcher.run();

			verify(connection).isConnected();
			verify(connection).fetchAgentMessages(10L);
			verify(platformManager).getPlatformId();
			verifyNoMoreInteractions(connection, platformManager);
			verifyZeroInteractions(eventPublisher);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void serverUnavailable() throws IdNotAvailableException, ServerUnavailableException {
			when(connection.isConnected()).thenReturn(true);
			when(platformManager.getPlatformId()).thenReturn(10L);
			when(connection.fetchAgentMessages(any(Long.class))).thenThrow(ServerUnavailableException.class);

			messageFetcher.run();

			verify(connection).isConnected();
			verify(connection).fetchAgentMessages(any(Long.class));
			verify(platformManager).getPlatformId();
			verifyNoMoreInteractions(connection, platformManager);
			verifyZeroInteractions(eventPublisher);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void unexpectedException() throws IdNotAvailableException, ServerUnavailableException {
			when(connection.isConnected()).thenReturn(true);
			when(platformManager.getPlatformId()).thenReturn(10L);
			when(connection.fetchAgentMessages(any(Long.class))).thenThrow(RuntimeException.class);

			messageFetcher.run();

			verify(connection).isConnected();
			verify(connection).fetchAgentMessages(any(Long.class));
			verify(platformManager).getPlatformId();
			verifyNoMoreInteractions(connection, platformManager);
			verifyZeroInteractions(eventPublisher);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void noIdAvailable() throws IdNotAvailableException {
			when(connection.isConnected()).thenReturn(true);
			when(platformManager.getPlatformId()).thenThrow(IdNotAvailableException.class);

			messageFetcher.run();

			verify(connection).isConnected();
			verify(platformManager).getPlatformId();
			verifyNoMoreInteractions(connection, platformManager);
			verifyZeroInteractions(eventPublisher);
		}

		@Test
		public void notConnected() {
			when(connection.isConnected()).thenReturn(false);

			messageFetcher.run();

			verify(connection).isConnected();
			verifyNoMoreInteractions(connection);
			verifyZeroInteractions(eventPublisher, platformManager);
		}
	}
}
