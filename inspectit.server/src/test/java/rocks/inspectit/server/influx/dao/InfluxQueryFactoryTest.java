package rocks.inspectit.server.influx.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.Alert;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;

/**
 * Tests the {@link InfluxQueryFactory}.
 *
 * @author Alexander Wert
 *
 */
public class InfluxQueryFactoryTest extends TestBase {

	/**
	 * Tests the {@link InfluxQueryFactory#buildTraceIdForAlertQuery(Alert, long)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class BuildTraceIdForAlertQuery extends InfluxQueryFactoryTest {
		AlertingDefinition alertingDefinition;
		String alertId;
		long time = 12345678L;
		long stopTime = 123456789L;
		String alertDefName = "MyAlert";
		double threshold = 100.1;
		long agentId = 123;

		@BeforeMethod
		public void init() {
			alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			alertingDefinition.setThreshold(threshold);
			alertingDefinition.setName(alertDefName);
		}

		@Test
		public void queryForNotFinalized() {
			String query = InfluxQueryFactory.buildTraceIdForAlertQuery(new Alert(alertingDefinition, time), agentId);

			assertThat(query, containsString("SELECT \"" + Series.BusinessTransaction.FIELD_TRACE_ID + "\" FROM \"" + Series.BusinessTransaction.NAME + "\""));
			assertThat(query, containsString("\"agentId\" = '" + agentId + "'"));
			assertThat(query, containsString("time >= " + time));
			assertThat(query, containsString("\"duration\" >= " + threshold));
			assertThat(query, not(containsString("time < " + stopTime)));
		}

		@Test
		public void queryForFinalized() {
			String query = InfluxQueryFactory.buildTraceIdForAlertQuery(new Alert(alertingDefinition, time, stopTime), agentId);

			assertThat(query, containsString("SELECT \"" + Series.BusinessTransaction.FIELD_TRACE_ID + "\" FROM \"" + Series.BusinessTransaction.NAME + "\""));
			assertThat(query, containsString("\"agentId\" = '" + agentId + "'"));
			assertThat(query, containsString("time >= " + time));
			assertThat(query, containsString("\"duration\" >= " + threshold));
			assertThat(query, containsString("time < " + stopTime));
		}

		@Test
		public void queryWithTags() throws BusinessException {
			String key1 = "keyOne";
			String key2 = "keyTwo";
			String value1 = "valueOne";
			String value2 = "valueTwo";
			alertingDefinition.putTag(key1, value1);
			alertingDefinition.putTag(key2, value2);

			String query = InfluxQueryFactory.buildTraceIdForAlertQuery(new Alert(alertingDefinition, time), agentId);

			assertThat(query, containsString("SELECT \"" + Series.BusinessTransaction.FIELD_TRACE_ID + "\" FROM \"" + Series.BusinessTransaction.NAME + "\""));
			assertThat(query, containsString("\"agentId\" = '" + agentId + "'"));
			assertThat(query, containsString("time >= " + time));
			assertThat(query, not(containsString("time < " + stopTime)));
			assertThat(query, containsString("\"duration\" >= " + threshold));
			assertThat(query, containsString("\"" + key1 + "\" = '" + value1 + "'"));
			assertThat(query, containsString("\"" + key2 + "\" = '" + value2 + "'"));
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void queryForInvalidAlertId() {
			InfluxQueryFactory.buildTraceIdForAlertQuery(null, agentId);
		}
	}

	/**
	 * Tests the
	 * {@link InfluxQueryFactory#buildThresholdCheckForAlertingStateQuery(rocks.inspectit.server.alerting.state.AlertingState, long)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class BuildThresholdCheckForAlertingStateQuery extends InfluxQueryFactoryTest {
		String field = "testField";
		String measurement = "testMeasurement";
		String tagKey = "tagKey";
		String tagValue = "tagVal";
		int timerange = 10;
		long currentTime = System.currentTimeMillis();
		long lastCheckTime = currentTime - timerange;
		AlertingDefinition alertingDefinition;
		AlertingState alertingState;

		@BeforeMethod
		public void initAlertingState() throws BusinessException {
			alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(measurement);
			alertingDefinition.setField(field);
			alertingDefinition.putTag(tagKey, tagValue);
			alertingDefinition.setTimerange(timerange);
			alertingState = new AlertingState(alertingDefinition);
			alertingState.setLastCheckTime(lastCheckTime);
		}

		@Test
		public void buildQueryStringUpper() throws BusinessException, Exception {
			alertingDefinition.setThresholdType(ThresholdType.UPPER_THRESHOLD);

			String query = InfluxQueryFactory.buildThresholdCheckForAlertingStateQuery(alertingState, currentTime);

			assertThat(query, equalTo("SELECT MAX(\"" + field + "\") FROM \"" + measurement + "\" WHERE \"" + tagKey + "\" = '" + tagValue + "' AND time <= " + currentTime + "ms" + " AND time > "
					+ lastCheckTime + "ms"));
		}

		@Test
		public void buildQueryStringLower() throws BusinessException, Exception {
			alertingDefinition.setThresholdType(ThresholdType.LOWER_THRESHOLD);

			String query = InfluxQueryFactory.buildThresholdCheckForAlertingStateQuery(alertingState, currentTime);

			assertThat(query, equalTo("SELECT MIN(\"" + field + "\") FROM \"" + measurement + "\" WHERE \"" + tagKey + "\" = '" + tagValue + "' AND time <= " + currentTime + "ms" + " AND time > "
					+ lastCheckTime + "ms"));
		}

		@Test
		public void buildQueryStringMultipleTags() throws BusinessException, Exception {
			String tagKey2 = "tagKey2";
			String tagValue2 = "tagVal2";
			alertingDefinition.setThresholdType(ThresholdType.UPPER_THRESHOLD);
			alertingDefinition.putTag(tagKey2, tagValue2);

			String query = InfluxQueryFactory.buildThresholdCheckForAlertingStateQuery(alertingState, currentTime);

			assertThat(query, equalTo("SELECT MAX(\"" + field + "\") FROM \"" + measurement + "\" WHERE \"" + tagKey + "\" = '" + tagValue + "' AND \"" + tagKey2 + "\" = '" + tagValue2
					+ "' AND time <= " + currentTime + "ms" + " AND time > " + lastCheckTime + "ms"));
		}
	}
}
