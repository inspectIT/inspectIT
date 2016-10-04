package rocks.inspectit.server.alerting.action.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;
import rocks.inspectit.shared.cs.communication.data.cmr.AlertClosingReason;

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

	@Mock
	AlertingState alertingState;

	@Mock
	AlertingDefinition alertingDefinition;

	@Mock
	Alert alert;

	private static final String HTML_BTX_OPEN = "htmlBtxOpen";
	private static final String TXT_BTX_OPEN = "txtBtxOpen";
	private static final String HTML_OPEN = "htmlOpen";
	private static final String TXT_OPEN = "txtOpen";

	private static final String HTML_BTX_CLOSE = "htmlBtxClose";
	private static final String TXT_BTX_CLOSE = "txtBtxClose";
	private static final String HTML_CLOSE = "htmlClose";
	private static final String TXT_CLOSE = "txtClose";

	private static final String ALERTING_NAME = "testName";
	private static final double THRESHOLD = 11;
	private static final double EXTREME_VALUE = 13;
	private static final String ALERT_ID = "alertIdentifier";
	private static final String APPLICATION_NAME = "appName";
	private static final String BTX_NAME = "btxName";
	private static final long START_TIME = 1234567;
	private static final long STOP_TIME = 12345678;
	private static final String RECIPIENT = "Test@test";

	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();;

	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_OPEN), any(Map.class))).thenReturn(HTML_BTX_OPEN);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_OPEN), any(Map.class))).thenReturn(TXT_BTX_OPEN);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), any(Map.class))).thenReturn(HTML_OPEN);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_OPEN), any(Map.class))).thenReturn(TXT_OPEN);

		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_BUSINESS_TX_ALERT_CLOSED), any(Map.class))).thenReturn(HTML_BTX_CLOSE);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_BUSINESS_TX_ALERT_CLOSED), any(Map.class))).thenReturn(TXT_BTX_CLOSE);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_CLOSED), any(Map.class))).thenReturn(HTML_CLOSE);
		when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_CLOSED), any(Map.class))).thenReturn(TXT_CLOSE);
	}

	/**
	 * Test the
	 * {@link EmailAlertAction#onStarting(rocks.inspectit.server.alerting.state.AlertingState, double)}
	 * method.
	 */
	public static class OnStart extends EmailAlertActionTest {

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void testOnStartForBusinessTransaction() throws IOException, ParseException {
			Map<String, String> tags = new HashMap<>();
			tags.put(Series.BusinessTransaction.TAG_APPLICATION_NAME, APPLICATION_NAME);
			tags.put(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, BTX_NAME);

			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(EXTREME_VALUE);
			when(alertingDefinition.getName()).thenReturn(ALERTING_NAME);
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Collections.singletonList(RECIPIENT));
			when(alert.getId()).thenReturn(ALERT_ID);
			when(alert.getStartTimestamp()).thenReturn(START_TIME);

			emailAction.onStarting(alertingState);

			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipient = ArgumentCaptor.forClass(List.class);
			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipient.capture());
			assertThat(captorHtmlBody.getValue(), equalTo(HTML_BTX_OPEN));
			assertThat(captorTextBody.getValue(), equalTo(TXT_BTX_OPEN));
			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat((List<String>) captorRecipient.getValue(), contains(RECIPIENT));

			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			ArgumentCaptor<AlertEMailTemplateType> templateTypeCaptor = ArgumentCaptor.forClass(AlertEMailTemplateType.class);
			verify(templateManager, times(2)).resolveTemplate(templateTypeCaptor.capture(), propertiesCaptor.capture());

			Map<String, String> propMap = propertiesCaptor.getValue();
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_DEFINITION_NAME), is(ALERTING_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.MEASUREMENT), is(Series.BusinessTransaction.NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.FIELD), is(Series.BusinessTransaction.FIELD_DURATION));
			assertThat(NUMBER_FORMAT.parse(propMap.get(AlertEMailTemplateType.Placeholders.THRESHOLD)).doubleValue(), is(THRESHOLD));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.APPLICATION_NAME), is(APPLICATION_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.BUSINESS_TX_NAME), is(BTX_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_ID), is(ALERT_ID));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.END_TIME), isEmptyOrNullString());

		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void testOnStart() throws IOException, ParseException {
			Map<String, String> tags = new HashMap<>();
			tags.put("tag1", "value1");
			String measurement = "CPU";
			String field = "util";
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(EXTREME_VALUE);
			when(alertingDefinition.getName()).thenReturn(ALERTING_NAME);
			when(alertingDefinition.getMeasurement()).thenReturn(measurement);
			when(alertingDefinition.getField()).thenReturn(field);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Collections.singletonList(RECIPIENT));
			when(alert.getId()).thenReturn(ALERT_ID);
			when(alert.getStartTimestamp()).thenReturn(START_TIME);

			emailAction.onStarting(alertingState);

			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipient = ArgumentCaptor.forClass(List.class);
			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipient.capture());
			assertThat(captorHtmlBody.getValue(), equalTo(HTML_OPEN));
			assertThat(captorTextBody.getValue(), equalTo(TXT_OPEN));
			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat((List<String>) captorRecipient.getValue(), contains(RECIPIENT));

			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			ArgumentCaptor<AlertEMailTemplateType> templateTypeCaptor = ArgumentCaptor.forClass(AlertEMailTemplateType.class);
			verify(templateManager, times(2)).resolveTemplate(templateTypeCaptor.capture(), propertiesCaptor.capture());

			Map<String, String> propMap = propertiesCaptor.getValue();
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_DEFINITION_NAME), is(ALERTING_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.MEASUREMENT), is(measurement));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.FIELD), is(field));
			assertThat(NUMBER_FORMAT.parse(propMap.get(AlertEMailTemplateType.Placeholders.THRESHOLD)).doubleValue(), is(THRESHOLD));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_ID), is(ALERT_ID));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.APPLICATION_NAME), isEmptyOrNullString());
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.BUSINESS_TX_NAME), isEmptyOrNullString());
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.END_TIME), isEmptyOrNullString());

		}
	}

	/**
	 * Test the {@link EmailAlertAction#onEnding(AlertingState) method.
	 */
	public static class OnEnding extends EmailAlertActionTest {

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void testOnEndingForBusinessTransaction() throws IOException, ParseException {
			Map<String, String> tags = new HashMap<>();
			tags.put(Series.BusinessTransaction.TAG_APPLICATION_NAME, APPLICATION_NAME);
			tags.put(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, BTX_NAME);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(EXTREME_VALUE);
			when(alertingDefinition.getName()).thenReturn(ALERTING_NAME);
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Collections.singletonList(RECIPIENT));
			when(alert.getId()).thenReturn(ALERT_ID);
			when(alert.getStartTimestamp()).thenReturn(START_TIME);
			when(alert.getStopTimestamp()).thenReturn(STOP_TIME);
			when(alert.getClosingReason()).thenReturn(AlertClosingReason.ALERT_RESOLVED);

			emailAction.onEnding(alertingState);

			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipient = ArgumentCaptor.forClass(List.class);
			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipient.capture());
			assertThat(captorHtmlBody.getValue(), equalTo(HTML_BTX_CLOSE));
			assertThat(captorTextBody.getValue(), equalTo(TXT_BTX_CLOSE));
			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat((List<String>) captorRecipient.getValue(), contains(RECIPIENT));

			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			ArgumentCaptor<AlertEMailTemplateType> templateTypeCaptor = ArgumentCaptor.forClass(AlertEMailTemplateType.class);
			verify(templateManager, times(2)).resolveTemplate(templateTypeCaptor.capture(), propertiesCaptor.capture());

			Map<String, String> propMap = propertiesCaptor.getValue();
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_DEFINITION_NAME), is(ALERTING_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.MEASUREMENT), is(Series.BusinessTransaction.NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.FIELD), is(Series.BusinessTransaction.FIELD_DURATION));
			assertThat(NUMBER_FORMAT.parse(propMap.get(AlertEMailTemplateType.Placeholders.THRESHOLD)).doubleValue(), is(THRESHOLD));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.APPLICATION_NAME), is(APPLICATION_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.BUSINESS_TX_NAME), is(BTX_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_ID), is(ALERT_ID));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.END_TIME), not(isEmptyOrNullString()));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.CLOSING_REASON), is(AlertClosingReason.ALERT_RESOLVED.toString()));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void testOnEnding() throws IOException, ParseException {
			String measurement = "CPU";
			String field = "util";
			Map<String, String> tags = new HashMap<>();
			tags.put(Series.BusinessTransaction.TAG_APPLICATION_NAME, APPLICATION_NAME);
			tags.put(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, BTX_NAME);
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(EXTREME_VALUE);
			when(alertingDefinition.getName()).thenReturn(ALERTING_NAME);
			when(alertingDefinition.getMeasurement()).thenReturn(measurement);
			when(alertingDefinition.getField()).thenReturn(field);
			when(alertingDefinition.getThreshold()).thenReturn(THRESHOLD);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Collections.singletonList(RECIPIENT));
			when(alert.getId()).thenReturn(ALERT_ID);
			when(alert.getStartTimestamp()).thenReturn(START_TIME);
			when(alert.getStopTimestamp()).thenReturn(STOP_TIME);
			when(alert.getClosingReason()).thenReturn(AlertClosingReason.ALERT_RESOLVED);

			emailAction.onEnding(alertingState);

			ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorHtmlBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captorTextBody = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<List> captorRecipient = ArgumentCaptor.forClass(List.class);
			verify(emailSender, times(1)).sendEMail(captorSubject.capture(), captorHtmlBody.capture(), captorTextBody.capture(), captorRecipient.capture());
			assertThat(captorHtmlBody.getValue(), equalTo(HTML_CLOSE));
			assertThat(captorTextBody.getValue(), equalTo(TXT_CLOSE));
			assertThat(captorSubject.getValue(), not(isEmptyOrNullString()));
			assertThat((List<String>) captorRecipient.getValue(), contains(RECIPIENT));

			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			ArgumentCaptor<AlertEMailTemplateType> templateTypeCaptor = ArgumentCaptor.forClass(AlertEMailTemplateType.class);
			verify(templateManager, times(2)).resolveTemplate(templateTypeCaptor.capture(), propertiesCaptor.capture());

			Map<String, String> propMap = propertiesCaptor.getValue();
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_DEFINITION_NAME), is(ALERTING_NAME));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.MEASUREMENT), is(measurement));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.FIELD), is(field));
			assertThat(NUMBER_FORMAT.parse(propMap.get(AlertEMailTemplateType.Placeholders.THRESHOLD)).doubleValue(), is(THRESHOLD));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.APPLICATION_NAME), isEmptyOrNullString());
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.BUSINESS_TX_NAME), isEmptyOrNullString());
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.ALERT_ID), is(ALERT_ID));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.END_TIME), not(isEmptyOrNullString()));
			assertThat(propMap.get(AlertEMailTemplateType.Placeholders.CLOSING_REASON), is(AlertClosingReason.ALERT_RESOLVED.toString()));
		}
	}
}
