package rocks.inspectit.server.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.EMailUtils;

/**
 * Central component for sending e-mails.
 *
 * @author Alexander Wert
 * @author Marius Oehler
 *
 */
@Component
public class EMailSender {
	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * SMTP Server host.
	 */
	@Value("${mail.smpt.host}")
	String smtpHost;

	/**
	 * SMTP Server port.
	 */
	@Value("${mail.smpt.port}")
	int smtpPort;

	/**
	 * SMTP user name.
	 */
	@Value("${mail.smtp.user}")
	String smtpUser;

	/**
	 * Password for SMTP authentication.
	 */
	@Value("${mail.smtp.passwd}")
	String smtpPassword;

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
	private List<String> defaultRecipients = new ArrayList<>();

	/**
	 * SMTP connection state.
	 */
	private boolean connected = false;

	/**
	 * Additional SMTP properties that might be required for certain SMTP servers.
	 */
	private Properties additionalProperties = new Properties();

	/**
	 * The {@link ObjectFactory} used to created object instances.
	 */
	private ObjectFactory objectFactory = new ObjectFactory();

	/**
	 * {@inheritDoc}
	 */
	public boolean sendEMail(String subject, String htmlMessage, String textMessage, List<String> recipients) {
		if (StringUtils.isEmpty(subject)) {
			throw new IllegalArgumentException("The given subject may not be null or empty.");
		}
		if (StringUtils.isEmpty(htmlMessage)) {
			throw new IllegalArgumentException("The given HTML body may not be null or empty.");
		}
		if (StringUtils.isEmpty(textMessage)) {
			throw new IllegalArgumentException("The given text body may not be null or empty.");
		}
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
		} catch (EmailException | IllegalArgumentException e) {
			log.warn("Failed sending e-mail!", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Unwrap the comma separated list string of default recipients into a real list.
	 */
	@PropertyUpdate(properties = { "mail.default.to" })
	private void parseRecipientsString() {
		defaultRecipients.clear();
		if (null != defaultRecipientString) {
			String[] strArray = defaultRecipientString.split(",");
			for (String element : strArray) {
				String address = element.trim();
				if (EMailUtils.isValidEmailAddress(address)) {
					defaultRecipients.add(address);
				}
			}
		}

	}

	/**
	 * Unwrap the comma separated list string of additional properties into real properties object.
	 */
	private void parseAdditionalPropertiesString() {
		additionalProperties.clear();
		if (null != smtpPropertiesString) {
			String[] strArray = smtpPropertiesString.split(",");
			for (String property : strArray) {
				int equalsIndex = property.indexOf('=');
				if ((equalsIndex > 0) && (equalsIndex < (property.length() - 1))) {
					additionalProperties.put(property.substring(0, equalsIndex).trim(), property.substring(equalsIndex + 1).trim());
				}
			}
		}
	}

	/**
	 * Parses the SMTP properties and checks whether a connection can be established.
	 */
	@PropertyUpdate(properties = { "mail.smpt.host", "mail.smpt.port", "mail.smpt.user", "mail.smpt.passwd", "mail.smtp.properties" })
	public void onSmtpPropertiesChanged() {
		parseAdditionalPropertiesString();
		checkConnection();
	}

	/**
	 * Initialize E-Mail service.
	 */
	@PostConstruct
	public void init() {
		parseRecipientsString();
		onSmtpPropertiesChanged();
	}

	/**
	 * Checks connection to SMTP server.
	 */
	private void checkConnection() {
		try {
			Transport transport = objectFactory.getSmtpTransport();
			transport.connect(smtpHost, smtpPort, smtpUser, smtpPassword);
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
	 * Prepares an email object.
	 *
	 * @param recipients
	 *            recipient to send to.
	 * @return Returns a prepared {@link HtmlEmail} object.
	 * @throws EmailException
	 *             is thrown when the from address could not be set
	 */
	private HtmlEmail prepareHtmlEmail(List<String> recipients) throws EmailException {
		HtmlEmail email = objectFactory.createHtmlEmail();
		email.setHostName(smtpHost);
		email.setSmtpPort(smtpPort);
		email.setAuthentication(smtpUser, smtpPassword);
		email.setFrom(senderAddress, senderName);

		if ((additionalProperties != null) && !additionalProperties.isEmpty()) {
			email.getMailSession().getProperties().putAll(additionalProperties);
		}

		for (String defaultTo : defaultRecipients) {
			try {
				email.addTo(defaultTo);
			} catch (EmailException e) {
				if (log.isWarnEnabled()) {
					log.warn("Invalid recipient e-mail address!", e);
				}
			}
		}
		if (recipients != null) {
			for (String to : recipients) {
				try {
					email.addTo(to);
				} catch (EmailException e) {
					if (log.isWarnEnabled()) {
						log.warn("Invalid recipient e-mail address!", e);
					}
				}
			}
		}

		return email;
	}

	/**
	 * Factory class to create objects required by the EMailSender. This class primary exists for
	 * better testing process.
	 *
	 * @author Marius Oehler
	 *
	 */
	class ObjectFactory {
		/**
		 * Get a {@link Transport} object for a SMTP connection.
		 *
		 * @return A new {@link Transport}.
		 * @throws NoSuchProviderException
		 *             If provider for SMTP protocol is not found.
		 */
		public Transport getSmtpTransport() throws NoSuchProviderException {
			return Session.getInstance(additionalProperties, new DefaultAuthenticator(smtpUser, smtpPassword)).getTransport("smtp");
		}

		/**
		 * Creates a new instance of {@link HtmlEmail}.
		 *
		 * @return the created instance
		 */
		public HtmlEmail createHtmlEmail() {
			return new HtmlEmail();
		}
	}
}
