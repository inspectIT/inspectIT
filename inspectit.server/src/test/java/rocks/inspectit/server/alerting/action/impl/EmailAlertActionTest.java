package rocks.inspectit.server.alerting.action.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getName()).thenReturn("myName");
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState)).thenReturn("htmlBody");
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState)).thenReturn("textBody");

			emailAction.onStarting(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getNotificationEmailAddresses();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(3)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState);
			verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onStartIsBtx() throws IOException {
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getName()).thenReturn("myName");
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN, alertingState)).thenReturn("htmlBody");
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN, alertingState)).thenReturn("textBody");

			emailAction.onStarting(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getNotificationEmailAddresses();
			verify(alertingDefinition).getField();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(3)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN, alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN, alertingState);
			verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void templateResolverThrowsException() throws IOException {
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getName()).thenReturn("myName");
			when(templateResolver.resolveTemplate(any(AlertEMailTemplateType.class), any(AlertingState.class))).thenThrow(IOException.class);

			emailAction.onStarting(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(2)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(any(AlertEMailTemplateType.class), eq(alertingState));
			verifyNoMoreInteractions(templateResolver);
			verifyZeroInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onStartUseDefaultName() throws IOException {
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState)).thenReturn("htmlBody");
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState)).thenReturn("textBody");

			emailAction.onStarting(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getNotificationEmailAddresses();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(3)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState);
			verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString(EmailAlertAction.DEFAULT_ALERTING_NAME));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			verifyNoMoreInteractions(emailSender);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() throws IOException {
			try {
				emailAction.onStarting(null);
			} finally {
				verifyZeroInteractions(alertingDefinition);
				verifyZeroInteractions(alertingState);
				verifyZeroInteractions(templateResolver);
				verifyZeroInteractions(emailSender);
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
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getName()).thenReturn("myName");
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState)).thenReturn("htmlBody");
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState)).thenReturn("textBody");

			emailAction.onEnding(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getNotificationEmailAddresses();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(3)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState);
			verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onEndIsBtx() throws IOException {
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getName()).thenReturn("myName");
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED, alertingState)).thenReturn("htmlBody");
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED, alertingState)).thenReturn("textBody");

			emailAction.onEnding(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getNotificationEmailAddresses();
			verify(alertingDefinition).getField();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(3)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED, alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED, alertingState);
			verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString("myName"));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			verifyNoMoreInteractions(emailSender);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void templateResolverThrowsException() throws IOException {
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getName()).thenReturn("myName");
			when(templateResolver.resolveTemplate(any(AlertEMailTemplateType.class), any(AlertingState.class))).thenThrow(IOException.class);

			emailAction.onEnding(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(2)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(any(AlertEMailTemplateType.class), eq(alertingState));
			verifyNoMoreInteractions(templateResolver);
			verifyZeroInteractions(emailSender);
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onEndUseDefaultName() throws IOException {
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState)).thenReturn("htmlBody");
			when(templateResolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState)).thenReturn("textBody");

			emailAction.onEnding(alertingState);

			verify(alertingDefinition).getName();
			verify(alertingDefinition).getMeasurement();
			verify(alertingDefinition).getNotificationEmailAddresses();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alertingState, times(3)).getAlertingDefinition();
			verifyNoMoreInteractions(alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.HTML_ALERT_CLOSED, alertingState);
			verify(templateResolver).resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState);
			verifyNoMoreInteractions(templateResolver);
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipients = ArgumentCaptor.forClass(List.class);
			verify(emailSender).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipients.capture());
			assertThat(captorHtmlBody.getValue(), equalTo("htmlBody"));
			assertThat(captorTextBody.getValue(), equalTo("textBody"));
			assertThat(captorSubject.getValue(), containsString(EmailAlertAction.DEFAULT_ALERTING_NAME));
			assertThat((List<String>) captorRecipients.getValue(), hasItem("test@example.com"));
			verifyNoMoreInteractions(emailSender);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() throws IOException {
			try {
				emailAction.onEnding(null);
			} finally {
				verifyZeroInteractions(alertingDefinition);
				verifyZeroInteractions(alertingState);
				verifyZeroInteractions(templateResolver);
				verifyZeroInteractions(emailSender);
			}
		}
	}
}
