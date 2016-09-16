package rocks.inspectit.agent.java.eum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class CarretTest extends TestBase {

	@Test
	public void testGet() {
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

	@Test
	public void testGoAndGetOffset() {
		String test = "0123456789";
		Carret c1 = new Carret(test);
		c1.goN(2);
		assertThat(c1.get(0), equalTo('2'));
		assertThat(c1.getOffset(), equalTo(2));
		assertThat(c1.wayToEnd(), equalTo(8));

		c1.goN(3);
		assertThat(c1.get(0), equalTo('5'));
		assertThat(c1.getOffset(), equalTo(5));
		assertThat(c1.wayToEnd(), equalTo(5));
		assertThat(c1.endReached(), equalTo(false));

		c1.goN(5);
		assertThat(c1.wayToEnd(), equalTo(0));
		assertThat(c1.endReached(), equalTo(true));

		c1.goTo(2);
		assertThat(c1.get(0), equalTo('2'));
		assertThat(c1.endReached(), equalTo(false));

		c1.goTo(10);
		assertThat(c1.endReached(), equalTo(true));
	}

	@Test
	public void testWalkToChar() {
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

		c.goTo(0);
		assertThat(c.walkToCharCheckCase('C'), equalTo(true));
		assertThat(c.get(0), equalTo('C'));

		c.goTo(0);
		assertThat(c.walkToCharCheckCase('c'), equalTo(true));
		assertThat(c.get(0), equalTo('c'));

		c.goTo(0);
		assertThat(c.walkToCharCheckCase('z'), equalTo(false));
		assertThat(c.endReached(), equalTo(true));
	}

	@Test
	public void testWhitespaceWalking() {
		String test = "  ABCDEFGH\t\r\nabcdefg";
		Carret c = new Carret(test);

		c.walkAfterWhitespaces();
		assertThat(c.get(0), equalTo('A'));
		c.walkAfterWhitespaces();
		assertThat(c.get(0), equalTo('A'));
		c.walkAfterCharCheckCase('H');
		c.walkAfterWhitespaces();
		assertThat(c.get(0), equalTo('a'));
		c.goN(-1);
		c.walkBackBeforeWhitespaces();
		assertThat(c.get(0), equalTo('H'));
	}

	@Test
	public void testStartsWith() {
		String test = "  ABCDEFGH\t\r\nabcdefg";
		Carret c = new Carret(test, 4);

		assertThat(c.startsWithIgnoreCase("cdef"), equalTo(true));
		assertThat(c.startsWithCheckCase("cdef"), equalTo(false));
		assertThat(c.startsWithCheckCase("CDEF"), equalTo(true));
	}

	@Test
	public void testWalkToMatch() {
		String test = "  ABCDEFGH\t\r\nabcdefgh";
		Carret c = new Carret(test, 0);

		assertThat(c.walkToMatchCheckCase("cdefg"), equalTo(true));
		assertThat(c.get(0), equalTo('c'));

		c.goTo(0);
		assertThat(c.walkToMatchIgnoreCase("cdefg"), equalTo(true));
		assertThat(c.get(0), equalTo('C'));

		c.goTo(0);
		assertThat(c.walkAfterMatchCheckCase("cdefg"), equalTo(true));
		assertThat(c.get(0), equalTo('h'));

		c.goTo(0);
		assertThat(c.walkAfterMatchIgnoreCase("cdefg"), equalTo(true));
		assertThat(c.get(0), equalTo('H'));

		c.goTo(0);
		assertThat(c.walkAfterMatchCheckCase("ce"), equalTo(false));
		assertThat(c.endReached(), equalTo(true));
	}

}
