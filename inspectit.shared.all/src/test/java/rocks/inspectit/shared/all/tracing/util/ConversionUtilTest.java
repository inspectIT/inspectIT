package rocks.inspectit.shared.all.tracing.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * @author Ivan Senic
 *
 */
public class ConversionUtilTest {

	public static class ParseHexStringSafe {

		@Test
		public void nullString() {
			long parsed = ConversionUtil.parseHexStringSafe(null);

			assertThat(parsed, is(0L));
		}

		@Test(invocationCount = 10)
		public void random() {
			long generated = RandomUtils.randomLong();
			String str = ConversionUtil.toHexString(generated);

			long parsed = ConversionUtil.parseHexStringSafe(str);

			assertThat(parsed, is(generated));
		}

	}
}
