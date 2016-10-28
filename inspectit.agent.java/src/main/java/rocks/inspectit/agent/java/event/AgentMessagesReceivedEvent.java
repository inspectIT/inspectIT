package rocks.inspectit.agent.java.event;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationEvent;

import rocks.inspectit.shared.all.communication.message.AbstractAgentMessage;

/**
 * Event signaling that one or multiple {@link AbstractAgentMessage}s have been received.
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
	private final Collection<AbstractAgentMessage> agentMessages;

	/**
	 * Default constructor for the event.
	 *
	 * @param source
	 *            event source
	 * @param agentMessages
	 *            the received messages
	 */
	public AgentMessagesReceivedEvent(Object source, Collection<AbstractAgentMessage> agentMessages) {
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
	public Collection<AbstractAgentMessage> getAgentMessages() {
		return this.agentMessages;
	}

}
