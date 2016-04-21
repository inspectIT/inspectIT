package rocks.inspectit.server.diagnosis.engine.rule.store;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.rule.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link DefaultRuleOutputStorage} class.
 *
 * @author Alexander Wert
 *
 */
public class DefaultRuleOutputStorageTest extends TestBase {
	@InjectMocks
	DefaultRuleOutputStorage storage;

	/**
	 * Tests the {@link DefaultRuleOutputStorage#store(java.util.Collection)} and
	 * {@link DefaultRuleOutputStorage#store(rocks.inspectit.server.diagnosis.engine.rule.RuleOutput)}
	 * methods.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Store extends DefaultRuleOutputStorageTest {
		@Test
		public void storeWithoutConditionFailures() {
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagA = Tags.tag("A", "inputA", rootTag);
			RuleOutput output = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagA));

			storage.store(output);

			assertThat(storage.getAvailableTagTypes(), containsInAnyOrder("A"));
			assertThat(storage.getAllOutputs().keySet(), hasSize(1));
			assertThat(storage.getAllOutputs().keySet(), containsInAnyOrder("A"));
			assertThat(storage.getAllOutputs().get("A"), containsInAnyOrder(output));
			assertThat(storage.getAllOutputsWithConditionFailures().keySet(), hasSize(0));
		}

		@Test
		public void storeMultipleWithoutConditionFailures() {
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagA = Tags.tag("A", "inputA", rootTag);
			Tag tagB = Tags.tag("B", "inputB", rootTag);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagA));
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagB));

			storage.store(Arrays.asList(outputA, outputB));

			assertThat(storage.getAvailableTagTypes(), containsInAnyOrder("A", "B"));
			assertThat(storage.getAllOutputs().keySet(), hasSize(2));
			assertThat(storage.getAllOutputs().keySet(), containsInAnyOrder("A", "B"));
			assertThat(storage.getAllOutputs().get("A"), containsInAnyOrder(outputA));
			assertThat(storage.getAllOutputs().get("B"), containsInAnyOrder(outputB));
			assertThat(storage.getAllOutputsWithConditionFailures().keySet(), hasSize(0));
		}

		@Test
		public void storeWithConditionFailures() {
			ConditionFailure failure = new ConditionFailure("ConditionX", "SomeHint");
			RuleOutput output = new RuleOutput("RuleA", "A", Collections.singleton(failure), Collections.<Tag> emptySet());

			storage.store(output);

			assertThat(storage.getAvailableTagTypes(), empty());
			assertThat(storage.getAllOutputs().keySet(), empty());
			assertThat(storage.getAllOutputsWithConditionFailures().keySet(), hasSize(1));
			assertThat(storage.getAllOutputsWithConditionFailures().get("A"), containsInAnyOrder(output));
		}

		@Test
		public void storeMultipleWithConditionFailures() {
			ConditionFailure failure1 = new ConditionFailure("ConditionX", "SomeHint");
			ConditionFailure failure2 = new ConditionFailure("ConditionY", "SomeHint");
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.singleton(failure1), Collections.<Tag> emptySet());
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.singleton(failure2), Collections.<Tag> emptySet());

			storage.store(Arrays.asList(outputA, outputB));

			assertThat(storage.getAvailableTagTypes(), empty());
			assertThat(storage.getAllOutputs().keySet(), empty());
			assertThat(storage.getAllOutputsWithConditionFailures().keySet(), hasSize(2));
			assertThat(storage.getAllOutputsWithConditionFailures().get("A"), containsInAnyOrder(outputA));
			assertThat(storage.getAllOutputsWithConditionFailures().get("B"), containsInAnyOrder(outputB));
		}

		@Test
		public void storeWithAndWithoutConditionFailure() {
			ConditionFailure failure = new ConditionFailure("ConditionX", "SomeHint");
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.singleton(failure), Collections.<Tag> emptySet());
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagB = Tags.tag("B", "inputB", rootTag);
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagB));

			storage.store(Arrays.asList(outputA, outputB));

			assertThat(storage.getAvailableTagTypes(), containsInAnyOrder("B"));
			assertThat(storage.getAllOutputs().keySet(), hasSize(1));
			assertThat(storage.getAllOutputs().keySet(), containsInAnyOrder("B"));
			assertThat(storage.getAllOutputs().get("A"), empty());
			assertThat(storage.getAllOutputs().get("B"), containsInAnyOrder(outputB));
			assertThat(storage.getAllOutputsWithConditionFailures().keySet(), hasSize(1));
			assertThat(storage.getAllOutputsWithConditionFailures().get("A"), containsInAnyOrder(outputA));
		}

	}

	/**
	 * Tests the
	 * {@link DefaultRuleOutputStorage#mapTags(rocks.inspectit.server.diagnosis.engine.tag.TagState)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class MapTags extends DefaultRuleOutputStorageTest {

		@Test
		public void mapSimpleLeafs() {
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagA = Tags.tag("A", "inputA", rootTag);
			Tag tagB = Tags.tag("B", "inputB", rootTag);
			Tag tagC = Tags.tag("C", "inputC", tagB);
			Tag tagD = Tags.tag("D", "inputD", tagA);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagA));
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagB));
			RuleOutput outputC = new RuleOutput("RuleC", "C", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagC));
			RuleOutput outputD = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD));
			storage.store(Arrays.asList(outputA, outputB, outputC, outputD));

			Multimap<String, Tag> tags = storage.mapTags(TagState.LEAF);

			assertThat(tags.size(), equalTo(2));
			assertThat(tags.keySet(), hasSize(2));
			assertThat(tags.get("A"), empty());
			assertThat(tags.get("B"), empty());
			assertThat(tags.get("C"), containsInAnyOrder(tagC));
			assertThat(tags.get("D"), containsInAnyOrder(tagD));
		}

		@Test
		public void mapMultiTagLeafs() {
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagA = Tags.tag("A", "inputA", rootTag);
			Tag tagB = Tags.tag("B", "inputB", rootTag);
			Tag tagC1 = Tags.tag("C", "inputC1", tagB);
			Tag tagC2 = Tags.tag("C", "inputC2", tagB);
			Tag tagD1 = Tags.tag("D", "inputD1", tagA);
			Tag tagD2 = Tags.tag("D", "inputD2", tagA);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagA));
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagB));
			RuleOutput outputC = new RuleOutput("RuleC", "C", Collections.<ConditionFailure> emptySet(), Arrays.asList(tagC1, tagC2));
			RuleOutput outputD1 = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD1));
			RuleOutput outputD2 = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD2));
			storage.store(Arrays.asList(outputA, outputB, outputC, outputD1, outputD2));

			Multimap<String, Tag> tags = storage.mapTags(TagState.LEAF);

			assertThat(tags.size(), equalTo(4));
			assertThat(tags.keySet(), hasSize(2));
			assertThat(tags.get("A"), empty());
			assertThat(tags.get("B"), empty());
			assertThat(tags.get("C"), containsInAnyOrder(tagC1, tagC2));
			assertThat(tags.get("D"), containsInAnyOrder(tagD1, tagD2));
		}

		@Test
		public void mapWithConditionFailuresLeafs() {
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagA = Tags.tag("A", "inputA", rootTag);
			Tag tagB = Tags.tag("B", "inputB", rootTag);
			Tags.tag("C", "inputC", tagB);
			ConditionFailure failure = new ConditionFailure("ConditionX", "SomeHint");
			Tag tagD = Tags.tag("D", "inputD", tagA);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagA));
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagB));
			RuleOutput outputC = new RuleOutput("RuleC", "C", Collections.singleton(failure), Collections.<Tag> emptySet());
			RuleOutput outputD = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD));
			storage.store(Arrays.asList(outputA, outputB, outputC, outputD));

			Multimap<String, Tag> tags = storage.mapTags(TagState.LEAF);

			assertThat(tags.size(), equalTo(1));
			assertThat(tags.get("A"), empty());
			assertThat(tags.get("B"), empty());
			assertThat(tags.get("C"), empty());
			assertThat(tags.get("D"), containsInAnyOrder(tagD));
		}
	}

	/**
	 * Tests the {@link DefaultRuleOutputStorage#findLatestResultsByTagType(java.util.Set)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class FindLatestResultsByTagType extends DefaultRuleOutputStorageTest {
		@Test
		public void findWithSingleTagType() {
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagA = Tags.tag("A", "inputA", rootTag);
			Tag tagB = Tags.tag("B", "inputB", rootTag);
			Tag tagC1 = Tags.tag("C", "inputC1", tagB);
			Tag tagC2 = Tags.tag("C", "inputC2", tagB);
			Tag tagD1 = Tags.tag("D", "inputD1", tagA);
			Tag tagD2 = Tags.tag("D", "inputD2", tagA);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagA));
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagB));
			RuleOutput outputC = new RuleOutput("RuleC", "C", Collections.<ConditionFailure> emptySet(), Arrays.asList(tagC1, tagC2));
			RuleOutput outputD1 = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD1));
			RuleOutput outputD2 = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD2));
			storage.store(Arrays.asList(outputA, outputB, outputC, outputD1, outputD2));

			Collection<RuleOutput> outputs = storage.findLatestResultsByTagType(new HashSet<>(Arrays.asList("D")));

			assertThat(outputs, hasSize(2));
			assertThat(outputs, containsInAnyOrder(outputD1, outputD2));
		}

		@Test
		public void findWithMultipleTagTypes() {
			Tag rootTag = Tags.tag(Tags.ROOT_TAG, "root");
			Tag tagA = Tags.tag("A", "inputA", rootTag);
			Tag tagB = Tags.tag("B", "inputB", rootTag);
			Tag tagC1 = Tags.tag("C", "inputC1", tagB);
			Tag tagC2 = Tags.tag("C", "inputC2", tagB);
			Tag tagD1 = Tags.tag("D", "inputD1", tagA);
			Tag tagD2 = Tags.tag("D", "inputD2", tagA);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagA));
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagB));
			RuleOutput outputC = new RuleOutput("RuleC", "C", Collections.<ConditionFailure> emptySet(), Arrays.asList(tagC1, tagC2));
			RuleOutput outputD1 = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD1));
			RuleOutput outputD2 = new RuleOutput("RuleD", "D", Collections.<ConditionFailure> emptySet(), Collections.singleton(tagD2));
			storage.store(Arrays.asList(outputA, outputB, outputC, outputD1, outputD2));

			Collection<RuleOutput> outputs = storage.findLatestResultsByTagType(new HashSet<>(Arrays.asList("D", "C")));

			assertThat(outputs, hasSize(3));
			assertThat(outputs, containsInAnyOrder(outputD1, outputD2, outputC));
		}
	}

}
