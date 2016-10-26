package rocks.inspectit.server.alerting.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

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

			boolean result = AlertingUtils.isBusinessTransactionAlert(alertingDefinition);

			assertTrue(result);
		}

		@Test
		public void wrongMeasurement() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement("measurement");
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);

			boolean result = AlertingUtils.isBusinessTransactionAlert(alertingDefinition);

			assertFalse(result);
		}

		@Test
		public void wrongField() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField("field");

			boolean result = AlertingUtils.isBusinessTransactionAlert(alertingDefinition);

			assertFalse(result);
		}

		@Test
		public void wrongFieldAndMeasurement() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement("measurement");
			alertingDefinition.setField("field");

			boolean result = AlertingUtils.isBusinessTransactionAlert(alertingDefinition);

			assertFalse(result);
		}
	}

	/**
	 * Tests the {@link AlertingUtils#retrieveApplicaitonName(AlertingDefinition)} method.
	 */
	public static class RetrieveApplicaitonName extends AlertingUtilsTest {

		@Test
		public void successful() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			alertingDefinition.putTag(Series.BusinessTransaction.TAG_APPLICATION_NAME, "appName");

			String result = AlertingUtils.retrieveApplicaitonName(alertingDefinition);

			assertThat(result, is("appName"));
		}

		@Test
		public void noNameAvailable() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);

			String result = AlertingUtils.retrieveApplicaitonName(alertingDefinition);

			assertThat(result, is(nullValue()));
		}

		@Test
		public void noBusinessTransaction() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.putTag(Series.BusinessTransaction.TAG_APPLICATION_NAME, "appName");

			String result = AlertingUtils.retrieveApplicaitonName(alertingDefinition);

			assertThat(result, is(nullValue()));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingDefinitionIsNull() {
			AlertingUtils.retrieveApplicaitonName(null);
		}
	}

	/**
	 * Tests the {@link AlertingUtils#retrieveBusinessTransactionName(AlertingDefinition)} method.
	 */
	public static class RetrieveBusinessTransactionName extends AlertingUtilsTest {

		@Test
		public void successful() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			alertingDefinition.putTag(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, "btxName");

			String result = AlertingUtils.retrieveBusinessTransactionName(alertingDefinition);

			assertThat(result, is("btxName"));
		}

		@Test
		public void noNameAvailable() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);

			String result = AlertingUtils.retrieveBusinessTransactionName(alertingDefinition);

			assertThat(result, is(nullValue()));
		}

		@Test
		public void noBusinessTransaction() {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.putTag(Series.BusinessTransaction.TAG_BUSINESS_TRANSACTION_NAME, "btxName");

			String result = AlertingUtils.retrieveBusinessTransactionName(alertingDefinition);

			assertThat(result, is(nullValue()));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void alertingDefinitionIsNull() {
			AlertingUtils.retrieveBusinessTransactionName(null);
		}
	}

}
