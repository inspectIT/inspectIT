package rocks.inspectit.server.diagnosis.service;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.SessionContext;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.server.diagnosis.service.aggregation.DiagnosisDataAggregator;
import rocks.inspectit.server.diagnosis.service.data.CauseCluster;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;


/**
 * @author Christian Voegele
 *
 */
@SuppressWarnings("PMD")
public class ProblemOccurrenceResultCollectorTest extends TestBase {

	@Mock
	SessionContext<InvocationSequenceData> sessionContext;

	@Mock
	DefaultRuleOutputStorage storage;

	public static class Collect extends ProblemOccurrenceResultCollectorTest {

		private static final long METHOD_IDENT = 108L;
		private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());
		boolean canBeProcessed = false;
		InvocationSequenceData invocationSequenceRoot;
		InvocationSequenceData firstChildSequence;
		InvocationSequenceData secondChildSequence;
		InvocationSequenceData thirdChildSequence;

		@BeforeMethod
		public void initSetup() {

			invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(5000d);

			firstChildSequence = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstChildSequence.setDuration(200d);
			firstChildSequence.setId(2);

			secondChildSequence = new InvocationSequenceData(DEF_DATE, 20, 20, METHOD_IDENT);
			secondChildSequence.setDuration(4000d);
			secondChildSequence.setId(3);
			long timestampValue = new Date().getTime();
			long platformIdent = new Random().nextLong();
			final long count = 2;
			final double min = 1;
			final double max = 2;
			final double duration = 3;
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

			thirdChildSequence = new InvocationSequenceData(DEF_DATE, 30, 30, METHOD_IDENT);
			thirdChildSequence.setDuration(500d);
			thirdChildSequence.setId(4);

			invocationSequenceRoot.getNestedSequences().add(firstChildSequence);
			invocationSequenceRoot.getNestedSequences().add(secondChildSequence);
			invocationSequenceRoot.getNestedSequences().add(thirdChildSequence);
		}

		@Test
		public void collectProblemInstances() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("Test", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 1);
		}


		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionGlobalContext() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, "Test", Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionGlobalContextNotFound() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag("Test", secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionProblemContext() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, "Test", tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionProblemContextNotFound() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag("Test", new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionRootCause() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, "Test", tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionRootCauseNotFound() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag("Test", aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionCauseStructure() {
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE, "Test", tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionCauseStructureNotFound() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			DiagnosisDataAggregator aggregator = new DiagnosisDataAggregator();
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = aggregator.getAggregatedDiagnosisData(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT, new CauseCluster(secondChildSequence), tagOne);
			Tag tagThree = new Tag(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag("Test", causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurrenceResultCollector problemInstanceResultCollector = new ProblemOccurrenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);

			assertEquals(problemOccurrence.size(), 0);
		}

	}

}
