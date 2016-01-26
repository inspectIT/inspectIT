package rocks.inspectit.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.event.AgentDeletedEvent;
import rocks.inspectit.shared.all.cmr.service.IKeepAliveService;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Bean that saves the time when the last time platform ident received the data.
 *
 * @author Ivan Senic
 *
 */
@Component
public class AgentStatusDataProvider implements InitializingBean, ApplicationListener<AgentDeletedEvent> {

	/**
	 * Runnable for checking the status of the received keep-alive signals.
	 */
	private final Runnable keepAliveCheckRunner = new Runnable() {
		@Override
		public void run() {
			long currentTime = System.currentTimeMillis();
			for (Entry<Long, AgentStatusData> entry : agentStatusDataMap.entrySet()) {
				if (entry.getValue().getAgentConnection() != AgentConnection.CONNECTED) {
					continue;
				}

				// Skip recently connected agents
				if (currentTime - entry.getValue().getConnectionTimestamp() < IKeepAliveService.KA_TIMEOUT) {
					continue;
				}

				long timeToLastSignal = currentTime - entry.getValue().getLastKeepAliveTimestamp();

				if (timeToLastSignal > IKeepAliveService.KA_TIMEOUT) {
					registerKeepAliveTimeout(entry.getKey());

					if (log.isInfoEnabled()) {
						log.info("Platform " + entry.getKey() + " timed out.");
					}
				}
			}
		}
	};

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ExecutorService} for sending keep alive messages.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService executorService;

	/**
	 * Map that holds IDs of the platform idents and {@link AgentStatusData} objects.
	 */
	private final ConcurrentHashMap<Long, AgentStatusData> agentStatusDataMap = new ConcurrentHashMap<Long, AgentStatusData>(8, 0.75f, 1);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(AgentDeletedEvent event) {
		agentStatusDataMap.remove(event.getPlatformIdent().getId());
	}

	/**
	 * Registers that the agent was connected.
	 *
	 * @param platformIdent
	 *            ID of the platform ident.
	 */
	public void registerConnected(long platformIdent) {
		AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent);
		if (null == agentStatusData) {
			agentStatusData = new AgentStatusData(AgentConnection.CONNECTED);
			AgentStatusData existing = agentStatusDataMap.putIfAbsent(platformIdent, agentStatusData);
			if (null != existing) {
				agentStatusData = existing;
			}
		}
		agentStatusData.setConnectionTimestamp(System.currentTimeMillis());
		agentStatusData.setAgentConnection(AgentConnection.CONNECTED);
	}

	/**
	 * Registers that the agent has been disconnected.
	 *
	 * @param platformIdent
	 *            ID of the platform ident.
	 */
	public void registerDisconnected(long platformIdent) {
		AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent);
		if (null != agentStatusData) {
			agentStatusData.setAgentConnection(AgentConnection.DISCONNECTED);
		}
	}

	/**
	 * Registers the time when last data was received for a given platform ident.
	 *
	 * @param platformIdent
	 *            ID of the platform ident.
	 */
	public void registerDataSent(long platformIdent) {
		AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent);
		if (null != agentStatusData) {
			agentStatusData.setLastDataSendTimestamp(System.currentTimeMillis());
		}
	}

	/**
	 * Registers the time when the last keep-alive was received for a given platform ident.
	 *
	 * @param platformIdent
	 *            ID of the platform ident.
	 */
	public void handleKeepAliveSignal(long platformIdent) {
		AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent);
		if (null != agentStatusData) {
			agentStatusData.setLastKeepAliveTimestamp(System.currentTimeMillis());

			// Updates the agent status if no keep-alive messages were received before
			if (agentStatusData.getAgentConnection() == AgentConnection.NO_KEEP_ALIVE) {
				agentStatusData.setAgentConnection(AgentConnection.CONNECTED);

				if (log.isInfoEnabled()) {
					log.info("Platform " + platformIdent + " sending keep-alive signals again.");
				}
			}
		}
	}

	/**
	 * Registers that the agent is not sending keep-alive messages anymore.
	 *
	 * @param platformIdent
	 *            ID of the platform ident.
	 */
	public void registerKeepAliveTimeout(long platformIdent) {
		AgentStatusData agentStatusData = agentStatusDataMap.get(platformIdent);
		if (null != agentStatusData) {
			agentStatusData.setAgentConnection(AgentConnection.NO_KEEP_ALIVE);
		}
	}

	/**
	 * @return Returns the map of platform ident IDs and dates when the last data was received.
	 */
	public Map<Long, AgentStatusData> getAgentStatusDataMap() {
		long currentTime = System.currentTimeMillis();
		Map<Long, AgentStatusData> map = new HashMap<Long, AgentStatusData>();
		for (Entry<Long, AgentStatusData> entry : agentStatusDataMap.entrySet()) {
			entry.getValue().setServerTimestamp(currentTime);
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Starts the continuous check of the keep-alive signals.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		executorService.scheduleAtFixedRate(keepAliveCheckRunner, IKeepAliveService.KA_INITIAL_DELAY, IKeepAliveService.KA_TIMEOUT, TimeUnit.MILLISECONDS);
	}
}
