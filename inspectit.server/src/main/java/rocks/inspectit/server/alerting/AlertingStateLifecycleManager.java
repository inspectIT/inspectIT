package rocks.inspectit.server.alerting;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.action.AlertingActionService;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * This component handles the management of the lifecycle of the alerts. It is responsible for
 * transition from the individual alert states (starting, ongoing, ending).
 *
 * @author Marius Oehler
 *
 */
@Component
public class AlertingStateLifecycleManager {

	/**
	 * The logger for this class.
	 */
	@Log
	private Logger log;

	/**
	 * The amount of intervals without a threshold violation before an alert is considered as valid.
	 */
	@Value("${alerting.resolutionDelay}")
	int thresholdResetCount;

	/**
	 * {@link AlertingActionService} instance.
	 */
	@Autowired
	AlertingActionService alertingActionService;

	/**
	 * This method is called if the given threshold (specified by the {@link AlertingDefinition}
	 * contained in the {@link AlertingState}) has been violated in the latest interval. The given
	 * value showed the largest deviation to the threshold.
	 *
	 * @param alertingState
	 *            the threshold which has been violated
	 * @param violationValue
	 *            the value which violated the threshold
	 */
	public void violation(AlertingState alertingState, double violationValue) {
		if (alertingState.isAlertActive()) {
			// alert is ongoing
			if (log.isDebugEnabled()) {
				log.debug("||-Violation of threshold '{}' is ongoing.", alertingState.getAlertingDefinition().getName());
			}

			alertingState.setValidCounter(0);

			alertingActionService.alertOngoing(alertingState, violationValue);
		} else {
			// alert is new
			if (log.isDebugEnabled()) {
				log.debug("||-Threshold violation. Value '{}' violated threshold '{}' and started a new alert.", violationValue, alertingState.getAlertingDefinition().getDescriptiveName());
			}

			alertingActionService.alertStarting(alertingState, violationValue);
		}
	}

	/**
	 * This method is called when the given threshold has not been violated in the latest period.
	 *
	 * @param alertingState
	 *            the threshold which has not been violated
	 */
	public void valid(AlertingState alertingState) {
		if (log.isDebugEnabled()) {
			log.debug("||-Threshold '{}' has not been violated in the last interval.", alertingState.getAlertingDefinition().getName());
		}

		if (!alertingState.isAlertActive()) {
			// threshold is not in a violation series
			return;
		}

		int validCount = alertingState.getValidCount();

		if (validCount >= thresholdResetCount) {
			// alert ended
			if (log.isDebugEnabled()) {
				log.debug("||-Ended threshold violation series of '{}'.", alertingState.getAlertingDefinition().getName());
			}

			alertingActionService.alertEnding(alertingState);
		} else {
			// alert is waiting for reset
			alertingState.setValidCounter(validCount + 1);
		}
	}

	/**
	 * This method is called when no data is existing for the given threshold in the latest period.
	 *
	 * @param alertingState
	 *            the threshold which has been checked
	 */
	public void noData(AlertingState alertingState) {
		if (log.isDebugEnabled()) {
			log.debug("||-No data available for alerting definition '{}'. Expecting the same behavior as before.", alertingState.getAlertingDefinition().getName());
		}

		if (!alertingState.isAlertActive() || alertingState.getValidCount() > 0) {
			valid(alertingState);
		} else {
			violation(alertingState, Double.NaN);
		}
	}
}
