package rocks.inspectit.server.mail.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.mail.impl.EMailSender.ObjectFactory;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link EMailSender} service.
 *
 * @author Alexander Wert
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class EMailSenderTest extends TestBase {

	@InjectMocks
	EMailSender mailSender;

	@Mock
	Logger log;

	@Mock
	ObjectFactory objectFactoryMock;

	static final String VALID_MAIL_1 = "max.mustermann@test.com";
	static final String VALID_MAIL_2 = "hans-heinrich@test.com";
	static final String INVALID_MAIL = "email";

	/**
	 * Tests the {@link EMailSender#init()} method.
	 *
	 */
	public static class Init extends EMailSenderTest {

		static final String SMTP_HOST = "smtpHost";
		static final int SMTP_PORT = 1;
		static final String SMTP_USER = "smtpUser";
		static final String SMTP_PASSWD = "smtpPort";
		static final String PROPERTY_KEY_1 = "key1";
		static final String PROPERTY_KEY_2 = "key2";
		static final String PROPERTY_VALUE_1 = "value1";
		static final String PROPERTY_VALUE_2 = "value2";

		@Mock
		Transport transportMock;

		// parseRecipientsString
		@Test
		public void initNullRecipients() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = null;

			mailSender.init();

			assertThat(mailSender.defaultRecipients, hasSize(0));
		}

		@Test
		public void initValidRecipients() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = VALID_MAIL_1 + "," + VALID_MAIL_2;

			mailSender.init();

			assertThat(mailSender.defaultRecipients, contains(VALID_MAIL_1, VALID_MAIL_2));
			assertThat(mailSender.defaultRecipients, hasSize(2));
		}

		@Test
		public void initInvalidRecipients() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = VALID_MAIL_1 + "," + INVALID_MAIL;

			mailSender.init();

			assertThat(mailSender.defaultRecipients, contains(VALID_MAIL_1));
			assertThat(mailSender.defaultRecipients, hasSize(1));
		}

		@Test
		public void initNoRecipients() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = "";

			mailSender.init();

			assertThat(mailSender.defaultRecipients, hasSize(0));
		}

		// parseAdditionalPropertiesString
		@Test
		public void initNullProperties() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpPropertiesString = null;

			mailSender.init();

			assertThat(mailSender.additionalProperties.entrySet(), hasSize(0));
		}

		@Test
		public void initValidProperties() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpPropertiesString = PROPERTY_KEY_1 + "=" + PROPERTY_VALUE_1 + "," + PROPERTY_KEY_2 + "=" + PROPERTY_VALUE_2;

			mailSender.init();

			assertThat(mailSender.additionalProperties.entrySet(), hasSize(2));
			assertThat(mailSender.additionalProperties.getProperty(PROPERTY_KEY_1), equalTo(PROPERTY_VALUE_1));
			assertThat(mailSender.additionalProperties.getProperty(PROPERTY_KEY_2), equalTo(PROPERTY_VALUE_2));
		}

		@Test
		public void initInvalidProperties() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpPropertiesString = "invalid," + PROPERTY_KEY_1 + "=" + PROPERTY_VALUE_1;

			mailSender.init();

			assertThat(mailSender.additionalProperties.entrySet(), hasSize(1));
			assertThat(mailSender.additionalProperties.getProperty(PROPERTY_KEY_1), equalTo(PROPERTY_VALUE_1));
		}

		@Test
		public void initInvalidPropertyEmptyKey() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpPropertiesString = "=" + PROPERTY_VALUE_1 + "," + PROPERTY_KEY_2 + "=" + PROPERTY_VALUE_2;

			mailSender.init();

			assertThat(mailSender.additionalProperties.entrySet(), hasSize(1));
			assertThat(mailSender.additionalProperties.getProperty(PROPERTY_KEY_2), equalTo(PROPERTY_VALUE_2));
		}

		@Test
		public void initInvalidPropertyEmptyValue() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpPropertiesString = PROPERTY_KEY_1 + "=," + PROPERTY_KEY_2 + "=" + PROPERTY_VALUE_2;

			mailSender.init();

			assertThat(mailSender.additionalProperties.entrySet(), hasSize(1));
			assertThat(mailSender.additionalProperties.getProperty(PROPERTY_KEY_2), equalTo(PROPERTY_VALUE_2));
		}

		@Test
		public void initEmptyProperties() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpPropertiesString = "";

			mailSender.init();

			assertThat(mailSender.additionalProperties.entrySet(), hasSize(0));
		}

		// checkConnection
		@Test
		public void checkConnection() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smptHost = SMTP_HOST;
			mailSender.smptPort = SMTP_PORT;
			mailSender.smptUser = SMTP_USER;
			mailSender.smptPassword = SMTP_PASSWD;

			mailSender.init();

			verify(transportMock, times(1)).connect(SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWD);
			assertThat(mailSender.connected, is(true));
		}

		@Test
		public void checkConnectionAuthenticationFailed() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			doThrow(AuthenticationFailedException.class).when(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));

			mailSender.init();

			assertThat(mailSender.connected, is(false));
		}

		@Test
		public void checkConnectionFailed() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			doThrow(MessagingException.class).when(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));

			mailSender.init();

			assertThat(mailSender.connected, is(false));
		}
	}

	/**
	 * Test the {@link EMailSender#onSmtpPropertiesChanged()} method.
	 *
	 */
	public static class OnSmtpPropertiesChanged extends EMailSenderTest {

		@Mock
		Transport transportMock;

		@Test
		public void testOnSmtpPropertiesChanged() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.connected = false;
			mailSender.additionalProperties = mock(Properties.class);

			mailSender.onSmtpPropertiesChanged();

			verify(mailSender.additionalProperties, times(1)).clear();
			assertThat(mailSender.connected, is(true));
		}
	}

	/**
	 * Tests the {@link EMailSender#sendEMail(String, String, String, java.util.List)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class SendEmail extends EMailSenderTest {

		static final String SMTP_HOST = "smtpHost";
		static final int SMTP_PORT = 1;
		static final String SMTP_USER = "smtpUser";
		static final String SMTP_PASSWD = "smtpPort";
		static final String SENDER_NAME = "Sender";
		static final String SENDER_ADDRESS = "sender@example.com";
		static final String DUMMY_SUBJECT = "subject";
		static final String DUMMY_HTML_BODY = "htmlBody";
		static final String DUMMY_TEXT_BODY = "textBody";

		@Mock
		HtmlEmail mockMail;

		@Test
		public void sendNotConnected() throws Exception {
			mailSender.connected = false;

			boolean result = mailSender.sendEMail(DUMMY_SUBJECT, DUMMY_HTML_BODY, DUMMY_TEXT_BODY, Arrays.asList(VALID_MAIL_1));

			assertThat(result, is(false));
		}

		@Test
		public void sendIsConnected() throws Exception {
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mockMail);
			mailSender.connected = true;

			boolean result = mailSender.sendEMail(DUMMY_SUBJECT, DUMMY_HTML_BODY, DUMMY_TEXT_BODY, Arrays.asList(VALID_MAIL_1));

			assertThat(result, is(true));
		}

		@Test
		public void badSmtpPort() throws Exception {
			doThrow(IllegalArgumentException.class).when(mockMail).setSmtpPort(any(int.class));
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mockMail);
			mailSender.connected = true;

			boolean result = mailSender.sendEMail(DUMMY_SUBJECT, DUMMY_HTML_BODY, DUMMY_TEXT_BODY, Arrays.asList(VALID_MAIL_1));

			assertThat(result, is(false));
		}

		@Test
		public void sendingFailed() throws Exception {
			doThrow(EmailException.class).when(mockMail).send();
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mockMail);
			mailSender.connected = true;

			boolean result = mailSender.sendEMail(DUMMY_SUBJECT, DUMMY_HTML_BODY, DUMMY_TEXT_BODY, Arrays.asList(VALID_MAIL_1));

			assertThat(result, is(false));
		}

		@Test
		public void sendMail() throws Exception {
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mockMail);
			mailSender.smptHost = SMTP_HOST;
			mailSender.smptPort = SMTP_PORT;
			mailSender.smptUser = SMTP_USER;
			mailSender.smptPassword = SMTP_PASSWD;
			mailSender.senderName = SENDER_NAME;
			mailSender.senderAddress = SENDER_ADDRESS;
			mailSender.defaultRecipients = Arrays.asList(VALID_MAIL_2);
			ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
			mailSender.connected = true;

			mailSender.sendEMail(DUMMY_SUBJECT, DUMMY_HTML_BODY, DUMMY_TEXT_BODY, Arrays.asList(VALID_MAIL_1));

			verify(mockMail).setSubject(DUMMY_SUBJECT);
			verify(mockMail).setHtmlMsg(DUMMY_HTML_BODY);
			verify(mockMail).setTextMsg(DUMMY_TEXT_BODY);
			verify(mockMail).setHostName(SMTP_HOST);
			verify(mockMail).setSmtpPort(SMTP_PORT);
			verify(mockMail).setAuthentication(SMTP_USER, SMTP_PASSWD);
			verify(mockMail).setFrom(SENDER_ADDRESS, SENDER_NAME);
			verify(mockMail).send();
			verify(mockMail, times(2)).addTo(toCaptor.capture());
			assertThat(toCaptor.getAllValues(), hasSize(2));
			assertThat(toCaptor.getAllValues(), hasItems(VALID_MAIL_1, VALID_MAIL_2));
		}

		@Test
		public void sendMailWithInvalidEmails() throws Exception {
			doThrow(EmailException.class).when(mockMail).addTo(INVALID_MAIL);
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mockMail);
			mailSender.defaultRecipients = Arrays.asList(VALID_MAIL_2, INVALID_MAIL);
			mailSender.connected = true;

			mailSender.sendEMail(DUMMY_SUBJECT, DUMMY_HTML_BODY, DUMMY_TEXT_BODY, Arrays.asList(VALID_MAIL_1, INVALID_MAIL));

			verify(mockMail).send();
		}

	}
}
