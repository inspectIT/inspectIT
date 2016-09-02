package rocks.inspectit.agent.java.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lmax.disruptor.EventHandler;

import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * {@link EventHandler} that sends the data wrapped in the {@link DefaultDataWrapper} to the CMR.
 *
 * @author Matthias Huber
 * @author Ivan Senic
 *
 */
@Component
public class DefaultDataHandler implements EventHandler<DefaultDataWrapper> {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * The connection to the Central Measurement Repository.
	 */
	@Autowired
	private IConnection connection;

	/**
	 * List where data is collected and then passed to the connection.
	 */
	private List<DefaultData> defaultDatas = new ArrayList<DefaultData>(128);

	/**
	 * Defines if there was an exception before while trying to send the data. Used to throttle the
	 * printing of log statements.
	 */
	private boolean sendingExceptionNotice = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEvent(DefaultDataWrapper defaultDataWrapper, long sequence, boolean endOfBatch) {
		defaultDatas.add(defaultDataWrapper.getDefaultData());

		if (endOfBatch) {
			try {
				if (connection.isConnected()) {
					connection.sendDataObjects(defaultDatas);
				}
			} catch (ServerUnavailableException serverUnavailableException) {
				if (serverUnavailableException.isServerTimeout()) {
					log.warn("Timeout on server when sending actual data. Data might be lost!", serverUnavailableException);
				} else {
					if (!sendingExceptionNotice) {
						sendingExceptionNotice = true;
						log.error("Connection problem appeared, stopping sending actual data!", serverUnavailableException);
					}
				}
			} finally {
				defaultDatas.clear();
			}
		}
	}

}