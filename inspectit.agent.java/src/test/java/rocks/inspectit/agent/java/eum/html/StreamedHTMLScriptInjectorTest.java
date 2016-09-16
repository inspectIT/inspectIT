package rocks.inspectit.agent.java.eum.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class StreamedHTMLScriptInjectorTest extends TestBase {

	private static final String TAG_TO_INJECT = "<here usually goes the script tag>";
	private static final String INJECTION_POS_MARKER = "<!--INJECTIONPOINT-->";


	StreamedHTMLScriptInjector injector;

	@BeforeMethod
	public void init() {
		injector = new StreamedHTMLScriptInjector(TAG_TO_INJECT);
	}

	String loadHtmlSource(String path) {
		InputStream is = StreamedHTMLScriptInjectorTest.class.getResourceAsStream(path);
		try {
			return new String(ByteStreams.toByteArray(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	String getExpectedResult(String originalHtml) {
		int injectionPos = originalHtml.indexOf(INJECTION_POS_MARKER);
		if (injectionPos != -1) {
			return originalHtml.substring(0, injectionPos) + TAG_TO_INJECT + originalHtml.substring(injectionPos);
		} else {
			return originalHtml;
		}
	}


	String runInjector(String source) {
		int pos = 0;
		StringBuilder result = new StringBuilder();
		while (pos < source.length()) {
			// give it to the parser in portions of 3 characters
			String str = source.substring(pos, Math.min(pos + 3, source.length()));
			String injection = injector.performInjection(str);
			if (injection != null) {
				result.append(injection);
			} else {
				result.append(str);
			}
			pos += 3;
		}
		return result.toString();
	}

	public static class PerformInjection extends StreamedHTMLScriptInjectorTest {

		@Test
		public void testPreventDoubleInejection() {
			String src = loadHtmlSource("/html/testCase-HeadInjection.html");

			String modifiedA = runInjector(src);
			String modified = new StreamedHTMLScriptInjector(TAG_TO_INJECT).performInjection(modifiedA);
			assertThat(modified, equalTo(null));
		}

		@Test
		public void testCaseXML() {
			String src = loadHtmlSource("/html/testCase-XML.html");
			String expectedResult = getExpectedResult(src);

			String modified = runInjector(src);

			assertThat(modified, equalTo(expectedResult));
		}

		@Test
		public void testCaseBodyInjection() {
			String src = loadHtmlSource("/html/testCase-BodyInjection.html");
			String expectedResult = getExpectedResult(src);

			String modified = runInjector(src);

			assertThat(modified, equalTo(expectedResult));
		}

		@Test
		public void testCaseHeadInjection() {
			String src = loadHtmlSource("/html/testCase-HeadInjection.html");
			String expectedResult = getExpectedResult(src);

			String modified = runInjector(src);

			assertThat(modified, equalTo(expectedResult));
		}

		@Test
		public void testCaseInvalidToken() {
			String src = loadHtmlSource("/html/testCase-InvalidToken.html");
			String expectedResult = getExpectedResult(src);

			String modified = runInjector(src);

			assertThat(modified, equalTo(expectedResult));
		}

	}

}
