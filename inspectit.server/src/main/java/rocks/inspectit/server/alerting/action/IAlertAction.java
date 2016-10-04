package rocks.inspectit.server.alerting.action;

import rocks.inspectit.server.alerting.state.AlertingState;

/**
 * Interface for alerting actions.
 *
 * @author Marius Oehler
 *
 */
public interface IAlertAction {
	/**
	 * Called when an alert has been opened.
	 *
	 * @param alertingState
	 *            The alert state containing all the information about the alert.
	 * @param violationValue
	 *            the value by which the threshold has been violated.
	 */
	void onStarting(AlertingState alertingState);

	/**
	 * Called when an alert remains open.
	 *
	 * @param alertingState
	 *            The alert state containing all the information about the alert.
	 */
	void onOngoing(AlertingState alertingState);

	/**
	 * Called when an alert has been closed.
	 *
	 * @param alertingState
	 *            The alert state containing all the information about the alert.
	 */
	void onEnding(AlertingState alertingState);
}
