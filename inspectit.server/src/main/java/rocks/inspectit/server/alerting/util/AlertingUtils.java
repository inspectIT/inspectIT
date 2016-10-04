package rocks.inspectit.server.alerting.util;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * This class provides utility functions for alerting purposes.
 *
 * @author Alexander Wert
 *
 */
public final class AlertingUtils {

	/**
	 * Private constructor due to utility class.
	 */
	private AlertingUtils() {
	}

	/**
	 * Checks whether the given {@link AlertingDefinition} belongs to a business transaction metric.
	 *
	 * @param alertingDefinition
	 *            {@link AlertingDefinition} instance to check.
	 * @return <code>true</code> if the given {@link AlertingDefinition} instance refers to a
	 *         business transaction metric, otherwise <code>false</code>
	 */
	public static boolean isBusinessTransactionAlert(AlertingDefinition alertingDefinition) {
		return Series.BusinessTransaction.NAME.equals(alertingDefinition.getMeasurement()) && Series.BusinessTransaction.FIELD_DURATION.equals(alertingDefinition.getField());
	}

	/**
	 * Retrieves the business transaction name from a business transaction related
	 * {@link AlertingDefinition}.
	 *
	 * @param alertingDefinition
	 *            The {@link AlertingDefinition} instance to retrieve the name from.
	 * @return The business transaction name or <code>null</code> if no business transaction is
	 *         specified.
	 */
	public static String retrieveBusinessTransactionName(AlertingDefinition alertingDefinition) {
		if (!isBusinessTransactionAlert(alertingDefinition)) {
			return null;
		}

		if (alertingDefinition.getTags().containsKey(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME)) {
			return alertingDefinition.getTags().get(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME);
		}

		return null;
	}

	/**
	 * Retrieves the application name from a business transaction related
	 * {@link AlertingDefinition}.
	 *
	 * @param alertingDefinition
	 *            The {@link AlertingDefinition} instance to retrieve the name from.
	 * @return The application name or <code>null</code> if no application is specified.
	 */
	public static String retrieveApplicaitonName(AlertingDefinition alertingDefinition) {
		if (!isBusinessTransactionAlert(alertingDefinition)) {
			return null;
		}

		if (alertingDefinition.getTags().containsKey(Series.BusinessTransaction.TAG_APPLICATION_NAME)) {
			return alertingDefinition.getTags().get(Series.BusinessTransaction.TAG_APPLICATION_NAME);
		}

		return null;
	}
}
