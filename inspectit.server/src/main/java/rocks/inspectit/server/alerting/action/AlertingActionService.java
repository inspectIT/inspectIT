package rocks.inspectit.server.alerting.action;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.Alert;
import rocks.inspectit.server.alerting.BusinessTransactionsAlertRegistry;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.alerting.util.AlertingUtils;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;

/**
 * This component manages the alerting actions like initiating sending e-mails, etc.
 *
 * @author Marius Oehler
 *
 */
@Component
public class AlertingActionService {

	/**
	 * Logger of this class.
	 */
	@Log
	private Logger log;

	/**
	 * List of {@link IAlertAction} to execute.
	 */
	@Autowired
	private List<IAlertAction> alertActions;

	/**
	 * {@link BusinessTransactionsAlertRegistry} instance.
	 */
	@Autowired
	private BusinessTransactionsAlertRegistry businessTransactionsAlertRegistry;

	/**
	 * This method is called when a new alert is started.
	 *
	 * @param alertingState
	 *            the started alert
	 * @param violationValue
	 *            the value has violated the threshold
	 */
	public void alertStarting(AlertingState alertingState, double violationValue) {
		Alert alert = new Alert(alertingState.getAlertingDefinition(), alertingState.getLastCheckTime());

		if (AlertingUtils.isBusinessTransactionAlert(alertingState.getAlertingDefinition())) {
			businessTransactionsAlertRegistry.registerAlert(alert);
		}

		alertingState.setAlert(alert);
		alertingState.setTotalExtremeValue(violationValue);

		for (IAlertAction alertAction : alertActions) {
			alertAction.onStarting(alertingState, violationValue);
		}
	}

	/**
	 * This method is called when an active alert is still violating its threshold.
	 *
	 * @param alertingState
	 *            the ongoing alert
	 * @param violationValue
	 *            the value has violated the threshold
	 */
	public void alertOngoing(AlertingState alertingState, double violationValue) {
		if (log.isDebugEnabled()) {
			log.debug("Alert definition '{}' is ongoing.", alertingState.getAlertingDefinition().getName());
		}

		if (alertingState.getAlertingDefinition().getThresholdType() == ThresholdType.UPPER_THRESHOLD) {
			alertingState.setTotalExtremeValue(Math.max(violationValue, alertingState.getTotalExtremeValue()));
		} else {
			alertingState.setTotalExtremeValue(Math.min(violationValue, alertingState.getTotalExtremeValue()));
		}

		for (IAlertAction alertAction : alertActions) {
			alertAction.onOngoing(alertingState);
		}
	}

	/**
	 * This method is called when an active alert is ending.
	 *
	 * @param alertingState
	 *            the ending alert
	 */
	public void alertEnding(AlertingState alertingState) {
		if (log.isDebugEnabled()) {
			log.debug("Alert definition '{}' is ending.", alertingState.getAlertingDefinition().getName());
		}

		alertingState.getAlert().setStopTimestamp(System.currentTimeMillis());

		for (IAlertAction alertAction : alertActions) {
			alertAction.onEnding(alertingState);
		}

		alertingState.setAlert(null);
	}
}
