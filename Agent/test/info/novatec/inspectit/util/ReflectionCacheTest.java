package info.novatec.inspectit.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.agent.AbstractLogSupport;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

/**
 * Tests for the {@link ReflectionCache} class.
 *
 * @author Patrice Bouillet
 *
 */
public class ReflectionCacheTest extends AbstractLogSupport {

	/**
	 * Class under test.
	 */
	@InjectMocks
	ReflectionCache cache;

	/**
	 * Tests for the
	 * {@link ReflectionCache#invokeMethod(Class, String, Class[], Object, Object[], Object)}
	 * method.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	public static class InvokeMethod extends ReflectionCacheTest {

		@Test
		public void normalUsage() {
			String testString = "I am a test";

			String result = (String) cache.invokeMethod(String.class, "toString", null, testString, null, null);

			assertThat(result, is(testString));
		}

		@Test
		public void multipleInvocationOnSameObject() {
			String testString = "I am a test";

			cache.invokeMethod(String.class, "toString", null, testString, null, null);
			cache.invokeMethod(String.class, "toString", null, testString, null, null);
			String result = (String) cache.invokeMethod(String.class, "toString", null, testString, null, null);

			assertThat(result, is(testString));
		}

		@Test
		public void methodWithParameter() {
			Object errorvalue = "errorvalue";
			String input = "input";
			String concat = "concat";

			String result = (String) cache.invokeMethod(String.class, "concat", new Class[] { String.class }, input, new Object[] { concat }, errorvalue);

			assertThat(result, is(input.concat(concat)));
		}

		@Test
		public void methodDoesNotExist() {
			Object errorvalue = "errorvalue";

			Object result = cache.invokeMethod(String.class, "methodDoesNotExist", null, "test", null, errorvalue);

			assertThat(result, is(errorvalue));
		}

		@Test
		public void methodWithParametersDoesNotExist() {
			Object errorvalue = "errorvalue";

			Object result = cache.invokeMethod(String.class, "toString", new Class[] { String.class }, "test", new Object[] { "test" }, errorvalue);

			assertThat(result, is(errorvalue));
		}

		@Test
		public void nullMethod() {
			Object errorvalue = "errorvalue";

			Object result = cache.invokeMethod(String.class, null, null, "test", null, errorvalue);

			assertThat(result, is(errorvalue));
		}

		@Test
		public void nullClass() {
			Object errorvalue = "errorvalue";

			Object result = cache.invokeMethod(null, "abc", null, "test", null, errorvalue);

			assertThat(result, is(errorvalue));
		}

		@Test
		public void nullInstanceGiven() {
			Object errorvalue = "errorvalue";

			String result = (String) cache.invokeMethod(String.class, "toString", null, null, null, errorvalue);

			assertThat(result, is(errorvalue));
		}

	}

}
