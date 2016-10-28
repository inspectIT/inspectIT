package rocks.inspectit.server.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.ClassInstrumentationChangedEvent;
import rocks.inspectit.server.event.AgentDeletedEvent;
import rocks.inspectit.server.event.AgentRegisteredEvent;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.InstrumentationStatus;
import rocks.inspectit.shared.all.communication.message.UpdatedInstrumentationMessage;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This class stores the changed, updated, removed or added {@link InstrumentationDefinition} which
 * should be send to the agent in the future.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AgentInstrumentationMessageGate implements ApplicationListener<ApplicationEvent> {

	/**
	 * Logger of this class.
	 */
	@Log
	private Logger log;

	/**
	 * The {@link AgentMessageProvider}.
	 */
	@Autowired
	private AgentMessageProvider messageProvider;

	/**
	 * The {@link AgentStatusDataProvider}.
	 */
	@Autowired
	private AgentStatusDataProvider agentStatusDataProvider;

	/**
	 * Map which maps agent IDs to a map. The inner map maps a class name to the latest
	 * {@link InstrumentationDefinition}.
	 */
	private final Map<Long, Map<String, InstrumentationDefinition>> definitionBuffer = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ClassInstrumentationChangedEvent) {
			handleClassInstrumentationChangedEvent((ClassInstrumentationChangedEvent) event);
		} else if (event instanceof AgentDeletedEvent) {
			handleAgentDeletedEvent((AgentDeletedEvent) event);
		} else if (event instanceof AgentRegisteredEvent) {
			handleAgentRegisteredEvent((AgentRegisteredEvent) event);
		}
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
	 * Handles an event of type {@link ClassInstrumentationChangedEvent}.
	 *
	 * @param event
	 *            the event instance
	 */
	private void handleClassInstrumentationChangedEvent(ClassInstrumentationChangedEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("Putting instrumentation definitions for agent {} into the definition buffer.", event.getAgentId());
		}

		Map<String, InstrumentationDefinition> pendingDefinitions = definitionBuffer.get(event.getAgentId());
		if (pendingDefinitions == null) {
			pendingDefinitions = new HashMap<>();
			definitionBuffer.put(event.getAgentId(), pendingDefinitions);
		}

		for (InstrumentationDefinition definition : event.getInstrumentationDefinitions()) {
			pendingDefinitions.put(definition.getClassName(), definition);
		}

		AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(event.getAgentId());
		if (agentStatusData != null) {
			if (agentStatusData.getInstrumentationStatus() != InstrumentationStatus.PENDING) {
				agentStatusData.setInstrumentationStatus(InstrumentationStatus.PENDING);
				agentStatusData.setPendingSinceTime(System.currentTimeMillis());
			}
		}
	}

	/**
	 * Creates an {@link UpdatedInstrumentationMessage} which contains all stored
	 * {@link InstrumentationDefinition}. The created message is put in the message provider for the
	 * agent to fetch.
	 *
	 * @param platformId
	 *            the id of the platform which {@link InstrumentationDefinition}s should be provided
	 *            for fetching
	 */
	public synchronized void flush(long platformId) {
		if (log.isDebugEnabled()) {
			log.debug("Flushing new instrumentations for agent {}.", platformId);
		}

		Map<String, InstrumentationDefinition> pendingDefinitions = definitionBuffer.put(platformId, new HashMap<String, InstrumentationDefinition>());

		if (MapUtils.isNotEmpty(pendingDefinitions)) {
			UpdatedInstrumentationMessage message = new UpdatedInstrumentationMessage();
			message.getMessageContent().addAll(pendingDefinitions.values());

			messageProvider.provideMessage(platformId, message);
		}

		AgentStatusData agentStatusData = agentStatusDataProvider.getAgentStatusDataMap().get(platformId);
		if (agentStatusData != null) {
			agentStatusData.setInstrumentationStatus(InstrumentationStatus.UP_TO_DATE);
		}
	}

	/**
	 * Removes all {@link InstrumentationDefinition} which have been stored to send to the agent in
	 * a later point of time.
	 *
	 * @param platformId
	 *            the id of the platform which {@link InstrumentationDefinition}s should be removed
	 */
	public synchronized void clear(long platformId) {
		Map<String, InstrumentationDefinition> pendingDefinitions = definitionBuffer.get(platformId);
		if (MapUtils.isNotEmpty(pendingDefinitions)) {
			if (log.isDebugEnabled()) {
				log.debug("Clearing stored instrumentations for agent {}.", platformId);
			}

			pendingDefinitions.clear();
		}
	}
}
