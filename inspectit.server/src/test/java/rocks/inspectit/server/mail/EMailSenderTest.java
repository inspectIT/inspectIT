package rocks.inspectit.server.mail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.mail.EMailSender.ObjectFactory;
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

	/**
	 * Tests the {@link EMailSender#init()} and {@link EMailSender#onSmtpPropertiesChanged()}
	 * methods.
	 *
	 */
	public static class InitAndOnSmtpPropertiesChanged extends EMailSenderTest {

		@Mock
		Transport transportMock;

		@Mock
		Properties additionalProperties;

		@Mock
		List<String> defaultRecipients;

		@Test
		public void successfully() throws Exception {
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = "one@example.com,two@example.com,invalid";
			mailSender.smtpPropertiesString = "key1=val1,key2=val2,=noKey,noVal=,=,invalid";
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			Mockito.verify(defaultRecipients).clear();
			Mockito.verify(defaultRecipients).add("one@example.com");
			Mockito.verify(defaultRecipients).add("two@example.com");
			Mockito.verifyNoMoreInteractions(defaultRecipients);
			Mockito.verify(additionalProperties).clear();
			Mockito.verify(additionalProperties).put("key1", "val1");
			Mockito.verify(additionalProperties).put("key2", "val2");
			Mockito.verifyNoMoreInteractions(additionalProperties);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect("host", 25, "user", "passwd");
			Mockito.verify(transportMock).close();
			Mockito.verifyNoMoreInteractions(transportMock);
			assertTrue(mailSender.isConnected());
		}

		@Test
		public void recipientsAndPopertiesAreNull() throws Exception {
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = null;
			mailSender.smtpPropertiesString = null;
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			Mockito.verify(defaultRecipients).clear();
			Mockito.verifyNoMoreInteractions(defaultRecipients);
			Mockito.verify(additionalProperties).clear();
			Mockito.verifyNoMoreInteractions(additionalProperties);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect("host", 25, "user", "passwd");
			Mockito.verify(transportMock).close();
			Mockito.verifyNoMoreInteractions(transportMock);
			assertTrue(mailSender.isConnected());
		}

		@Test
		public void authenticationFailes() throws Exception {
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			Mockito.doThrow(AuthenticationFailedException.class).when(transportMock).connect("host", 25, "user", "passwd");
			mailSender.defaultRecipientString = null;
			mailSender.smtpPropertiesString = null;
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			Mockito.verify(defaultRecipients).clear();
			Mockito.verifyNoMoreInteractions(defaultRecipients);
			Mockito.verify(additionalProperties).clear();
			Mockito.verifyNoMoreInteractions(additionalProperties);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect("host", 25, "user", "passwd");
			Mockito.verifyNoMoreInteractions(transportMock);
			assertFalse(mailSender.isConnected());
		}

		@Test
		public void connectionToSmtpFailed() throws Exception {
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			Mockito.doThrow(MessagingException.class).when(transportMock).connect("host", 25, "user", "passwd");
			mailSender.defaultRecipientString = null;
			mailSender.smtpPropertiesString = null;
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			Mockito.verify(defaultRecipients).clear();
			Mockito.verifyNoMoreInteractions(defaultRecipients);
			Mockito.verify(additionalProperties).clear();
			Mockito.verifyNoMoreInteractions(additionalProperties);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect("host", 25, "user", "passwd");
			Mockito.verifyNoMoreInteractions(transportMock);
			assertFalse(mailSender.isConnected());
		}
	}

	/**
	 * Tests the {@link EMailSender#isConnected()} method.
	 */
	public static class IsConnected extends EMailSenderTest {

		@Mock
		Transport transportMock;

		@Test
		public void connected() throws MessagingException {
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.init();

			boolean result = mailSender.isConnected();

			assertTrue(result);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			Mockito.verify(transportMock).close();
			Mockito.verifyNoMoreInteractions(transportMock);
		}

		@Test
		public void notConnected() throws MessagingException {
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			Mockito.doThrow(MessagingException.class).when(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			mailSender.init();

			boolean result = mailSender.isConnected();

			assertFalse(result);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			Mockito.verifyNoMoreInteractions(transportMock);
		}
	}

	/**
	 * Tests the {@link EMailSender#sendEMail(String, String, String, java.util.List)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class SendEmail extends EMailSenderTest {

		@Mock
		HtmlEmail mailMock;

		@Mock
		Transport transportMock;

		@Test
		public void succesfully() throws Exception {
			Mockito.when(objectFactoryMock.createHtmlEmail()).thenReturn(mailMock);
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = "one@example.com,two@example.com,invalid";
			mailSender.smtpPropertiesString = "key1=val1,key2=val2,=noKey,noVal=,=,invalid";
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";
			mailSender.senderAddress = "sender@example.com";
			mailSender.senderName = "Sender Name";
			mailSender.init();

			boolean result = mailSender.sendEMail("subject", "htmlBody", "textBody", Arrays.asList("three@example.com"));

			assertTrue(result);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verify(objectFactoryMock).createHtmlEmail();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			Mockito.verify(transportMock).close();
			Mockito.verifyNoMoreInteractions(transportMock);
			Mockito.verify(mailMock).setHostName("host");
			Mockito.verify(mailMock).setSmtpPort(25);
			Mockito.verify(mailMock).setAuthentication("user", "passwd");
			Mockito.verify(mailMock).setFrom("sender@example.com", "Sender Name");
			Mockito.verify(mailMock).addTo("one@example.com");
			Mockito.verify(mailMock).addTo("two@example.com");
			Mockito.verify(mailMock).addTo("three@example.com");
			Mockito.verify(mailMock).setSubject("subject");
			Mockito.verify(mailMock).setHtmlMsg("htmlBody");
			Mockito.verify(mailMock).setTextMsg("textBody");
			Mockito.verify(mailMock).send();
			Mockito.verifyNoMoreInteractions(mailMock);
		}

		@Test
		public void notConnected() throws Exception {
			boolean result = mailSender.sendEMail("subject", "htmlBody", "textBody", Arrays.asList("one@example.com"));

			assertFalse(result);

			Mockito.verifyZeroInteractions(mailMock);
			Mockito.verifyZeroInteractions(transportMock);
			Mockito.verifyZeroInteractions(objectFactoryMock);
		}

		@Test
		public void badSmtpPort() throws Exception {
			Mockito.doThrow(IllegalArgumentException.class).when(mailMock).setSmtpPort(-1);
			Mockito.when(objectFactoryMock.createHtmlEmail()).thenReturn(mailMock);
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpHost = "host";
			mailSender.smtpPort = -1;
			mailSender.init();

			boolean result = mailSender.sendEMail("subject", "htmlBody", "textBody", Arrays.asList("one@example.com"));

			assertFalse(result);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verify(objectFactoryMock).createHtmlEmail();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			Mockito.verify(transportMock).close();
			Mockito.verifyNoMoreInteractions(transportMock);
			Mockito.verify(mailMock).setHostName("host");
			Mockito.verify(mailMock).setSmtpPort(-1);
			Mockito.verifyNoMoreInteractions(mailMock);
		}

		@Test
		public void badSenderAdress() throws Exception {
			Mockito.when(objectFactoryMock.createHtmlEmail()).thenReturn(mailMock);
			Mockito.when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			Mockito.doThrow(EmailException.class).when(mailMock).setFrom(eq("invalid"), any(String.class));
			mailSender.defaultRecipientString = "one@example.com,two@example.com,invalid";
			mailSender.smtpPropertiesString = "key1=val1,key2=val2,=noKey,noVal=,=,invalid";
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";
			mailSender.senderAddress = "invalid";
			mailSender.senderName = "Sender Name";
			mailSender.init();

			boolean result = mailSender.sendEMail("subject", "htmlBody", "textBody", Arrays.asList("three@example.com"));

			assertFalse(result);
			Mockito.verify(objectFactoryMock).getSmtpTransport();
			Mockito.verify(objectFactoryMock).createHtmlEmail();
			Mockito.verifyNoMoreInteractions(objectFactoryMock);
			Mockito.verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			Mockito.verify(transportMock).close();
			Mockito.verifyNoMoreInteractions(transportMock);
			Mockito.verify(mailMock).setHostName("host");
			Mockito.verify(mailMock).setSmtpPort(25);
			Mockito.verify(mailMock).setAuthentication("user", "passwd");
			Mockito.verify(mailMock).setFrom("invalid", "Sender Name");
			Mockito.verifyNoMoreInteractions(mailMock);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void subjectIsNull() throws Exception {
			try {
				mailSender.sendEMail(null, "htmlBody", "textBody", Arrays.asList("three@example.com"));
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void htmlBodyIsNull() throws Exception {
			try {
				mailSender.sendEMail("subject", null, "textBody", Arrays.asList("three@example.com"));
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void textBodyIsNull() throws Exception {
			try {
				mailSender.sendEMail("subject", "htmlBody", null, Arrays.asList("three@example.com"));
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void recipientsAreNull() throws Exception {
			try {
				mailSender.sendEMail("subject", "htmlBody", "textBody", null);
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void subjectIsEmpty() throws Exception {
			try {
				mailSender.sendEMail("", "htmlBody", "textBody", Arrays.asList("three@example.com"));
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void htmlBodyIsEmpty() throws Exception {
			try {
				mailSender.sendEMail("subject", "", "textBody", Arrays.asList("three@example.com"));
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void textBodyIsEmpty() throws Exception {
			try {
				mailSender.sendEMail("subject", "htmlBody", "", Arrays.asList("three@example.com"));
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		@SuppressWarnings("unchecked")
		public void recipientsAreEmpty() throws Exception {
			try {
				mailSender.sendEMail("subject", "htmlBody", "textBody", Collections.EMPTY_LIST);
			} finally {
				Mockito.verifyZeroInteractions(mailMock);
				Mockito.verifyZeroInteractions(transportMock);
				Mockito.verifyZeroInteractions(objectFactoryMock);
			}
		}
	}
}
