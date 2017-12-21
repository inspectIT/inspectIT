package rocks.inspectit.server.influx.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point.Builder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.communication.data.diagnosis.AggregatedDiagnosisTimerData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.DiagnosisTimerData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.InvocationIdentifier;
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
		static final String AGENT_NAME = "agentName";
		static final String APPLICATION_NAME = "appName";
		static final String BUSINESS_TX = "businessTx";
		static final String PROBLEM_CONTEXT_METHOD_NAME = "problemContextMethodName";
		static final String ROOTCAUSE_METHOD_NAME = "rootCauseMethodName";
		static final String PROBLEM_CONTEXT_FQN = PROBLEM_CONTEXT_METHOD_NAME + "Fqn";
		static final String ROOT_CAUSE_FQN = ROOTCAUSE_METHOD_NAME + "Fqn";
		static final String METHOD = "methodName";
		static final String FQN = "fqn";
		static final double EXCLUSIVE_DURATION = 50;
		static final double DURATION = 100;
		static final String UNKNOWN_METHOD_FQN = "Unknown method";
		static final int APP_ID = 123;
		static final int BUSINESS_TX_ID = 456;
		CauseType causeType = CauseType.ITERATIVE;
		SourceType sourceType = SourceType.DATABASE;

		@Mock
		PlatformIdent platformIdent;

		@Mock
		BusinessTransactionData businessTransactionData;

		@Mock
		ApplicationData applicationData;

		@Mock
		ProblemOccurrence problemOccurrence;

		@Mock
		RootCause rootCause;

		@Mock
		MethodIdent methodIdent;

		@Mock
		DiagnosisTimerData diagnosisTimerData;

		@Mock
		AggregatedDiagnosisTimerData aggregatedDiagnosisTimerData;

		@Mock
		InvocationIdentifier invocationIdentifier;

		@BeforeMethod
		public void init() {
			when(problemOccurrence.getApplicationNameIdent()).thenReturn(APP_ID);
			when(problemOccurrence.getBusinessTransactionNameIdent()).thenReturn(BUSINESS_TX_ID);
			when(problemOccurrence.getProblemContext()).thenReturn(invocationIdentifier);
			when(problemOccurrence.getRequestRoot()).thenReturn(invocationIdentifier);
			when(problemOccurrence.getGlobalContext()).thenReturn(invocationIdentifier);
			when(problemOccurrence.getCauseType()).thenReturn(causeType);
			when(problemOccurrence.getSourceType()).thenReturn(sourceType);
			when(problemOccurrence.getRootCause()).thenReturn(rootCause);
			when(invocationIdentifier.getDiagnosisTimerData()).thenReturn(diagnosisTimerData);
			when(rootCause.getAggregatedDiagnosisTimerData()).thenReturn(aggregatedDiagnosisTimerData);
			when(diagnosisTimerData.getDuration()).thenReturn(DURATION);
			when(diagnosisTimerData.getExclusiveDuration()).thenReturn(EXCLUSIVE_DURATION);
			when(aggregatedDiagnosisTimerData.getExclusiveDuration()).thenReturn(EXCLUSIVE_DURATION);
			when(applicationData.getName()).thenReturn(APPLICATION_NAME);
			when(businessTransactionData.getName()).thenReturn(BUSINESS_TX);
			when(methodIdent.getMethodName()).thenReturn(METHOD);
			when(methodIdent.getFQN()).thenReturn(FQN);
		}

		@Test
		public void happyPath() throws Exception {
			when(cachedDataService.getApplicationForId(problemOccurrence.getApplicationNameIdent())).thenReturn(applicationData);
			when(cachedDataService.getPlatformIdentForId(APP_ID)).thenReturn(platformIdent);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BUSINESS_TX_ID)).thenReturn(businessTransactionData);
			when(cachedDataService.getMethodIdentForId(invocationIdentifier.getMethodIdent())).thenReturn(methodIdent);

			Builder pointBuilder = builder.getBuilder(problemOccurrence);

			assertThat(getMeasurement(pointBuilder), is(Series.ProblemOccurrenceInformation.NAME));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, String.valueOf(APPLICATION_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BUSINESS_TX)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_CAUSE_TYPE, String.valueOf(causeType)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_SOURCE_TYPE, String.valueOf(sourceType)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_INVOCATION_ROOT_DURATION, DURATION));
			assertThat(getFields(pointBuilder),
					hasEntry(Series.ProblemOccurrenceInformation.FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
			assertThat(getFields(pointBuilder),
					hasEntry(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
		}

		@Test
		public void noApplicationData() throws Exception {
			when(cachedDataService.getApplicationForId(problemOccurrence.getApplicationNameIdent())).thenReturn(null);
			when(cachedDataService.getPlatformIdentForId(APP_ID)).thenReturn(null);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BUSINESS_TX_ID)).thenReturn(businessTransactionData);
			when(cachedDataService.getMethodIdentForId(invocationIdentifier.getMethodIdent())).thenReturn(methodIdent);

			Builder pointBuilder = builder.getBuilder(problemOccurrence);

			assertThat(getMeasurement(pointBuilder), is(Series.ProblemOccurrenceInformation.NAME));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, String.valueOf(ApplicationDefinition.UNKNOWN_APP)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BUSINESS_TX)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_CAUSE_TYPE, String.valueOf(causeType)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_SOURCE_TYPE, String.valueOf(sourceType)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_INVOCATION_ROOT_DURATION, DURATION));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
		}

		@Test
		public void noPlatform() throws Exception {
			when(cachedDataService.getApplicationForId(problemOccurrence.getApplicationNameIdent())).thenReturn(applicationData);
			when(cachedDataService.getPlatformIdentForId(APP_ID)).thenReturn(null);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BUSINESS_TX_ID)).thenReturn(businessTransactionData);
			when(cachedDataService.getMethodIdentForId(invocationIdentifier.getMethodIdent())).thenReturn(methodIdent);

			Builder pointBuilder = builder.getBuilder(problemOccurrence);

			assertThat(getMeasurement(pointBuilder), is(Series.ProblemOccurrenceInformation.NAME));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, String.valueOf(APPLICATION_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BUSINESS_TX)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_CAUSE_TYPE, String.valueOf(causeType)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_SOURCE_TYPE, String.valueOf(sourceType)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_INVOCATION_ROOT_DURATION, DURATION));
			assertThat(getFields(pointBuilder),
					hasEntry(Series.ProblemOccurrenceInformation.FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
			assertThat(getFields(pointBuilder),
					hasEntry(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
		}

		@Test
		public void noBusinessContext() throws Exception {
			when(cachedDataService.getApplicationForId(problemOccurrence.getApplicationNameIdent())).thenReturn(applicationData);
			when(cachedDataService.getPlatformIdentForId(APP_ID)).thenReturn(platformIdent);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BUSINESS_TX_ID)).thenReturn(null);
			when(cachedDataService.getMethodIdentForId(invocationIdentifier.getMethodIdent())).thenReturn(methodIdent);

			Builder pointBuilder = builder.getBuilder(problemOccurrence);

			assertThat(getMeasurement(pointBuilder), is(Series.ProblemOccurrenceInformation.NAME));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, String.valueOf(APPLICATION_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_BUSINESS_TRANSACTION_NAME, BusinessTransactionDefinition.UNKNOWN_BUSINESS_TX));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, FQN + "." + METHOD));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_CAUSE_TYPE, String.valueOf(causeType)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_SOURCE_TYPE, String.valueOf(sourceType)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_INVOCATION_ROOT_DURATION, DURATION));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
		}

		@Test
		public void noMethodIdentifier() throws Exception {
			when(cachedDataService.getApplicationForId(problemOccurrence.getApplicationNameIdent())).thenReturn(applicationData);
			when(cachedDataService.getPlatformIdentForId(APP_ID)).thenReturn(platformIdent);
			when(cachedDataService.getBusinessTransactionForId(APP_ID, BUSINESS_TX_ID)).thenReturn(businessTransactionData);
			when(cachedDataService.getMethodIdentForId(invocationIdentifier.getMethodIdent())).thenReturn(null);

			Builder pointBuilder = builder.getBuilder(problemOccurrence);

			assertThat(getMeasurement(pointBuilder), is(Series.ProblemOccurrenceInformation.NAME));
			assertThat(getPrecision(pointBuilder), is(TimeUnit.MILLISECONDS));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, String.valueOf(APPLICATION_NAME)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_BUSINESS_TRANSACTION_NAME, String.valueOf(BUSINESS_TX)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, UNKNOWN_METHOD_FQN));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, UNKNOWN_METHOD_FQN));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_CAUSE_TYPE, String.valueOf(causeType)));
			assertThat(getTags(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_SOURCE_TYPE, String.valueOf(sourceType)));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_INVOCATION_ROOT_DURATION, DURATION));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
			assertThat(getFields(pointBuilder), hasEntry(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, EXCLUSIVE_DURATION));
		}
	}
}
