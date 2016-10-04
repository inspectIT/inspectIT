package rocks.inspectit.server.influx.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;
import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

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
		private static final long START_TIME = 12345678L;
		private static final long STOP_TIME = 123456789L;
		private static final String ALERT_DEF_NAME = "MyAlert";
		private static final double THRESHOLD = 100.1;

		@BeforeMethod
		public void init() {
			alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(Series.BusinessTransaction.NAME);
			alertingDefinition.setField(Series.BusinessTransaction.FIELD_DURATION);
			alertingDefinition.setThreshold(THRESHOLD);
			alertingDefinition.setName(ALERT_DEF_NAME);
		}

		@Test
		public void queryForNotFinalized() {
			String query = InfluxQueryFactory.buildTraceIdForAlertQuery(new Alert(alertingDefinition, START_TIME));

			assertThat(query,
					is("SELECT \"" + Series.BusinessTransaction.FIELD_TRACE_ID + "\" FROM \"" + Series.BusinessTransaction.NAME + "\" WHERE time >= " + START_TIME + "ms AND \"duration\" >= "
							+ THRESHOLD));

		}

		@Test
		public void queryForFinalized() {
			String query = InfluxQueryFactory.buildTraceIdForAlertQuery(new Alert(alertingDefinition, START_TIME, STOP_TIME));

			assertThat(query, is("SELECT \"" + Series.BusinessTransaction.FIELD_TRACE_ID + "\" FROM \"" + Series.BusinessTransaction.NAME + "\" WHERE time >= " + START_TIME + "ms AND time < "
					+ STOP_TIME + "ms AND \"duration\" >= " + THRESHOLD));

		}

		@Test
		public void queryWithTags() throws BusinessException {
			String key1 = "keyOne";
			String key2 = "keyTwo";
			String value1 = "valueOne";
			String value2 = "valueTwo";
			alertingDefinition.putTag(key1, value1);
			alertingDefinition.putTag(key2, value2);

			String query = InfluxQueryFactory.buildTraceIdForAlertQuery(new Alert(alertingDefinition, START_TIME));

			assertThat(query, containsString("SELECT \"" + Series.BusinessTransaction.FIELD_TRACE_ID + "\" FROM \"" + Series.BusinessTransaction.NAME + "\""));
			assertThat(query, containsString("time >= " + START_TIME));
			assertThat(query, not(containsString("time < " + STOP_TIME)));
			assertThat(query, containsString("\"duration\" >= " + THRESHOLD));
			assertThat(query, containsString("\"" + key1 + "\" = '" + value1 + "'"));
			assertThat(query, containsString("\"" + key2 + "\" = '" + value2 + "'"));
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void queryForInvalidAlertId() {
			InfluxQueryFactory.buildTraceIdForAlertQuery(null);
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
		private static final String FIELD = "testField";
		private static final String MEASUREMENT = "testMeasurement";
		private static final String TAG_KEY = "tagKey";
		private static final String TAG_VALUE = "tagVal";
		private static final long TIMERANGE = 10;
		private static final long CURRENT_TIME = System.currentTimeMillis();
		private static final long LAST_CHECK_TIME = CURRENT_TIME - TIMERANGE;
		AlertingDefinition alertingDefinition;
		AlertingState alertingState;

		@BeforeMethod
		public void initAlertingState() throws BusinessException {
			alertingDefinition = new AlertingDefinition();
			alertingDefinition.setMeasurement(MEASUREMENT);
			alertingDefinition.setField(FIELD);
			alertingDefinition.putTag(TAG_KEY, TAG_VALUE);
			alertingDefinition.setTimeRange(TIMERANGE, TimeUnit.MINUTES);
			alertingState = new AlertingState(alertingDefinition);
			alertingState.setLastCheckTime(LAST_CHECK_TIME);
		}

		@Test
		public void buildQueryStringUpper() throws BusinessException, Exception {
			alertingDefinition.setThresholdType(ThresholdType.UPPER_THRESHOLD);

			String query = InfluxQueryFactory.buildThresholdCheckForAlertingStateQuery(alertingState, CURRENT_TIME);

			assertThat(query, equalTo("SELECT MAX(\"" + FIELD + "\") FROM \"" + MEASUREMENT + "\" WHERE \"" + TAG_KEY + "\" = '" + TAG_VALUE + "' AND time <= " + CURRENT_TIME + "ms" + " AND time > "
					+ (LAST_CHECK_TIME - alertingDefinition.getTimeRange(TimeUnit.MILLISECONDS)) + "ms"));
		}

		@Test
		public void buildQueryStringLower() throws BusinessException, Exception {
			alertingDefinition.setThresholdType(ThresholdType.LOWER_THRESHOLD);

			String query = InfluxQueryFactory.buildThresholdCheckForAlertingStateQuery(alertingState, CURRENT_TIME);

			assertThat(query, is("SELECT MIN(\"" + FIELD + "\") FROM \"" + MEASUREMENT + "\" WHERE \"" + TAG_KEY + "\" = '" + TAG_VALUE + "' AND time <= " + CURRENT_TIME + "ms" + " AND time > "
					+ (LAST_CHECK_TIME - alertingDefinition.getTimeRange(TimeUnit.MILLISECONDS)) + "ms"));
		}

		@Test
		public void buildQueryStringMultipleTags() throws BusinessException, Exception {
			String tagKey2 = "tagKey2";
			String tagValue2 = "tagVal2";
			alertingDefinition.setThresholdType(ThresholdType.UPPER_THRESHOLD);
			alertingDefinition.putTag(tagKey2, tagValue2);

			String query = InfluxQueryFactory.buildThresholdCheckForAlertingStateQuery(alertingState, CURRENT_TIME);

			assertThat(query, is("SELECT MAX(\"" + FIELD + "\") FROM \"" + MEASUREMENT + "\" WHERE \"" + TAG_KEY + "\" = '" + TAG_VALUE + "' AND \"" + tagKey2 + "\" = '" + tagValue2 + "' AND time <= "
					+ CURRENT_TIME + "ms AND time > " + (LAST_CHECK_TIME - alertingDefinition.getTimeRange(TimeUnit.MILLISECONDS)) + "ms"));

		}
	}
}
