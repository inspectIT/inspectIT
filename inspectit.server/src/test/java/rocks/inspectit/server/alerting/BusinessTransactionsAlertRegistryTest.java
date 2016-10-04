package rocks.inspectit.server.alerting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import java.lang.reflect.Field;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.util.FifoMap;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

/**
 * Tests the {@link AlertRegistry}.
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionsAlertRegistryTest extends TestBase {

	@InjectMocks
	AlertRegistry alertRegistry;

	@Mock
	Logger log;

	Alert testAlert = null;
	long time = 12345678L;
	long stopTime = 123456789L;
	String alertDefName = "MyAlert";
	double threshold = 100.1;
	String alertId = "";

	@BeforeMethod
	public void registerAlert() {
		AlertingDefinition alertingDefinition = new AlertingDefinition();
		alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
		alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
		alertingDefinition.setThreshold(threshold);
		alertingDefinition.setName(alertDefName);

		testAlert = new Alert(alertingDefinition, time);

		alertRegistry.registerAlert(testAlert);
		alertId = testAlert.getId();
	}

	/**
	 * Tests the
	 * {@link AlertRegistry#registerAlert(rocks.inspectit.shared.cs.ci.AlertingDefinition, long)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class RegisterAlert extends BusinessTransactionsAlertRegistryTest {

		@Test
		@SuppressWarnings("unchecked")
		public void registerSuccessful() throws Exception {
			alertRegistry.registerAlert(testAlert);

			Field field = AlertRegistry.class.getDeclaredField("registry");
			field.setAccessible(true);
			FifoMap<String, Alert> registry = (FifoMap<String, Alert>) field.get(alertRegistry);

			assertThat(registry.values(), hasItem(testAlert));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void registerNonBusinessTransactionAlert() throws Exception {
			AlertingDefinition definition = new AlertingDefinition();
			definition.setMeasurement("cpu");
			definition.setField("utilization");

			Alert nonBTAlert = new Alert(definition, time);

			alertRegistry.registerAlert(nonBTAlert);

			Field field = AlertRegistry.class.getDeclaredField("registry");
			field.setAccessible(true);
			FifoMap<String, Alert> registry = (FifoMap<String, Alert>) field.get(alertRegistry);

			assertThat(registry.values(), hasItem(nonBTAlert));
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
	public static class GetAlert extends BusinessTransactionsAlertRegistryTest {

		@Test
		public void getCorrect() {
			Alert alert = alertRegistry.getAlert(alertId);
			assertThat(alert.getStartTimestamp(), equalTo(time));
			assertThat(alert.getStopTimestamp(), lessThan(0L));
			assertThat(alert.getAlertingDefinition(), not(equalTo(null)));
			assertThat(alert.getAlertingDefinition().getName(), equalTo(alertDefName));
		}

		@Test
		public void getWrong() {
			Alert alert = alertRegistry.getAlert("invalid-id");
			assertThat(alert, equalTo(null));
		}
	}
}
