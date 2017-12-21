package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
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

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 *
 * @author Isabel Vico Peinado
 *
 */
public class GlobalContextRuleTest extends TestBase {

	@InjectMocks
	GlobalContextRule globalContextRule;

	@Mock
	InvocationSequenceData invocationSequenceRoot;

	public static class Action extends GlobalContextRuleTest {
		private static final double HIGH_DURATION = 4700d;

		@Test
		private void currentGlobalContextRuleMustNotBeNull() {
			when(invocationSequenceRoot.getDuration()).thenReturn(HIGH_DURATION);

			InvocationSequenceData currentGlobalContextRule = globalContextRule.action();

			assertThat("Invocation sequence root must not be null", currentGlobalContextRule, notNullValue());
		}

		@Test
		private void currentGlobalContextRuleMustBeTheSequenceWithMaximumDuration() {
			double baseline = 1000d;
			long methodIdent = 108L;
			Timestamp defDate = new Timestamp(new Date().getTime());
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			globalContextRule.baseline = baseline;
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			firstChildSequence.setDuration(200d);
			TimerData firstChildTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			firstChildTimerData.calculateExclusiveMin(200d);
			firstChildTimerData.setExclusiveDuration(200d);
			firstChildSequence.setTimerData(firstChildTimerData);

			InvocationSequenceData higherDurationChild = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			higherDurationChild.setDuration(4000d);
			TimerData higherDurationChildTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			higherDurationChildTimerData.calculateExclusiveMin(4000d);
			higherDurationChildTimerData.setExclusiveDuration(4000d);
			higherDurationChild.setTimerData(higherDurationChildTimerData);

			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			secondChildSequence.setDuration(500d);
			TimerData secondChildSequenceTimerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
			secondChildSequenceTimerData.calculateExclusiveMin(500d);
			secondChildSequenceTimerData.setExclusiveDuration(500d);
			secondChildSequence.setTimerData(secondChildSequenceTimerData);

			nestedSequences.add(firstChildSequence);
			nestedSequences.add(higherDurationChild);
			nestedSequences.add(secondChildSequence);
			when(invocationSequenceRoot.getDuration()).thenReturn(HIGH_DURATION);
			when(invocationSequenceRoot.getNestedSequences()).thenReturn(nestedSequences);

			InvocationSequenceData currentGlobalContextRule = globalContextRule.action();

			assertThat("The returned global context rule must be the child with higher duration", currentGlobalContextRule, is(higherDurationChild));
		}

		@Test
		private void currentGlobalContextRuleMustNotBeTheSequenceWithMaximumDuration() {
			long methodIdent = 108L;
			double baseline = 1000d;
			globalContextRule.baseline = baseline;
			Timestamp defDate = new Timestamp(new Date().getTime());
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			firstChildSequence.setDuration(200d);
			InvocationSequenceData higherDurationChild = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			higherDurationChild.setDuration(3000d);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			secondChildSequence.setDuration(500d);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(higherDurationChild);
			nestedSequences.add(secondChildSequence);
			when(invocationSequenceRoot.getDuration()).thenReturn(HIGH_DURATION);
			when(invocationSequenceRoot.getNestedSequences()).thenReturn(nestedSequences);

			InvocationSequenceData currentGlobalContextRule = globalContextRule.action();

			assertThat("The returned global context rule must not be the child with higher duration", currentGlobalContextRule, not(is(higherDurationChild)));
		}

		@Test
		private void currentGlobalContextRuleMustBeTheRootSequenceWhenThereIsNotAnyHigherAndDominatingInvocation() {
			long methodIdent = 108L;
			double baseline = 1000d;
			globalContextRule.baseline = baseline;
			Timestamp defDate = new Timestamp(new Date().getTime());
			Random random = new Random();
			long platformIdent = random.nextLong();
			long sensorTypeIdent = random.nextLong();
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			firstChildSequence.setDuration(200d);
			InvocationSequenceData higherDurationChild = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			higherDurationChild.setDuration(3000d);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(defDate, platformIdent, sensorTypeIdent, methodIdent);
			secondChildSequence.setDuration(500d);
			nestedSequences.add(firstChildSequence);
			nestedSequences.add(higherDurationChild);
			nestedSequences.add(secondChildSequence);
			when(invocationSequenceRoot.getDuration()).thenReturn(HIGH_DURATION);
			when(invocationSequenceRoot.getNestedSequences()).thenReturn(nestedSequences);

			InvocationSequenceData currentGlobalContextRule = globalContextRule.action();

			assertThat("The returned global context rule must be the invocationSequenceRoot", currentGlobalContextRule, is(invocationSequenceRoot));
		}
	}
}
