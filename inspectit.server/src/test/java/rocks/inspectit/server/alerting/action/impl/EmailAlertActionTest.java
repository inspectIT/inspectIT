package rocks.inspectit.server.alerting.action.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.mail.EMailSender;
import rocks.inspectit.server.template.AlertEMailTemplateType;
import rocks.inspectit.server.template.EMailTemplateResolver;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 *
 * Test the {@link EmailAlertActionTest}.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class EmailAlertActionTest extends TestBase {

	@InjectMocks
	EmailAlertAction emailAction;

	@Mock
	Logger log;

	@Mock
	EMailSender emailSender;

	@Mock
	EMailTemplateResolver templateResolver;

	/**
	 * Test the
	 * {@link EmailAlertAction#onStarting(rocks.inspectit.server.alerting.state.AlertingState, double)}
	 * method.
	 */
	public static class OnStarting extends EmailAlertActionTest {

		@Mock
		AlertingState alertingState;

		@Mock
		AlertingDefinition alertingDefinition;

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onStartNoBtx() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getName()).thenReturn("myName");
			Mockito.when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState)).thenReturn("htmlBody");
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState)).thenReturn("textBody");

			emailAction.onStarting(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getNotificationEmailAddresses();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(3)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState);
			Mockito.verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			Mockito.verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			Mockito.verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onStartIsBtx() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getName()).thenReturn("myName");
			Mockito.when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			Mockito.when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			Mockito.when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN, alertingState)).thenReturn("htmlBody");
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN, alertingState)).thenReturn("textBody");

			emailAction.onStarting(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getNotificationEmailAddresses();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(3)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN, alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN, alertingState);
			Mockito.verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			Mockito.verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			Mockito.verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void templateResolverThrowsException() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getName()).thenReturn("myName");
			Mockito.when(templateResolver.resolveTemplate(any(AlertEMailTemplateType.class), any(AlertingState.class))).thenThrow(IOException.class);

			emailAction.onStarting(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(2)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(any(AlertEMailTemplateType.class), eq(alertingState));
			Mockito.verifyNoMoreInteractions(templateResolver);
			Mockito.verifyZeroInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onStartUseDefaultName() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState)).thenReturn("htmlBody");
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState)).thenReturn("textBody");

			emailAction.onStarting(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getNotificationEmailAddresses();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(3)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState);
			Mockito.verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			Mockito.verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString(EmailAlertAction.DEFAULT_ALERTING_NAME));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			Mockito.verifyNoMoreInteractions(emailSender);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() throws IOException {
			try {
				emailAction.onStarting(null);
			} finally {
				Mockito.verifyZeroInteractions(alertingDefinition);
				Mockito.verifyZeroInteractions(alertingState);
				Mockito.verifyZeroInteractions(templateResolver);
				Mockito.verifyZeroInteractions(emailSender);
			}
		}
	}

	/**
	 * Test the {@link EmailAlertAction#onEnding(AlertingState) method.
	 */
	public static class OnEnding extends EmailAlertActionTest {
		@Mock
		AlertingState alertingState;

		@Mock
		AlertingDefinition alertingDefinition;

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onEndNoBtx() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getName()).thenReturn("myName");
			Mockito.when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState)).thenReturn("htmlBody");
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState)).thenReturn("textBody");

			emailAction.onEnding(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getNotificationEmailAddresses();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(3)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState);
			Mockito.verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			Mockito.verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			Mockito.verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onEndIsBtx() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getName()).thenReturn("myName");
			Mockito.when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			Mockito.when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			Mockito.when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED, alertingState)).thenReturn("htmlBody");
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED, alertingState)).thenReturn("textBody");

			emailAction.onEnding(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getNotificationEmailAddresses();
			Mockito.verify(alertingDefinition).getField();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(3)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED, alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED, alertingState);
			Mockito.verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			Mockito.verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			Mockito.verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void templateResolverThrowsException() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getName()).thenReturn("myName");
			Mockito.when(templateResolver.resolveTemplate(any(AlertEMailTemplateType.class), any(AlertingState.class))).thenThrow(IOException.class);

			emailAction.onEnding(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(2)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(any(AlertEMailTemplateType.class), eq(alertingState));
			Mockito.verifyNoMoreInteractions(templateResolver);
			Mockito.verifyZeroInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onEndUseDefaultName() throws IOException {
			Mockito.when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			Mockito.when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState)).thenReturn("htmlBody");
			Mockito.when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState)).thenReturn("textBody");

			emailAction.onEnding(alertingState);

			Mockito.verify(alertingDefinition).getName();
			Mockito.verify(alertingDefinition).getMeasurement();
			Mockito.verify(alertingDefinition).getNotificationEmailAddresses();
			Mockito.verifyNoMoreInteractions(alertingDefinition);
			Mockito.verify(alertingState, times(3)).getAlertingDefinition();
			Mockito.verifyNoMoreInteractions(alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState);
			Mockito.verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState);
			Mockito.verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			Mockito.verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString(EmailAlertAction.DEFAULT_ALERTING_NAME));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			Mockito.verifyNoMoreInteractions(emailSender);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() throws IOException {
			try {
				emailAction.onEnding(null);
			} finally {
				Mockito.verifyZeroInteractions(alertingDefinition);
				Mockito.verifyZeroInteractions(alertingState);
				Mockito.verifyZeroInteractions(templateResolver);
				Mockito.verifyZeroInteractions(emailSender);
			}
		}
	}
}
