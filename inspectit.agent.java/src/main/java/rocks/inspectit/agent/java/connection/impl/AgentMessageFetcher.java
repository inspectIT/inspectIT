package rocks.inspectit.agent.java.connection.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.event.AgentMessagesReceivedEvent;
import rocks.inspectit.shared.all.communication.message.IAgentMessage;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This component is responsible for fetching and publishing available
 * {@link IAgentMessage}s.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AgentMessageFetcher implements Runnable {

	/**
	 * The interval in seconds of fetching the available {@link IAgentMessage}s.
	 */
	private static final long FETCH_INTERVAL_SECONDS = 30L;

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * Core-service executor service.
	 */
	@Autowired
	@Qualifier("coreServiceExecutorService")
	private ScheduledExecutorService executorService;

	/**
	 * The used connection.
	 */
	@Autowired
	IConnection connection;

	/**
	 * Spring {@link ApplicationEventPublisher} for publishing the events.
	 */
	@Autowired
	private ApplicationEventPublisher eventPublisher;

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void postConstruct() {
		executorService.scheduleAtFixedRate(this, FETCH_INTERVAL_SECONDS, FETCH_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			List<IAgentMessage<?>> messages = fetchMessages();

			if (CollectionUtils.isNotEmpty(messages)) {
				AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(this, messages);
				eventPublisher.publishEvent(event);
			}
		} catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn("An unexpected exception ocurred.", e);
			}
		}
	}

	/**
	 * Fetches the available {@link IAgentMessage}s from the CMR.
	 *
	 * @return Collection containing all {@link IAgentMessage}s
	 */
	@SuppressWarnings("unchecked")
	private List<IAgentMessage<?>> fetchMessages() {
		if (log.isDebugEnabled()) {
			log.debug("Fetching agent messages of CMR.");
		}
		if (connection.isConnected()) {
			try {
				return connection.fetchAgentMessages(platformManager.getPlatformId());
			} catch (ServerUnavailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("CMR is not available. Agent messages could not been fetched.", e);
				}
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("Platform ID is not available. Agent messages could not been fetched.", e);
				}
			}
		}
		return Collections.EMPTY_LIST;
	}
}
