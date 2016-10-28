package rocks.inspectit.server.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.ci.event.ClassInstrumentationChangedEvent;
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
public class AgentInstrumentationMessageGate implements ApplicationListener<ClassInstrumentationChangedEvent> {

	/**
	 * Logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * The {@link AgentMessageProvider}.
	 */
	@Autowired
	AgentMessageProvider messageProvider;

	/**
	 * The {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider agentStatusDataProvider;

	/**
	 * Map which maps agent IDs to a map. The inner map maps a class name to the latest
	 * {@link InstrumentationDefinition}.
	 */
	private final Map<Long, Map<String, InstrumentationDefinition>> definitionBuffer = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(ClassInstrumentationChangedEvent event) {
		if (event == null) {
			if (log.isDebugEnabled()) {
				log.debug("The received event is 'null' and will not be processed.");
			}
			return;
		}
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
				agentStatusData.setLastInstrumentationUpate(System.currentTimeMillis());
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
	public void flush(long platformId) {
		if (platformId < 0L) {
			throw new IllegalArgumentException("The platform ident may not be negative.");
		}
		if (log.isDebugEnabled()) {
			log.debug("Flushing new instrumentations for agent {}.", platformId);
		}

		Map<String, InstrumentationDefinition> pendingDefinitions = definitionBuffer.put(platformId, new HashMap<String, InstrumentationDefinition>());

		if (MapUtils.isNotEmpty(pendingDefinitions)) {
			UpdatedInstrumentationMessage message = new UpdatedInstrumentationMessage();
			message.getUpdatedInstrumentationDefinitions().addAll(pendingDefinitions.values());

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
	public void clear(long platformId) {
		if (platformId < 0L) {
			throw new IllegalArgumentException("The platform ident may not be negative.");
		}
		if (log.isDebugEnabled()) {
			log.debug("Clearing stored instrumentations for agent {}.", platformId);
		}

		Map<String, InstrumentationDefinition> pendingDefinitions = definitionBuffer.get(platformId);
		if (MapUtils.isNotEmpty(pendingDefinitions)) {
			pendingDefinitions.clear();
		}
	}
}
