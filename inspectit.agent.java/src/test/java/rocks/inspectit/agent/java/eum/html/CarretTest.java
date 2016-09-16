package rocks.inspectit.agent.java.eum.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import rocks.inspectit.agent.java.eum.html.Carret;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class CarretTest extends TestBase {

	public static class Get extends CarretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Carret c1 = new Carret(test);
			assertThat(c1.get(0), equalTo('0'));
			assertThat(c1.get(3), equalTo('3'));

			Carret c2 = new Carret(test, 5);
			assertThat(c2.get(1), equalTo('6'));

			Carret c3 = c2.copy();
			assertThat(c3.get(2), equalTo('7'));

			c3.goN(2);
			assertThat(c2.get(1), equalTo('6'));
		}
	}

	public static class GoN extends CarretTest {

		@Test
		public void test() {
			String test = "0123456789";

			Carret c1 = new Carret(test);
			c1.goN(2);
			assertThat(c1.get(0), equalTo('2'));

			c1.goN(3);
			assertThat(c1.get(0), equalTo('5'));
		}
	}

	public static class GoTo extends CarretTest {

		@Test
		public void test() {
			String test = "0123456789";

			Carret c1 = new Carret(test, 5);
			c1.goTo(2);
			assertThat(c1.get(0), equalTo('2'));

			c1.goTo(7);
			assertThat(c1.get(0), equalTo('7'));
		}
	}

	public static class GetOffset extends CarretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Carret c1 = new Carret(test, 2);

			c1.goN(1);
			assertThat(c1.getOffset(), equalTo(3));

			c1.goN(2);
			assertThat(c1.getOffset(), equalTo(5));
		}
	}

	public static class WayToEnd extends CarretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Carret c1 = new Carret(test, 2);

			c1.goN(1);
			assertThat(c1.wayToEnd(), equalTo(7));

			c1.goN(2);
			assertThat(c1.wayToEnd(), equalTo(5));

			c1.goN(5);
			assertThat(c1.wayToEnd(), equalTo(0));
		}
	}

	public static class EndReached extends CarretTest {

		@Test
		public void test() {
			String test = "0123456789";
			Carret c1 = new Carret(test, 2);

			assertThat(c1.endReached(), equalTo(false));

			c1.goTo(10);
			assertThat(c1.endReached(), equalTo(true));

			c1.goTo(5);
			assertThat(c1.endReached(), equalTo(false));
		}
	}

	public static class WalkAfterCharCheckCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Carret c = new Carret(test);

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

	public static class WalkToCharCheckCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Carret c = new Carret(test);

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

	public static class WalkAfterWhitespace extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r \nabcdefg  ";
			Carret c = new Carret(test);

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

	public static class WalkBackBeforeWhitespaces extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r \nabcdefg";

			Carret c = new Carret(test);
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

	public static class StartsWithIgnoreCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Carret c = new Carret(test, 4);

			assertThat(c.startsWithIgnoreCase("def"), equalTo(false));
			assertThat(c.startsWithIgnoreCase("cdef"), equalTo(true));
			assertThat(c.startsWithIgnoreCase("CDEF"), equalTo(true));
		}
	}

	public static class StartsWithCheckCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefg";
			Carret c = new Carret(test, 4);

			assertThat(c.startsWithCheckCase("def"), equalTo(false));
			assertThat(c.startsWithCheckCase("cdef"), equalTo(false));
			assertThat(c.startsWithCheckCase("CDEF"), equalTo(true));
		}
	}

	public static class WalkToMatchCheckCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Carret c = new Carret(test, 0);

			assertThat(c.walkToMatchCheckCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('c'));

			c.goN(1);
			assertThat(c.walkToMatchCheckCase("cdefg"), equalTo(false));
		}
	}

	public static class WalkToMatchIgnoreCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Carret c = new Carret(test, 0);

			assertThat(c.walkToMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('C'));

			c.goN(1);
			assertThat(c.walkToMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('c'));

			c.goN(1);
			assertThat(c.walkToMatchIgnoreCase("cdefg"), equalTo(false));
		}
	}

	public static class WalkAfterMatchCheckCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Carret c = new Carret(test, 0);

			assertThat(c.walkAfterMatchCheckCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('h'));

			c.goN(1);
			assertThat(c.walkAfterMatchCheckCase("cdefg"), equalTo(false));
		}
	}

	public static class WalkAfterMatchIgnoreCase extends CarretTest {

		@Test
		public void test() {
			String test = "  ABCDEFGH\t\r\nabcdefgh";
			Carret c = new Carret(test, 0);

			assertThat(c.walkAfterMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('H'));

			assertThat(c.walkAfterMatchIgnoreCase("cdefg"), equalTo(true));
			assertThat(c.get(0), equalTo('h'));

			c.goN(1);
			assertThat(c.walkAfterMatchIgnoreCase("cdefg"), equalTo(false));
		}
	}

}
