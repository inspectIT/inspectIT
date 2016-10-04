package rocks.inspectit.server.mail.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.io.IOException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.dumbster.smtp.ServerOptions;
import com.dumbster.smtp.SmtpServer;
import com.dumbster.smtp.SmtpServerFactory;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link EMailSender} service.
 *
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
public class EMailSenderTest extends TestBase {
	private static final int SMTP_PORT = 9981;


	static SmtpServer fakeSmtpServer;

	@BeforeClass
	public static void initSMTPServer() throws IOException {
		ServerOptions serverOptions = new ServerOptions();
		serverOptions.port = SMTP_PORT;
		fakeSmtpServer = SmtpServerFactory.startServer(serverOptions);
		fakeSmtpServer.clearMessages();
	}

	@AfterClass
	public static void cleanUp() throws IOException {
		fakeSmtpServer.stop();
	}

	@InjectMocks
	EMailSender sender;

	@Mock
	Logger log;



	/**
	 * Tests the {@link EMailSender#checkConnection()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class CheckConnection extends EMailSenderTest {


		@Test
		public void successful() {
			sender.smptHost = "localhost";
			sender.smptPort = SMTP_PORT;
			sender.checkConnection();
			Assert.assertTrue(sender.isConnected());
		}

		@Test
		public void wrongHost() {
			sender.smptHost = "wrongHost";
			sender.smptPort = SMTP_PORT;
			sender.checkConnection();
			Assert.assertFalse(sender.isConnected());
		}

		@Test
		public void wrongPort() {
			sender.smptHost = "localhost";
			sender.smptPort = 1234;
			sender.checkConnection();
			Assert.assertFalse(sender.isConnected());
		}
	}

	/**
	 * Tests the {@link EMailSender#parseRecipientsString()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class ParseRecipientsString extends EMailSenderTest {
		String validEmail1 = "max.mustermann@test.com";
		String validEmail2 = "hans-heinrich@test.com";
		String invalidEmail = "email";

		@Test
		public void valid() {
			sender.defaultRecipientString = validEmail1 + "," + validEmail2;
			sender.parseRecipientsString();
			assertThat(sender.getDefaultRecipients(), contains(validEmail1, validEmail2));
		}

		@Test
		public void oneInvalid() {
			sender.defaultRecipientString = validEmail1 + "," + invalidEmail + "," + validEmail2;
			sender.parseRecipientsString();
			assertThat(sender.getDefaultRecipients(), contains(validEmail1, validEmail2));
			assertThat(sender.getDefaultRecipients(), hasSize(2));
		}

		@Test
		public void empty() {
			sender.defaultRecipientString = " ";
			sender.parseRecipientsString();
			assertThat(sender.getDefaultRecipients(), hasSize(0));
		}
	}

	/**
	 * Tests the {@link EMailSender#parseAdditionalPropertiesString()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class ParseAdditionalPropertiesString extends EMailSenderTest {
		String property1 = "a=1";
		String property2 = "b=2";
		String invalidProperty = "c3";

		@Test
		public void valid() {
			sender.smtpPropertiesString = property1 + "," + property2;
			sender.parseAdditionalPropertiesString();
			assertThat(sender.getAdditionalProperties().getProperty("a"), equalTo("1"));
			assertThat(sender.getAdditionalProperties().getProperty("b"), equalTo("2"));
			assertThat(sender.getAdditionalProperties().size(), equalTo(2));
		}

		@Test
		public void oneInvalid() {
			sender.smtpPropertiesString = property1 + "," + invalidProperty + "," + property2;
			sender.parseAdditionalPropertiesString();
			assertThat(sender.getAdditionalProperties().getProperty("a"), equalTo("1"));
			assertThat(sender.getAdditionalProperties().getProperty("b"), equalTo("2"));
			assertThat(sender.getAdditionalProperties().size(), equalTo(2));
		}

		@Test
		public void empty() {
			sender.smtpPropertiesString = " ";
			sender.parseAdditionalPropertiesString();
			assertThat(sender.getAdditionalProperties().size(), equalTo(0));
		}
	}

	/**
	 * Tests the {@link EMailSender#sendEMail(String, String, String, java.util.List)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class SendEmail extends EMailSenderTest {
		String validEmail1 = "max.mustermann@test.com";
		String validEmail2 = "hans-heinrich@test.com";
		String invalidEmail = "email";

		@Test
		public void sendOneRecipient() {
			sender.smptHost = "localhost";
			sender.smptPort = SMTP_PORT;
			sender.senderAddress = "test@test.com";
			sender.senderName = "Tester";
			sender.smptUser = "";
			sender.smptPassword = "";
			sender.defaultRecipientString = "";
			sender.smtpPropertiesString = "";
			sender.init();

			fakeSmtpServer.clearMessages();
			String subject = "sub";
			sender.sendEMail(subject, "htmlBody", "textBody", validEmail1);

			assertThat(fakeSmtpServer.getEmailCount(), equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("Subject").length, equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("Subject")[0], equalTo(subject));
		}

		@Test
		public void sendWithTwoRecipient() {
			String from = "test@test.com";
			sender.smptHost = "localhost";
			sender.smptPort = SMTP_PORT;
			sender.senderAddress = from;
			sender.senderName = "Tester";
			sender.smptUser = "";
			sender.smptPassword = "";
			sender.defaultRecipientString = "";
			sender.smtpPropertiesString = "";
			sender.init();
			fakeSmtpServer.clearMessages();

			String subject = "sub";

			sender.sendEMail(subject, "htmlBody", "textBody", validEmail1, validEmail2);

			assertThat(fakeSmtpServer.getEmailCount(), equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To").length, equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To")[0], containsString(validEmail1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To")[0], containsString(validEmail2));
		}

		@Test
		public void sendWithDefaultRecipient() {
			String from = "test@test.com";
			sender.smptHost = "localhost";
			sender.smptPort = SMTP_PORT;
			sender.senderAddress = from;
			sender.senderName = "Tester";
			sender.smptUser = "";
			sender.smptPassword = "";
			sender.defaultRecipientString = validEmail2;
			sender.smtpPropertiesString = "";
			sender.init();
			fakeSmtpServer.clearMessages();

			String subject = "sub";

			sender.sendEMail(subject, "htmlBody", "textBody", validEmail1);

			assertThat(fakeSmtpServer.getEmailCount(), equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To").length, equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To")[0], containsString(validEmail1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To")[0], containsString(validEmail2));
		}

		@Test
		public void sendNoRecipient() {
			String from = "test@test.com";
			sender.smptHost = "localhost";
			sender.smptPort = SMTP_PORT;
			sender.senderAddress = from;
			sender.senderName = "Tester";
			sender.smptUser = "";
			sender.smptPassword = "";
			sender.defaultRecipientString = "";
			sender.smtpPropertiesString = "";
			sender.init();
			fakeSmtpServer.clearMessages();

			String subject = "sub";

			sender.sendEMail(subject, "htmlBody", "textBody");

			assertThat(fakeSmtpServer.getEmailCount(), equalTo(0));
		}

		@Test
		public void sendWrongRecipient() {
			String from = "test@test.com";
			sender.smptHost = "localhost";
			sender.smptPort = SMTP_PORT;
			sender.senderAddress = from;
			sender.senderName = "Tester";
			sender.smptUser = "";
			sender.smptPassword = "";
			sender.defaultRecipientString = "";
			sender.smtpPropertiesString = "";
			sender.init();
			fakeSmtpServer.clearMessages();

			String subject = "sub";

			sender.sendEMail(subject, "htmlBody", "textBody", validEmail1, invalidEmail);

			assertThat(fakeSmtpServer.getEmailCount(), equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To").length, equalTo(1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To")[0], containsString(validEmail1));
			assertThat(fakeSmtpServer.getMessage(0).getHeaderValues("To")[0], not(containsString(invalidEmail)));
		}

		@Test
		public void sendWrongSender() {
			String from = "test";
			sender.smptHost = "localhost";
			sender.smptPort = SMTP_PORT;
			sender.senderAddress = from;
			sender.senderName = "Tester";
			sender.smptUser = "";
			sender.smptPassword = "";
			sender.defaultRecipientString = "";
			sender.smtpPropertiesString = "";
			sender.init();
			fakeSmtpServer.clearMessages();

			String subject = "sub";

			sender.sendEMail(subject, "htmlBody", "textBody");

			assertThat(fakeSmtpServer.getEmailCount(), equalTo(0));
		}
	}
}
