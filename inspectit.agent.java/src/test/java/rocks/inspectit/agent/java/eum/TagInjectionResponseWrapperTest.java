package rocks.inspectit.agent.java.eum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class TagInjectionResponseWrapperTest extends TestBase {

	/**
	 *
	 */
	private static final String CHARACTER_ENCODING = "UTF-8";

	private static final String TEST_TAG = "<script>dosomething</script>";

	private static final String HTML_TEST_CASE_A = "<!DOCTYPE html> <html lang=\"en-US\">  <head> <title>HTML Examples</title> <meta charset=\"utf-8\"> </head> <body><div> <p> a äüpöäüöäüöl paragraph </p> </div></body>  </html>    ";

	private static final String HTML_TEST_CASE_B = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\r\n  \"http://www.w3.org/TR/html4/strict.dtd\"> <!-- First comment --> <!-- comment with äüö --><html lang=\"en-US\"> <body><div> <p a paragraph /> </div></body>  </html>    ";

	private static final String HTML_TEST_CASE_A_REFERENCE =
			"<!DOCTYPE html> <html lang=\"en-US\">  <head>" + TEST_TAG
			+ " <title>HTML Examples</title> <meta charset=\"utf-8\"> </head> <body><div> <p> a äüpöäüöäüöl paragraph </p> </div></body>  </html>    ";
	private static final String HTML_TEST_CASE_B_REFERENCE =
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\r\n  \"http://www.w3.org/TR/html4/strict.dtd\"> <!-- First comment --> <!-- comment with äüö --><html lang=\"en-US\"> <body>"
					+ TEST_TAG
			+ "<div> <p a paragraph /> </div></body>  </html>    ";

	private static final String NON_HTML_TEST_CASE_A = "this is plain text and not html";

	private static final String NON_HTML_TEST_CASE_B = "<!DOCTYPE html> <html lang=\"en-US\"> <nothead> </nothead>  </html>    ";


	@Mock
	javax.servlet.http.HttpServletResponse dummyProxy;


	@Mock
	javax.servlet.http.HttpServletResponse dummyResponse;

	ByteArrayOutputStream streamResult;

	StringWriter printerResult;

	@Mock
	IRuntimeLinker linker;

	TagInjectionResponseWrapper respWrapper;

	@BeforeMethod
	public void initMocks() throws IOException {
		ServletOutputStream stream = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				streamResult.write(b);
			}
		};

		streamResult = new ByteArrayOutputStream();
		printerResult = new StringWriter();
		PrintWriter pw = new PrintWriter(printerResult);

		when(dummyResponse.getCharacterEncoding()).thenReturn(CHARACTER_ENCODING);
		when(dummyResponse.getWriter()).thenReturn(pw);
		when(dummyResponse.getOutputStream()).thenReturn(stream);

	}

	public static class GetWriterInjectionTest extends TagInjectionResponseWrapperTest {

		@BeforeMethod
		public void init() {
			printerResult.getBuffer().setLength(0);
			respWrapper = new TagInjectionResponseWrapper(dummyResponse, null, TEST_TAG);
			respWrapper.proxyLinked(dummyProxy, linker);
		}

		@Test
		public void testHeadInjection() throws IOException {
			testWriterResults(HTML_TEST_CASE_A, HTML_TEST_CASE_A_REFERENCE);
		}

		@Test
		public void testBodyInjection() throws IOException {
			testWriterResults(HTML_TEST_CASE_B, HTML_TEST_CASE_B_REFERENCE);
		}

		@Test
		public void testPlainTextNoInjection() throws IOException {
			testWriterResults(NON_HTML_TEST_CASE_A, NON_HTML_TEST_CASE_A);
		}

		@Test
		public void testInvalidMarkupNoInjection() throws IOException {
			testWriterResults(NON_HTML_TEST_CASE_B, NON_HTML_TEST_CASE_B);
		}

		private void testWriterResults(String source, String reference) throws IOException {
			respWrapper.getWriter().write(source);
			assertThat(printerResult.toString(), equalTo(reference));
		}


	}

	public static class GetOutputStreamInjectionTest extends TagInjectionResponseWrapperTest {

		@Mock
		javax.servlet.ServletOutputStream dummyStreamProxy;

		@SuppressWarnings("unchecked")
		@BeforeMethod
		public void init() {
			when(linker.isProxyInstance(any(Object.class), any(Class.class))).thenReturn(false);
			when(linker.createProxy(any(Class.class), any(TagInjectionOutputStream.class), any(ClassLoader.class))).then(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					TagInjectionOutputStream injectedStream = (TagInjectionOutputStream) invocation.getArguments()[1];
					injectedStream.proxyLinked(dummyStreamProxy, linker);
					return dummyStreamProxy;
				}
			});
			streamResult.reset();
			respWrapper = new TagInjectionResponseWrapper(dummyResponse, null, TEST_TAG);
			respWrapper.proxyLinked(dummyProxy, linker);
		}

		@Test
		public void testHeadInjection() throws IOException {
			testStreamResults(HTML_TEST_CASE_A, HTML_TEST_CASE_A_REFERENCE);
		}

		@Test
		public void testBodyInjection() throws IOException {
			testStreamResults(HTML_TEST_CASE_B, HTML_TEST_CASE_B_REFERENCE);
		}

		@Test
		public void testPlainTextNoInjection() throws IOException {
			testStreamResults(NON_HTML_TEST_CASE_A, NON_HTML_TEST_CASE_A);
		}

		@Test
		public void testInvalidMarkupNoInjection() throws IOException {
			testStreamResults(NON_HTML_TEST_CASE_B, NON_HTML_TEST_CASE_B);
		}

		@SuppressWarnings("unchecked")
		private void testStreamResults(String source, String reference) throws IOException {
			ArgumentCaptor<TagInjectionOutputStream> streamCaptor = ArgumentCaptor.forClass(TagInjectionOutputStream.class);
			respWrapper.getOutputStream();
			verify(linker, times(1)).createProxy(any(Class.class), streamCaptor.capture(), any(ClassLoader.class));

			TagInjectionOutputStream stream = streamCaptor.getValue();
			byte[] bytes = source.getBytes(CHARACTER_ENCODING);
			int pos = 0;
			while (pos < bytes.length) {
				stream.write(bytes, pos, Math.min(3, bytes.length - pos));
				pos += 3; // write 3 bytes at once
			}

			String result = new String(streamResult.toByteArray(), CHARACTER_ENCODING);
			assertThat(result, equalTo(reference));
		}


	}

}
