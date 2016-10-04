package rocks.inspectit.server.influx.dao;

import java.util.Map.Entry;

import rocks.inspectit.server.alerting.Alert;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;

/**
 * Utility class for creating influx queries.
 *
 * @author Alexander Wert
 *
 */
public final class InfluxQueryFactory {
	/**
	 * Private constructor for utility class.
	 */
	private InfluxQueryFactory() {
	}

	/**
	 * Creates a query that retrieves the max / min value from a metric to check whether it violates
	 * a threshold.
	 *
	 * @param alertingState
	 *            {@link AlertingState} defining the alerting rule and state to check the threshold
	 *            for.
	 * @param currentTime
	 *            The current system time.
	 * @return The influxDB query as String.
	 */
	public static String buildThresholdCheckForAlertingStateQuery(AlertingState alertingState, long currentTime) {
		AlertingDefinition definition = alertingState.getAlertingDefinition();

		String aggregationFunction;
		if (definition.getThresholdType() == ThresholdType.UPPER_THRESHOLD) {
			aggregationFunction = "MAX";
		} else {
			aggregationFunction = "MIN";
		}

		StringBuilder builder = new StringBuilder();
		builder.append("SELECT " + aggregationFunction + "(\"").append(definition.getField()).append("\") FROM \"").append(definition.getMeasurement()).append("\" WHERE ");

		for (Entry<String, String> entry : definition.getTags().entrySet()) {
			builder.append('"').append(entry.getKey());
			builder.append("\" = '").append(entry.getValue()).append("' AND ");
		}

		builder.append("time <= ").append(currentTime).append("ms AND time > ").append(alertingState.getLastCheckTime()).append("ms");

		return builder.toString();
	}

	/**
	 * Creates a influxDB query for the given alert id. The query retrieves the ids of the
	 * invocation sequences that constitute the alert.
	 *
	 * @param alert
	 *            the alert to retrieve the invocation IDs for.
	 * @param agentId
	 *            the identifier of the agent for which the invocation sequences shall be retrieved.
	 * @return Returns the query string.
	 */
	public static String buildTraceIdForAlertQuery(Alert alert, long agentId) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT \"").append(Series.BusinessTransaction.FIELD_TRACE_ID).append("\" FROM \"").append(Series.BusinessTransaction.NAME);
		builder.append("\" WHERE \"").append(Series.BusinessTransaction.TAG_AGENT_ID).append("\" = '").append(String.valueOf(agentId)).append('\'');
		for (Entry<String, String> entry : alert.getAlertingDefinition().getTags().entrySet()) {
			builder.append(" AND \"").append(entry.getKey()).append("\" = '").append(entry.getValue()).append('\'');
		}

		builder.append(" AND time >= ").append(alert.getStartTimestamp()).append("ms");
		if (alert.getStopTimestamp() >= 0) {
			builder.append(" AND time < ").append(alert.getStopTimestamp()).append("ms");
		}
		builder.append(" AND \"").append(Series.BusinessTransaction.FIELD_DURATION).append("\" >= ").append(alert.getAlertingDefinition().getThreshold());

		return builder.toString();
	}
}
