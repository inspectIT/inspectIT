package rocks.inspectit.server.alerting.action.impl;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.alerting.action.IAlertAction;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.alerting.util.AlertingUtils;
import rocks.inspectit.server.mail.EMailSender;
import rocks.inspectit.server.template.AlertEMailTemplateType;
import rocks.inspectit.server.template.EMailTemplateResolver;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * This alert action uses the {@link EMailSender} to send notification e-mails.
 *
 * @author Marius Oehler
 * @author Alexander Wert
 *
 */
@Component
public class EmailAlertAction implements IAlertAction {

	/**
	 * The default alerting name.
	 */
	static final String DEFAULT_ALERTING_NAME = "unnamed";

	/**
	 * Logger for this class.
	 */
	@Log
	private Logger log;

	/**
	 * {@link EMailSender} service used to send e-mails.
	 */
	@Autowired
	private EMailSender emailSender;

	/**
	 * {@link EMailTemplateResolver} used to resolve e-mail templates.
	 */
	@Autowired
	private EMailTemplateResolver templateResolver;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStarting(AlertingState alertingState) {
		if (alertingState == null) {
			throw new IllegalArgumentException("The given alerting state may not be null");
		}
		if (log.isDebugEnabled()) {
			log.debug("||-Sending e-mail because of starting an alert specified by: {}", alertingState.getAlertingDefinition().toString());
		}

		try {
			String subject = "Alert - Threshold '" + getAlertingName(alertingState) + "' violated";
			String htmlBody;
			String textBody;

			if (AlertingUtils.isBusinessTransactionAlert(alertingState.getAlertingDefinition())) {
				htmlBody = templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN, alertingState);
				textBody = templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN, alertingState);
			} else {
				htmlBody = templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);
				textBody = templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState);
			}

			emailSender.sendEMail(subject, htmlBody, textBody, alertingState.getAlertingDefinition().getNotificationEmailAddresses());
		} catch (IOException e) {
			if (log.isWarnEnabled()) {
				log.warn("Could not send starting alert e-mail!", e);
			}
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
		if (alertingState == null) {
			throw new IllegalArgumentException("The given alerting state may not be null");
		}
		if (log.isDebugEnabled()) {
			log.debug("||-Sending e-mail because of alert '{}' has ended.", alertingState.getAlertingDefinition().getName());
		}

		try {
			String subject = "Alert - Threshold '" + getAlertingName(alertingState) + "' has been closed";
			String htmlBody;
			String textBody;

			if (AlertingUtils.isBusinessTransactionAlert(alertingState.getAlertingDefinition())) {
				htmlBody = templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED, alertingState);
				textBody = templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED, alertingState);
			} else {
				htmlBody = templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState);
				textBody = templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState);
			}

			emailSender.sendEMail(subject, htmlBody, textBody, alertingState.getAlertingDefinition().getNotificationEmailAddresses());
		} catch (IOException e) {
			if (log.isWarnEnabled()) {
				log.warn("Could not send ending alert e-mail!", e);
			}
			return;
		}
	}

	/**
	 * Returns the name of the given {@link AlertingState}. If the name is empty or null a default
	 * name is returned.
	 *
	 * @param alertingState
	 *            the {@link AlertingState}
	 * @return the name of the given {@link AlertingState}
	 */
	private String getAlertingName(AlertingState alertingState) {
		String alertingDefinitionName = alertingState.getAlertingDefinition().getName();
		if (StringUtils.isEmpty(alertingDefinitionName)) {
			return DEFAULT_ALERTING_NAME;
		} else {
			return alertingDefinitionName;
		}
	}

}
