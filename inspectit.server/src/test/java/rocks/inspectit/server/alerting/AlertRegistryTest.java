package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Tests the {@link AlertRegistry}.
 *
 * @author Alexander Wert
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AlertRegistryTest extends TestBase {

	@InjectMocks
	AlertRegistry alertRegistry;

	@Mock
	Logger log;

	/**
	 * Tests the
	 * {@link AlertRegistry#registerAlert(rocks.inspectit.shared.cs.ci.AlertingDefinition, long)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class RegisterAlert extends AlertRegistryTest {

		@Test
		public void registerSuccessful() throws Exception {
			Alert testAlert = Mockito.mock(Alert.class);
			Mockito.when(testAlert.getAlertingDefinition()).thenReturn(Mockito.mock(AlertingDefinition.class));
			Mockito.when(testAlert.getId()).thenReturn("id");

			alertRegistry.registerAlert(testAlert);

			assertThat(alertRegistry.getAlert("id"), equalTo(testAlert));
		}

		@Test(expectedExceptions = { IllegalArgumentException.class })
		public void alertIdIsNull() throws Exception {
			Alert testAlert = Mockito.mock(Alert.class);
			Mockito.when(testAlert.getAlertingDefinition()).thenReturn(Mockito.mock(AlertingDefinition.class));

			alertRegistry.registerAlert(testAlert);
		}

		@Test(expectedExceptions = { IllegalArgumentException.class })
		public void alertAlertingDefinitionIsNull() throws Exception {
			Alert testAlert = Mockito.mock(Alert.class);
			Mockito.when(testAlert.getId()).thenReturn("id");

			alertRegistry.registerAlert(testAlert);
		}

		@Test(expectedExceptions = { IllegalArgumentException.class })
		public void nullAlert() {
			Alert alert = null;

			alertRegistry.registerAlert(alert);
		}

		@Test
		public void replaceAlert() throws Exception {
			Alert alertOne = Mockito.mock(Alert.class);
			Mockito.when(alertOne.getId()).thenReturn("id");
			Mockito.when(alertOne.getAlertingDefinition()).thenReturn(Mockito.mock(AlertingDefinition.class));
			Alert alertTwo = Mockito.mock(Alert.class);
			Mockito.when(alertTwo.getId()).thenReturn("id");
			Mockito.when(alertTwo.getAlertingDefinition()).thenReturn(Mockito.mock(AlertingDefinition.class));

			alertRegistry.registerAlert(alertOne);
			alertRegistry.registerAlert(alertTwo);

			assertThat(alertRegistry.getAlert("id"), equalTo(alertTwo));
		}
	}

	/**
	 * Tests the {@link AlertRegistry#getAlert(String)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class GetAlert extends AlertRegistryTest {

		@Test
		public void getAlert() {
			Alert testAlert = Mockito.mock(Alert.class);
			Mockito.when(testAlert.getId()).thenReturn("id");
			Mockito.when(testAlert.getAlertingDefinition()).thenReturn(Mockito.mock(AlertingDefinition.class));
			alertRegistry.registerAlert(testAlert);

			Alert alert = alertRegistry.getAlert("id");

			assertThat(alert, equalTo(testAlert));
		}

		@Test
		public void getUnknownAlert() {
			Alert alert = alertRegistry.getAlert("unknown-id");

			assertThat(alert, is(nullValue()));
		}

		@Test
		public void getByNull() {
			Alert alert = alertRegistry.getAlert(null);

			assertThat(alert, is(nullValue()));
		}
	}

	/**
	 * Tests the {@link AlertRegistry#getAlerts()} method.
	 */
	public static class GetAlerts extends AlertRegistryTest {

		@Test
		public void getAlerts() {
			Alert alertOne = Mockito.mock(Alert.class);
			Alert alertTwo = Mockito.mock(Alert.class);
			Mockito.when(alertOne.getId()).thenReturn("id_1");
			Mockito.when(alertTwo.getId()).thenReturn("id_2");
			Mockito.when(alertOne.getAlertingDefinition()).thenReturn(Mockito.mock(AlertingDefinition.class));
			Mockito.when(alertTwo.getAlertingDefinition()).thenReturn(Mockito.mock(AlertingDefinition.class));
			alertRegistry.registerAlert(alertOne);
			alertRegistry.registerAlert(alertTwo);

			List<Alert> alerts = alertRegistry.getAlerts();

			assertThat(alerts, hasSize(2));
			assertThat(alerts, hasItems(alertOne, alertTwo));
		}
	}

	/**
	 * Tests the {@link AlertRegistry#getBusinessTransactionAlerts()} method.
	 */
	public static class GetBusinessTransactionAlerts extends AlertRegistryTest {

		@Test
		public void getOnlyBTAlerts() {
			AlertingDefinition definitionOne = Mockito.mock(AlertingDefinition.class);
			AlertingDefinition definitionTwo = Mockito.mock(AlertingDefinition.class);
			Mockito.when(definitionOne.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			Mockito.when(definitionOne.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);
			Alert alertOne = Mockito.mock(Alert.class);
			Alert alertTwo = Mockito.mock(Alert.class);
			Mockito.when(alertOne.getId()).thenReturn("id_1");
			Mockito.when(alertTwo.getId()).thenReturn("id_2");
			Mockito.when(alertOne.getAlertingDefinition()).thenReturn(definitionOne);
			Mockito.when(alertTwo.getAlertingDefinition()).thenReturn(definitionTwo);

			alertRegistry.registerAlert(alertOne);
			alertRegistry.registerAlert(alertTwo);

			List<Alert> alerts = alertRegistry.getBusinessTransactionAlerts();

			assertThat(alerts, hasSize(1));
			assertThat(alerts, hasItems(alertOne));
		}
	}
}
