package rocks.inspectit.agent.java.eum.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class CaretTest extends TestBase {

	public static class Get extends CaretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Caret c1 = new Caret(test);
			assertThat(c1.get(0), equalTo('0'));
			assertThat(c1.get(3), equalTo('3'));

			Caret c2 = new Caret(test, 5);
			assertThat(c2.get(1), equalTo('6'));

			Caret c3 = c2.copy();
			assertThat(c3.get(2), equalTo('7'));

			c3.goN(2);
			assertThat(c2.get(1), equalTo('6'));
		}
	}

	public static class GoN extends CaretTest {

		@Test
		public void test() {
			String test = "0123456789";

			Caret c1 = new Caret(test);
			c1.goN(2);
			assertThat(c1.get(0), equalTo('2'));

			c1.goN(3);
			assertThat(c1.get(0), equalTo('5'));
		}
	}

	public static class GoTo extends CaretTest {

		@Test
		public void test() {
			String test = "0123456789";

			Caret c1 = new Caret(test, 5);
			c1.goTo(2);
			assertThat(c1.get(0), equalTo('2'));

			c1.goTo(7);
			assertThat(c1.get(0), equalTo('7'));
		}
	}

	public static class GetOffset extends CaretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Caret c1 = new Caret(test, 2);

			c1.goN(1);
			assertThat(c1.getOffset(), equalTo(3));

			c1.goN(2);
			assertThat(c1.getOffset(), equalTo(5));
		}
	}

	public static class WayToEnd extends CaretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Caret c1 = new Caret(test, 2);

			c1.goN(1);
			assertThat(c1.wayToEnd(), equalTo(7));

			c1.goN(2);
			assertThat(c1.wayToEnd(), equalTo(5));

			c1.goN(5);
			assertThat(c1.wayToEnd(), equalTo(0));
		}
	}

	public static class EndReached extends CaretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Caret c1 = new Caret(test, 2);

			assertThat(c1.endReached(), equalTo(false));

			c1.goTo(10);
			assertThat(c1.endReached(), equalTo(true));

			c1.goTo(5);
			assertThat(c1.endReached(), equalTo(false));
		}
	}

	public static class WalkAfterCharCheckCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Caret c = new Caret(test);

			assertThat(c.walkAfterCharCheckCase('C'), equalTo(true));
			assertThat(c.get(0), equalTo('D'));

			c.goTo(0);
			assertThat(c.walkAfterCharCheckCase('c'), equalTo(true));
			assertThat(c.get(0), equalTo('d'));

			c.goTo(0);
			assertThat(c.walkAfterCharCheckCase('z'), equalTo(false));
			assertThat(c.endReached(), equalTo(true));
		}
	}

	public static class WalkToCharCheckCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Caret c = new Caret(test);

			assertThat(c.walkToCharCheckCase('C'), equalTo(true));
			assertThat(c.get(0), equalTo('C'));

			c.goTo(0);
			assertThat(c.walkToCharCheckCase('c'), equalTo(true));
			assertThat(c.get(0), equalTo('c'));

			c.goTo(0);
			assertThat(c.walkToCharCheckCase('z'), equalTo(false));
			assertThat(c.endReached(), equalTo(true));
		}
	}

	public static class WalkAfterWhitespace extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r \nabcdefg  ";
			Caret c = new Caret(test);

			c.walkAfterWhitespaces();
			assertThat(c.get(0), equalTo('A'));

			c.walkAfterWhitespaces();
			assertThat(c.get(0), equalTo('A'));

			c.walkAfterCharCheckCase('H');
			c.walkAfterWhitespaces();
			assertThat(c.get(0), equalTo('a'));

			c.walkAfterCharCheckCase('g');
			c.walkAfterWhitespaces();
			assertThat(c.endReached(), equalTo(true));
		}
	}

	public static class WalkToWhitespace extends CaretTest {

		@Test
		public void test() {
			String test = "ABCDEFGH\t\r \nabcdefg  h";
			Caret c = new Caret(test);

			c.walkToWhitespace();
			assertThat(c.get(0), equalTo('\t'));
			c.walkToWhitespace();
			assertThat(c.get(0), equalTo('\t'));

			c.goN(1);
			c.walkToWhitespace();
			assertThat(c.get(0), equalTo('\r'));

			c.goN(4);
			c.walkToWhitespace();
			assertThat(c.get(0), equalTo(' '));

			c.goN(2);
			assertThat(c.walkToWhitespace(), equalTo(false));

		}
	}

	public static class WalkBackBeforeWhitespaces extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r \nabcdefg";

			Caret c = new Caret(test);
			c.walkToCharCheckCase('A');

			c.walkBackBeforeWhitespaces();
			assertThat(c.get(0), equalTo('A'));

			c.goN(-1);
			c.walkBackBeforeWhitespaces();
			assertThat(c.getOffset(), equalTo(0));

			c.walkToCharCheckCase('a');
			c.goN(-1);
			c.walkBackBeforeWhitespaces();
			assertThat(c.get(0), equalTo('H'));
		}
	}

	public static class StartsWithIgnoreCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Caret c = new Caret(test, 4);

			assertThat(c.startsWithIgnoreCase("def"), equalTo(false));
			assertThat(c.startsWithIgnoreCase("cdef"), equalTo(true));
			assertThat(c.startsWithIgnoreCase("CDEF"), equalTo(true));
		}
	}

	public static class StartsWithCheckCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Caret c = new Caret(test, 4);

			assertThat(c.startsWithCheckCase("def"), equalTo(false));
			assertThat(c.startsWithCheckCase("cdef"), equalTo(false));
			assertThat(c.startsWithCheckCase("CDEF"), equalTo(true));
		}
	}

	public static class WalkToMatchCheckCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Caret c = new Caret(test, 0);

			assertThat(c.walkToMatchCheckCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('c'));

			c.goN(1);
			assertThat(c.walkToMatchCheckCase("cdefg"), equalTo(false));
		}
	}

	public static class WalkToMatchIgnoreCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Caret c = new Caret(test, 0);

			assertThat(c.walkToMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('C'));

			c.goN(1);
			assertThat(c.walkToMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('c'));

			c.goN(1);
			assertThat(c.walkToMatchIgnoreCase("cdefg"), equalTo(false));
		}
	}

	public static class WalkAfterMatchCheckCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Caret c = new Caret(test, 0);

			assertThat(c.walkAfterMatchCheckCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('h'));

			c.goN(1);
			assertThat(c.walkAfterMatchCheckCase("cdefg"), equalTo(false));
		}
	}

	public static class WalkAfterMatchIgnoreCase extends CaretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Caret c = new Caret(test, 0);

			assertThat(c.walkAfterMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('H'));

			assertThat(c.walkAfterMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('h'));

			c.goN(1);
			assertThat(c.walkAfterMatchIgnoreCase("cdefg"), equalTo(false));
		}
	}

}
