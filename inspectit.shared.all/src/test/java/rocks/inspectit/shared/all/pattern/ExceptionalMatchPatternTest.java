package rocks.inspectit.shared.all.pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
public class ExceptionalMatchPatternTest extends TestBase {

	ExceptionalMatchPattern pattern;

	@Mock
	IMatchPattern main;

	@Mock
	IMatchPattern exception1;

	@Mock
	IMatchPattern exception2;

	public static class Match extends ExceptionalMatchPatternTest {

		@Test
		public void noExceptionsMatching() {
			pattern = new ExceptionalMatchPattern(main, null);
			String str = "str";
			when(main.match(str)).thenReturn(true);

			boolean matched = pattern.match(str);

			assertThat(matched, is(true));
		}

		@Test
		public void noExceptionsNotMatching() {
			pattern = new ExceptionalMatchPattern(main, null);
			String str = "str";
			when(main.match(str)).thenReturn(false);

			boolean matched = pattern.match(str);

			assertThat(matched, is(false));
		}

		@Test
		public void oneExceptionMainNotMatching() {
			pattern = new ExceptionalMatchPattern(main, Collections.singleton(exception1));
			String str = "str";
			when(main.match(str)).thenReturn(false);
			when(exception1.match(str)).thenReturn(false);

			boolean matched = pattern.match(str);

			assertThat(matched, is(false));
		}

		@Test
		public void oneExceptionNotMatching() {
			pattern = new ExceptionalMatchPattern(main, Collections.singleton(exception1));
			String str = "str";
			when(main.match(str)).thenReturn(true);
			when(exception1.match(str)).thenReturn(false);

			boolean matched = pattern.match(str);

			assertThat(matched, is(true));
		}

		@Test
		public void oneExceptionMatching() {
			pattern = new ExceptionalMatchPattern(main, Collections.singleton(exception1));
			String str = "str";
			when(main.match(str)).thenReturn(true);
			when(exception1.match(str)).thenReturn(true);

			boolean matched = pattern.match(str);

			assertThat(matched, is(false));
		}

		@Test
		public void twoExceptionsNotMatching() {
			pattern = new ExceptionalMatchPattern(main, Arrays.asList(exception1, exception2));
			String str = "str";
			when(main.match(str)).thenReturn(true);
			when(exception1.match(str)).thenReturn(false);
			when(exception2.match(str)).thenReturn(false);

			boolean matched = pattern.match(str);

			assertThat(matched, is(true));
		}

		@Test
		public void twoExceptionsMixed() {
			pattern = new ExceptionalMatchPattern(main, Arrays.asList(exception1, exception2));
			String str = "str";
			when(main.match(str)).thenReturn(true);
			when(exception1.match(str)).thenReturn(false);
			when(exception2.match(str)).thenReturn(true);

			boolean matched = pattern.match(str);

			assertThat(matched, is(false));
		}

	}

}
