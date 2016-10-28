package rocks.inspectit.server.messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.event.AgentDeletedEvent;
import rocks.inspectit.server.event.AgentRegisteredEvent;
import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.communication.message.UpdatedInstrumentationMessage;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * Provides {@link IAgentMessage} for the agent to fetch.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AgentMessageProvider implements ApplicationListener<ApplicationEvent> {

	/**
	 * Logger of this class.
	 */
	@Log
	private Logger log;

	/**
	 * The registration service.
	 */
	@Autowired
	private IRegistrationService registrationService;

	/**
	 * Map containing messages which can be fetched by the agent.
	 */
	private final Map<Long, List<IAgentMessage<?>>> agentMessageMap = new HashMap<>();

	/**
	 * Fetches all available messages for the agent with the given id. The returned list is an
	 * ordered list, ordered by time (ascending -> index 0 is the oldest).
	 *
	 * @param platformId
	 *            the agent id
	 * @return {@link List} of {@link IAgentMessage}s.
	 */
	public synchronized List<IAgentMessage<?>> fetchMessages(long platformId) {
		if (log.isTraceEnabled()) {
			log.trace("Fetch messages for agent {}.", platformId);
		}

		List<IAgentMessage<?>> currentList = agentMessageMap.put(platformId, new ArrayList<IAgentMessage<?>>());

		if (CollectionUtils.isEmpty(currentList)) {
			currentList = Collections.emptyList();
		}

		// update timestamp of method idents (resulting in a disabled method ident)
		updateMethodIdentTimestamps(platformId, currentList);

		return currentList;
	}

	/**
	 * Updates the timestamp of all method idents matching the {@link InstrumentationDefinition}s in
	 * the given message.
	 *
	 * @param platformId
	 *            the platform id
	 * @param messages
	 *            all agent messages
	 */
	private void updateMethodIdentTimestamps(long platformId, List<IAgentMessage<?>> messages) {
		for (IAgentMessage<?> agentMessage : messages) {
			if (agentMessage instanceof UpdatedInstrumentationMessage) {
				UpdatedInstrumentationMessage message = (UpdatedInstrumentationMessage) agentMessage;
				for (InstrumentationDefinition definition : message.getMessageContent()) {
					String fqn = definition.getClassName();
					int index = fqn.lastIndexOf('.');
					String packageName = fqn.substring(0, index);
					String className = fqn.substring(index + 1, fqn.length());

					registrationService.updateMethodIdentTimestamp(platformId, packageName, className);
				}
			}
		}
	}

	/**
	 * Puts the given {@link IAgentMessage} in the list which can be fetched by the agent.
	 *
	 * @param platformId
	 *            the id of the agent to receive the message
	 * @param message
	 *            the {@link IAgentMessage}
	 */
	public synchronized void provideMessage(long platformId, IAgentMessage<?> message) {
		if (message == null) {
			throw new IllegalArgumentException("The agent message may not be null.");
		}
		List<IAgentMessage<?>> messageList = Arrays.<IAgentMessage<?>> asList(message);
		provideMessages(platformId, messageList);
	}

	/**
	 * Puts the given {@link Collection} of {@link IAgentMessage}s in the list which can be fetched
	 * by the agent.
	 *
	 * @param platformId
	 *            the id of the agent to receive the message
	 * @param messages
	 *            the {@link Collection} of {@link IAgentMessage}s
	 */
	public synchronized void provideMessages(long platformId, Collection<IAgentMessage<?>> messages) {
		if (CollectionUtils.isEmpty(messages)) {
			throw new IllegalArgumentException("The agent messages may not be null or empty.");
		}
		if (log.isDebugEnabled()) {
			log.debug("Provide new messages for agent {}.", platformId);
		}

		List<IAgentMessage<?>> messageList = agentMessageMap.get(platformId);

		if (messageList == null) {
			messageList = new ArrayList<>();
			agentMessageMap.put(platformId, messageList);
		}

		messageList.addAll(messages);
	}

	/**
	 * Removes all available messages of the agent with the given ID.
	 *
	 * @param platformId
	 *            id of the agent which messages should be removed
	 */
	public synchronized void clear(long platformId) {
		List<IAgentMessage<?>> messages = agentMessageMap.get(platformId);
		if (CollectionUtils.isNotEmpty(messages)) {
			if (log.isDebugEnabled()) {
				log.debug("Clearing messages of agent {}.", platformId);
			}

			messages.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof AgentDeletedEvent) {
			handleAgentDeletedEvent((AgentDeletedEvent) event);
		} else if (event instanceof AgentRegisteredEvent) {
			handleAgentRegisteredEvent((AgentRegisteredEvent) event);
		}
	}

	/**
	 * Handles an event of type {@link AgentDeletedEvent}.
	 *
	 * @param event
	 *            the event instance
	 */
	private void handleAgentDeletedEvent(AgentDeletedEvent event) {
		clear(event.getPlatformId());
	}

	/**
	 * Handles an event of type {@link AgentRegisteredEvent}.
	 *
	 * @param event
	 *            the event instance
	 */
	private void handleAgentRegisteredEvent(AgentRegisteredEvent event) {
		clear(event.getPlatformId());
	}
}
