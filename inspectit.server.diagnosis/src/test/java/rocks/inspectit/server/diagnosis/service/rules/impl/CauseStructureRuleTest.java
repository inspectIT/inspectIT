package rocks.inspectit.server.diagnosis.service.rules.impl;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.server.diagnosis.service.data.CauseCluster;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.cs.communication.data.diagnosis.AggregatedDiagnosisTimerData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;

/**
 *
 * @author Isabel Vico Peinado
 *
 */
public class CauseStructureRuleTest extends TestBase {

	@InjectMocks
	CauseStructureRule causeStructureRule;

	@Mock
	CauseCluster problemContext;

	@Mock
	InvocationSequenceData commonContext;

	@Mock
	AggregatedDiagnosisData rootCause;

	public static class Action extends CauseStructureRuleTest {

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void expectedExceptionsIfTheCauseHasNoElements() {
			long methodIdentEqual = new Long(108);
			when(rootCause.getMethodIdent()).thenReturn(methodIdentEqual);
			when(rootCause.size()).thenReturn(0);

			CauseStructure causeStructure = causeStructureRule.action();

			assert (causeStructure.getCauseType() == CauseType.SINGLE);
		}

		@Test
		public void causeTypeMustBeSingleWhenTheRootCauseHasJustOneElement() {
			when(rootCause.size()).thenReturn(1);

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be single", causeStructure.getCauseType(), is(CauseType.SINGLE));
		}

		@Test
		public void causeTypeMustBeRecursiveTheChildInvocationsOfTheRootCauseAreRecursiveAndHasARegularTimerData() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			TimerData timerData = new TimerData(currentTime, 10L, 20L, 30L);
			AggregatedDiagnosisTimerData aggregatedTimerData = new AggregatedDiagnosisTimerData(timerData);
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			long methodIdentEqual = new Long(108);
			long methodIdentDiff = random.nextLong();
			InvocationSequenceData detectedProblemContext = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			detectedProblemContext.setTimerData(timerData);
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentEqual);
			firstMethod.setTimerData(timerData);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentEqual);
			secondMethod.setTimerData(timerData);
			detectedProblemContext.getNestedSequences().add(firstMethod);
			firstMethod.setParentSequence(detectedProblemContext);
			firstMethod.getNestedSequences().add(secondMethod);
			secondMethod.setParentSequence(firstMethod);
			when(rootCause.getMethodIdent()).thenReturn(detectedProblemContext.getNestedSequences().get(0).getMethodIdent());
			when(rootCause.size()).thenReturn(2);
			when(rootCause.getAggregatedDiagnosisTimerData()).thenReturn(aggregatedTimerData);
			when(rootCause.getAggregationKey()).thenReturn(detectedProblemContext.getNestedSequences().get(0).getMethodIdent());
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getParentSequence()).thenReturn(null);
			when(commonContext.getNestedSequences()).thenReturn(Collections.singletonList(detectedProblemContext));
			when(commonContext.getMethodIdent()).thenReturn(firstMethod.getMethodIdent());
			when(commonContext.getTimerData()).thenReturn(firstMethod.getTimerData());
			when(problemContext.getCauseInvocations()).thenReturn(detectedProblemContext.getNestedSequences());
			when(commonContext.getDuration()).thenReturn(firstMethod.getDuration());

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be recursive", causeStructure.getCauseType(), is(CauseType.RECURSIVE));
		}

		@Test
		public void causeTypeMustBeIterativeWhenTheChildInvocationsOfTheRootCauseAreIterativeAndHasARegularTimerData() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			TimerData timerData = new TimerData(currentTime, 10L, 20L, 30L);
			AggregatedDiagnosisTimerData aggregatedTimerData = new AggregatedDiagnosisTimerData(timerData);
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			long methodIdentEqual = new Long(108);
			long methodIdentDiff = random.nextLong();
			InvocationSequenceData childSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			childSequence.setTimerData(timerData);
			InvocationSequenceData parentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			parentSequence.setTimerData(timerData);
			InvocationSequenceData grandParentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			grandParentSequence.setTimerData(timerData);
			parentSequence.setParentSequence(grandParentSequence);
			List<InvocationSequenceData> rawInvocations = new ArrayList<>();
			rawInvocations.add(new InvocationSequenceData());
			rawInvocations.add(childSequence);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getParentSequence()).thenReturn(parentSequence);
			when(commonContext.getTimerData()).thenReturn(timerData);
			when(rootCause.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
			when(rootCause.getMethodIdent()).thenReturn(methodIdentEqual);
			when(rootCause.getAggregatedDiagnosisTimerData()).thenReturn(aggregatedTimerData);

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be iterative", causeStructure.getCauseType(), is(CauseType.ITERATIVE));
		}

		@Test
		public void causeTypeMustBeIterativeWhenTheChildInvocationsOfTheRootCauseAreIterativeAndHasASqlTimerData() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			SqlStatementData timerDataSql = new SqlStatementData(currentTime, 10L, 20L, 30L);
			AggregatedDiagnosisTimerData aggregatedTimerDataSql = new AggregatedDiagnosisTimerData(timerDataSql);
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			long methodIdentEqual = new Long(108);
			long methodIdentDiff = random.nextLong();
			InvocationSequenceData childSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			timerDataSql.setCount(1);
			timerDataSql.setSql("somethingsomething");
			childSequence.setTimerData(timerDataSql);
			InvocationSequenceData parentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			parentSequence.setTimerData(timerDataSql);
			InvocationSequenceData grandParentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			grandParentSequence.setTimerData(timerDataSql);
			parentSequence.setParentSequence(grandParentSequence);
			List<InvocationSequenceData> rawInvocations = new ArrayList<>();
			rawInvocations.add(new InvocationSequenceData());
			rawInvocations.add(childSequence);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getParentSequence()).thenReturn(parentSequence);
			when(commonContext.getTimerData()).thenReturn(timerDataSql);
			when(commonContext.getSqlStatementData()).thenReturn(timerDataSql);
			when(rootCause.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
			when(rootCause.getMethodIdent()).thenReturn(methodIdentEqual);
			when(rootCause.getAggregatedDiagnosisTimerData()).thenReturn(aggregatedTimerDataSql);

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be iterative", causeStructure.getCauseType(), is(CauseType.ITERATIVE));
		}

		@Test
		public void causeTypeMustBeRecursiveWhenTheChildInvocationsOfTheRootCauseAreRecursiveAndHasASqlTimerData() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			SqlStatementData timerDataSql = new SqlStatementData(currentTime, 10L, 20L, 30L);
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			long methodIdentEqual = new Long(108);
			long methodIdentDiff = random.nextLong();
			InvocationSequenceData detectedProblemContext = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			timerDataSql.setCount(1);
			timerDataSql.setSql("somethingsomething");
			detectedProblemContext.setSqlStatementData(timerDataSql);
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentEqual);
			firstMethod.setSqlStatementData(timerDataSql);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentEqual);
			secondMethod.setSqlStatementData(timerDataSql);
			detectedProblemContext.getNestedSequences().add(firstMethod);
			firstMethod.setParentSequence(detectedProblemContext);
			firstMethod.getNestedSequences().add(secondMethod);
			secondMethod.setParentSequence(firstMethod);
			when(rootCause.getMethodIdent()).thenReturn(detectedProblemContext.getNestedSequences().get(0).getMethodIdent());
			when(rootCause.size()).thenReturn(2);
			Pair<Long, String> pair = new Pair<Long, String>(detectedProblemContext.getNestedSequences().get(0).getMethodIdent(), "somethingsomething");
			when(rootCause.getAggregationKey()).thenReturn(pair);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getParentSequence()).thenReturn(null);
			when(commonContext.getNestedSequences()).thenReturn(Collections.singletonList(detectedProblemContext));
			when(commonContext.getMethodIdent()).thenReturn(firstMethod.getMethodIdent());
			when(commonContext.getTimerData()).thenReturn(firstMethod.getTimerData());
			when(commonContext.getDuration()).thenReturn(firstMethod.getDuration());
			when(commonContext.getSqlStatementData()).thenReturn(timerDataSql);
			when(problemContext.getCauseInvocations()).thenReturn(detectedProblemContext.getNestedSequences());

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be recursive", causeStructure.getCauseType(), is(CauseType.RECURSIVE));
		}

		@Test
		public void causeTypeMustBeRecursiveWhenTheChildInvocationsOfTheRootCauseAreRecursiveAndHasAHttpTimerData() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			HttpTimerData timerDataHttp = new HttpTimerData(currentTime, 10L, 20L, 30L);
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			long methodIdentEqual = new Long(108);
			long methodIdentDiff = random.nextLong();
			InvocationSequenceData detectedProblemContext = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			detectedProblemContext.setTimerData(timerDataHttp);
			InvocationSequenceData firstMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentEqual);
			firstMethod.setTimerData(timerDataHttp);
			InvocationSequenceData secondMethod = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentEqual);
			secondMethod.setTimerData(timerDataHttp);
			detectedProblemContext.getNestedSequences().add(firstMethod);
			firstMethod.setParentSequence(detectedProblemContext);
			firstMethod.getNestedSequences().add(secondMethod);
			secondMethod.setParentSequence(firstMethod);
			when(rootCause.getMethodIdent()).thenReturn(detectedProblemContext.getNestedSequences().get(0).getMethodIdent());
			when(rootCause.size()).thenReturn(2);
			Pair<Long, String> pair = new Pair<Long, String>(detectedProblemContext.getNestedSequences().get(0).getMethodIdent(), "n.a.");
			when(rootCause.getAggregationKey()).thenReturn(pair);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getParentSequence()).thenReturn(null);
			when(commonContext.getNestedSequences()).thenReturn(Collections.singletonList(detectedProblemContext));
			when(commonContext.getMethodIdent()).thenReturn(firstMethod.getMethodIdent());
			when(commonContext.getTimerData()).thenReturn(firstMethod.getTimerData());
			when(problemContext.getCauseInvocations()).thenReturn(detectedProblemContext.getNestedSequences());
			when(commonContext.getDuration()).thenReturn(firstMethod.getDuration());

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be recursive", causeStructure.getCauseType(), is(CauseType.RECURSIVE));
		}

		@Test
		public void causeTypeMustBeIterativeWhenTheChildInvocationsOfTheRootCauseAreIterativeAndHasAHttpTimerData() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			HttpTimerData timerDataHttp = new HttpTimerData(currentTime, 10L, 20L, 30L);
			AggregatedDiagnosisTimerData aggregatedTimerDataHttp = new AggregatedDiagnosisTimerData(timerDataHttp);
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			long methodIdentEqual = new Long(108);
			long methodIdentDiff = random.nextLong();
			InvocationSequenceData childSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			childSequence.setTimerData(timerDataHttp);
			InvocationSequenceData parentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			parentSequence.setTimerData(timerDataHttp);
			InvocationSequenceData grandParentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			grandParentSequence.setTimerData(timerDataHttp);
			parentSequence.setParentSequence(grandParentSequence);
			List<InvocationSequenceData> rawInvocations = new ArrayList<>();
			rawInvocations.add(new InvocationSequenceData());
			rawInvocations.add(childSequence);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getParentSequence()).thenReturn(parentSequence);
			when(commonContext.getTimerData()).thenReturn(timerDataHttp);
			when(rootCause.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
			when(rootCause.getMethodIdent()).thenReturn(methodIdentEqual);
			when(rootCause.getAggregatedDiagnosisTimerData()).thenReturn(aggregatedTimerDataHttp);

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be iterative", causeStructure.getCauseType(), is(CauseType.ITERATIVE));
		}

		@Test
		public void causeTypeIsRecursiveIfThereIsNotTimerData() {
			Timestamp defDate = new Timestamp(new Date().getTime());
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			long methodIdentEqual = new Long(108);
			long methodIdentDiff = random.nextLong();
			InvocationSequenceData childSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			InvocationSequenceData parentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			InvocationSequenceData grandParentSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdentDiff);
			parentSequence.setParentSequence(grandParentSequence);
			List<InvocationSequenceData> rawInvocations = new ArrayList<>();
			rawInvocations.add(new InvocationSequenceData());
			rawInvocations.add(childSequence);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getParentSequence()).thenReturn(parentSequence);
			when(rootCause.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
			when(rootCause.getMethodIdent()).thenReturn(methodIdentEqual);

			CauseStructure causeStructure = causeStructureRule.action();

			assertThat("The returned cause type must be iterative", causeStructure.getCauseType(), is(CauseType.ITERATIVE));
		}
	}
}
