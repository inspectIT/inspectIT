package rocks.inspectit.server.alerting.state;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

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
	private AlertingDefinition alertingDefinition;

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
	private double extremeValue = Double.NaN;

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
	 * Gets {@link #extremeValue}.
	 *
	 * @return {@link #extremeValue}
	 */
	public double getExtremeValue() {
		return extremeValue;
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
	 * Sets {@link #extremeValue}.
	 *
	 * @param extremeValue
	 *            New value for {@link #extremeValue}
	 */
	public void setExtremeValue(double extremeValue) {
		this.extremeValue = extremeValue;
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

	/**
	 * Sets {@link #alertingDefinition}.
	 *
	 * @param alertingDefinition
	 *            New value for {@link #alertingDefinition}
	 */
	public void setAlertingDefinition(AlertingDefinition alertingDefinition) {
		this.alertingDefinition = alertingDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alert == null) ? 0 : alert.hashCode());
		result = prime * result + ((alertingDefinition == null) ? 0 : alertingDefinition.hashCode());
		long temp;
		temp = Double.doubleToLongBits(extremeValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (lastCheckTime ^ (lastCheckTime >>> 32));
		result = prime * result + validCount;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AlertingState other = (AlertingState) obj;
		if (alert == null) {
			if (other.alert != null) {
				return false;
			}
		} else if (!alert.equals(other.alert)) {
			return false;
		}
		if (alertingDefinition == null) {
			if (other.alertingDefinition != null) {
				return false;
			}
		} else if (!alertingDefinition.equals(other.alertingDefinition)) {
			return false;
		}
		if (Double.doubleToLongBits(extremeValue) != Double.doubleToLongBits(other.extremeValue)) {
			return false;
		}
		if (lastCheckTime != other.lastCheckTime) {
			return false;
		}
		return validCount == other.validCount;
	}

}
