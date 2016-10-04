package rocks.inspectit.shared.cs.communication.data.cmr;

import java.util.UUID;

import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Represents a detected alert.
 *
 * @author Alexander Wert
 *
 */
public class Alert {
	/**
	 * Unique id of the alert.
	 */
	private final String id = UUID.randomUUID().toString();

	/**
	 * {@link AlertingDefinition} this alert belongs to.
	 */
	private AlertingDefinition alertingDefinition;

	/**
	 * The timestamp when the alert began.
	 */
	private long startTimestamp = -1L;

	/**
	 * The timestamp when the alert was gone.
	 */
	private long stopTimestamp = -1L;

	/**
	 * Constructor.
	 *
	 * @param alertingDefinition
	 *            {@link AlertingDefinition} this alert belongs to.
	 * @param startTimestamp
	 *            The timestamp when the alert began.
	 * @param stopTimestamp
	 *            The timestamp when the alert was gone.
	 */
	public Alert(AlertingDefinition alertingDefinition, long startTimestamp, long stopTimestamp) {
		this.alertingDefinition = alertingDefinition;
		this.startTimestamp = startTimestamp;
		this.stopTimestamp = stopTimestamp;
	}

	/**
	 * Constructor.
	 *
	 * @param alertingDefinition
	 *            {@link AlertingDefinition} this alert belongs to.
	 * @param startTimestamp
	 *            The timestamp when the alert began.
	 */
	public Alert(AlertingDefinition alertingDefinition, long startTimestamp) {
		this.alertingDefinition = alertingDefinition;
		this.startTimestamp = startTimestamp;
	}

	/**
	 * Gets {@link #alertingDefinition}.
	 *
	 * @return {@link #alertingDefinition}
	 */
	public AlertingDefinition getAlertingDefinition() {
		return this.alertingDefinition;
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
	 * Gets {@link #startTimestamp}.
	 *
	 * @return {@link #startTimestamp}
	 */
	public long getStartTimestamp() {
		return this.startTimestamp;
	}

	/**
	 * Sets {@link #startTimestamp}.
	 *
	 * @param startTimestamp
	 *            New value for {@link #startTimestamp}
	 */
	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/**
	 * Gets {@link #stopTimestamp}.
	 *
	 * @return {@link #stopTimestamp}
	 */
	public long getStopTimestamp() {
		return this.stopTimestamp;
	}

	/**
	 * Sets {@link #stopTimestamp}.
	 *
	 * @param stopTimestamp
	 *            New value for {@link #stopTimestamp}
	 */
	public void setStopTimestamp(long stopTimestamp) {
		this.stopTimestamp = stopTimestamp;
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alertingDefinition == null) ? 0 : alertingDefinition.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
		result = prime * result + (int) (stopTimestamp ^ (stopTimestamp >>> 32));
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
		Alert other = (Alert) obj;
		if (alertingDefinition == null) {
			if (other.alertingDefinition != null) {
				return false;
			}
		} else if (!alertingDefinition.equals(other.alertingDefinition)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (startTimestamp != other.startTimestamp) {
			return false;
		}

		return stopTimestamp == other.stopTimestamp;
	}

}
