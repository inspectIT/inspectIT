package rocks.inspectit.server.diagnosis.engine.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.rule.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link DefaultSessionResultCollector} class.
 *
 * @author Alexander Wert
 *
 */
public class DefaultSessionResultCollectorTest extends TestBase {
	@InjectMocks
	DefaultSessionResultCollector<String> collector;

	@Mock
	SessionContext<String> sessionContext;

	@Mock
	DefaultRuleOutputStorage storage;

	/**
	 * Tests the {@link DefaultSessionResultCollector#collect(SessionContext)} method;
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Collect extends DefaultSessionResultCollectorTest {
		@Test
		public void collect() {
			String input = "input";
			Multimap<String, RuleOutput> outputMap = ArrayListMultimap.create();
			Tag tagA = Tags.tag("A", input, Tags.rootTag(input));
			Tag tagB = Tags.tag("B", input, tagA);
			Tag tagC = Tags.tag("C", input, tagB);
			Tag tagD1 = Tags.tag("D1", input, tagC);
			Tag tagD2 = Tags.tag("D2", input, tagC);
			ConditionFailure cFailure1 = new ConditionFailure("Cond1", "test hint");
			ConditionFailure cFailure2 = new ConditionFailure("Cond2", "test hint2");
			RuleOutput output1 = new RuleOutput("RuleA", "A", Arrays.asList(cFailure1), Arrays.asList(tagA));
			RuleOutput output2 = new RuleOutput("RuleB", "B", Arrays.asList(cFailure2), Arrays.asList(tagB));
			RuleOutput output3 = new RuleOutput("RuleC", "C", Collections.<ConditionFailure> emptyList(), Arrays.asList(tagC));
			RuleOutput output4 = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptyList(), Arrays.asList(tagD1, tagD2));
			outputMap.put(output1.getRuleName(), output1);
			outputMap.put(output2.getRuleName(), output2);
			outputMap.put(output3.getRuleName(), output3);
			outputMap.put(output4.getRuleName(), output4);
			Multimap<String, Tag> tagMap = ArrayListMultimap.create();
			tagMap.put("D", tagD1);
			tagMap.put("D", tagD2);
			when(sessionContext.getStorage()).thenReturn(storage);
			when(sessionContext.getInput()).thenReturn(input);
			when(storage.getAllOutputsWithConditionFailures()).thenReturn(outputMap);
			when(storage.mapTags(TagState.LEAF)).thenReturn(tagMap);

			DefaultSessionResult<String> result = collector.collect(sessionContext);

			assertThat(result.getInput(), equalTo(input));
			assertThat(result.getEndTags().values(), containsInAnyOrder(tagD1, tagD2));

			assertThat(result.getConditionFailures().values(), containsInAnyOrder(cFailure1, cFailure2));
		}
	}
}
