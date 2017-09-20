package rocks.inspectit.server.influx.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point.Builder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.AggregatedDiagnosisTimerData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.RootCause;

/**
 * @author Isabel Vico Peinado
 *
 */
public class ProblemOccurrencePointBuilderTest extends AbstractPointBuilderTest {

	@InjectMocks
	ProblemOccurrencePointBuilder builder;

	@Mock
	ICachedDataService cachedDataService;

	public class GetBuilder extends ProblemOccurrencePointBuilderTest {

		static final String APPLICATION_NAME = "appName";
		static final String BUSINESS_TX = "businessTx";
		static final String PROBLEM_CONTEXT_METHOD_NAME = "problemContextMethodName";
		static final String ROOTCAUSE_METHOD_NAME = "rootCauseMethodName";
		static final String PROBLEM_CONTEXT_FQN = "null.null." + PROBLEM_CONTEXT_METHOD_NAME + "()";
		static final String ROOT_CAUSE_FQN = "null.null." + ROOTCAUSE_METHOD_NAME + "()";
		CauseType causeType = CauseType.ITERATIVE;
		SourceType sourceType = SourceType.DATABASE;
		ProblemOccurrence problemOccurrence;

		@BeforeMethod
		public void init() {
			InvocationSequenceData requestRoot;
			InvocationSequenceData globalContext;
			InvocationSequenceData problemContext;
			RootCause rootCause;
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			long methodIdent = 1L;
			int applicationId = 123;
			int businessTxId = 456;
			int exclusiveDuration = 100;

			Timestamp defDate = new Timestamp(new Date().getTime());
			AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData = new AggregatedDiagnosisTimerData(new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L));
			requestRoot = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerData.setDuration(exclusiveDuration);
			timerData.setExclusiveDuration(exclusiveDuration);
			timerData.setExclusiveCount(1);
			requestRoot.setTimerData(timerData);
			globalContext = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			globalContext.setTimerData(timerData);
			problemContext = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			problemContext.setTimerData(timerData);
			rootCause = new RootCause(methodIdent, aggregatedDiagnosisTimerData);
			problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeType, sourceType);
			problemOccurrence.setApplicationNameIdent(applicationId);
			problemOccurrence.setBusinessTransactionNameIdent(businessTxId);
			ApplicationData applicationData = new ApplicationData(applicationId, applicationId, APPLICATION_NAME);
			when(cachedDataService.getApplicationForId(problemOccurrence.getApplicationNameIdent())).thenReturn(applicationData);
			BusinessTransactionData businessTx = new BusinessTransactionData(businessTxId, businessTxId, applicationData, BUSINESS_TX);
			when(cachedDataService.getBusinessTransactionForId(applicationId, businessTxId)).thenReturn(businessTx);
			MethodIdent problemContextMethod = new MethodIdent();
			problemContextMethod.setMethodName(PROBLEM_CONTEXT_METHOD_NAME);
			MethodIdent rootCauseMethod = new MethodIdent();
			rootCauseMethod.setMethodName(ROOTCAUSE_METHOD_NAME);
			when(cachedDataService.getMethodIdentForId(problemContext.getMethodIdent())).thenReturn(problemContextMethod);
			when(cachedDataService.getMethodIdentForId(rootCause.getMethodIdent())).thenReturn(rootCauseMethod);
		}

		@Test
		public void hasAllTheNeededTagsAndFields() throws Exception {
			Builder pointBuilder = builder.getBuilder(problemOccurrence);

			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getMeasurement(pointBuilder), is(Series.ProblemOccurrenceInformation.NAME));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, String.valueOf(APPLICATION_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BUSINESS_TX)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, PROBLEM_CONTEXT_FQN));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, ROOT_CAUSE_FQN));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_CAUSE_TYPE, String.valueOf(causeType)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_SOURCE_TYPE, String.valueOf(sourceType)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_INVOCATION_ROOT_DURATION, problemOccurrence.getRequestRoot().getDiagnosisTimerData().getDuration()));
			assertThat(getFields(pointBuilder),
					hasEntry(Series.ProblemOccurrenceInformation.FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME, problemOccurrence.getGlobalContext().getDiagnosisTimerData().getExclusiveDuration()));
			assertThat(getFields(pointBuilder),
					hasEntry(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, problemOccurrence.getRootCause().getAggregatedDiagnosisTimerData().getExclusiveDuration()));
		}
	}
}
