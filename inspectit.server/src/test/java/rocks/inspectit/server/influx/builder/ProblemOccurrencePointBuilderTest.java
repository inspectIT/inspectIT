package rocks.inspectit.server.influx.builder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Date;

import org.influxdb.dto.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.communication.data.diagnosis.AggregatedDiagnosisTimerData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.RootCause;

/**
 * @author Isabel Vico Peinado
 *
 */
public class ProblemOccurrencePointBuilderTest extends TestBase {

	@InjectMocks
	ProblemOccurrencePointBuilder problemOccurrencePointBuilder;

	@Mock
	ICachedDataService cachedDataService;

	@Mock
	InfluxDBDao influxDBDao;

	public static class SaveProblemOccurrenceToInflux extends ProblemOccurrencePointBuilderTest {

		@Test
		public void mustCallToInsertIntoInfluxOnce() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			long methodIdent = 1L;
			int applicationId = 123;
			int businessTxId = 456;
			Timestamp defDate = new Timestamp(new Date().getTime());
			AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData = new AggregatedDiagnosisTimerData(new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L));
			InvocationSequenceData requestRoot = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerData.setDuration(100);
			timerData.setExclusiveDuration(100);
			timerData.setExclusiveCount(1);
			requestRoot.setTimerData(timerData);
			InvocationSequenceData globalContext = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			globalContext.setTimerData(timerData);
			InvocationSequenceData problemContext = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			problemContext.setTimerData(timerData);
			RootCause rootCause = new RootCause(methodIdent, aggregatedDiagnosisTimerData);
			ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, CauseType.SINGLE, SourceType.TIMERDATA);
			problemOccurrence.setApplicationNameIdent(applicationId);
			problemOccurrence.setBusinessTransactionNameIdent(businessTxId);
			ApplicationData applicationData = new ApplicationData(applicationId, applicationId, "firstApplication");
			when(cachedDataService.getApplicationForId(problemOccurrence.getApplicationNameIdent())).thenReturn(applicationData);
			BusinessTransactionData businessTx = new BusinessTransactionData(businessTxId, businessTxId, applicationData, "businessTx");
			when(cachedDataService.getBusinessTransactionForId(applicationId, businessTxId)).thenReturn(businessTx);
			MethodIdent globalContextMethod = new MethodIdent();
			globalContextMethod.setMethodName(Series.ProblemOccurrenceInformation.TAG_GLOBAL_CONTEXT_METHOD_NAME);
			MethodIdent problemContextMethod = new MethodIdent();
			problemContextMethod.setMethodName(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME);
			MethodIdent rootCauseMethod = new MethodIdent();
			rootCauseMethod.setMethodName(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME);
			when(cachedDataService.getMethodIdentForId(globalContext.getMethodIdent())).thenReturn(globalContextMethod);
			when(cachedDataService.getMethodIdentForId(problemContext.getMethodIdent())).thenReturn(problemContextMethod);
			when(cachedDataService.getMethodIdentForId(rootCause.getMethodIdent())).thenReturn(rootCauseMethod);

			problemOccurrencePointBuilder.saveProblemOccurrenceToInflux(problemOccurrence);

			verify(influxDBDao, times(1)).insert(any(Point.class));
		}
	}
}
