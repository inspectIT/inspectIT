package rocks.inspectit.server.alerting.state;

import rocks.inspectit.server.alerting.Alert;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Represents the state of an alert.
 *
 * @author Marius Oehler
 *
 */
public class AlertingState {

	/**
	 * The {@link AlertingDefinition} specifying the related threshold.
	 */
	private final AlertingDefinition alertingDefinition;

	/**
	 * Amount of consecutive intervals without a threshold violation. It is reseted at threshold
	 * violation and only incremented when the alert is active.
	 */
	private int validCount = 0;

	/**
	 * The time of the last check.
	 */
	private long lastCheckTime = -1L;

	/**
	 * The currently active alert.
	 */
	private Alert alert;

	/**
	 * The value which showed the largest deviation to the threshold.
	 */
	private double totalExtremeValue = Double.NaN;

	/**
	 * @param alertingDefinition
	 *            the {@link AlertingDefinition} which state is represented by this instance
	 */
	public AlertingState(AlertingDefinition alertingDefinition) {
		this.alertingDefinition = alertingDefinition;
	}

	/**
	 * Gets {@link #alert}.
	 *
	 * @return {@link #alert}
	 */
	public Alert getAlert() {
		return alert;
	}

	/**
	 * Gets {@link #alertingDefinition}.
	 *
	 * @return {@link #alertingDefinition}
	 */
	public AlertingDefinition getAlertingDefinition() {
		return alertingDefinition;
	}

	/**
	 * Gets {@link #lastCheckTime}.
	 *
	 * @return {@link #lastCheckTime}
	 */
	public long getLastCheckTime() {
		return lastCheckTime;
	}

	/**
	 * Gets {@link #totalExtremeValue}.
	 *
	 * @return {@link #totalExtremeValue}
	 */
	public double getTotalExtremeValue() {
		return totalExtremeValue;
	}

	/**
	 * Gets {@link #validCount}.
	 *
	 * @return {@link #validCount}
	 */
	public int getValidCount() {
		return validCount;
	}

	/**
	 * Gets {@link #alertActive}.
	 *
	 * @return {@link #alertActive}
	 */
	public boolean isAlertActive() {
		return alert != null;
	}

	/**
	 * Sets {@link #alert}.
	 *
	 * @param alert
	 *            New value for {@link #alert}
	 */
	public void setAlert(Alert alert) {
		this.alert = alert;
	}

	/**
	 * Sets {@link #lastCheckTime}.
	 *
	 * @param lastCheckTime
	 *            New value for {@link #lastCheckTime}
	 */
	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	/**
	 * Sets {@link #totalExtremeValue}.
	 *
	 * @param totalExtremeValue
	 *            New value for {@link #totalExtremeValue}
	 */
	public void setTotalExtremeValue(double totalExtremeValue) {
		this.totalExtremeValue = totalExtremeValue;
	}

	/**
	 * Sets {@link #validCount}.
	 *
	 * @param validCounter
	 *            New value for {@link #validCount}
	 */
	public void setValidCounter(int validCounter) {
		this.validCount = validCounter;
	}

}
