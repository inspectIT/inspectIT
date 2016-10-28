package rocks.inspectit.server.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AgentMessageProvider} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AgentMessageProviderTest extends TestBase {

	@InjectMocks
	AgentMessageProvider messageProvider;

	@Mock
	Logger log;

	/**
	 * Tests the {@link AgentMessageProvider#provideMessage(long, IAgentMessage)} class.
	 */
	public static class ProvideMessage extends AgentMessageProviderTest {

		@Test
		@SuppressWarnings("unchecked")
		public void successful() {
			IAgentMessage<?> messageOne = mock(IAgentMessage.class);
			IAgentMessage<?> messageTwo = mock(IAgentMessage.class);
			IAgentMessage<?> messageThree = mock(IAgentMessage.class);

			messageProvider.provideMessage(10L, messageOne);
			messageProvider.provideMessage(10L, messageTwo);
			messageProvider.provideMessage(10L, messageThree);

			assertThat(messageProvider.fetchMessages(10L), contains(messageOne, messageTwo, messageThree));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullMessage() {
			try {
				messageProvider.provideMessage(10L, null);
			} finally {
				assertThat(messageProvider.fetchMessages(10L), hasSize(0));
			}
		}
	}

	/**
	 * Tests the {@link AgentMessageProvider#provideMessages(long, java.util.Collection)} class.
	 */
	public static class ProvideMessages extends AgentMessageProviderTest {

		@Test
		@SuppressWarnings("unchecked")
		public void successful() {
			IAgentMessage<?> messageOne = mock(IAgentMessage.class);
			IAgentMessage<?> messageTwo = mock(IAgentMessage.class);
			IAgentMessage<?> messageThree = mock(IAgentMessage.class);

			messageProvider.provideMessages(10L, Arrays.asList(messageOne, messageTwo, messageThree));

			assertThat(messageProvider.fetchMessages(10L), contains(messageOne, messageTwo, messageThree));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullMessages() {
			try {
				messageProvider.provideMessages(10L, null);
			} finally {
				assertThat(messageProvider.fetchMessages(10L), hasSize(0));
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		@SuppressWarnings("unchecked")
		public void emptyMessages() {
			try {
				messageProvider.provideMessages(10L, Collections.EMPTY_LIST);
			} finally {
				assertThat(messageProvider.fetchMessages(10L), hasSize(0));
			}
		}
	}

	/**
	 * Tests the {@link AgentMessageProvider#fetchMessages(long)} class.
	 */
	public static class Fetch extends AgentMessageProviderTest {

		@Test
		@SuppressWarnings("unchecked")
		public void successful() {
			IAgentMessage<?> messageOne = mock(IAgentMessage.class);
			IAgentMessage<?> messageTwo = mock(IAgentMessage.class);
			IAgentMessage<?> messageThree = mock(IAgentMessage.class);

			messageProvider.provideMessages(10L, Arrays.asList(messageOne, messageTwo));

			Collection<IAgentMessage<?>> resultOne = messageProvider.fetchMessages(10L);
			Collection<IAgentMessage<?>> resultTwo = messageProvider.fetchMessages(10L);

			messageProvider.provideMessage(10L, messageThree);

			Collection<IAgentMessage<?>> resultThree = messageProvider.fetchMessages(10L);

			assertThat(resultOne, contains(messageOne, messageTwo));
			assertThat(resultTwo, is(empty()));
			assertThat(resultThree, hasSize(1));
			assertThat(resultThree, hasItem(messageThree));
		}

		@Test
		public void unknownPlatformId() {
			IAgentMessage<?> messageOne = mock(IAgentMessage.class);
			IAgentMessage<?> messageTwo = mock(IAgentMessage.class);
			messageProvider.provideMessages(10L, Arrays.asList(messageOne, messageTwo));

			Collection<IAgentMessage<?>> result = messageProvider.fetchMessages(1L);

			assertThat(result, hasSize(0));
		}
	}

	/**
	 * Tests the {@link AgentMessageProvider#clear(long)} class.
	 */
	public static class Clear extends AgentMessageProviderTest {

		@Test
		public void successful() {
			IAgentMessage<?> messageOne = mock(IAgentMessage.class);
			IAgentMessage<?> messageTwo = mock(IAgentMessage.class);
			messageProvider.provideMessages(10L, Arrays.asList(messageOne, messageTwo));

			messageProvider.clear(10L);

			assertThat(messageProvider.fetchMessages(10L), hasSize(0));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void unknownPlatformId() {
			IAgentMessage<?> messageOne = mock(IAgentMessage.class);
			IAgentMessage<?> messageTwo = mock(IAgentMessage.class);
			messageProvider.provideMessages(10L, Arrays.asList(messageOne, messageTwo));

			messageProvider.clear(20L);

			assertThat(messageProvider.fetchMessages(10L), contains(messageOne, messageTwo));
		}
	}
}
