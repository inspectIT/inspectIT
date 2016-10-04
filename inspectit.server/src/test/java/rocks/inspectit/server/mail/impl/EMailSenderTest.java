package rocks.inspectit.server.mail.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
	EMailSender sender;

	@Mock
	Logger log;

	protected String dummySubject = "subject";
	protected String dummyHtmlBody = "htmlBody";
	protected String dummyTextBody = "textBody";
	protected String validEmail1 = "max.mustermann@test.com";
	protected String validEmail2 = "hans-heinrich@test.com";
	protected String invalidEmail = "email";

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
	 * Tests the {@link EMailSender#sendEMail(String, String, String, java.util.List)} and
	 * {@link EMailSender#sendEMail(String, String, String, String...)} methods.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class SendEmail extends EMailSenderTest {

		protected HtmlEmail mockMail;
		protected EMailSender senderSpy;

		@BeforeMethod
		@SuppressWarnings("unchecked")
		public void beforeMethod() throws EmailException {
			mockMail = mock(HtmlEmail.class);
			senderSpy = spy(sender);

			doReturn(mockMail).when(senderSpy).prepareHtmlEmail(any(List.class));
		}

		@Test
		public void sendNotConnected() throws Exception {
			Field fieldConnected = EMailSender.class.getDeclaredField("connected");
			fieldConnected.setAccessible(true);
			fieldConnected.set(senderSpy, false);

			boolean result = senderSpy.sendEMail(dummySubject, dummyHtmlBody, dummyTextBody, Arrays.asList(validEmail1));
			assertThat(result, is(false));
		}

		@Test
		public void sendIsConnected() throws Exception {
			Field fieldConnected = EMailSender.class.getDeclaredField("connected");
			fieldConnected.setAccessible(true);
			fieldConnected.set(senderSpy, true);

			boolean result = senderSpy.sendEMail(dummySubject, dummyHtmlBody, dummyTextBody, Arrays.asList(validEmail1));
			assertThat(result, is(true));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void badSmtpPort() throws Exception {
			doThrow(IllegalArgumentException.class).when(senderSpy).prepareHtmlEmail(any(List.class));

			Field fieldConnected = EMailSender.class.getDeclaredField("connected");
			fieldConnected.setAccessible(true);
			fieldConnected.set(senderSpy, true);

			boolean result = senderSpy.sendEMail(dummySubject, dummyHtmlBody, dummyTextBody, Arrays.asList(validEmail1));
			assertThat(result, is(false));
		}

		@Test
		public void sendingFailed() throws Exception {
			doThrow(EmailException.class).when(mockMail).send();

			Field fieldConnected = EMailSender.class.getDeclaredField("connected");
			fieldConnected.setAccessible(true);
			fieldConnected.set(senderSpy, true);

			boolean result = senderSpy.sendEMail(dummySubject, dummyHtmlBody, dummyTextBody, Arrays.asList(validEmail1));
			assertThat(result, is(false));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void sendMail() {
			ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

			senderSpy.sendEMail(dummySubject, dummyHtmlBody, dummyTextBody, Arrays.asList(validEmail1, validEmail2));

			verify(senderSpy, times(1)).sendEMail(eq(dummySubject), eq(dummyHtmlBody), eq(dummyTextBody), captor.capture());

			List<String> recipientList = captor.getValue();
			assertThat(recipientList.size(), is(2));
			assertThat(recipientList, hasItems(validEmail1, validEmail2));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void sendMailNoRecipient() {
			ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

			senderSpy.sendEMail(dummySubject, dummyHtmlBody, dummyTextBody, new ArrayList<String>());

			verify(senderSpy, times(1)).sendEMail(eq(dummySubject), eq(dummyHtmlBody), eq(dummyTextBody), captor.capture());

			List<String> recipientList = captor.getValue();
			assertThat(recipientList.size(), is(0));
		}

		@Test
		public void testSetMailContent() throws Exception {
			Field fieldConnected = EMailSender.class.getDeclaredField("connected");
			fieldConnected.setAccessible(true);
			fieldConnected.set(senderSpy, true);

			senderSpy.sendEMail(dummySubject, dummyHtmlBody, dummyTextBody, Arrays.asList(validEmail1));

			verify(mockMail).setSubject(dummySubject);
			verify(mockMail).setHtmlMsg(dummyHtmlBody);
			verify(mockMail).setTextMsg(dummyTextBody);
			verify(mockMail).send();
		}
	}

	/**
	 * Test the {@link EMailSender#prepareHtmlEmail(List)} method.
	 */
	public static class PrepareHtmlEmail extends EMailSenderTest {

		protected EMailSender senderSpy;

		@BeforeMethod
		public void beforeMethod() {
			sender.smptHost = "localhost";
			sender.smptPort = 1;
			sender.senderAddress = "test@test.com";
			sender.senderName = "Tester";
			sender.smptUser = "";
			sender.smptPassword = "";
			sender.defaultRecipientString = "";
			sender.smtpPropertiesString = "";

			senderSpy = spy(sender);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void wrongSmtpPort() throws EmailException {
			sender.smptPort = 0;
			sender.prepareHtmlEmail(new ArrayList<String>());
		}

		@Test
		public void testEmptyRecipients() throws EmailException {
			HtmlEmail htmlMail = sender.prepareHtmlEmail(new ArrayList<String>());

			assertThat(htmlMail.getHostName(), equalTo(sender.smptHost));
			assertThat(htmlMail.getSmtpPort(), equalTo(String.valueOf(sender.smptPort)));
			assertThat(htmlMail.getToAddresses().size(), is(0));
			assertThat(htmlMail.getFromAddress().getAddress(), equalTo(sender.senderAddress));
		}

		@Test
		public void testDefaultRecipients() throws EmailException {
			senderSpy.defaultRecipientString = validEmail1;
			senderSpy.init();

			HtmlEmail htmlMail = senderSpy.prepareHtmlEmail(new ArrayList<String>());

			assertThat(htmlMail.getToAddresses().size(), is(1));
			assertThat(htmlMail.getToAddresses().get(0).getAddress(), equalTo(validEmail1));
		}

		@Test
		public void testSendWithRecipients() throws EmailException {
			senderSpy.defaultRecipientString = validEmail1;
			senderSpy.init();

			HtmlEmail htmlMail = senderSpy.prepareHtmlEmail(Arrays.asList(validEmail1, validEmail2));

			assertThat(htmlMail.getToAddresses().size(), is(3));
			assertThat(htmlMail.getToAddresses().get(0).getAddress(), equalTo(validEmail1));
			assertThat(htmlMail.getToAddresses().get(1).getAddress(), equalTo(validEmail1));
			assertThat(htmlMail.getToAddresses().get(2).getAddress(), equalTo(validEmail2));
		}
	}
}
