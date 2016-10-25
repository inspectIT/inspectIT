package rocks.inspectit.shared.cs.communication.data.cmr;

/**
 * Enumeration for the closing reason of an alert.
 *
 * @author Alexander Wert
 *
 */
public enum AlertClosingReason {
	/**
	 * Closing reasons.
	 */
	ALERT_RESOLVED("System recovered from the laert state."), ALERTING_DEFINITION_DELETED("Corresponding Alerting Definition has been deleted.");

	/**
	 * Description text.
	 */
	private String description;

	/**
	 * Default constructor.
	 */
	AlertClosingReason() {
	}

	/**
	 * Constructor.
	 *
	 * @param description
	 *            Description text.
	 */
	AlertClosingReason(String description) {
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return description;
	}
}
