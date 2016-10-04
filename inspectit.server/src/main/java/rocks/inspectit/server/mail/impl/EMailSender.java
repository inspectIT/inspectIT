package rocks.inspectit.server.mail.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.mail.IEMailSender;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.StringUtils;

/**
 * Central component for sending e-mails.
 *
 * @author Alexander Wert
 *
 */
@Component
public class EMailSender implements IEMailSender {
	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * SMTP Server host.
	 */
	@Value("${mail.smpt.host}")
	String smptHost;

	/**
	 * SMTP Server port.
	 */
	@Value("${mail.smpt.port}")
	int smptPort;

	/**
	 * SMTP user name.
	 */
	@Value("${mail.smtp.user}")
	String smptUser;

	/**
	 * Password for SMTP authentication.
	 */
	@Value("${mail.smtp.passwd}")
	String smptPassword;

	/**
	 * The e-mail address used as sender.
	 */
	@Value("${mail.from}")
	String senderAddress;

	/**
	 * Displayed name of the sender.
	 */
	@Value("${mail.from.name}")
	String senderName;

	/**
	 * A comma separated list of default recipient e-mail addresses.
	 */
	@Value("${mail.default.to}")
	String defaultRecipientString;

	/**
	 * Additional SMTP properties as a comma separated string.
	 */
	@Value("${mail.smtp.properties}")
	String smtpPropertiesString;

	/**
	 * Unwrapped list of default recipients.
	 */
	private final List<String> defaultRecipients = new ArrayList<>();

	/**
	 * SMTP connection state.
	 */
	private boolean connected = false;

	/**
	 * Additional SMTP properties that might be required for certain SMTP servers.
	 */
	private final Properties additionalProperties = new Properties();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean sendEMail(String subject, String htmlMessage, String textMessage, List<String> recipients) {
		if (!connected) {
			log.warn("Failed sending e-mail! E-Mail service cannot connect to the SMTP server. Check the connection settings!");
			return false;
		}
		try {
			HtmlEmail email = prepareHtmlEmail(recipients);
			email.setSubject(subject);
			email.setHtmlMsg(htmlMessage);
			email.setTextMsg(textMessage);
			email.send();
			return true;
		} catch (EmailException e) {
			log.warn("Failed sending e-mail!", e);
			return false;
		} catch (IllegalArgumentException e) {
			log.warn("Cannot send e-mail because the SMTP port is < 1.", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Gets {@link #defaultRecipients}.
	 *
	 * @return {@link #defaultRecipients}
	 */
	List<String> getDefaultRecipients() {
		return defaultRecipients;
	}

	/**
	 * Gets {@link #additionalProperties}.
	 *
	 * @return {@link #additionalProperties}
	 */
	Properties getAdditionalProperties() {
		return additionalProperties;
	}

	/**
	 * Prepares an email object.
	 *
	 * @param recipients
	 *            recipient to send to.
	 * @return Returns a prepared {@link HtmlEmail} object.
	 * @throws EmailException
	 *             is thrown when the from address could not be set
	 */
	HtmlEmail prepareHtmlEmail(List<String> recipients) throws EmailException {
		HtmlEmail email = new HtmlEmail();
		email.setHostName(smptHost);
		email.setSmtpPort(smptPort);
		email.setAuthentication(smptUser, smptPassword);
		email.setFrom(senderAddress, senderName);

		for (String defaultTo : getDefaultRecipients()) {
			try {
				email.addTo(defaultTo);
			} catch (EmailException e) {
				if (log.isWarnEnabled()) {
					log.warn("Invalid recipient e-mail address!", e);
				}
			}
		}
		for (String to : recipients) {
			try {
				email.addTo(to);
			} catch (EmailException e) {
				if (log.isWarnEnabled()) {
					log.warn("Invalid recipient e-mail address!", e);
				}
			}
		}

		return email;
	}

	/**
	 * Unwrap the comma separated list string of default recipients into a real list.
	 */
	@PropertyUpdate(properties = { "mail.default.to" })
	void parseRecipientsString() {
		getDefaultRecipients().clear();
		if (null != defaultRecipientString) {
			String[] strArray = defaultRecipientString.split(",");
			for (String element : strArray) {
				String address = element.trim();
				if (StringUtils.isValidEmailAddress(address)) {
					getDefaultRecipients().add(address);
				}
			}
		}

	}

	/**
	 * Unwrap the comma separated list string of additional properties into real properties object.
	 */
	@PropertyUpdate(properties = { "mail.smtp.properties" })
	void parseAdditionalPropertiesString() {
		getAdditionalProperties().clear();
		if (null != smtpPropertiesString) {
			String[] strArray = smtpPropertiesString.split(",");
			for (String property : strArray) {
				int equalsIndex = property.indexOf('=');
				if ((equalsIndex > 0) && (equalsIndex < (property.length() - 1))) {
					getAdditionalProperties().put(property.substring(0, equalsIndex).trim(), property.substring(equalsIndex + 1).trim());
				}
			}
		}

		checkConnection();
	}

	/**
	 * Get a {@link Transport} object for a SMTP connection.
	 *
	 * @return A new {@link Transport}.
	 * @throws NoSuchProviderException
	 *             If provider for SMTP protocol is not found.
	 */
	Transport getTransport() throws NoSuchProviderException {
		return Session.getInstance(getAdditionalProperties(), new DefaultAuthenticator(smptUser, smptPassword)).getTransport("smtp");
	}

	/**
	 * Checks connection to SMTP server.
	 */
	@PropertyUpdate(properties = { "mail.smpt.host", "mail.smpt.port", "mail.smpt.user", "mail.smpt.passwd" })
	void checkConnection() {
		try {
			Transport transport = getTransport();
			transport.connect(smptHost, smptPort, smptUser, smptPassword);
			transport.close();
			log.info("|-eMail Service active and connected...");
			connected = true;
		} catch (AuthenticationFailedException e) {
			log.warn("|-eMail Service was not able to connect! Authentication failed!");
			connected = false;
		} catch (MessagingException e) {
			log.warn("|-eMail Service was not able to connect! Check connection settings!");
			connected = false;
		}
	}

	/**
	 * Initialize E-Mail service.
	 */
	@PostConstruct
	protected void init() {
		parseRecipientsString();
		parseAdditionalPropertiesString();
		checkConnection();
	}
}
