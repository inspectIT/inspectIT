package rocks.inspectit.agent.java.eum.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class CharSequenceUtilTest extends TestBase {

	public static class checkEqualCheckCaseTest extends TestBase {

		@Test
		public void testEqualCaseNoOffset() {
			String a = "abcd";
			String b = "abcd";

			assertThat(CharSequenceUtil.checkEqualCheckCase(a, b), equalTo(true));
			assertThat(CharSequenceUtil.checkEqualCheckCase(a, 0, 4, b, 0, 4), equalTo(true));
		}

		@Test
		public void testNonEqualCase() {
			String a = "abcd";
			String b = "aBcd";

			assertThat(CharSequenceUtil.checkEqualCheckCase(a, b), equalTo(false));
			assertThat(CharSequenceUtil.checkEqualCheckCase(a, 0, 4, b, 0, 4), equalTo(false));
		}

		@Test
		public void testInvalidLengths() {
			String a1 = "abcd";
			String b1 = "abcde";

			String a2 = "abcde";
			String b2 = "abcd";

			assertThat(CharSequenceUtil.checkEqualCheckCase(a1, 0, 5, b1, 0, 5), equalTo(false));
			assertThat(CharSequenceUtil.checkEqualCheckCase(a2, 0, 5, b2, 0, 5), equalTo(false));
		}

		@Test
		public void testNonMatching() {
			String a = "abcd";
			String b = "efgh";

			assertThat(CharSequenceUtil.checkEqualCheckCase(a, b), equalTo(false));
			assertThat(CharSequenceUtil.checkEqualCheckCase(a, 0, 4, b, 0, 4), equalTo(false));
		}

		@Test
		public void testEqualCaseWithOffset() {
			String a = "12abcd34";
			String b = "34abcd56";

			assertThat(CharSequenceUtil.checkEqualCheckCase(a, 2, 4, b, 2, 4), equalTo(true));
		}

	}

	public static class checkEqualCheckIgnoreTest extends TestBase {

		@Test
		public void testEqualCaseNoOffset() {
			String a = "abcd";
			String b = "abcd";

			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, b), equalTo(true));
			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, 0, 4, b, 0, 4), equalTo(true));
		}

		@Test
		public void testNonEqualCaseNoOffset() {
			String a = "abcd";
			String b = "aBcd";

			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, b), equalTo(true));
			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, 0, 4, b, 0, 4), equalTo(true));
		}

		@Test
		public void testNonMatching() {
			String a = "abcd";
			String b = "efgh";

			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, b), equalTo(false));
			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, 0, 4, b, 0, 4), equalTo(false));
		}

		@Test
		public void testInvalidLengths() {
			String a1 = "abcd";
			String b1 = "abcde";

			String a2 = "abcde";
			String b2 = "abcd";

			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a1, 0, 5, b1, 0, 5), equalTo(false));
			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a2, 0, 5, b2, 0, 5), equalTo(false));
		}

		@Test
		public void testEqualCaseWithOffset() {
			String a = "12abcd34";
			String b = "34abcd56";

			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, 2, 4, b, 2, 4), equalTo(true));
		}

		@Test
		public void testNonEqualCaseWithOffset() {
			String a = "12abcd34";
			String b = "34abCD56";

			assertThat(CharSequenceUtil.checkEqualIgnoreCase(a, 2, 4, b, 2, 4), equalTo(true));
		}

	}

}
