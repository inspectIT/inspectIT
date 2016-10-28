package rocks.inspectit.agent.java.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AgentMessagesReceivedEvent} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AgentMessagesReceivedEventTest extends TestBase {

	/**
	 * Tests the {@link AgentMessagesReceivedEvent#AgentMessagesReceivedEvent(Object, Collection)}
	 * constructor.
	 */
	public static class Constrcutor extends AgentMessagesReceivedEventTest {

		@Test
		public void successful() {
			Object eventSource = mock(Object.class);
			IAgentMessage<?> message = mock(IAgentMessage.class);
			List<IAgentMessage<?>> messages = Arrays.<IAgentMessage<?>> asList(message);

			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, messages);

			assertThat(eventSource, is(equalTo(event.getSource())));
			assertThat(messages, is(equalTo(event.getAgentMessages())));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullAgentMessages() {
			Object eventSource = mock(Object.class);

			new AgentMessagesReceivedEvent(eventSource, null);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullEventSource() {
			IAgentMessage<?> message = mock(IAgentMessage.class);
			List<IAgentMessage<?>> messages = Arrays.<IAgentMessage<?>> asList(message);

			new AgentMessagesReceivedEvent(null, messages);
		}
	}

}
