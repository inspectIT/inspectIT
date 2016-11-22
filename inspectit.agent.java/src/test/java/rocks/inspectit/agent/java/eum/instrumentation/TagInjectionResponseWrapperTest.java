package rocks.inspectit.agent.java.eum.instrumentation;

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
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class TagInjectionResponseWrapperTest extends TestBase {

	private static final String CHARACTER_ENCODING = "UTF-8";

	private static final String TEST_TAG = "<script>dosomething</script>";

	private static final String HTML_TEST_CASE_A =
			"<!DOCTYPE html> "
					+ "<html lang=\"en-US\">  "
					+ "<head> "
					+ "<title>HTML Examples</title> "
					+ "<meta charset=\"utf-8\"> "
					+ "</head> "
					+ "<body>"
					+ "<div> "
					+ "<p> a äüpöäüöäüöl paragraph </p>"
					+ " </div>"
					+ "</body>  "
					+ "</html>    ";

	private static final String HTML_TEST_CASE_B =
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\r\n  \"http://www.w3.org/TR/html4/strict.dtd\"> "
					+ "<!-- First comment --> "
					+ "<!-- comment with äüö -->"
					+ "<html lang=\"en-US\"> "
					+ "<body>"
					+ "<div> "
					+ "<p a paragraph /> "
					+ "</div>"
					+ "</body>  "
					+ "</html>    ";

	private static final String HTML_TEST_CASE_A_REFERENCE =
			"<!DOCTYPE html> <html lang=\"en-US\">  "
					+ "<head>" + TEST_TAG
					+ " <title>HTML Examples</title> "
					+ "<meta charset=\"utf-8\"> "
					+ "</head>"
					+ " <body>"
					+ "<div> "
					+ "<p> a äüpöäüöäüöl paragraph </p> "
					+ "</div>"
					+ "</body>  "
					+ "</html>    ";
	private static final String HTML_TEST_CASE_B_REFERENCE =
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\r\n  \"http://www.w3.org/TR/html4/strict.dtd\"> "
					+ "<!-- First comment --> "
					+ "<!-- comment with äüö -->"
					+ "<html lang=\"en-US\"> "
					+ "<body>"
					+ TEST_TAG
					+ "<div> "
					+ "<p a paragraph /> "
					+ "</div>"
					+ "</body>  "
					+ "</html>    ";

	private static final String NON_HTML_TEST_CASE_A = "this is plain text and not html";

	private static final String NON_HTML_TEST_CASE_B =
			"<!DOCTYPE html> "
					+ "<html lang=\"en-US\"> "
					+ "<nothead> "
					+ "</nothead>  "
					+ "</html>    ";

	@Mock
	javax.servlet.http.HttpServletResponse dummyProxy;

	@Mock
	javax.servlet.http.HttpServletResponse dummyResponse;

	@Mock
	javax.servlet.http.HttpServletRequest dummyRequest;

	ByteArrayOutputStream streamResult;

	StringWriter printerResult;

	@Mock
	EumScriptTagPrinter tagPrinter;

	@Mock
	TracerImpl tracer;

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

		when(tagPrinter.printTags()).thenReturn(TEST_TAG);
		when(tagPrinter.clone()).thenReturn(tagPrinter);

		when(dummyResponse.getCharacterEncoding()).thenReturn(CHARACTER_ENCODING);
		when(dummyResponse.getWriter()).thenReturn(pw);
		when(dummyResponse.getOutputStream()).thenReturn(stream);

	}

	public static class GetWriter extends TagInjectionResponseWrapperTest {

		@BeforeMethod
		public void init() {
			printerResult.getBuffer().setLength(0);
			respWrapper = new TagInjectionResponseWrapper(dummyRequest, dummyResponse, tracer, tagPrinter);
			respWrapper.proxyLinked(dummyProxy, linker);
		}

		@Test
		public void testHeadInjection() throws IOException {
			respWrapper.getWriter().write(HTML_TEST_CASE_A);
			assertThat(printerResult.toString(), equalTo(HTML_TEST_CASE_A_REFERENCE));
		}

		@Test
		public void testBodyInjection() throws IOException {
			respWrapper.getWriter().write(HTML_TEST_CASE_B);
			assertThat(printerResult.toString(), equalTo(HTML_TEST_CASE_B_REFERENCE));
		}

		@Test
		public void testPlainTextNoInjection() throws IOException {
			respWrapper.getWriter().write(NON_HTML_TEST_CASE_A);
			assertThat(printerResult.toString(), equalTo(NON_HTML_TEST_CASE_A));
		}

		@Test
		public void testInvalidMarkupNoInjection() throws IOException {
			respWrapper.getWriter().write(NON_HTML_TEST_CASE_B);
			assertThat(printerResult.toString(), equalTo(NON_HTML_TEST_CASE_B));
		}

	}

	public static class GetOutputStream extends TagInjectionResponseWrapperTest {

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
			respWrapper = new TagInjectionResponseWrapper(dummyRequest, dummyResponse, tracer, tagPrinter);
			respWrapper.proxyLinked(dummyProxy, linker);
		}

		@SuppressWarnings("unchecked")
		@Test
		public void testHeadInjection() throws IOException {
			ArgumentCaptor<TagInjectionOutputStream> streamCaptor = ArgumentCaptor.forClass(TagInjectionOutputStream.class);
			respWrapper.getOutputStream();
			verify(linker, times(1)).createProxy(any(Class.class), streamCaptor.capture(), any(ClassLoader.class));

			TagInjectionOutputStream stream = streamCaptor.getValue();
			byte[] bytes = HTML_TEST_CASE_A.getBytes(CHARACTER_ENCODING);
			int pos = 0;
			while (pos < bytes.length) {
				stream.write(bytes, pos, Math.min(3, bytes.length - pos));
				pos += 3; // write 3 bytes at once
			}

			String result = new String(streamResult.toByteArray(), CHARACTER_ENCODING);
			assertThat(result, equalTo(HTML_TEST_CASE_A_REFERENCE));
		}

		@SuppressWarnings("unchecked")
		@Test
		public void testBodyInjection() throws IOException {
			ArgumentCaptor<TagInjectionOutputStream> streamCaptor = ArgumentCaptor.forClass(TagInjectionOutputStream.class);
			respWrapper.getOutputStream();
			verify(linker, times(1)).createProxy(any(Class.class), streamCaptor.capture(), any(ClassLoader.class));

			TagInjectionOutputStream stream = streamCaptor.getValue();
			byte[] bytes = HTML_TEST_CASE_B.getBytes(CHARACTER_ENCODING);
			int pos = 0;
			while (pos < bytes.length) {
				stream.write(bytes, pos, Math.min(3, bytes.length - pos));
				pos += 3; // write 3 bytes at once
			}

			String result = new String(streamResult.toByteArray(), CHARACTER_ENCODING);
			assertThat(result, equalTo(HTML_TEST_CASE_B_REFERENCE));
		}

		@SuppressWarnings("unchecked")
		@Test
		public void testPlainTextNoInjection() throws IOException {
			ArgumentCaptor<TagInjectionOutputStream> streamCaptor = ArgumentCaptor.forClass(TagInjectionOutputStream.class);
			respWrapper.getOutputStream();
			verify(linker, times(1)).createProxy(any(Class.class), streamCaptor.capture(), any(ClassLoader.class));

			TagInjectionOutputStream stream = streamCaptor.getValue();
			byte[] bytes = NON_HTML_TEST_CASE_A.getBytes(CHARACTER_ENCODING);
			int pos = 0;
			while (pos < bytes.length) {
				stream.write(bytes, pos, Math.min(3, bytes.length - pos));
				pos += 3; // write 3 bytes at once
			}

			String result = new String(streamResult.toByteArray(), CHARACTER_ENCODING);
			assertThat(result, equalTo(NON_HTML_TEST_CASE_A));
		}

		@SuppressWarnings("unchecked")
		@Test
		public void testInvalidMarkupNoInjection() throws IOException {
			ArgumentCaptor<TagInjectionOutputStream> streamCaptor = ArgumentCaptor.forClass(TagInjectionOutputStream.class);
			respWrapper.getOutputStream();
			verify(linker, times(1)).createProxy(any(Class.class), streamCaptor.capture(), any(ClassLoader.class));

			TagInjectionOutputStream stream = streamCaptor.getValue();
			byte[] bytes = NON_HTML_TEST_CASE_B.getBytes(CHARACTER_ENCODING);
			int pos = 0;
			while (pos < bytes.length) {
				stream.write(bytes, pos, Math.min(3, bytes.length - pos));
				pos += 3; // write 3 bytes at once
			}

			String result = new String(streamResult.toByteArray(), CHARACTER_ENCODING);
			assertThat(result, equalTo(NON_HTML_TEST_CASE_B));
		}

	}

}
