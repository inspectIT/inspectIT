package rocks.inspectit.server.alerting;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.dao.IInfluxDBDao;
import rocks.inspectit.server.influx.dao.InfluxQueryFactory;
import rocks.inspectit.server.influx.util.QueryResultWrapper;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * The threshold checker component. It is used to check the status of a given threshold defined by
 * an {@link AlertingDefinition}.
 *
 * @author Marius Oehler
 *
 */
@Component
public class ThresholdChecker {

	/**
	 * Logger of this class.
	 */
	@Log
	private Logger log;

	/**
	 * {@link IInfluxDBDao} instance.
	 */
	@Autowired
	private IInfluxDBDao influxDao;

	/**
	 * {@link AlertingStateLifecycleManager} instance.
	 */
	@Autowired
	private AlertingStateLifecycleManager stateManager;

	/**
	 * Checks whether the threshold defined by the {@link AlertingDefinition} contained in the given
	 * {@link AlertingState} has been violated. The result is given to the
	 * {@link AlertingStateLifecycleManager}.
	 *
	 * @param alertingState
	 *            the {@link AlertingState} containing the threshold to check
	 */
	public void checkThreshold(AlertingState alertingState) {
		if (log.isDebugEnabled()) {
			log.debug("||-Check threshold defined by alerting definition '{}'.", alertingState.getAlertingDefinition().toString());
		}

		if (!influxDao.isOnline()) {
			if (log.isDebugEnabled()) {
				log.debug("||-Cannot check threshold without connected influxDB.");
			}
			return;
		}

		long currentTime = System.currentTimeMillis();

		long lastCheckTime = alertingState.getLastCheckTime();
		if (lastCheckTime < 0) {
			lastCheckTime = currentTime - alertingState.getAlertingDefinition().getTimeRange(TimeUnit.MILLISECONDS);
			alertingState.setLastCheckTime(lastCheckTime);
		}

		String queryString = InfluxQueryFactory.buildThresholdCheckForAlertingStateQuery(alertingState, currentTime);
		QueryResult queryResult = influxDao.query(queryString);

		QueryResultWrapper resultWrapper = new QueryResultWrapper(queryResult);

		if (resultWrapper.isEmpty()) {
			stateManager.noData(alertingState);
		} else {
			double extremeValue = resultWrapper.getDouble(0, 1);

			if (isViolating(alertingState.getAlertingDefinition(), extremeValue)) {
				stateManager.violation(alertingState, extremeValue);
			} else {
				stateManager.valid(alertingState);
			}
		}

		alertingState.setLastCheckTime(currentTime);
	}

	/**
	 * Checks whether the given double value violates the threshold of the given
	 * {@link AlertingDefinition}.
	 *
	 * @param definition
	 *            the {@link AlertingDefinition} defining the threshold
	 * @param testValue
	 *            the value to test against the threshold
	 * @return Returns <code>true</code> if the value violates the threshold.
	 */
	private boolean isViolating(AlertingDefinition definition, double testValue) {
		switch (definition.getThresholdType()) {
		case LOWER_THRESHOLD:
			return testValue < definition.getThreshold();

		case UPPER_THRESHOLD:
		default:
			return testValue > definition.getThreshold();
		}
	}
}
