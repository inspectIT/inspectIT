package rocks.inspectit.server.diagnosis.service.aggregation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.sql.Timestamp;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;

/**
 * @author Isabel Vico Peinado
 *
 */
public class DiagnosisDataAggregatorTest extends TestBase {

	public class GetAggreatedDiagnosisData extends DiagnosisDataAggregatorTest {

		AggregatedDiagnosisData aggregatedDiagnosisData;

		@Test
		private void MusteReturnAnInstanceWithHttpSourceTypeDataIfTheTimerDataIsHttpTimerData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);
			HttpTimerData timerData = new HttpTimerData();
			invocationSequenceData.setTimerData(timerData);

			aggregatedDiagnosisData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(invocationSequenceData);

			assertThat("The object must have HTTP as source type", aggregatedDiagnosisData.getSourceType(), is(SourceType.HTTP));
		}

		@Test
		private void MusteReturnAnInstanceWithTimerDataSourceTypeDataIfTheTimerDataIsTimerData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);
			TimerData timerData = new TimerData();
			invocationSequenceData.setTimerData(timerData);

			aggregatedDiagnosisData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(invocationSequenceData);

			assertThat("The object must have TIMERDATA as source type", aggregatedDiagnosisData.getSourceType(), is(SourceType.TIMERDATA));
		}

		@Test
		private void MusteReturnAnInstanceWithDataBaseSourceTypeDataIfHasSqlData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);
			SqlStatementData sqlStatementData = new SqlStatementData(new Timestamp(10), 10, 10, 108L);
			sqlStatementData.setCount(1);
			sqlStatementData.setSql("blahblahblah");
			invocationSequenceData.setSqlStatementData(sqlStatementData);

			aggregatedDiagnosisData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(invocationSequenceData);

			assertThat("The object must have DATABASE as source type", aggregatedDiagnosisData.getSourceType(), is(SourceType.DATABASE));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		private void MusteReturnAnExceptionIfThereIsNoTimerData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);

			aggregatedDiagnosisData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(invocationSequenceData);
		}

		@Test
		private void MustReturnAnInstanceWithDataBaseSourceTypeDataIfHasSqlDataAndTimerData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);
			SqlStatementData sqlStatementData = new SqlStatementData(new Timestamp(10), 10, 10, 108L);
			sqlStatementData.setCount(1);
			sqlStatementData.setSql("blahblahblah");
			invocationSequenceData.setSqlStatementData(sqlStatementData);
			TimerData timerData = new TimerData(new Timestamp(10), 10L, 20, 108L);
			invocationSequenceData.setTimerData(timerData);

			aggregatedDiagnosisData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(invocationSequenceData);

			assertThat("The object must have DATABASE as source type", aggregatedDiagnosisData.getSourceType(), is(SourceType.DATABASE));
		}
	}

	@SuppressWarnings("unchecked")
	public class GetAggregationKey extends DiagnosisDataAggregatorTest {

		@Test
		private void MusteReturnAnObjectWithAPairLongStringIfTheTimerDataHasSqlData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);
			SqlStatementData sqlStatementData = new SqlStatementData(new Timestamp(10), 10, 10, 108L);
			sqlStatementData.setCount(1);
			sqlStatementData.setSql("blahblahblah");
			invocationSequenceData.setSqlStatementData(sqlStatementData);
			TimerData timerData = new TimerData();
			invocationSequenceData.setTimerData(timerData);

			Pair<Long, String> aggregationKey = (Pair<Long, String>) DiagnosisDataAggregator.getInstance().getAggregationKey(invocationSequenceData);

			assertThat("The string of the pair must be the sql data", aggregationKey.getSecond(), is(sqlStatementData.getSql()));
		}

		@Test
		private void MusteReturnAnObjectWithAPairLongStringIfTheTimerDataHasHttpTimerData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);
			HttpTimerData timerData = new HttpTimerData(new Timestamp(10), 10, 10, 108L);
			HttpInfo httpInfo = new HttpInfo("URI", "requestMethod", "headerValue");
			timerData.setHttpInfo(httpInfo);
			invocationSequenceData.setTimerData(timerData);

			Pair<Long, String> aggregationKey = (Pair<Long, String>) DiagnosisDataAggregator.getInstance().getAggregationKey(invocationSequenceData);

			assertThat("The string of the pair must be the sql data", aggregationKey.getSecond(), is(timerData.getHttpInfo().getUri()));
		}

		@Test
		private void MusteReturnAnObjectWithALongStringIfTheTimerDataHasNotHttpTimerDataOrSqlData() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 108L);
			TimerData timerData = new TimerData();
			invocationSequenceData.setTimerData(timerData);

			long aggregationKey = (long) DiagnosisDataAggregator.getInstance().getAggregationKey(invocationSequenceData);

			assertThat("The returned object must be the method ident of the invocationsequencedata", aggregationKey, is(invocationSequenceData.getMethodIdent()));
		}
	}

}
