package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.cmr.service.IKeepAliveService;
import info.novatec.inspectit.spring.logger.Log;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class handles the sending of keep-alive signals to the CMR.
 * 
 * @author Marius Oehler
 *
 */
@Component
public class KeepAliveManager implements InitializingBean, DisposableBean {

	/**
	 * Runnable which sends the keep-alive signal.
	 */
	private final Runnable keepAliveRunner = new Runnable() {
		public void run() {
			try {
				connection.sendKeepAlive(idManager.getPlatformId());
			} catch (IdNotAvailableException e) {
				if (log.isDebugEnabled()) {
					log.debug("keep-alive signal could not be sent. No platform id available.", e);
				}
			}
		}
	};

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * Connection to send the keep-alive signals.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * Id manager.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * ScheduledFuture representing pending keep-alive sending task.
	 */
	private ScheduledFuture<?> scheduledTask;

	/**
	 * The default core service.
	 */
	@Autowired
	private ICoreService coreService;

	/**
	 * {@inheritDoc}
	 * 
	 * Starts the sending of the keep-alive signals.
	 */
	public void afterPropertiesSet() throws Exception {
		if (scheduledTask == null) {
			scheduledTask = coreService.getScheduledExecutorService().scheduleAtFixedRate(keepAliveRunner, IKeepAliveService.KA_INITIAL_DELAY, IKeepAliveService.KA_PERIOD, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Stops the further sending of keep-alive messages.
	 */
	public void destroy() throws Exception {
		scheduledTask.cancel(false);
	}

}
