package rocks.inspectit.server.alerting;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.util.AlertingUtils;
import rocks.inspectit.server.influx.dao.InfluxQueryFactory;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.FifoMap;

/**
 * The registry for business transaction alerts.
 *
 * @author Alexander Wert
 *
 */
@Component
public class BusinessTransactionsAlertRegistry {
	/**
	 * The capacity of the registry buffer.
	 */
	private static final int BUFFER_SIZE = 100000;
	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * The registry buffer implemented as a {@link FifoMap}.
	 */
	private final FifoMap<String, Alert> registry = new FifoMap<>(BUFFER_SIZE);

	/**
	 * Returns the {@link Alert} for the given id.
	 *
	 * @param alertId
	 *            the alert identifier.
	 * @return Returns the {@link Alert} for the given id.
	 */
	public Alert getAlert(String alertId) {
		return registry.get(alertId);
	}

	/**
	 * Registers the given alert. If the passed {@link Alert} does not refer to the business
	 * transaction metric, then the registration fails.
	 *
	 * @param alert
	 *            the {@link Alert} to register
	 */
	public void registerAlert(Alert alert) {
		if (!AlertingUtils.isBusinessTransactionAlert(alert.getAlertingDefinition())) {
			log.warn("Alerts can be only registered for business transaction metrics in this registry!");
			return;
		}

		registry.put(alert.getId(), alert);
	}

	/**
	 * Creates a influxDB query for the given alert id. The query retrieves the ids of the
	 * invocation sequences that constitute the alert.
	 *
	 * @param alertId
	 *            the identifier of the alert
	 * @param agentId
	 *            the identifier of the agent for which the invocation sequences shall be retrieved.
	 * @return Returns the query string.
	 */
	public String createInfluxDBQueryForAlert(String alertId, long agentId) {
		Alert alert = registry.get(alertId);
		if (null != alert) {
			return InfluxQueryFactory.buildTraceIdForAlertQuery(alert, agentId);
		} else {
			log.warn("Could not retrieve influxDB query for alert with id " + alertId + "! Alert with that id does not exist (anymore).");
			return null;
		}
	}

}
