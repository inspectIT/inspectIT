package rocks.inspectit.server.mail;

import java.util.List;

/**
 * Interface for an e-mail sending service.
 *
 * @author Alexander Wert
 *
 */
public interface IEMailSender {
	/**
	 * Sends a HTML/Text email with the given parameters.
	 *
	 * @param subject
	 *            The e-mail subject
	 * @param htmlMessage
	 *            The HTML e-mail message.
	 * @param textMessage
	 *            The alternative textual e-mail message.
	 * @param recipients
	 *            List of recipient e-mail addresses.
	 * @return <code>true</code>, if the e-mail has been sent successfully, otherwise false.
	 */
	boolean sendEMail(String subject, String htmlMessage, String textMessage, List<String> recipients);

	/**
	 * Sends a HTML/Text email with the given parameters.
	 *
	 * @param subject
	 *            The e-mail subject
	 * @param htmlMessage
	 *            The HTML e-mail message.
	 * @param textMessage
	 *            The alternative textual e-mail message.
	 * @param recipients
	 *            List of recipient e-mail addresses.
	 * @return <code>true</code>, if the e-mail has been sent successfully, otherwise false.
	 */
	boolean sendEMail(String subject, String htmlMessage, String textMessage, String... recipients);

	/**
	 * Returns <code>true</code> if the e-mail service is able to connect to the SMTP server.
	 *
	 * @return Returns <code>true</code> if the e-mail service is able to connect to the SMTP
	 *         server.
	 */
	boolean isConnected();
}
