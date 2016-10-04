package rocks.inspectit.server.alerting;

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
		super();
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
		super();
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
}
