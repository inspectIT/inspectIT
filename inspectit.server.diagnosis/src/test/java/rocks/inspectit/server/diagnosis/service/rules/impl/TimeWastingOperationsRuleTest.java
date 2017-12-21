package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 *
 * @author Isabel Vico Peinado
 *
 */
@SuppressWarnings("PMD")
public class TimeWastingOperationsRuleTest extends TestBase {

	@InjectMocks
	TimeWastingOperationsRule timeWastingOperationsRule;

	@Mock
	InvocationSequenceData globalContext;

	public static class Action extends TimeWastingOperationsRuleTest {
		@BeforeMethod
		public void init() {
			timeWastingOperationsRule.baseline = 1000;
		}

		/**
		 * Checks that the sequenceData have all the mandatory attributes.
		 *
		 * @param AggregatedDiagnosisInvocationData
		 *            Sequence data to check if has all the mandatory data.
		 */
		private void isAValidAggregatedDiagnosisData(AggregatedDiagnosisData AggregatedDiagnosisInvocationData) {
			for (InvocationSequenceData aggregatedSequence : AggregatedDiagnosisInvocationData.getRawInvocationsSequenceElements()) {
				assertThat("The aggregated sequence cannot be null", aggregatedSequence, notNullValue());
				assertThat("Duration of the aggregated sequence cannot be null", aggregatedSequence.getDuration(), notNullValue());
				assertThat("Start time of the aggregated cannot be null", aggregatedSequence.getStart(), notNullValue());
				assertThat("End time of the aggregated cannot be null", aggregatedSequence.getEnd(), notNullValue());
				assertThat("Child count of the aggregated sequence cannot be null", aggregatedSequence.getChildCount(), notNullValue());
				assertThat("ApplicationId of the aggregated sequence cannot be null", aggregatedSequence.getApplicationId(), notNullValue());
				assertThat("Business transaction id of the aggregated sequence cannot be null", aggregatedSequence.getBusinessTransactionId(), notNullValue());
			}
		}

		@Test
		public void timerDataMustReturnANotNullListOfAggregatedDiagnosisData() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(200d);
			firstSeqTimerData.setExclusiveDuration(200d);
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("The returned list of AggregatedDiagnosisData must not be null", timeWastingOperationsResults, notNullValue());
		}

		@Test
		public void timerDataMustReturnANotEmptyListOfAggregatedDiagnosisDataWhenTheDurationIsTooLong() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(200d);
			firstSeqTimerData.setExclusiveDuration(200d);
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of AggregatedDiagnosisData not empty", timeWastingOperationsResults, not(hasSize(0)));
		}

		@Test
		public void timerDataMustReturnAEmptyListOfAggregatedDiagnosisDataWhenTheDurationIsZero() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(200d);
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(new Double(0));
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return an array of AggregatedDiagnosisData not empty", timeWastingOperationsResults, hasSize(0));
		}

		@Test
		public void timerDataMustReturnAListOfAggregatedDiagnosisDataWithOneElement() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(200d);
			firstSeqTimerData.setExclusiveDuration(200d);
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with one AggregatedDiagnosisData", timeWastingOperationsResults, hasSize(1));
		}

		@Test
		public void timerDataMustReturnAListOfAggregatedDiagnosisDataWithTwoElements() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(200d);
			firstSeqTimerData.setExclusiveDuration(200d);
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			TimerData thirdTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			thirdTimerData.calculateExclusiveMin(200d);
			firstSeqTimerData.setExclusiveDuration(200d);
			thirdChildSequence.setTimerData(thirdTimerData);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			nestedSequences.add(thirdChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with two AggregatedDiagnosisData", timeWastingOperationsResults, hasSize(2));
		}

		@Test
		public void timerDataMustReturnAValidListOfAggregatedDiagnosisData() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(200d);
			firstSeqTimerData.setExclusiveDuration(200d);
			firstChildSequence.setTimerData(firstSeqTimerData);
			nestedSequences.add(firstChildSequence);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperations = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of AggregatedDiagnosisData not empty", timeWastingOperations, not(hasSize(0)));
			for (AggregatedDiagnosisData AggregatedDiagnosisInvocationData : timeWastingOperations) {
				isAValidAggregatedDiagnosisData(AggregatedDiagnosisInvocationData);
			}
		}

		@Test
		public void timerDataMustReturnTheExpectedAggregatedDiagnosisData() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(200d);
			firstSeqTimerData.setExclusiveDuration(200d);
			firstMethod.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			TimerData thirdSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			thirdSeqTimerData.calculateExclusiveMin(200d);
			thirdSeqTimerData.setExclusiveDuration(200d);
			thirdMethod.setTimerData(thirdSeqTimerData);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			nestedSequences.add(thirdMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Identifier is not the expected one, the first result must have 1 as method identifier", timeWastingOperationsResults.get(0).getMethodIdent(), is(1L));
			assertThat("Identifier is not the expected one, the first result must have 3 as method identifier", timeWastingOperationsResults.get(1).getMethodIdent(), is(3L));
		}

		@Test
		public void timerDataMustReturnAClonedElementOfTheGlobalContextIfItHasNoNestedSequences() {
			long platformIdent = 108;
			double highDuration = 2000;
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerData.calculateExclusiveMin(1);
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			when(globalContext.getTimerData()).thenReturn(timerData);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getMethodIdent()).thenReturn(platformIdent);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);
			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Method ident must be the same than the global context", timeWastingOperationsResults.get(0).getMethodIdent(), is(platformIdent));
		}

		@Test
		public void timerDataMustReturnAListOfAggregatedDiagnosisDataWithTwoElementsWhenItsDurationIsHigherThanTheBaseline() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(1008d);
			firstSeqTimerData.setExclusiveDuration(1008d);
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			TimerData thirdTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			thirdTimerData.calculateExclusiveMin(2000d);
			thirdTimerData.setExclusiveDuration(2000d);
			thirdChildSequence.setTimerData(thirdTimerData);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			nestedSequences.add(thirdChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with two AggregatedDiagnosisData", timeWastingOperationsResults, hasSize(2));
			assertThat("Identifier is not the expected one, the first result must have 1 as method identifier", timeWastingOperationsResults.get(0).getMethodIdent(), is(3L));
			assertThat("Identifier is not the expected one, the first result must have 3 as method identifier", timeWastingOperationsResults.get(1).getMethodIdent(), is(1L));
		}

		@Test
		public void sqlStatementDataMustReturnANotNullListOfAggregatedDiagnosisData() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(200d);
			firstSqlStatementData.setExclusiveDuration(200d);
			firstSqlStatementData.setCount(1);
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("The returned list of AggregatedDiagnosisData must not be null", timeWastingOperationsResults, notNullValue());
		}

		@Test
		public void sqlStatementDataMustReturnANotEmptyListOfAggregatedDiagnosisDataWhenTheDurationIsTooLong() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(200d);
			firstSqlStatementData.setExclusiveDuration(200d);
			firstSqlStatementData.setCount(1);
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a group of AggregatedDiagnosisData not empty", timeWastingOperationsResults, not(hasSize(0)));
		}

		@Test
		public void sqlStatementDataMustReturnAEmptyListOfAggregatedDiagnosisDataWhenTheDurationIsZero() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(200d);
			firstSqlStatementData.setExclusiveDuration(200d);
			firstSqlStatementData.setCount(1);
			firstMethod.setSqlStatementData(firstSqlStatementData);
			nestedSequences.add(firstMethod);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(new Double(0));
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return an array of AggregatedDiagnosisData not empty", timeWastingOperationsResults, hasSize(0));
		}

		@Test
		public void sqlStatementDataMustReturnAListOfAggregatedDiagnosisDataWithOneElement() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(200d);
			firstSqlStatementData.setExclusiveDuration(200d);
			firstSqlStatementData.setCount(1);
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with one AggregatedDiagnosisData", timeWastingOperationsResults, hasSize(1));
		}

		@Test
		public void sqlStatementDataMustReturnAListOfAggregatedDiagnosisDataWithTwoElements() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(200d);
			firstSqlStatementData.setExclusiveDuration(200d);
			firstSqlStatementData.setCount(1);
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			SqlStatementData thirdSqlStatementData = new SqlStatementData();
			thirdSqlStatementData.calculateExclusiveMin(200d);
			thirdSqlStatementData.setExclusiveDuration(200d);
			thirdSqlStatementData.setCount(1);
			secondMethod.setSqlStatementData(thirdSqlStatementData);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			nestedSequences.add(thirdMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with two AggregatedDiagnosisData", timeWastingOperationsResults, hasSize(2));
		}

		@Test
		public void sqlStatementDataMustReturnAValidListOfAggregatedDiagnosisData() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(200d);
			firstSqlStatementData.setExclusiveDuration(200d);
			firstSqlStatementData.setCount(1);
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of AggregatedDiagnosisData not empty", timeWastingOperationsResults, not(hasSize(0)));
			for (AggregatedDiagnosisData AggregatedDiagnosisInvocationData : timeWastingOperationsResults) {
				isAValidAggregatedDiagnosisData(AggregatedDiagnosisInvocationData);
			}
		}

		@Test
		public void sqlStatementDataMustReturnTheExpectedAggregatedDiagnosisData() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(200d);
			firstSqlStatementData.setExclusiveDuration(200d);
			firstSqlStatementData.setCount(1);
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			SqlStatementData thirdSqlStatementData = new SqlStatementData();
			thirdSqlStatementData.calculateExclusiveMin(300d);
			thirdSqlStatementData.setExclusiveDuration(300d);
			thirdSqlStatementData.setCount(1);
			thirdMethod.setSqlStatementData(thirdSqlStatementData);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			nestedSequences.add(thirdMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Identifier is not the expected one, the first result must have 3 as method identifier", timeWastingOperationsResults.get(0).getMethodIdent(), is(3L));
			assertThat("Identifier is not the expected one, the first result must have 1 as method identifier", timeWastingOperationsResults.get(1).getMethodIdent(), is(1L));
		}

		@Test
		public void sqlStatementDataMustReturnAListOfAggregatedDiagnosisDataWithTwoElementsWhenItsDurationIsHigherThanTheBaseline() {
			long platformIdent = 108;
			long sensorTypeIdent = 1;
			double highDuration = 2000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSeqTimerData = new SqlStatementData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstSeqTimerData.calculateExclusiveMin(1008d);
			firstSeqTimerData.setExclusiveDuration(1008d);
			firstSeqTimerData.setCount(1);
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			SqlStatementData thirdTimerData = new SqlStatementData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			thirdTimerData.calculateExclusiveMin(2000d);
			thirdTimerData.setExclusiveDuration(2000d);
			thirdTimerData.setCount(1);
			thirdChildSequence.setTimerData(thirdTimerData);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			nestedSequences.add(thirdChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with two AggregatedDiagnosisData", timeWastingOperationsResults, hasSize(2));
			assertThat("Identifier is not the expected one, the first result must have 1 as method identifier", timeWastingOperationsResults.get(0).getMethodIdent(), is(3L));
			assertThat("Identifier is not the expected one, the first result must have 3 as method identifier", timeWastingOperationsResults.get(1).getMethodIdent(), is(1L));
		}
	}
}
