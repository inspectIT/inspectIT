package rocks.inspectit.server.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.message.AbstractAgentMessage;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Provides {@link AbstractAgentMessage} for the agent to fetch.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AgentMessageProvider {

	/**
	 * Logger of this class.
	 */
	@Log
	private Logger log;

	/**
	 * Map containing messages which can be fetched by the agent.
	 */
	private final Map<Long, List<AbstractAgentMessage>> agentMessageMap = new HashMap<>();

	/**
	 * Fetches all available messages for the agent with the given id.
	 *
	 * @param platformId
	 *            the agent id
	 * @return {@link Collection} of {@link AbstractAgentMessage}s.
	 */
	public Collection<AbstractAgentMessage> fetchMessages(long platformId) {
		if (log.isTraceEnabled()) {
			log.trace("Fetch messages for agent {}.", platformId);
		}

		List<AbstractAgentMessage> currentList = agentMessageMap.put(platformId, new ArrayList<AbstractAgentMessage>());

		if (CollectionUtils.isEmpty(currentList)) {
			currentList = new ArrayList<>(0);
		}

		return currentList;
	}

	/**
	 * Puts the given {@link AbstractAgentMessage} in the list which can be fetched by the agent.
	 *
	 * @param platformId
	 *            the id of the agent to receive the message
	 * @param message
	 *            the {@link AbstractAgentMessage}
	 */
	public void provideMessage(long platformId, AbstractAgentMessage message) {
		if (message == null) {
			throw new IllegalArgumentException("The agent message may not be null.");
		}
		if (log.isDebugEnabled()) {
			log.debug("Provide new message for agent {}.", platformId);
		}

		List<AbstractAgentMessage> messageList = agentMessageMap.get(platformId);

		if (messageList == null) {
			messageList = new ArrayList<>();
			agentMessageMap.put(platformId, messageList);
		}

		messageList.add(message);
	}

	/**
	 * Puts the given {@link Collection} of {@link AbstractAgentMessage}s in the list which can be
	 * fetched by the agent.
	 *
	 * @param platformId
	 *            the id of the agent to receive the message
	 * @param messages
	 *            the {@link Collection} of {@link AbstractAgentMessage}s
	 */
	public void provideMessages(long platformId, Collection<AbstractAgentMessage> messages) {
		if (CollectionUtils.isEmpty(messages)) {
			throw new IllegalArgumentException("The agent messages may not be null or empty.");
		}
		if (log.isDebugEnabled()) {
			log.debug("Provide new messages for agent {}.", platformId);
		}

		List<AbstractAgentMessage> messageList = agentMessageMap.get(platformId);

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
	public void clear(long platformId) {
		List<AbstractAgentMessage> messages = agentMessageMap.get(platformId);

		if (CollectionUtils.isNotEmpty(messages)) {
			messages.clear();
		}
	}
}
