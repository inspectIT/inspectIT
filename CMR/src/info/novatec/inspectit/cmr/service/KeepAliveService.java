package info.novatec.inspectit.cmr.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;
import info.novatec.inspectit.spring.logger.Log;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to keep track of the online-status of registered agents.
 * 
 * @author Marius Oehler 
 *
 */
@Service
public class KeepAliveService implements IKeepAliveService, Runnable {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider platformIdentDateSaver;

	/**
	 * Thread pool to provide a scheduled thread to send keep alive messages.
	 */
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendKeepAlive(long platformId) {
		if (log.isDebugEnabled()) {
			log.debug("Received keep-alive signal from platform " + platformId);
		}

		// Updates the agent status if no keep-alive messages were received before
		AgentStatusData statusData = platformIdentDateSaver.getAgentStatusDataMap().get(platformId);
		if (statusData != null && statusData.getAgentConnection() == AgentConnection.NO_KEEP_ALIVE) {
			platformIdentDateSaver.registerConnected(platformId);
		}

		platformIdentDateSaver.registerKeepAliveSent(platformId);
	}

	/**
	 * Starts a continuous thread to be able to detect dead agents.
	 */
	@PostConstruct
	public void startKeepAliveObservator() {
		scheduledThreadPool.scheduleAtFixedRate(this, IKeepAliveService.KA_INITIAL_DELAY, IKeepAliveService.KA_TIMEOUT, TimeUnit.MILLISECONDS);

		if (log.isInfoEnabled()) {
			log.info("|-Keep Alive Service active...");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Map<Long, AgentStatusData> statusMap = platformIdentDateSaver.getAgentStatusDataMap();

		for (Entry<Long, AgentStatusData> entry : statusMap.entrySet()) {
			if (entry.getValue().getAgentConnection() != AgentConnection.CONNECTED) {
				continue;
			}

			long timeToLastSignal = System.currentTimeMillis() - entry.getValue().getLastKeepAliveTimestamp();

			if (timeToLastSignal > IKeepAliveService.KA_TIMEOUT) {
				platformIdentDateSaver.registerKeepAliveTimeout(entry.getKey());

				if (log.isInfoEnabled()) {
					log.info("Platform " + entry.getKey() + " timed out.");
				}
			}
		}
	}
}
