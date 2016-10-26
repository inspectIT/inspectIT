package rocks.inspectit.server.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.template.AlertEMailTemplateType.Placeholders;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;
import rocks.inspectit.shared.cs.communication.data.cmr.AlertClosingReason;

/**
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class EMailTemplateResolverTest extends TestBase {

	@InjectMocks
	EMailTemplateResolver resolver;

	@Mock
	TemplateManager templateManager;

	public static class ResolveTemplate extends EMailTemplateResolverTest {

		@Mock
		AlertingState alertingState;

		@Mock
		AlertingDefinition alertingDefinition;

		@Mock
		Alert alert;

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void isBusinessTransaction() throws IOException, ParseException {
			NumberFormat numberFormat = new DecimalFormat("0.0#");
			ImmutableMap<String, String> tags = ImmutableMap.of(Series.BusinessTransaction.TAG_APPLICATION_NAME, "appName", Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, "btName");
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(100D);
			when(alertingDefinition.getName()).thenReturn("name");
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			when(alertingDefinition.getThreshold()).thenReturn(200D);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(alert.getId()).thenReturn("id");
			when(alert.getStartTimestamp()).thenReturn(10L);
			when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), any(Map.class))).thenReturn("tplt");

			String result = resolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);

			assertThat(result, is("tplt"));
			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			verify(templateManager).resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), propertiesCaptor.capture());
			verifyNoMoreInteractions(templateManager);
			verify(alertingState).getAlertingDefinition();
			verify(alertingState).getAlert();
			verify(alertingState, times(2)).getExtremeValue();
			verifyNoMoreInteractions(alertingState);
			verify(alertingDefinition).getName();
			verify(alertingDefinition, times(4)).getMeasurement();
			verify(alertingDefinition, times(4)).getField();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition, times(3)).getTags();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alert).getStartTimestamp();
			verify(alert).getStopTimestamp();
			verify(alert).getId();
			verifyNoMoreInteractions(alert);
			//
			Map<String, String> map = propertiesCaptor.getValue();
			assertThat(map.get(Placeholders.ALERT_DEFINITION_NAME), is("name"));
			assertThat(map.get(Placeholders.MEASUREMENT), is(Series.BusinessTransaction.NAME));
			assertThat(map.get(Placeholders.FIELD), is(Series.BusinessTransaction.FIELD_DURATION));
			assertThat(numberFormat.parse(map.get(Placeholders.THRESHOLD)).doubleValue(), is(200D));
			assertThat(map.get(Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(numberFormat.parse(map.get(Placeholders.VIOLATION_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.CURRENT_TIME), not(isEmptyOrNullString()));
			assertThat(map.get(Placeholders.ALERT_ID), is("id"));
			assertThat(numberFormat.parse(map.get(Placeholders.EXTREME_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.APPLICATION_NAME), is("appName"));
			assertThat(map.get(Placeholders.BUSINESS_TX_NAME), is("btName"));
			assertThat(map.get(Placeholders.TAGS), containsString(Series.BusinessTransaction.TAG_APPLICATION_NAME + "=appName"));
			assertThat(map.get(Placeholders.TAGS), containsString(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME + "=btName"));
			assertThat(map.entrySet(), hasSize(12));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void isBusinessTransactionNoApplicationAndBTName() throws IOException, ParseException {
			NumberFormat numberFormat = new DecimalFormat("0.0#");
			ImmutableMap<String, String> tags = ImmutableMap.of("k1", "appName", "k2", "btName");
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(100D);
			when(alertingDefinition.getName()).thenReturn("name");
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			when(alertingDefinition.getThreshold()).thenReturn(200D);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(alert.getId()).thenReturn("id");
			when(alert.getStartTimestamp()).thenReturn(10L);
			when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), any(Map.class))).thenReturn("tplt");

			String result = resolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);

			assertThat(result, is("tplt"));
			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			verify(templateManager).resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), propertiesCaptor.capture());
			verifyNoMoreInteractions(templateManager);
			verify(alertingState).getAlertingDefinition();
			verify(alertingState).getAlert();
			verify(alertingState, times(2)).getExtremeValue();
			verifyNoMoreInteractions(alertingState);
			verify(alertingDefinition).getName();
			verify(alertingDefinition, times(4)).getMeasurement();
			verify(alertingDefinition, times(4)).getField();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition, times(3)).getTags();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alert).getStartTimestamp();
			verify(alert).getStopTimestamp();
			verify(alert).getId();
			verifyNoMoreInteractions(alert);
			//
			Map<String, String> map = propertiesCaptor.getValue();
			assertThat(map.get(Placeholders.ALERT_DEFINITION_NAME), is("name"));
			assertThat(map.get(Placeholders.MEASUREMENT), is(Series.BusinessTransaction.NAME));
			assertThat(map.get(Placeholders.FIELD), is(Series.BusinessTransaction.FIELD_DURATION));
			assertThat(numberFormat.parse(map.get(Placeholders.THRESHOLD)).doubleValue(), is(200D));
			assertThat(map.get(Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(numberFormat.parse(map.get(Placeholders.VIOLATION_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.CURRENT_TIME), not(isEmptyOrNullString()));
			assertThat(map.get(Placeholders.ALERT_ID), is("id"));
			assertThat(numberFormat.parse(map.get(Placeholders.EXTREME_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.APPLICATION_NAME), is("All"));
			assertThat(map.get(Placeholders.BUSINESS_TX_NAME), is("All"));
			assertThat(map.get(Placeholders.TAGS), containsString("k1=appName"));
			assertThat(map.get(Placeholders.TAGS), containsString("k2=btName"));
			assertThat(map.entrySet(), hasSize(12));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void noBusinessTransaction() throws IOException, ParseException {
			NumberFormat numberFormat = new DecimalFormat("0.0#");
			ImmutableMap<String, String> tags = ImmutableMap.of("k1", "appName", "k2", "btName");
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(100D);
			when(alertingDefinition.getName()).thenReturn("name");
			when(alertingDefinition.getMeasurement()).thenReturn("measurement");
			when(alertingDefinition.getField()).thenReturn("field");
			when(alertingDefinition.getThreshold()).thenReturn(200D);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(alert.getId()).thenReturn("id");
			when(alert.getStartTimestamp()).thenReturn(10L);
			when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), any(Map.class))).thenReturn("tplt");

			String result = resolver.resolveTemplate(AlertEMailTemplateType.HTML_ALERT_OPEN, alertingState);

			assertThat(result, is("tplt"));
			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			verify(templateManager).resolveTemplate(eq(AlertEMailTemplateType.HTML_ALERT_OPEN), propertiesCaptor.capture());
			verifyNoMoreInteractions(templateManager);
			verify(alertingState).getAlertingDefinition();
			verify(alertingState).getAlert();
			verify(alertingState, times(2)).getExtremeValue();
			verifyNoMoreInteractions(alertingState);
			verify(alertingDefinition).getName();
			verify(alertingDefinition, times(2)).getMeasurement();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition).getTags();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alert).getStartTimestamp();
			verify(alert).getStopTimestamp();
			verify(alert).getId();
			verifyNoMoreInteractions(alert);
			//
			Map<String, String> map = propertiesCaptor.getValue();
			assertThat(map.get(Placeholders.ALERT_DEFINITION_NAME), is("name"));
			assertThat(map.get(Placeholders.MEASUREMENT), is("measurement"));
			assertThat(map.get(Placeholders.FIELD), is("field"));
			assertThat(numberFormat.parse(map.get(Placeholders.THRESHOLD)).doubleValue(), is(200D));
			assertThat(map.get(Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(numberFormat.parse(map.get(Placeholders.VIOLATION_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.CURRENT_TIME), not(isEmptyOrNullString()));
			assertThat(map.get(Placeholders.ALERT_ID), is("id"));
			assertThat(numberFormat.parse(map.get(Placeholders.EXTREME_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.TAGS), containsString("k1=appName"));
			assertThat(map.get(Placeholders.TAGS), containsString("k2=btName"));
			assertThat(map.entrySet(), hasSize(10));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void noBusinessTransactionIsText() throws IOException, ParseException {
			NumberFormat numberFormat = new DecimalFormat("0.0#");
			ImmutableMap<String, String> tags = ImmutableMap.of("k1", "appName", "k2", "btName");
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(100D);
			when(alertingDefinition.getName()).thenReturn("name");
			when(alertingDefinition.getMeasurement()).thenReturn("measurement");
			when(alertingDefinition.getField()).thenReturn("field");
			when(alertingDefinition.getThreshold()).thenReturn(200D);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(alert.getId()).thenReturn("id");
			when(alert.getStartTimestamp()).thenReturn(10L);
			when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_OPEN), any(Map.class))).thenReturn("tplt");

			String result = resolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_OPEN, alertingState);

			assertThat(result, is("tplt"));
			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			verify(templateManager).resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_OPEN), propertiesCaptor.capture());
			verifyNoMoreInteractions(templateManager);
			verify(alertingState).getAlertingDefinition();
			verify(alertingState).getAlert();
			verify(alertingState, times(2)).getExtremeValue();
			verifyNoMoreInteractions(alertingState);
			verify(alertingDefinition).getName();
			verify(alertingDefinition, times(2)).getMeasurement();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition).getTags();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alert).getStartTimestamp();
			verify(alert).getStopTimestamp();
			verify(alert).getId();
			verifyNoMoreInteractions(alert);
			//
			Map<String, String> map = propertiesCaptor.getValue();
			assertThat(map.get(Placeholders.ALERT_DEFINITION_NAME), is("name"));
			assertThat(map.get(Placeholders.MEASUREMENT), is("measurement"));
			assertThat(map.get(Placeholders.FIELD), is("field"));
			assertThat(numberFormat.parse(map.get(Placeholders.THRESHOLD)).doubleValue(), is(200D));
			assertThat(map.get(Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(numberFormat.parse(map.get(Placeholders.VIOLATION_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.CURRENT_TIME), not(isEmptyOrNullString()));
			assertThat(map.get(Placeholders.ALERT_ID), is("id"));
			assertThat(numberFormat.parse(map.get(Placeholders.EXTREME_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.TAGS), containsString("k1: appName"));
			assertThat(map.get(Placeholders.TAGS), containsString("k2: btName"));
			assertThat(map.entrySet(), hasSize(10));
		}

		@Test
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void noBusinessTransactionIsTextHasStopped() throws IOException, ParseException {
			NumberFormat numberFormat = new DecimalFormat("0.0#");
			ImmutableMap<String, String> tags = ImmutableMap.of("k1", "appName", "k2", "btName");
			when(alertingState.getAlertingDefinition()).thenReturn(alertingDefinition);
			when(alertingState.getAlert()).thenReturn(alert);
			when(alertingState.getExtremeValue()).thenReturn(100D);
			when(alertingDefinition.getName()).thenReturn("name");
			when(alertingDefinition.getMeasurement()).thenReturn("measurement");
			when(alertingDefinition.getField()).thenReturn("field");
			when(alertingDefinition.getThreshold()).thenReturn(200D);
			when(alertingDefinition.getTags()).thenReturn(tags);
			when(alertingDefinition.getNotificationEmailAddresses()).thenReturn(Arrays.asList("test@example.com"));
			when(alert.getId()).thenReturn("id");
			when(alert.getStartTimestamp()).thenReturn(10L);
			when(alert.getStopTimestamp()).thenReturn(20L);
			when(alert.getClosingReason()).thenReturn(AlertClosingReason.ALERT_RESOLVED);
			when(templateManager.resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_CLOSED), any(Map.class))).thenReturn("tplt");

			String result = resolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, alertingState);

			assertThat(result, is("tplt"));
			ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
			verify(templateManager).resolveTemplate(eq(AlertEMailTemplateType.TXT_ALERT_CLOSED), propertiesCaptor.capture());
			verifyNoMoreInteractions(templateManager);
			verify(alertingState).getAlertingDefinition();
			verify(alertingState).getAlert();
			verify(alertingState, times(2)).getExtremeValue();
			verifyNoMoreInteractions(alertingState);
			verify(alertingDefinition).getName();
			verify(alertingDefinition, times(2)).getMeasurement();
			verify(alertingDefinition).getField();
			verify(alertingDefinition).getThreshold();
			verify(alertingDefinition).getTags();
			verifyNoMoreInteractions(alertingDefinition);
			verify(alert).getStartTimestamp();
			verify(alert, times(2)).getStopTimestamp();
			verify(alert).getId();
			verify(alert).getClosingReason();
			verifyNoMoreInteractions(alert);
			//
			Map<String, String> map = propertiesCaptor.getValue();
			assertThat(map.get(Placeholders.ALERT_DEFINITION_NAME), is("name"));
			assertThat(map.get(Placeholders.MEASUREMENT), is("measurement"));
			assertThat(map.get(Placeholders.FIELD), is("field"));
			assertThat(numberFormat.parse(map.get(Placeholders.THRESHOLD)).doubleValue(), is(200D));
			assertThat(map.get(Placeholders.START_TIME), not(isEmptyOrNullString()));
			assertThat(numberFormat.parse(map.get(Placeholders.VIOLATION_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.CURRENT_TIME), not(isEmptyOrNullString()));
			assertThat(map.get(Placeholders.ALERT_ID), is("id"));
			assertThat(numberFormat.parse(map.get(Placeholders.EXTREME_VALUE)).doubleValue(), is(100D));
			assertThat(map.get(Placeholders.END_TIME), not(isEmptyOrNullString()));
			assertThat(map.get(Placeholders.CLOSING_REASON), is(AlertClosingReason.ALERT_RESOLVED.toString()));
			assertThat(map.get(Placeholders.TAGS), containsString("k1: appName"));
			assertThat(map.get(Placeholders.TAGS), containsString("k2: btName"));
			assertThat(map.entrySet(), hasSize(12));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingStateIsNull() throws IOException {
			try {
				resolver.resolveTemplate(AlertEMailTemplateType.TXT_ALERT_CLOSED, null);
			} finally {
				verifyZeroInteractions(templateManager);
				verifyZeroInteractions(alertingState);
				verifyZeroInteractions(alertingDefinition);
				verifyZeroInteractions(alert);
			}
		}
	}

}
