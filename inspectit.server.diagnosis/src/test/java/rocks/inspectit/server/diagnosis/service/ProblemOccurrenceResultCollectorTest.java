package rocks.inspectit.server.diagnosis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

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
		private InvocationSequenceData invocationSequenceRoot;
		private InvocationSequenceData firstChildSequence;
		private InvocationSequenceData secondChildSequence;
		private InvocationSequenceData thirdChildSequence;
		private static final long TIMESTAMP_VALUE = new Date().getTime();
		private static final long PLATFORM_IDENT = new Random().nextLong();
		private static final long COUNT = 2;
		private static final double MIN = 1;
		private static final double MAX = 2;
		private static final double DURATION = 3;
		private static final int FIRST_ID = 1;
		private static final int SECOND_ID = 2;
		private static final int THIRD_ID = 3;
		private static final int FOURTH_ID = 4;
		private TimerData timerData = new TimerData();
		private static final double ROOT_DURATION = 5000d;
		private static final double FIRST_DURATION = 200d;
		private static final double SECOND_DURATION = 4000d;
		private static final double THIRD_DURATION = 500d;
		private static final long TIMER_DATA_IDENT = 50L;

		@BeforeMethod
		public void initSetup() {
			invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(FIRST_ID);
			invocationSequenceRoot.setDuration(ROOT_DURATION);
			firstChildSequence = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstChildSequence.setDuration(FIRST_DURATION);
			firstChildSequence.setId(SECOND_ID);
			secondChildSequence = new InvocationSequenceData(DEF_DATE, 20, 20, METHOD_IDENT);
			secondChildSequence.setDuration(SECOND_DURATION);
			secondChildSequence.setId(THIRD_ID);
			timerData.setTimeStamp(new Timestamp(TIMESTAMP_VALUE));
			timerData.setPlatformIdent(PLATFORM_IDENT);
			timerData.setCount(COUNT);
			timerData.setExclusiveCount(COUNT);
			timerData.setDuration(DURATION);
			timerData.setCpuDuration(DURATION);
			timerData.setExclusiveDuration(DURATION);
			timerData.calculateMin(MIN);
			timerData.calculateCpuMin(MIN);
			timerData.calculateExclusiveMin(MIN);
			timerData.calculateMax(MAX);
			timerData.calculateCpuMax(MAX);
			timerData.calculateExclusiveMax(MAX);
			timerData.setMethodIdent(TIMER_DATA_IDENT);
			secondChildSequence.setTimerData(timerData);
			thirdChildSequence = new InvocationSequenceData(DEF_DATE, 30, 30, METHOD_IDENT);
			thirdChildSequence.setDuration(THIRD_DURATION);
			thirdChildSequence.setId(FOURTH_ID);
			invocationSequenceRoot.getNestedSequences().add(firstChildSequence);
			invocationSequenceRoot.getNestedSequences().add(secondChildSequence);
			invocationSequenceRoot.getNestedSequences().add(thirdChildSequence);
		}

		@Test
		public void collectProblemInstances() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			AggregatedDiagnosisData aggregatedInvocationSequenceData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(secondChildSequence);
			DiagnosisDataAggregator.getInstance().aggregate(aggregatedInvocationSequenceData, secondChildSequence);
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

			assertThat(problemOccurrence, hasSize(1));
		}


		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionGlobalContext() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(secondChildSequence);
			DiagnosisDataAggregator.getInstance().aggregate(aggregatedInvocationSequenceData, secondChildSequence);
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

			assertThat(problemOccurrence, hasSize(0));
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionProblemContext() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(secondChildSequence);
			DiagnosisDataAggregator.getInstance().aggregate(aggregatedInvocationSequenceData, secondChildSequence);
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

			assertThat(problemOccurrence, hasSize(0));
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionRootCause() {
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, SourceType.TIMERDATA);
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(secondChildSequence);
			DiagnosisDataAggregator.getInstance().aggregate(aggregatedInvocationSequenceData, secondChildSequence);
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

			assertThat(problemOccurrence, hasSize(0));
		}

		@Test(expectedExceptions = RuntimeException.class)
		public void collectProblemInstancesWithRuntimeExceptionCauseStructure() {
			AggregatedDiagnosisData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = DiagnosisDataAggregator.getInstance().getAggregatedDiagnosisData(secondChildSequence);
			DiagnosisDataAggregator.getInstance().aggregate(aggregatedInvocationSequenceData, secondChildSequence);
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

			assertThat(problemOccurrence, hasSize(0));
		}

	}
}
