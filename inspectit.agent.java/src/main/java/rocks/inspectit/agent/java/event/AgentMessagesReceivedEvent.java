package rocks.inspectit.agent.java.event;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.all.communication.message.IAgentMessage;

/**
 * Event signaling that one or multiple {@link IAgentMessage}s have been received.
 *
 * @author Marius Oehler
 *
 */
public class AgentMessagesReceivedEvent extends ApplicationEvent {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2672674294030516359L;

	/**
	 * The received messages.
	 */
	private final List<IAgentMessage<?>> agentMessages;

	/**
	 * Default constructor for the event.
	 *
	 * @param source
	 *            event source
	 * @param agentMessages
	 *            the received messages
	 */
	public AgentMessagesReceivedEvent(Object source, List<IAgentMessage<?>> agentMessages) {
		super(source);
		if (CollectionUtils.isEmpty(agentMessages)) {
			throw new IllegalArgumentException("The given collection of agent messages may not be null or empty.");
		}
		this.agentMessages = agentMessages;
	}

	/**
	 * Gets {@link #agentMessages}.
	 *
	 * @return {@link #agentMessages}
	 */
	public List<IAgentMessage<?>> getAgentMessages() {
		return this.agentMessages;
	}

}
