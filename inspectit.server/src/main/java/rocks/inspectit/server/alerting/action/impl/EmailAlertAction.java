package rocks.inspectit.server.alerting.action.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.action.IAlertAction;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.alerting.util.AlertingUtils;
import rocks.inspectit.server.mail.IEMailSender;
import rocks.inspectit.server.mail.impl.EMailSender;
import rocks.inspectit.server.template.AlertEMailTemplateType;
import rocks.inspectit.server.template.TemplateManager;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This alert action uses the {@link EMailSender} to send notification e-mails.
 *
 * @author Marius Oehler, Alexander Wert
 *
 */
@Component
public class EmailAlertAction implements IAlertAction {

	/**
	 * Date format for pretty printing in email.
	 */
	private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

	/**
	 * Number format for pretty printing in email.
	 */
	private static final String NUMBER_FORMAT = "0.0#";

	/**
	 * Logger for this class.
	 */
	@Log
	private Logger log;

	/**
	 * {@link EMailSender} service used to send e-mails.
	 */
	@Autowired
	private IEMailSender emailSender;

	/**
	 * {@link TemplateManager} used to load e-mail templates.
	 */
	@Autowired
	private TemplateManager templateManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStarting(AlertingState alertingState) {
		if (log.isDebugEnabled()) {
			log.debug("||-Sending e-mail because of starting an alert specified by: {}", alertingState.getAlertingDefinition().toString());
		}

		try {
			String alertingName = getAlertingName(alertingState);
			String subject = "Alert - Threshold '" + alertingName + "' violated";
			String htmlBody;
			String textBody;

			if (AlertingUtils.isBusinessTransactionAlert(alertingState.getAlertingDefinition())) {
				htmlBody = getEmailBodyForAlert(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN, alertingState, alertingState.getExtremeValue());
				textBody = getEmailBodyForAlert(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN, alertingState, alertingState.getExtremeValue());
			} else {
				htmlBody = getEmailBodyForAlert(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState, alertingState.getExtremeValue());
				textBody = getEmailBodyForAlert(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState, alertingState.getExtremeValue());
			}

			emailSender.sendEMail(subject, htmlBody, textBody, alertingState.getAlertingDefinition().getNotificationEmailAddresses());
		} catch (IOException e) {
			log.warn("Could not send open alert e-mail!", e);
			return;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onOngoing(AlertingState alertingState) {
		// not needed
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnding(AlertingState alertingState) {
		if (log.isDebugEnabled()) {
			log.debug("||-Sending e-mail because of alert '{}' has ended.", alertingState.getAlertingDefinition().getName());
		}

		try {
			String alertingName = getAlertingName(alertingState);
			String subject = "Alert - Threshold '" + alertingName + "' has been closed";
			String htmlBody;
			String textBody;

			if (AlertingUtils.isBusinessTransactionAlert(alertingState.getAlertingDefinition())) {
				htmlBody = getEmailBodyForAlert(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED, alertingState, -1);
				textBody = getEmailBodyForAlert(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED, alertingState, -1);
			} else {
				htmlBody = getEmailBodyForAlert(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState, -1);
				textBody = getEmailBodyForAlert(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState, -1);
			}

			emailSender.sendEMail(subject, htmlBody, textBody, alertingState.getAlertingDefinition().getNotificationEmailAddresses());
		} catch (IOException e) {
			log.warn("Could not send close alert e-mail!", e);
			return;
		}
	}

	/**
	 * retrieves name for alert.
	 *
	 * @param alertingState
	 *            the alerting state.
	 * @return name for the alert.
	 */
	private String getAlertingName(AlertingState alertingState) {
		String alertingName;
		if (alertingState.getAlertingDefinition().getName() == null) {
			alertingName = "unnamed";
		} else {
			alertingName = alertingState.getAlertingDefinition().getName();
		}
		return alertingName;
	}

	/**
	 * Creates the e-mail body for the given type of alert e-mail.
	 *
	 * @param templateType
	 *            the alert email template type.
	 * @param alertingState
	 *            the state of the alert.
	 * @param violationValue
	 *            the value by which the threshold has been violated.
	 * @return Returns the e-amil body as string.
	 * @throws IOException
	 *             Throws this exception if the e-mail template could not be loaded.
	 */
	private String getEmailBodyForAlert(AlertEMailTemplateType templateType, AlertingState alertingState, double violationValue) throws IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		NumberFormat numberFormat = new DecimalFormat(NUMBER_FORMAT);

		Map<String, String> properties = new HashMap<>();
		properties.put(AlertEMailTemplateType.Placeholders.ALERT_DEFINITION_NAME, alertingState.getAlertingDefinition().getName());
		properties.put(AlertEMailTemplateType.Placeholders.MEASUREMENT, alertingState.getAlertingDefinition().getMeasurement());
		properties.put(AlertEMailTemplateType.Placeholders.FIELD, alertingState.getAlertingDefinition().getField());
		properties.put(AlertEMailTemplateType.Placeholders.THRESHOLD, numberFormat.format(alertingState.getAlertingDefinition().getThreshold()));
		properties.put(AlertEMailTemplateType.Placeholders.START_TIME, String.valueOf(dateFormat.format(new Date(alertingState.getAlert().getStartTimestamp()))));
		properties.put(AlertEMailTemplateType.Placeholders.VIOLATION_VALUE, numberFormat.format(violationValue));
		properties.put(AlertEMailTemplateType.Placeholders.CURRENT_TIME, String.valueOf(new Date(System.currentTimeMillis())));
		properties.put(AlertEMailTemplateType.Placeholders.ALERT_ID, alertingState.getAlert().getId());
		properties.put(AlertEMailTemplateType.Placeholders.EXTREME_VALUE, numberFormat.format(alertingState.getExtremeValue()));

		if (AlertingUtils.isBusinessTransactionAlert(alertingState.getAlertingDefinition())) {
			String applicationName = AlertingUtils.retrieveApplicaitonName(alertingState.getAlertingDefinition());
			if (null == applicationName) {
				applicationName = "All";
			}
			properties.put(AlertEMailTemplateType.Placeholders.APPLICATION_NAME, applicationName);

			String businessTxName = AlertingUtils.retrieveBusinessTransactionName(alertingState.getAlertingDefinition());
			if (null == businessTxName) {
				businessTxName = "All";
			}
			properties.put(AlertEMailTemplateType.Placeholders.BUSINESS_TX_NAME, businessTxName);
		}

		if (alertingState.getAlert().getStopTimestamp() > 0) {
			properties.put(AlertEMailTemplateType.Placeholders.END_TIME, String.valueOf(dateFormat.format(new Date(alertingState.getAlert().getStopTimestamp()))));
		}
		if (templateType.isText()) {
			properties.put(AlertEMailTemplateType.Placeholders.TAGS, convertTagsToTextProperty(alertingState.getAlertingDefinition().getTags()));
		} else {
			properties.put(AlertEMailTemplateType.Placeholders.TAGS, convertTagsToHtmlTableRowsProperty(alertingState.getAlertingDefinition().getTags()));
		}
		return templateManager.resolveTemplate(templateType, properties);
	}

	/**
	 * Converts a map of tags to text representation.
	 *
	 * @param tags
	 *            Tag map to convert.
	 * @return A text representation of the tags.
	 */
	private String convertTagsToTextProperty(Map<String, String> tags) {
		StringBuilder stringBuilder = new StringBuilder();
		String lineSep = System.getProperty("line.separator");
		for (Entry<String, String> tagKeyValuePair : tags.entrySet()) {
			stringBuilder.append("- ").append(tagKeyValuePair.getKey()).append(": ").append(tagKeyValuePair.getValue()).append(lineSep);
		}
		return stringBuilder.toString();
	}

	/**
	 * Converts a map of tags to HTML table representation.
	 *
	 * @param tags
	 *            Tag map to convert.
	 * @return A HTML table representation of the tags.
	 */
	private String convertTagsToHtmlTableRowsProperty(Map<String, String> tags) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<String, String> tagKeyValuePair : tags.entrySet()) {
			stringBuilder.append("<tr><td>").append(tagKeyValuePair.getKey()).append("</td><td>").append(tagKeyValuePair.getValue()).append("</td></tr>");
		}
		return stringBuilder.toString();
	}
}
