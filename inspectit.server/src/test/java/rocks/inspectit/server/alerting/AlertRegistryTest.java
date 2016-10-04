package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
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
 *
 */
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

		@Mock
		Alert testAlert;

		@Mock
		AlertingDefinition alertingDefinition;

		@Test
		public void registerSuccessful() throws Exception {
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);

			alertRegistry.registerAlert(testAlert);

			assertThat(alertRegistry.registry.values(), hasItem(testAlert));
		}

		@Test
		public void registerNonBusinessTransactionAlertWrongField() throws Exception {
			when(alertingDefinition.getMeasurement()).thenReturn(Series.BusinessTransaction.NAME);
			when(alertingDefinition.getField()).thenReturn("utilization");

			alertRegistry.registerAlert(testAlert);

			assertThat(alertRegistry.registry.values(), hasItem(testAlert));
		}

		@Test
		public void registerNonBusinessTransactionAlertWrongMeasurement() throws Exception {
			when(alertingDefinition.getMeasurement()).thenReturn("cpu");
			when(alertingDefinition.getField()).thenReturn(Series.BusinessTransaction.FIELD_DURATION);

			alertRegistry.registerAlert(testAlert);

			assertThat(alertRegistry.registry.values(), hasItem(testAlert));
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void registerNull() {
			alertRegistry.registerAlert(null);
		}
	}

	/**
	 * Tests the {@link AlertRegistry#getAlert(String)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class GetAlert extends AlertRegistryTest {
		private static final String TEST_ID = "xyz";

		@Mock
		Alert testAlert;


		@Test
		public void getCorrect() {
			when(testAlert.getId()).thenReturn(TEST_ID);
			alertRegistry.registerAlert(testAlert);

			Alert alert = alertRegistry.getAlert(TEST_ID);

			assertThat(alert.getId(), equalTo(testAlert.getId()));
		}

		@Test
		public void getWrong() {
			Alert alert = alertRegistry.getAlert("invalid-id");

			assertThat(alert, equalTo(null));
		}
	}
}
