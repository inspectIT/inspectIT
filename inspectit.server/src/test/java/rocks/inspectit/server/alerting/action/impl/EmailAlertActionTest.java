package rocks.inspectit.server.alerting.action.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.mail.IEMailSender;
import rocks.inspectit.server.template.AlertEMailTemplateType;
import rocks.inspectit.server.template.TemplateManager;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 *
 * Test the {@link EmailAlertActionTest}.
 *
 * @author Marius Oehler
 *
 */
public class EmailAlertActionTest extends TestBase {

	@InjectMocks
	EmailAlertAction emailAction;

	@Mock
	Logger log;

	@Mock
	IEMailSender emailSender;

	@Mock
	TemplateManager templateManager;

	AlertingState alertingState;

	protected String htmlBtxOpen = "htmlBtxOpen";
	protected String txtBtxOpen = "txtBtxOpen";
	protected String htmlOpen = "htmlOpen";
	protected String txtOpen = "txtOpen";

	protected String htmlBtxClose = "htmlBtxClose";
	protected String txtBtxClose = "txtBtxClose";
	protected String htmlClose = "htmlClose";
	protected String txtClose = "txtClose";

	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN), any(Map.class))).thenReturn(htmlBtxOpen);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN), any(Map.class))).thenReturn(txtBtxOpen);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), any(Map.class))).thenReturn(htmlOpen);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_OPEN), any(Map.class))).thenReturn(txtOpen);

		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED), any(Map.class))).thenReturn(htmlBtxClose);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED), any(Map.class))).thenReturn(txtBtxClose);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_CLOSED), any(Map.class))).thenReturn(htmlClose);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_CLOSED), any(Map.class))).thenReturn(txtClose);

		AlertingDefinition alertingDefinition = new AlertingDefinition();
		alertingDefinition.setName("myAlert");
		alertingDefinition.setField("field");
		alertingDefinition.setMeasurement("measurement");
		alertingDefinition.setThreshold(1D);
		alertingDefinition.setThresholdType(ThresholdType.LOWER_THRESHOLD);
		alertingDefinition.setTimeRange(1L, TimeUnit.MINUTES);
		alertingDefinition.addNotificationEmailAddress("test@example.com");
		alertingDefinition.putTag("tagKey", "tagVal");
		alertingDefinition.setThreshold(10);
		alertingDefinition.setThresholdType(ThresholdType.UPPER_THRESHOLD);
		alertingDefinition.setTimeRange(0L, TimeUnit.MINUTES);

		Alert alert = new Alert(alertingDefinition, 0);

		alertingState = new AlertingState(alertingDefinition);
		alertingState.setExtremeValue(10D);
		alertingState.setAlert(alert);
	}

	/**
	 * Test the
	 * {@link EmailAlertAction#onStarting(rocks.inspectit.server.alerting.state.AlertingState, double)}
	 * method.
	 */
	public static class OnStart extends EmailAlertActionTest {

		@Test
		@SuppressWarnings("unchecked")
		public void testOnStartForBusinessTransaction() {
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);

			alertingState.getAlertingDefinition().setMeasurement(Series.BusinessTransaction.NAME);
			alertingState.getAlertingDefinition().setField(Series.BusinessTransaction.FIELD_DURATION);

			emailAction.onStarting(alertingState);

			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), any(List.class));

			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat(captorHtmlBody.getValue(), equalTo(htmlBtxOpen));
			assertThat(captorTextBody.getValue(), equalTo(txtBtxOpen));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void testOnStart() {
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);

			emailAction.onStarting(alertingState);

			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), any(List.class));

			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat(captorHtmlBody.getValue(), equalTo(htmlOpen));
			assertThat(captorTextBody.getValue(), equalTo(txtOpen));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void testOnEndingForBusinessTransaction() {
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);

			alertingState.getAlertingDefinition().setMeasurement(Series.BusinessTransaction.NAME);
			alertingState.getAlertingDefinition().setField(Series.BusinessTransaction.FIELD_DURATION);

			emailAction.onEnding(alertingState);

			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), any(List.class));

			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat(captorHtmlBody.getValue(), equalTo(htmlBtxClose));
			assertThat(captorTextBody.getValue(), equalTo(txtBtxClose));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void testOnEnding() {
			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);

			emailAction.onEnding(alertingState);

			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), any(List.class));

			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat(captorHtmlBody.getValue(), equalTo(htmlClose));
			assertThat(captorTextBody.getValue(), equalTo(txtClose));
		}
	}
}
