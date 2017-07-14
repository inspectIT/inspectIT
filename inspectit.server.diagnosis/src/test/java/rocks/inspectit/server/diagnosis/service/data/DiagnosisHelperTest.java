package rocks.inspectit.server.diagnosis.service.data;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.sql.Timestamp;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Isabel Vico Peinado
 *
 */
public class DiagnosisHelperTest extends TestBase {

	public class getExclusiveDuration extends DiagnosisHelperTest {

		@Test
		public void getExclusiveDurationMustReturnZeroIfTheInvocationHasNotTimerDataNeitherSqlData() {
			InvocationSequenceData invocationData = new InvocationSequenceData();

			double exclusiveDuration = DiagnosisHelper.getExclusiveDuration(invocationData);

			assertThat("Exclusive duration must be 0.", exclusiveDuration, is(0d));
		}

		@Test
		public void getExclusiveDurationMustReturnZeroIfTheExclusiveTimeIsNotAvailable() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			invocationData.setTimerData(timerData);

			double exclusiveDuration = DiagnosisHelper.getExclusiveDuration(invocationData);

			assertThat("Exclusive duration must be 0.", exclusiveDuration, is(timerData.getExclusiveDuration()));
		}

		@Test
		public void getExclusiveDurationMustReturnTheExclusiveDurationOfTheTimerDataWhenItIsARegularTimerData() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerData.calculateExclusiveMin(300d);
			timerData.setExclusiveDuration(300d);
			invocationData.setTimerData(timerData);

			double exclusiveDuration = DiagnosisHelper.getExclusiveDuration(invocationData);

			assertThat("Exclusive duration must be 300.", exclusiveDuration, is(timerData.getExclusiveDuration()));
		}

		@Test
		public void getExclusiveDurationMustReturnTheExclusiveDurationOfTheTimerDataWhenItIsASqlData() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			SqlStatementData timerData = new SqlStatementData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerData.calculateExclusiveMin(300d);
			timerData.setExclusiveDuration(300d);
			invocationData.setSqlStatementData(timerData);

			double exclusiveDuration = DiagnosisHelper.getExclusiveDuration(invocationData);

			assertThat("Exclusive duration must be 300.", exclusiveDuration, is(timerData.getExclusiveDuration()));
		}
	}
}
