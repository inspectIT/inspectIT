package rocks.inspectit.server.diagnosis.service.aggregation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
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
public class DiagnosisDataAggregationPerformerTest extends TestBase {

	@Mock
	DiagnosisDataAggregationPerformer diagnosisDataAggregationPerformer;

	public class AggregateInvocationSequenceData extends DiagnosisDataAggregationPerformerTest {
		@Mock
		AggregatedDiagnosisData alreadyAggregatedObject;

		@Test
		private void ifTheAggregatedObjectIsNotDefineInTheMapItMustBeAggregated() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 2L);
			diagnosisDataAggregationPerformer = new DiagnosisDataAggregationPerformer();
			TimerData timerData = new TimerData(new Timestamp(10), 10, 10, 108L);
			invocationSequenceData.setTimerData(timerData);

			diagnosisDataAggregationPerformer.aggregateInvocationSequenceData(invocationSequenceData);

			List<AggregatedDiagnosisData> resultList = diagnosisDataAggregationPerformer.getAggregationResultList();
			assertThat("The aggregated object must have the same method ident that the invocationSequenceData", resultList.get(0).getMethodIdent(), equalTo(invocationSequenceData.getMethodIdent()));
		}

		@Test
		private void ifTheAggregatedObjectHasSQLDataAndIsDefinedInTheMapItMustBeAggregatedToTheDiagnosisAggregator() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 2L);
			diagnosisDataAggregationPerformer = new DiagnosisDataAggregationPerformer();
			SqlStatementData sqlStatementData = new SqlStatementData(new Timestamp(10), 10, 10, 108L);
			sqlStatementData.setCount(1);
			sqlStatementData.setSql("blahblahblah");
			invocationSequenceData.setSqlStatementData(sqlStatementData);
			HttpTimerData timerData = new HttpTimerData(new Timestamp(10), 10, 10, 108L);
			invocationSequenceData.setTimerData(timerData);
			Object key = new Pair<Long, String>(invocationSequenceData.getMethodIdent(), invocationSequenceData.getSqlStatementData().getSql());
			diagnosisDataAggregationPerformer.diagnosisDataAggregationMap.put(key, alreadyAggregatedObject);

			diagnosisDataAggregationPerformer.aggregateInvocationSequenceData(invocationSequenceData);

			verify(alreadyAggregatedObject, times(1)).aggregate(invocationSequenceData);
		}

		@Test
		private void ifTheAggregatedObjectHasHttpTimerDataAndIsDefinedInTheMapItMustBeAggregatedToTheDiagnosisAggregator() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 2L);
			diagnosisDataAggregationPerformer = new DiagnosisDataAggregationPerformer();
			HttpTimerData timerData = new HttpTimerData(new Timestamp(10), 10, 10, 108L);
			HttpInfo httpInfo = new HttpInfo("URI", "requestMethod", "headerValue");
			timerData.setHttpInfo(httpInfo);
			invocationSequenceData.setTimerData(timerData);
			Object key = new Pair<Long, String>(invocationSequenceData.getMethodIdent(), ((HttpTimerData) invocationSequenceData.getTimerData()).getHttpInfo().getUri());
			diagnosisDataAggregationPerformer.diagnosisDataAggregationMap.put(key, alreadyAggregatedObject);

			diagnosisDataAggregationPerformer.aggregateInvocationSequenceData(invocationSequenceData);

			verify(alreadyAggregatedObject, times(1)).aggregate(invocationSequenceData);
		}
	}

	public class AggregateInvocationSequenceDataList extends DiagnosisDataAggregationPerformerTest {

		@Test
		private void ifTheAggregatedObjectIsNotDefineInTheMapItMustBeAggregated() {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 2L);
			diagnosisDataAggregationPerformer = new DiagnosisDataAggregationPerformer();
			SqlStatementData sqlStatementData = new SqlStatementData(new Timestamp(10), 10, 10, 108L);
			sqlStatementData.setCount(1);
			sqlStatementData.setSql("blahblahblah");
			invocationSequenceData.setSqlStatementData(sqlStatementData);
			TimerData timerData = new TimerData(new Timestamp(10), 10, 10, 108L);
			invocationSequenceData.setTimerData(timerData);
			Object key = new Pair<Long, String>(invocationSequenceData.getMethodIdent(), invocationSequenceData.getSqlStatementData().getSql());
			AggregatedDiagnosisData alreadyAggregatedObject = new AggregatedDiagnosisData(SourceType.TIMERDATA, invocationSequenceData, key);
			diagnosisDataAggregationPerformer.diagnosisDataAggregationMap.put(key, alreadyAggregatedObject);
			InvocationSequenceData secondInvocationSequenceData = new InvocationSequenceData(new Timestamp(10L), 10L, 20L, 2L);
			secondInvocationSequenceData.setTimerData(timerData);
			List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<>();
			invocationSequenceDataList.add(invocationSequenceData);
			invocationSequenceDataList.add(secondInvocationSequenceData);

			diagnosisDataAggregationPerformer.aggregateInvocationSequenceDataList(invocationSequenceDataList);

			List<AggregatedDiagnosisData> resultList = diagnosisDataAggregationPerformer.getAggregationResultList();
			assertThat("The list must have 2 aggregated objects", resultList.size(), is(2));
			assertThat("The first aggregated object must have the same method ident that the invocationSequenceData", resultList.get(0).getMethodIdent(), equalTo(invocationSequenceData.getMethodIdent()));
			assertThat("The second aggregated object must have the same method ident that the secondInvocationSequenceData", resultList.get(1).getMethodIdent(),
					equalTo(secondInvocationSequenceData.getMethodIdent()));
		}
	}

}
