package rocks.inspectit.server.diagnosis.engine.tag;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Alexander Wert
 *
 */
public class TagsTest extends TestBase {

	/**
	 * Tests the {@link rocks.inspectit.server.diagnosis.engine.tag.Tags#rootTag(Object)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class RootTag extends TagsTest {
		@Test
		public void validRootTagInput() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tag = rocks.inspectit.server.diagnosis.engine.tag.Tags.rootTag("TestInput");

			assertThat(tag.getType(), equalTo(rocks.inspectit.server.diagnosis.engine.tag.Tags.ROOT_TAG));
			assertThat(tag.getValue().toString(), equalTo("TestInput"));
			assertThat(tag.getParent(), equalTo(null));
			assertThat(tag.getState(), equalTo(TagState.LEAF));
		}

		@Test
		public void nullRootTagInput() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tag = rocks.inspectit.server.diagnosis.engine.tag.Tags.rootTag(null);

			assertThat(tag.getType(), equalTo(rocks.inspectit.server.diagnosis.engine.tag.Tags.ROOT_TAG));
			assertThat(tag.getValue(), equalTo(null));
			assertThat(tag.getParent(), equalTo(null));
			assertThat(tag.getState(), equalTo(TagState.LEAF));
		}
	}

	/**
	 * Tests the {@link rocks.inspectit.server.diagnosis.engine.tag.Tags#tag(String, Object))}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Tag extends TagsTest {
		@Test
		public void nullParentTag() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tag = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("A", "TestInput");

			assertThat(tag.getType(), equalTo("A"));
			assertThat(tag.getValue().toString(), equalTo("TestInput"));
			assertThat(tag.getParent(), equalTo(null));
			assertThat(tag.getState(), equalTo(TagState.LEAF));
		}

		@Test
		public void withParentTag() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagA = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("A", "TestInput");

			assertThat(tagA.getState(), equalTo(TagState.LEAF));

			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "TestInput", tagA);

			assertThat(tagB.getType(), equalTo("B"));
			assertThat(tagB.getValue().toString(), equalTo("TestInput"));
			assertThat(tagB.getParent(), equalTo(tagA));
			assertThat(tagB.getState(), equalTo(TagState.LEAF));
			assertThat(tagA.getState(), equalTo(TagState.PARENT));
		}
	}

	/**
	 * Tests the
	 * {@link rocks.inspectit.server.diagnosis.engine.tag.Tags#tags(String, Tag, java.util.Collection)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Tags extends TagsTest {
		@Test
		public void nullParentTag() {
			Collection<rocks.inspectit.server.diagnosis.engine.tag.Tag> tags = rocks.inspectit.server.diagnosis.engine.tag.Tags.tags("A", null, "TestInput1", "TestInput2");

			rocks.inspectit.server.diagnosis.engine.tag.Tag[] tagArray = tags.toArray(new rocks.inspectit.server.diagnosis.engine.tag.Tag[0]);

			assertThat(tags, hasSize(2));

			assertThat(tagArray[0].getType(), equalTo("A"));
			assertThat(tagArray[1].getType(), equalTo("A"));
			assertThat(tagArray[0].getValue().toString(), equalTo("TestInput1"));
			assertThat(tagArray[1].getValue().toString(), equalTo("TestInput2"));
			assertThat(tagArray[0].getParent(), equalTo(null));
			assertThat(tagArray[1].getParent(), equalTo(null));
			assertThat(tagArray[0].getState(), equalTo(TagState.LEAF));
			assertThat(tagArray[1].getState(), equalTo(TagState.LEAF));
		}

		@Test
		public void withParentTag() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagA = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("A", "TestInput");

			Collection<rocks.inspectit.server.diagnosis.engine.tag.Tag> tags = rocks.inspectit.server.diagnosis.engine.tag.Tags.tags("B", tagA, "TestInput1", "TestInput2");

			rocks.inspectit.server.diagnosis.engine.tag.Tag[] tagArray = tags.toArray(new rocks.inspectit.server.diagnosis.engine.tag.Tag[0]);

			assertThat(tags, hasSize(2));

			assertThat(tagArray[0].getType(), equalTo("B"));
			assertThat(tagArray[1].getType(), equalTo("B"));
			assertThat(tagArray[0].getValue().toString(), equalTo("TestInput1"));
			assertThat(tagArray[1].getValue().toString(), equalTo("TestInput2"));
			assertThat(tagArray[0].getParent(), equalTo(tagA));
			assertThat(tagArray[1].getParent(), equalTo(tagA));
			assertThat(tagArray[0].getState(), equalTo(TagState.LEAF));
			assertThat(tagArray[1].getState(), equalTo(TagState.LEAF));
			assertThat(tagA.getState(), equalTo(TagState.PARENT));
		}
	}

	/**
	 * Tests the {@link rocks.inspectit.server.diagnosis.engine.tag.Tags#unwrap(Tag, Collection)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Unwrap extends TagsTest {
		@Test
		public void unwrapValid() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagA = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("A", "IN-A");
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B1", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B2", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C1", tagB1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C2", tagB2);
			rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D1", tagC1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagD2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D2", tagC2);

			Collection<rocks.inspectit.server.diagnosis.engine.tag.Tag> tags = rocks.inspectit.server.diagnosis.engine.tag.Tags.unwrap(tagD2, Arrays.asList(new String[] { "A", "B", "D" }));

			assertThat(tags, hasSize(3));
			assertThat(tags, containsInAnyOrder(tagA, tagB2, tagD2));
			assertThat(tags, not(contains(tagB1)));
		}

		@Test
		public void parentFilterMoreGeneric() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagA = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("A", "IN-A");
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B1", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B2", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C1", tagB1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C2", tagB2);
			rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D1", tagC1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagD2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D2", tagC2);

			Collection<rocks.inspectit.server.diagnosis.engine.tag.Tag> tags = rocks.inspectit.server.diagnosis.engine.tag.Tags.unwrap(tagD2, Arrays.asList(new String[] { "A", "B", "D", "E" }));

			assertThat(tags, hasSize(3));
			assertThat(tags, containsInAnyOrder(tagA, tagB2, tagD2));
			assertThat(tags, not(contains(tagB1)));
		}

		@Test
		public void filterInReverseOrder() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagA = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("A", "IN-A");
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B1", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B2", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C1", tagB1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C2", tagB2);
			rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D1", tagC1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagD2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D2", tagC2);

			Collection<rocks.inspectit.server.diagnosis.engine.tag.Tag> tags = rocks.inspectit.server.diagnosis.engine.tag.Tags.unwrap(tagD2, Arrays.asList(new String[] { "D", "B", "A", }));

			assertThat(tags, hasSize(3));
			assertThat(tags, containsInAnyOrder(tagA, tagB2, tagD2));
			assertThat(tags, not(contains(tagB1)));
		}

		@Test
		public void filterNotMatchingAtAll() {
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagA = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("A", "IN-A");
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B1", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagB2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("B", "IN-B2", tagA);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC1 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C1", tagB1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagC2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("C", "IN-C2", tagB2);
			rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D1", tagC1);
			rocks.inspectit.server.diagnosis.engine.tag.Tag tagD2 = rocks.inspectit.server.diagnosis.engine.tag.Tags.tag("D", "IN-D2", tagC2);

			Collection<rocks.inspectit.server.diagnosis.engine.tag.Tag> tags = rocks.inspectit.server.diagnosis.engine.tag.Tags.unwrap(tagD2, Arrays.asList(new String[] { "X", "Y", "Z" }));

			assertThat(tags, hasSize(1));
			assertThat(tags, containsInAnyOrder(tagD2));
		}
	}
}
