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
import java.util.Random;

import org.mockito.InjectMocks;
import org.mockito.Mock;
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
		private static final Random RANDOM = new Random();

		/**
		 * Checks that the sequenceData have all the mandatory attributes.
		 *
		 * @param AggregatedDiagnosisInvocationData
		 *            Sequence data to check if has all the mandatory data.
		 */
		private void isAValidRule(AggregatedDiagnosisData AggregatedDiagnosisInvocationData) {
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
		public void timerDataMustReturnANotNullGroupOfRules() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = timerData;
			firstSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("The returned list of rules must not be null", timeWastingOperationsResults, notNullValue());
		}

		@Test
		public void timerDataMustReturnANotEmptyGroupOfRulesWhenTheDurationIsTooLong() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = timerData;
			firstSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of rules not empty", timeWastingOperationsResults, not(hasSize(0)));
		}

		@Test
		public void timerDataMustReturnAEmptyGroupOfRulesWhenTheDurationIsZero() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = timerData;
			firstSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(new Double(0));
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return an array of rules not empty", timeWastingOperationsResults, hasSize(0));
		}

		@Test
		public void timerDataMustReturnAGroupOfRulesWithOneElement() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = timerData;
			firstSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of one rule", timeWastingOperationsResults, hasSize(1));
		}

		@Test
		public void timerDataMustReturnAGroupOfRulesWithTwoElements() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = timerData;
			firstSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			firstChildSequence.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			TimerData thirdTimerData = timerData;
			thirdTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			thirdChildSequence.setTimerData(thirdTimerData);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(secondChildSequence);
			nestedSequences.add(thirdChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with two rules", timeWastingOperationsResults, hasSize(2));
		}

		@Test
		public void timerDataMustReturnAValidGroupOfRules() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = timerData;
			firstSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			firstChildSequence.setTimerData(firstSeqTimerData);
			nestedSequences.add(firstChildSequence);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(secondChildSequence);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperations = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of rules not empty", timeWastingOperations, not(hasSize(0)));
			for (AggregatedDiagnosisData AggregatedDiagnosisInvocationData : timeWastingOperations) {
				isAValidRule(AggregatedDiagnosisInvocationData);
			}
		}

		@Test
		public void timerDataMustReturnTheExpectedRules() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			TimerData firstSeqTimerData = timerData;
			firstSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setTimerData(firstSeqTimerData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			TimerData thirdSeqTimerData = timerData;
			thirdSeqTimerData.calculateExclusiveMin(RANDOM.nextDouble());
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
			Double highDuration = RANDOM.nextDouble() + 1000;
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			when(globalContext.getTimerData()).thenReturn(timerData);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getMethodIdent()).thenReturn(108L);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Method ident must be the same that the global context", timeWastingOperationsResults.get(0).getMethodIdent(), is(108L));
		}

		@Test
		public void sqlStatementDataMustReturnANotNullGroupOfRules() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("The returned list of rules must not be null", timeWastingOperationsResults, notNullValue());
		}

		@Test
		public void sqlStatementDataMustReturnANotEmptyGroupOfRulesWhenTheDurationIsTooLong() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of rules not empty", timeWastingOperationsResults, not(hasSize(0)));
		}

		@Test
		public void sqlStatementDataMustReturnAEmptyGroupOfRulesWhenTheDurationIsZero() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setSqlStatementData(firstSqlStatementData);
			nestedSequences.add(firstMethod);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(new Double(0));
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return an array of rules not empty", timeWastingOperationsResults, hasSize(0));
		}

		@Test
		public void sqlStatementDataMustReturnAGroupOfRulesWithOneElement() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of one rule", timeWastingOperationsResults, hasSize(1));
		}

		@Test
		public void sqlStatementDataMustReturnAGroupOfRulesWithTwoElements() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			SqlStatementData thirdSqlStatementData = new SqlStatementData();
			thirdSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			secondMethod.setSqlStatementData(thirdSqlStatementData);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			nestedSequences.add(thirdMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list with two rules", timeWastingOperationsResults, hasSize(2));
		}

		@Test
		public void sqlStatementDataMustReturnAValidGroupOfRules() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			nestedSequences.add(firstMethod);
			nestedSequences.add(secondMethod);
			when(globalContext.getDuration()).thenReturn(highDuration);
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);

			List<AggregatedDiagnosisData> timeWastingOperationsResults = timeWastingOperationsRule.action();

			assertThat("Action method must return a list of rules not empty", timeWastingOperationsResults, not(hasSize(0)));
			for (AggregatedDiagnosisData AggregatedDiagnosisInvocationData : timeWastingOperationsResults) {
				isAValidRule(AggregatedDiagnosisInvocationData);
			}
		}

		@Test
		public void sqlStatementDataMustReturnTheExpectedRules() {
			Double highDuration = RANDOM.nextDouble() + 1000;
			Timestamp defDate = new Timestamp(new Date().getTime());
			long platformIdent = RANDOM.nextLong();
			long sensorTypeIdent = RANDOM.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 1L);
			SqlStatementData firstSqlStatementData = new SqlStatementData();
			firstSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
			firstMethod.setSqlStatementData(firstSqlStatementData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 2L);
			InvocationSequenceData thirdMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, 3L);
			SqlStatementData thirdSqlStatementData = new SqlStatementData();
			thirdSqlStatementData.calculateExclusiveMin(RANDOM.nextDouble());
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

	}
}
