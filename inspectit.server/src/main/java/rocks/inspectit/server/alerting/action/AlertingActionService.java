package rocks.inspectit.server.alerting.action;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.AlertRegistry;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

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
	 * {@link AlertRegistry} instance.
	 */
	@Autowired
	private AlertRegistry alertRegistry;

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

		alertRegistry.registerAlert(alert);

		alertingState.setAlert(alert);
		alertingState.setExtremeValue(violationValue);

		for (IAlertAction alertAction : alertActions) {
			alertAction.onStarting(alertingState);
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
			alertingState.setExtremeValue(Math.max(violationValue, alertingState.getExtremeValue()));
		} else {
			alertingState.setExtremeValue(Math.min(violationValue, alertingState.getExtremeValue()));
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
