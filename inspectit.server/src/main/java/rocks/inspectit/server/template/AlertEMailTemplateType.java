package rocks.inspectit.server.template;

/**
 * Template type for alert e-mails.
 *
 * @author Alexander Wert
 *
 */
public enum AlertEMailTemplateType implements ITemplateType {
	/**
	 * HTML email template for an open alert.
	 */
	HTML_ALERT_OPEN("alert-open.html", false),

	/**
	 * HTML email template for an open alert on a business transaction.
	 */
	HTML_BUSINESS_TX_ALERT_OPEN("alert-bt-open.html", false),

	/**
	 * HTML email template for a closed alert.
	 */
	HTML_ALERT_CLOSED("alert-closed.html", false),

	/**
	 * HTML email template for a closed alert on a business transaction.
	 */
	HTML_BUSINESS_TX_ALERT_CLOSED("alert-bt-closed.html", false),

	/**
	 * Text email template for an open alert.
	 */
	TXT_ALERT_OPEN("alert-open.txt", true),

	/**
	 * Text email template for an open alert on a business transaction.
	 */
	TXT_BUSINESS_TX_ALERT_OPEN("alert-bt-open.txt", true),

	/**
	 * Text email template for a closed alert.
	 */
	TXT_ALERT_CLOSED("alert-closed.txt", true),

	/**
	 * Text email template for a closed alert on a business transaction.
	 */
	TXT_BUSINESS_TX_ALERT_CLOSED("alert-bt-closed.txt", true);

	/**
	 * File name.
	 */
	private String fileName;

	/**
	 * Indicates whether the email is in pure text or HTML format.
	 */
	private boolean isText;

	/**
	 * Constructor.
	 *
	 * @param fileName
	 *            File name of the template.
	 * @param isText
	 *            Indicates whether the email is in pure text or HTML format.
	 */
	AlertEMailTemplateType(String fileName, boolean isText) {
		this.fileName = fileName;
		this.isText = isText;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	/**
	 * Gets {@link #isText}.
	 *
	 * @return {@link #isText}
	 */
	public boolean isText() {
		return isText;
	}

	/**
	 * Constants interface for template placeholders.
	 *
	 * @author Alexander Wert
	 *
	 */
	public interface Placeholders {
		/**
		 * Placeholder for alert definition name.
		 */
		String ALERT_DEFINITION_NAME = "{alertDefinitionName}";

		/**
		 * Placeholder for the measurement name.
		 */
		String MEASUREMENT = "{measurement}";

		/**
		 * Placeholder for field name.
		 */
		String FIELD = "{field}";

		/**
		 * Application name.
		 */
		String APPLICATION_NAME = "{applicationName}";

		/**
		 * Application name.
		 */
		String BUSINESS_TX_NAME = "{businessTxName}";

		/**
		 * Placeholder for the tags table/list.
		 */
		String TAGS = "{tags}";

		/**
		 * Placeholder for the threshold.
		 */
		String THRESHOLD = "{threshold}";

		/**
		 * Placeholder for the begin date of the alert.
		 */
		String START_TIME = "{startTime}";

		/**
		 * Placeholder for the end date of the alert.
		 */
		String END_TIME = "{endTime}";

		/**
		 * Placeholder for the alert id in case of a business transaction alert.
		 */
		String ALERT_ID = "{alertId}";

		/**
		 * Placeholder for the violation value.
		 */
		String VIOLATION_VALUE = "{violationValue}";

		/**
		 * Placeholder for the violation value.
		 */
		String CURRENT_TIME = "{currentTime}";

		/**
		 * Placeholder for the extreme value.
		 */
		String EXTREME_VALUE = "{extremeValue}";
	}

}
