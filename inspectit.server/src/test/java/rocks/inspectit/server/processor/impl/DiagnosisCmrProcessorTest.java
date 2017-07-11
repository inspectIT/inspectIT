package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.results.IDiagnosisResults;
import rocks.inspectit.server.diagnosis.service.DiagnosisService;
import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.server.diagnosis.service.aggregation.DiagnosisDataAggregator;
import rocks.inspectit.server.influx.builder.ProblemOccurrencePointBuilder;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.RootCause;


/**
 * @author Christian Voegele, Isabel Vico Peinado
 *
 */
@SuppressWarnings("PMD")
public class DiagnosisCmrProcessorTest extends TestBase {

	DiagnosisCmrProcessor cmrProcessor = new DiagnosisCmrProcessor(1000);

	DiagnosisService diagnosisService;

	private static final long METHOD_IDENT = 108L;
	private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());

	public static class Process extends DiagnosisCmrProcessorTest {

		@Mock
		DiagnosisService diagnosisService;

		@Mock
		EntityManager entityManager;

		@Test
		public void processData() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
			Field fieldEngine;
			fieldEngine = DiagnosisCmrProcessor.class.getDeclaredField("diagnosisService");
			fieldEngine.setAccessible(true);
			fieldEngine.set(cmrProcessor, diagnosisService);
			boolean canBeProcessed = false;
			InvocationSequenceData invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(5000d);
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstChildSequence.setDuration(200d);
			firstChildSequence.setId(2);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(DEF_DATE, 20, 20, METHOD_IDENT);
			secondChildSequence.setDuration(4000d);
			secondChildSequence.setId(3);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(DEF_DATE, 30, 30, METHOD_IDENT);
			thirdChildSequence.setDuration(500d);
			thirdChildSequence.setId(4);
			invocationSequenceRoot.getNestedSequences().add(firstChildSequence);
			invocationSequenceRoot.getNestedSequences().add(secondChildSequence);
			invocationSequenceRoot.getNestedSequences().add(thirdChildSequence);
			cmrProcessor.processData(invocationSequenceRoot, entityManager);

			verifyZeroInteractions(entityManager);
			canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);
			assertThat("Can be processed must be true.", canBeProcessed, is(true));
			canBeProcessed = cmrProcessor.canBeProcessed(thirdChildSequence);
			assertThat("Can be processed must be false.", canBeProcessed, is(false));
		}
	}

	public static class OnNewDiagnosisResult extends DiagnosisCmrProcessorTest {

		@Mock
		ProblemOccurrencePointBuilder problemOccurrencePointBuilder;

		@Mock
		IDiagnosisResults<ProblemOccurrence> diagnosisResults;

		CauseStructure causeStructure;
		InvocationSequenceData secondChildSequence;
		RootCause rootCause;
		Set<ProblemOccurrence> problemOccurenceSet;

		@BeforeMethod
		private void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
			Field diagnosisResultsField;
			diagnosisResultsField = DiagnosisCmrProcessor.class.getDeclaredField("diagnosisResults");
			diagnosisResultsField.setAccessible(true);
			diagnosisResultsField.set(cmrProcessor, diagnosisResults);
			Field problemOccurrencePointBuilderField;
			problemOccurrencePointBuilderField = DiagnosisCmrProcessor.class.getDeclaredField("problemOccurrencePointBuilder");
			problemOccurrencePointBuilderField.setAccessible(true);
			problemOccurrencePointBuilderField.set(cmrProcessor, problemOccurrencePointBuilder);
			long timestampValue = new Date().getTime();
			long platformIdent = new Random().nextLong();
			final long count = 2;
			final double min = 1;
			final double max = 2;
			final double duration = 3;
			InvocationSequenceData invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(5000d);
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstChildSequence.setDuration(200d);
			firstChildSequence.setId(2);
			secondChildSequence = new InvocationSequenceData(DEF_DATE, 20, 20, METHOD_IDENT);
			secondChildSequence.setDuration(4000d);
			secondChildSequence.setId(3);
			TimerData timerData = new TimerData();
			timerData.setTimeStamp(new Timestamp(timestampValue));
			timerData.setPlatformIdent(platformIdent);
			timerData.setCount(count);
			timerData.setExclusiveCount(count);
			timerData.setDuration(duration);
			timerData.setCpuDuration(duration);
			timerData.setExclusiveDuration(duration);
			timerData.calculateMin(min);
			timerData.calculateCpuMin(min);
			timerData.calculateExclusiveMin(min);
			timerData.calculateMax(max);
			timerData.calculateCpuMax(max);
			timerData.calculateExclusiveMax(max);
			timerData.setMethodIdent(50L);
			secondChildSequence.setTimerData(timerData);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(DEF_DATE, 30, 30, METHOD_IDENT);
			thirdChildSequence.setDuration(500d);
			thirdChildSequence.setId(4);
			invocationSequenceRoot.getNestedSequences().add(firstChildSequence);
			invocationSequenceRoot.getNestedSequences().add(secondChildSequence);
			invocationSequenceRoot.getNestedSequences().add(thirdChildSequence);
			causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = DiagnosisDataAggregator.getInstance();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);
			rootCause = new RootCause(aggregatedInvocationSequenceData.getMethodIdent(), aggregatedInvocationSequenceData.getAggregatedDiagnosisTimerData());
			problemOccurenceSet = new HashSet<ProblemOccurrence>();
		}

		@Test
		public void onNewDiagnosisResult() {
			ProblemOccurrence problemOccurrence = new ProblemOccurrence(secondChildSequence, secondChildSequence, secondChildSequence, rootCause, causeStructure.getCauseType(),
					causeStructure.getSourceType());
			problemOccurenceSet.add(problemOccurrence);
			when(diagnosisResults.getDiagnosisResults()).thenReturn(problemOccurenceSet);

			cmrProcessor.onNewDiagnosisResult(problemOccurrence);

			verify(problemOccurrencePointBuilder, times(1)).saveProblemOccurrenceToInflux(problemOccurrence);
			verifyNoMoreInteractions(problemOccurrencePointBuilder);
		}

		@Test
		public void onNewDiagnosisResultsMustContainsAllTheProblemOccurrences() {
			List<ProblemOccurrence> problemOccurrences = new ArrayList<ProblemOccurrence>();
			ProblemOccurrence firstProblemOccurrence = new ProblemOccurrence(secondChildSequence, secondChildSequence, secondChildSequence, rootCause, causeStructure.getCauseType(),
					causeStructure.getSourceType());
			ProblemOccurrence secondProblemOccurrence = new ProblemOccurrence(secondChildSequence, secondChildSequence, secondChildSequence, rootCause, causeStructure.getCauseType(),
					causeStructure.getSourceType());
			problemOccurrences.add(firstProblemOccurrence);
			problemOccurrences.add(secondProblemOccurrence);
			problemOccurenceSet.add(firstProblemOccurrence);
			problemOccurenceSet.add(secondProblemOccurrence);
			when(diagnosisResults.getDiagnosisResults()).thenReturn(problemOccurenceSet);

			cmrProcessor.onNewDiagnosisResult(problemOccurenceSet);

			verify(problemOccurrencePointBuilder, times(1)).saveProblemOccurrenceToInflux(firstProblemOccurrence);
			verify(problemOccurrencePointBuilder, times(1)).saveProblemOccurrenceToInflux(secondProblemOccurrence);
			verifyNoMoreInteractions(problemOccurrencePointBuilder);
		}
	}
}
