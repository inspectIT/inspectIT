package rocks.inspectit.ui.rcp.formatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test for the {@link NumberFormatter} class.
 *
 * @author Marius Oehler
 *
 */
public class NumberFormatterTest extends TestBase {

	/**
	 * Tests the {@link NumberFormatter#humanReadableByteCount(long)} and
	 * {@link NumberFormatter#humanReadableByteCount(long, int)} methods.
	 */
	public static class humanReadableByteCount extends NumberFormatterTest {

		@Test
		public void validResultBytes() {
			String result = NumberFormatter.humanReadableByteCount(512L);

			assertThat(result, is(equalTo("512 B")));
		}

		@Test
		public void validResultWithOneDecimalPlace() {
			String result = NumberFormatter.humanReadableByteCount(1900L);

			assertThat(result, is(equalTo("1.9 KiB")));
		}

		@Test
		public void validResultWithMultipleDecimalPlaces() {
			String result = NumberFormatter.humanReadableByteCount(1900L, 3);

			assertThat(result, is(equalTo("1.855 KiB")));
		}

		@Test
		public void validResultWithoutDecimalPlaces() {
			String result = NumberFormatter.humanReadableByteCount(1900L, 0);

			assertThat(result, is(equalTo("2 KiB")));
		}

		@Test
		public void validResultMegaBytes() {
			long input = (long) Math.pow(1024D, 2D);

			String result = NumberFormatter.humanReadableByteCount(input);

			assertThat(result, is(equalTo("1.0 MiB")));
		}

		@Test
		public void validResultGigaBytes() {
			long input = (long) Math.pow(1024D, 3D);

			String result = NumberFormatter.humanReadableByteCount(input);

			assertThat(result, is(equalTo("1.0 GiB")));
		}

		@Test
		public void validResultTerraBytes() {
			long input = (long) Math.pow(1024D, 4D);

			String result = NumberFormatter.humanReadableByteCount(input);

			assertThat(result, is(equalTo("1.0 TiB")));
		}

		@Test
		public void validResultPetaBytes() {
			long input = (long) Math.pow(1024D, 5D);

			String result = NumberFormatter.humanReadableByteCount(input);

			assertThat(result, is(equalTo("1.0 PiB")));
		}

		@Test
		public void validResultExaBytes() {
			long input = (long) Math.pow(1024D, 6D);

			String result = NumberFormatter.humanReadableByteCount(input);

			assertThat(result, is(equalTo("1.0 EiB")));
		}

		@Test
		public void validResultMaxLong() {
			String result = NumberFormatter.humanReadableByteCount(Long.MAX_VALUE);

			assertThat(result, is(equalTo("8.0 EiB")));
		}
	}

}
