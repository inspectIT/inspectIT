package rocks.inspectit.server.template;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.alerting.util.AlertingUtils;
import rocks.inspectit.server.template.AlertEMailTemplateType.Placeholders;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Component to resolve templates for alerting e-mails.
 *
 * @author Marius Oehler
 *
 */
@Component
public class EMailTemplateResolver {

	/**
	 * Date format for pretty printing in email.
	 */
	private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

	/**
	 * Number format for pretty printing in email.
	 */
	private static final String NUMBER_FORMAT = "0.0#";

	/**
	 * {@link TemplateManager} used to load e-mail templates.
	 */
	@Autowired
	private TemplateManager templateManager;

	/**
	 * Resolves an e-mail template for the given {@link AlertEMailTemplateType} and
	 * {@link AlertingState}.
	 *
	 * @param templateType
	 *            the type of the template
	 * @param alertingState
	 *            the state of the alert
	 * @return a template represented as a {@link String}
	 * @throws IOException
	 *             Thrown if the template cannot be resolved.
	 */
	public String resolveTemplate(AlertEMailTemplateType templateType, AlertingState alertingState) throws IOException {
		Map<String, String> propertiesMap = createPropertiesMap(alertingState, templateType.isText());

		return templateManager.resolveTemplate(templateType, propertiesMap);
	}

	/**
	 * Creates a map containing available values which are used to replace corresponding
	 * placeholder.
	 *
	 * @param alertingState
	 *            the state of the alert
	 * @param isText
	 *            whether the target template is textual
	 * @return {@link Map} containing placeholder and corresponding values
	 */
	private Map<String, String> createPropertiesMap(AlertingState alertingState, boolean isText) {
		if (alertingState == null) {
			throw new IllegalArgumentException("The given alerting state may not be null.");
		}

		Map<String, String> properties = new HashMap<>();

		NumberFormat numberFormat = new DecimalFormat(NUMBER_FORMAT);
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Alert alert = alertingState.getAlert();
		AlertingDefinition definition = alertingState.getAlertingDefinition();

		properties.put(Placeholders.ALERT_DEFINITION_NAME, definition.getName());
		properties.put(Placeholders.MEASUREMENT, definition.getMeasurement());
		properties.put(Placeholders.FIELD, definition.getField());
		properties.put(Placeholders.THRESHOLD, numberFormat.format(definition.getThreshold()));
		properties.put(Placeholders.START_TIME, String.valueOf(dateFormat.format(new Date(alert.getStartTimestamp()))));
		properties.put(Placeholders.VIOLATION_VALUE, numberFormat.format(alertingState.getExtremeValue()));
		properties.put(Placeholders.CURRENT_TIME, String.valueOf(new Date(System.currentTimeMillis())));
		properties.put(Placeholders.ALERT_ID, alert.getId());
		properties.put(Placeholders.EXTREME_VALUE, numberFormat.format(alertingState.getExtremeValue()));

		if (AlertingUtils.isBusinessTransactionAlert(definition)) {
			String applicationName = AlertingUtils.retrieveApplicaitonName(definition);
			if (null == applicationName) {
				applicationName = "All";
			}
			properties.put(AlertEMailTemplateType.Placeholders.APPLICATION_NAME, applicationName);

			String businessTxName = AlertingUtils.retrieveBusinessTransactionName(definition);
			if (null == businessTxName) {
				businessTxName = "All";
			}
			properties.put(AlertEMailTemplateType.Placeholders.BUSINESS_TX_NAME, businessTxName);
		}

		if (alert.getStopTimestamp() > 0) {
			properties.put(AlertEMailTemplateType.Placeholders.END_TIME, dateFormat.format(new Date(alert.getStopTimestamp())));
			properties.put(AlertEMailTemplateType.Placeholders.CLOSING_REASON, alert.getClosingReason().toString());
		}

		if (isText) {
			properties.put(Placeholders.TAGS, convertTagsToTextProperty(definition.getTags()));
		} else {
			properties.put(Placeholders.TAGS, convertTagsToHtmlTextProperty(definition.getTags()));
		}

		return properties;
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
	 * Converts a map of tags to HTML text representation.
	 *
	 * @param tags
	 *            Tag map to convert.
	 * @return A HTML text representation of the tags.
	 */
	private String convertTagsToHtmlTextProperty(Map<String, String> tags) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<String, String> tagKeyValuePair : tags.entrySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("<br />");
			}
			stringBuilder.append(tagKeyValuePair.getKey()).append('=').append(tagKeyValuePair.getValue());
		}
		return stringBuilder.toString();
	}
}
