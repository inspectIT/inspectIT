package rocks.inspectit.server.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

		private Properties getAdditionalProperties() throws Exception {
			Field field = EMailSender.class.getDeclaredField("additionalProperties");
			field.setAccessible(true);
			return (Properties) field.get(mailSender);
		}

		@SuppressWarnings("unchecked")
		private List<String> getDefaultRecipients() throws Exception {
			Field field = EMailSender.class.getDeclaredField("defaultRecipients");
			field.setAccessible(true);
			return (List<String>) field.get(mailSender);
		}

		@Test
		public void successfully() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = "one@example.com,two@example.com,invalid";
			mailSender.smtpPropertiesString = "key1=val1,key2=val2,=noKey,noVal=,=,invalid";
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			verify(objectFactoryMock).getSmtpTransport();
			verifyNoMoreInteractions(objectFactoryMock);
			verify(transportMock).connect("host", 25, "user", "passwd");
			verify(transportMock).close();
			verifyNoMoreInteractions(transportMock);
			assertThat(mailSender.isConnected(), is(true));
			assertThat(getAdditionalProperties().entrySet(), hasSize(2));
			assertThat(getAdditionalProperties(), hasEntry((Object) "key1", (Object) "val1"));
			assertThat(getAdditionalProperties(), hasEntry((Object) "key2", (Object) "val2"));
			assertThat(getDefaultRecipients(), hasSize(2));
			assertThat(getDefaultRecipients(), hasItems("one@example.com", "two@example.com"));
		}

		@Test
		public void recipientsAndPopertiesAreNull() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.defaultRecipientString = null;
			mailSender.smtpPropertiesString = null;
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			verify(objectFactoryMock).getSmtpTransport();
			verify(transportMock).connect("host", 25, "user", "passwd");
			verify(transportMock).close();
			verifyNoMoreInteractions(objectFactoryMock, transportMock);
			assertThat(mailSender.isConnected(), is(true));
			assertThat(getAdditionalProperties().entrySet(), hasSize(0));
			assertThat(getDefaultRecipients(), hasSize(0));
		}

		@Test
		public void authenticationFailes() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			doThrow(AuthenticationFailedException.class).when(transportMock).connect("host", 25, "user", "passwd");
			mailSender.defaultRecipientString = null;
			mailSender.smtpPropertiesString = null;
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			verify(objectFactoryMock).getSmtpTransport();
			verify(transportMock).connect("host", 25, "user", "passwd");
			verifyNoMoreInteractions(objectFactoryMock, transportMock);
			assertThat(mailSender.isConnected(), is(false));
		}

		@Test
		public void connectionToSmtpFailed() throws Exception {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			doThrow(MessagingException.class).when(transportMock).connect("host", 25, "user", "passwd");
			mailSender.defaultRecipientString = null;
			mailSender.smtpPropertiesString = null;
			mailSender.smtpHost = "host";
			mailSender.smtpPort = 25;
			mailSender.smtpUser = "user";
			mailSender.smtpPassword = "passwd";

			mailSender.init();

			verify(objectFactoryMock).getSmtpTransport();
			verify(transportMock).connect("host", 25, "user", "passwd");
			verifyNoMoreInteractions(objectFactoryMock, transportMock);
			assertThat(mailSender.isConnected(), is(false));
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
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.init();

			boolean result = mailSender.isConnected();

			assertThat(result, is(true));
			verify(objectFactoryMock).getSmtpTransport();
			verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			verify(transportMock).close();
			verifyNoMoreInteractions(objectFactoryMock, transportMock);
		}

		@Test
		public void notConnected() throws MessagingException {
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			doThrow(MessagingException.class).when(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			mailSender.init();

			boolean result = mailSender.isConnected();

			assertThat(result, is(false));
			verify(objectFactoryMock).getSmtpTransport();
			verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			verifyNoMoreInteractions(objectFactoryMock, transportMock);
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
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mailMock);
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
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

			assertThat(result, is(true));
			verify(objectFactoryMock).getSmtpTransport();
			verify(objectFactoryMock).createHtmlEmail();
			verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			verify(transportMock).close();
			verify(mailMock).setHostName("host");
			verify(mailMock).setSmtpPort(25);
			verify(mailMock).setAuthentication("user", "passwd");
			verify(mailMock).setFrom("sender@example.com", "Sender Name");
			verify(mailMock).addTo("one@example.com");
			verify(mailMock).addTo("two@example.com");
			verify(mailMock).addTo("three@example.com");
			verify(mailMock).setSubject("subject");
			verify(mailMock).setHtmlMsg("htmlBody");
			verify(mailMock).setTextMsg("textBody");
			verify(mailMock).send();
			verifyNoMoreInteractions(objectFactoryMock, transportMock, mailMock);
		}

		@Test
		public void notConnected() throws Exception {
			boolean result = mailSender.sendEMail("subject", "htmlBody", "textBody", Arrays.asList("one@example.com"));

			assertThat(result, is(false));

			verifyZeroInteractions(mailMock);
			verifyZeroInteractions(transportMock);
			verifyZeroInteractions(objectFactoryMock);
		}

		@Test
		public void badSmtpPort() throws Exception {
			doThrow(IllegalArgumentException.class).when(mailMock).setSmtpPort(-1);
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mailMock);
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			mailSender.smtpHost = "host";
			mailSender.smtpPort = -1;
			mailSender.init();

			boolean result = mailSender.sendEMail("subject", "htmlBody", "textBody", Arrays.asList("one@example.com"));

			assertThat(result, is(false));
			verify(objectFactoryMock).getSmtpTransport();
			verify(objectFactoryMock).createHtmlEmail();
			verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			verify(transportMock).close();
			verify(mailMock).setHostName("host");
			verify(mailMock).setSmtpPort(-1);
			verifyNoMoreInteractions(objectFactoryMock, transportMock, mailMock);
		}

		@Test
		public void badSenderAdress() throws Exception {
			when(objectFactoryMock.createHtmlEmail()).thenReturn(mailMock);
			when(objectFactoryMock.getSmtpTransport()).thenReturn(transportMock);
			doThrow(EmailException.class).when(mailMock).setFrom(eq("invalid"), any(String.class));
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

			assertThat(result, is(false));
			verify(objectFactoryMock).getSmtpTransport();
			verify(objectFactoryMock).createHtmlEmail();
			verify(transportMock).connect(any(String.class), any(Integer.class), any(String.class), any(String.class));
			verify(transportMock).close();
			verify(mailMock).setHostName("host");
			verify(mailMock).setSmtpPort(25);
			verify(mailMock).setAuthentication("user", "passwd");
			verify(mailMock).setFrom("invalid", "Sender Name");
			verifyNoMoreInteractions(objectFactoryMock, transportMock, mailMock);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void subjectIsNull() throws Exception {
			try {
				mailSender.sendEMail(null, "htmlBody", "textBody", Arrays.asList("three@example.com"));
			} finally {
				verifyZeroInteractions(mailMock, transportMock, objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void htmlBodyIsNull() throws Exception {
			try {
				mailSender.sendEMail("subject", null, "textBody", Arrays.asList("three@example.com"));
			} finally {
				verifyZeroInteractions(mailMock, transportMock, objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void textBodyIsNull() throws Exception {
			try {
				mailSender.sendEMail("subject", "htmlBody", null, Arrays.asList("three@example.com"));
			} finally {
				verifyZeroInteractions(mailMock, transportMock, objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void subjectIsEmpty() throws Exception {
			try {
				mailSender.sendEMail("", "htmlBody", "textBody", Arrays.asList("three@example.com"));
			} finally {
				verifyZeroInteractions(mailMock, transportMock, objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void htmlBodyIsEmpty() throws Exception {
			try {
				mailSender.sendEMail("subject", "", "textBody", Arrays.asList("three@example.com"));
			} finally {
				verifyZeroInteractions(mailMock, transportMock, objectFactoryMock);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void textBodyIsEmpty() throws Exception {
			try {
				mailSender.sendEMail("subject", "htmlBody", "", Arrays.asList("three@example.com"));
			} finally {
				verifyZeroInteractions(mailMock, transportMock, objectFactoryMock);
			}
		}
	}
}
