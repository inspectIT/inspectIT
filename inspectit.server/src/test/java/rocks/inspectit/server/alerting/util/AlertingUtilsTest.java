package rocks.inspectit.server.alerting.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;

/**
 * Testing the functionality of {@link AlertingUtils}.
 *
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
public class AlertingUtilsTest extends TestBase {

	/**
	 * Tests the {@link AlertingUtils#isBusinessTransactionAlert(AlertingDefinition)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class IsBusinessTransactionAlert extends AlertingUtilsTest {

		@Test
		public void successful() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			Assert.assertTrue(AlertingUtils.isBusinessTransactionAlert(alertingDefinition));
		}

		@Test
		public void wrongMeasurement() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement("wrong");
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			Assert.assertFalse(AlertingUtils.isBusinessTransactionAlert(alertingDefinition));
		}

		@Test
		public void wrongField() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField("wrong");
			Assert.assertFalse(AlertingUtils.isBusinessTransactionAlert(alertingDefinition));
		}
	}
}
