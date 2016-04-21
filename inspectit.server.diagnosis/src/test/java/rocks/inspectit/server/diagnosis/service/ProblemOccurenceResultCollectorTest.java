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
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.diagnosis.results.CauseStructure;
import rocks.inspectit.shared.all.communication.data.diagnosis.results.CauseStructure.CauseType;
import rocks.inspectit.shared.all.communication.data.diagnosis.results.ProblemOccurrence;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.InvocationSequenceDataAggregator;

/**
 * @author Christian Voegele
 *
 */
@SuppressWarnings("PMD")
public class ProblemOccurenceResultCollectorTest extends TestBase {

	@Mock
	SessionContext<InvocationSequenceData> sessionContext;

	@Mock
	DefaultRuleOutputStorage storage;

	public static class Collect extends ProblemOccurenceResultCollectorTest {

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
			CauseStructure causeStructure = new CauseStructure(CauseType.SINGLE, 0);
			InvocationSequenceDataAggregator aggregator = new InvocationSequenceDataAggregator();
			AggregatedInvocationSequenceData aggregatedInvocationSequenceData = null;
			aggregatedInvocationSequenceData = (AggregatedInvocationSequenceData) aggregator.getClone(secondChildSequence);
			aggregator.aggregate(aggregatedInvocationSequenceData, secondChildSequence);

			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			Tag tagOne = new Tag(RuleConstants.TAG_GLOBAL_CONTEXT, secondChildSequence, Tags.rootTag(secondChildSequence));
			Tag tagTwo = new Tag(RuleConstants.TAG_PROBLEM_CONTEXT, secondChildSequence, tagOne);
			Tag tagThree = new Tag(RuleConstants.TAG_PROBLEM_CAUSE, aggregatedInvocationSequenceData, tagTwo);
			Tag tagFour = new Tag(RuleConstants.TAG_CAUSE_STRUCTURE, causeStructure, tagThree);

			tagMap.put("D", tagFour);

			when(sessionContext.getInput()).thenReturn(secondChildSequence);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			ProblemOccurenceResultCollector problemInstanceResultCollector = new ProblemOccurenceResultCollector();
			List<ProblemOccurrence> problemOccurrence = problemInstanceResultCollector.collect(sessionContext);
			assertEquals(problemOccurrence.size(), 1);
		}

	}

}
