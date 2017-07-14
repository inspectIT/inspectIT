package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
import rocks.inspectit.server.diagnosis.service.data.CauseCluster;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 *
 * @author Isabel Vico Peinado
 *
 */
public class ProblemCauseRuleTest extends TestBase {

	@InjectMocks
	ProblemCauseRule problemCauseRule;

	@Mock
	CauseCluster problemContext;

	@Mock
	InvocationSequenceData commonContext;

	public static class Action extends ProblemCauseRuleTest {

		private static final Random RANDOM = new Random();
		private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());
		private static final long METHOD_IDENT_EQUAL = 108L;
		private static final long PLATFORM_IDENT = RANDOM.nextLong();
		private static final long SENSOR_TYPE_IDENT = RANDOM.nextLong();

		@Test
		public void rootCauseMustBeNotNullWhenMethodIdentIsEqualAndTheInvocationHasTimerData() {
			double highDuration = RANDOM.nextDouble() + 1000;
			List<InvocationSequenceData> nestedSequences = new ArrayList<InvocationSequenceData>();
			InvocationSequenceData firstChildSequenceData = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL);
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerData.calculateExclusiveMin(RANDOM.nextDouble());
			timerData.setExclusiveDuration(RANDOM.nextDouble());
			firstChildSequenceData.setTimerData(timerData);
			InvocationSequenceData secondChildSequenceData = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL);
			timerData.calculateExclusiveMin(RANDOM.nextDouble());
			timerData.setExclusiveDuration(RANDOM.nextDouble());
			secondChildSequenceData.setTimerData(timerData);
			nestedSequences.add(firstChildSequenceData);
			nestedSequences.add(secondChildSequenceData);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getMethodIdent()).thenReturn(1L);
			when(commonContext.getDuration()).thenReturn(highDuration);
			when(problemContext.getCauseInvocations()).thenReturn(nestedSequences);

			AggregatedDiagnosisData rootCause = problemCauseRule.action();

			assertThat("The returned root cause rule must not be null", rootCause, notNullValue());
		}

		@Test
		public void rootCauseMustBeNullWhenMethodIdentIsEqualAndInvocationHasNotTimerData() {
			double highDuration = RANDOM.nextDouble() + 1000;
			List<InvocationSequenceData> nestedSequences = new ArrayList<InvocationSequenceData>();
			nestedSequences.add(new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL));
			nestedSequences.add(new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL));
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getMethodIdent()).thenReturn(1L);
			when(commonContext.getDuration()).thenReturn(highDuration);
			when(commonContext.getNestedSequences()).thenReturn(nestedSequences);

			AggregatedDiagnosisData rootCause = problemCauseRule.action();

			assertThat("The returned root cause rule must be null", rootCause, nullValue());
		}

		@Test
		public void rootCauseMustHaveTwoElementsInRawInvocationSequence() {
			List<InvocationSequenceData> nestedSequences = new ArrayList<InvocationSequenceData>();
			InvocationSequenceData firstChildSequenceData = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL);
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			TimerData firstTimerData = timerData;
			firstTimerData.setExclusiveDuration(2000);
			firstTimerData.setDuration(2000);
			firstChildSequenceData.setTimerData(firstTimerData);
			InvocationSequenceData secondChildSequenceData = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL);
			TimerData secondTimerData = timerData;
			secondTimerData.setExclusiveDuration(100);
			secondTimerData.setDuration(100);
			secondChildSequenceData.setTimerData(secondTimerData);
			nestedSequences.add(firstChildSequenceData);
			nestedSequences.add(secondChildSequenceData);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getMethodIdent()).thenReturn(METHOD_IDENT_EQUAL);
			when(commonContext.getDuration()).thenReturn(3100.0);
			when(problemContext.getCauseInvocations()).thenReturn(nestedSequences);

			AggregatedDiagnosisData rootCause = problemCauseRule.action();

			assertThat("Raw invocation sequence must have two elements", rootCause.getRawInvocationsSequenceElements(), hasSize(2));
		}

		@Test
		public void rootCauseMustAggregateElementsWithThreeSigmaLimitApproach() {
			long methodIdentDiff = 100L;
			InvocationSequenceData currentProblemContext = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, methodIdentDiff);
			TimerData timerDataCurrentProblemContext = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerDataCurrentProblemContext.setExclusiveDuration(2500d);
			timerDataCurrentProblemContext.calculateExclusiveMin(1d);
			currentProblemContext.setTimerData(timerDataCurrentProblemContext);
			currentProblemContext.setDuration(2500d);
			InvocationSequenceData firstMethod = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL);
			TimerData timerDataFirstMethod = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerDataFirstMethod.setExclusiveDuration(1200d);
			timerDataFirstMethod.calculateExclusiveMin(1d);
			firstMethod.setTimerData(timerDataFirstMethod);
			firstMethod.setDuration(1200d);
			InvocationSequenceData secondMethod = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL);
			TimerData timerDataSecondMethod = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerDataSecondMethod.setExclusiveDuration(800d);
			timerDataSecondMethod.calculateExclusiveMin(1d);
			secondMethod.setTimerData(timerDataSecondMethod);
			secondMethod.setDuration(800d);
			InvocationSequenceData thirdMethod = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, METHOD_IDENT_EQUAL);
			TimerData timerDataThirdMethod = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			timerDataThirdMethod.setExclusiveDuration(500d);
			timerDataThirdMethod.calculateExclusiveMin(1d);
			thirdMethod.setTimerData(timerDataThirdMethod);
			thirdMethod.setDuration(500d);
			currentProblemContext.getNestedSequences().add(firstMethod);
			currentProblemContext.getNestedSequences().add(secondMethod);
			currentProblemContext.getNestedSequences().add(thirdMethod);
			firstMethod.setParentSequence(currentProblemContext);
			secondMethod.setParentSequence(currentProblemContext);
			thirdMethod.setParentSequence(currentProblemContext);
			when(problemContext.getCommonContext()).thenReturn(commonContext);
			when(commonContext.getMethodIdent()).thenReturn(currentProblemContext.getMethodIdent());
			when(commonContext.getDuration()).thenReturn(currentProblemContext.getDuration());
			when(problemContext.getCauseInvocations()).thenReturn(currentProblemContext.getNestedSequences());

			AggregatedDiagnosisData rootCause = problemCauseRule.action();

			assertThat("Raw invocation sequence must have three elements", rootCause.getRawInvocationsSequenceElements(), hasSize(3));
		}
	}
}
