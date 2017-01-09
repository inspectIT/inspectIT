package rocks.inspectit.agent.java.sdk.opentracing.internal.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ConversionUtilsTest {

	public static class ParseHexStringSafe {

		@Test
		public void nullString() {
			long parsed = ConversionUtils.parseHexStringSafe(null);

			assertThat(parsed, is(0L));
		}

		@Test(expectedExceptions = NumberFormatException.class)
		public void notParsable() {
			ConversionUtils.parseHexStringSafe("something funny");
		}

		@Test(invocationCount = 10)
		public void random() {
			long generated = RandomUtils.randomLong();
			String str = ConversionUtils.toHexString(generated);

			long parsed = ConversionUtils.parseHexStringSafe(str);

			assertThat(parsed, is(generated));
		}

	}
}
