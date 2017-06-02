package rocks.inspectit.agent.java.eum.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.ByteStreams;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class DecodingHtmlScriptInjectorTest extends TestBase {

	private static final String TAG_TO_INJECT = "<here usually goes the script tag>";
	private static final String INJECTION_POS_MARKER = "<!--INJECTIONPOINT-->";

	DecodingHtmlScriptInjector injector;

	@BeforeMethod
	public void init() {
		injector = new DecodingHtmlScriptInjector(TAG_TO_INJECT, "UTF-8");
	}

	String loadHtmlSource(String path) {
		InputStream is = StreamedHtmlScriptInjectorTest.class.getResourceAsStream(path);
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

	byte[] runInjector(byte[] source) {
		int pos = 0;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while (pos < source.length) {
			// give it to the parser in portions of 3 characters
			int len = Math.min(3, source.length - pos);
			byte[] result = injector.performInjection(source, pos, len);
			try {
				if (result != null) {
					bout.write(result);
				} else {
					bout.write(source, pos, len);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			pos += 3;
		}
		return bout.toByteArray();
	}

	public static class PerformInjection extends DecodingHtmlScriptInjectorTest {

		@Test
		public void testUTF8() throws UnsupportedEncodingException {
			String src = loadHtmlSource("/html/testCase-HeadInjection.html");
			String expectedResult = getExpectedResult(src);

			String encoding = "UTF-8";
			injector.setCharacterEncoding(encoding);

			String result = new String(runInjector(src.getBytes(encoding)), encoding);
			assertThat(result, equalTo(expectedResult));
		}

		@Test
		public void testInvalidBytes() throws UnsupportedEncodingException {
			String encoding = "UTF-8";
			injector.setCharacterEncoding(encoding);

			assertThat(injector.performInjection(new byte[] { -1 }), equalTo(null)); // -1 alone is not a valid char in UTF-8

			assertThat(injector.hasTerminated(), equalTo(true));
		}

		@Test
		public void testInvalidCharset() throws UnsupportedEncodingException {
			String encoding = "invalid-haha";
			injector.setCharacterEncoding(encoding);

			// The injector should be unable to decode these chars
			assertThat(injector.performInjection("<html></html>".getBytes()), equalTo(null));
			assertThat(injector.hasTerminated(), equalTo(true));
		}

		@Test
		public void testLeftOverRemaining() throws UnsupportedEncodingException {
			String encoding = "UTF-8";
			injector.setCharacterEncoding(encoding);
			byte[] testdata = new byte[] { "ü".getBytes(encoding)[0] }; // "ü" is a two-byte char

			assertThat(injector.performInjection(testdata), equalTo(null));
			assertThat(injector.hasTerminated(), equalTo(false));

			assertThat(injector.performInjection("This string is ignored as the previous character was not finished"), equalTo(null));
			assertThat(injector.hasTerminated(), equalTo(true));
		}

	}

}
