package rocks.inspectit.agent.java.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class CharacterRingBufferTest extends TestBase {

	public static class ToStringAndCharAt extends CharacterRingBufferTest {

		@Test
		public void testSimple() {
			CharacterRingBuffer rb = new CharacterRingBuffer("abcdefg");
			assertThat(rb.toString(), equalTo("abcdefg"));
			for (int pos = 0; pos < "abcdefg".length(); pos++) {
				assertThat(rb.charAt(pos), equalTo("abcdefg".charAt(pos)));
			}
		}

		@Test
		public void testWithWrapping() {
			CharacterRingBuffer rb = new CharacterRingBuffer(10);
			rb.append("123456789");
			rb.erase(5);
			rb.append("abcd");
			assertThat(rb.toString(), equalTo("6789abcd"));
			for (int pos = 0; pos < "6789abcd".length(); pos++) {
				assertThat(rb.charAt(pos), equalTo("6789abcd".charAt(pos)));
			}
		}

		@Test(expectedExceptions = { IllegalArgumentException.class })
		public void testNegativeIndex() {
			CharacterRingBuffer rb = new CharacterRingBuffer("test");
			rb.charAt(-1);
		}

		@Test(expectedExceptions = { IllegalArgumentException.class })
		public void testOutOfBoundsIndex() {
			CharacterRingBuffer rb = new CharacterRingBuffer("test");
			rb.charAt(4);
		}
	}

	public static class Append extends CharacterRingBufferTest {

		@Test
		public void testSimple() {
			CharacterRingBuffer rb = new CharacterRingBuffer(5);
			rb.append("abcd");
			rb.append("efghij");
			assertThat(rb.toString(), equalTo("abcdefghij"));
		}

		@Test
		public void testWrapped() {
			CharacterRingBuffer rb = new CharacterRingBuffer(5);
			rb.append("abcd");
			rb.erase(2);
			rb.append("efghij");
			assertThat(rb.toString(), equalTo("cdefghij"));
		}
	}

	public static class Erase extends CharacterRingBufferTest {

		@Test
		public void testSimple() {
			CharacterRingBuffer rb = new CharacterRingBuffer("0123456789");
			rb.erase(3);
			assertThat(rb.toString(), equalTo("3456789"));
		}

		@Test
		public void testClear() {
			CharacterRingBuffer rb = new CharacterRingBuffer("0123456789");
			rb.erase(10);
			assertThat(rb.toString(), equalTo(""));
		}
	}

	public static class SubSequence extends CharacterRingBufferTest {

		@Test
		public void testSimple() {
			CharacterRingBuffer rb = new CharacterRingBuffer("0123456789");
			assertThat(rb.subSequence(3, 8).toString(), equalTo("34567"));
		}

		@Test
		public void testWrapped() {
			CharacterRingBuffer rb = new CharacterRingBuffer(5);
			rb.append("ab012");
			rb.erase(2);
			rb.append("3456789");
			assertThat(rb.subSequence(3, 8).toString(), equalTo("34567"));
		}

		@Test
		public void testSubSequence() {
			CharacterRingBuffer rb = new CharacterRingBuffer(5);
			rb.append("ab012");
			rb.erase(2);
			rb.append("3456789");
			assertThat(rb.subSequence(3, 8).subSequence(1, 4).toString(), equalTo("456"));
		}
	}

}
