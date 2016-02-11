package info.novatec.inspectit.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.agent.AbstractLogSupport;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ReflectionCacheTest extends AbstractLogSupport {

	private ReflectionCache cache;

	@BeforeMethod
	public void init() {
		cache = new ReflectionCache();
	}

	@Test
	public void normalUsage() {
		String testString = "I am a test";

		String result = (String) cache.invokeMethod(String.class, "toString", null, testString, null, null);
		assertThat(result, is(testString));
		assertThat((int) cache.cache.size(), is(1));
		result = (String) cache.invokeMethod(String.class, "toString", null, testString, null, null);
		assertThat(result, is(testString));

		// check that there is only one cache entry
		assertThat((int) cache.cache.size(), is(1));
	}

	@Test
	public void methodWithParameter() {
		Object errorvalue = "errorvalue";
		String input = "input";
		String concat = "concat";
		String result = (String) cache.invokeMethod(String.class, "concat", new Class[] { String.class }, input, new Object[] { concat }, errorvalue);
		assertThat(result, is(equalTo(input.concat(concat))));
	}

	@Test
	public void methodIncorrect() {
		Object errorvalue = "errorvalue";
		Object result = cache.invokeMethod(String.class, "methodDoesNotExist", null, "test", null, errorvalue);
		assertThat(result, is(errorvalue));

		result = cache.invokeMethod(String.class, "toString", new Class[] { String.class }, "test", null, errorvalue);
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
