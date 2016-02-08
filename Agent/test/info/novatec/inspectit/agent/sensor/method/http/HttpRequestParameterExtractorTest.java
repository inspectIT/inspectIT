package info.novatec.inspectit.agent.sensor.method.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.communication.data.HttpInfo;
import info.novatec.inspectit.util.StringConstraint;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.MapUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class HttpRequestParameterExtractorTest extends AbstractLogSupport {

	private HttpRequestParameterExtractor extractor;

	@Mock
	private HttpServletRequest httpServletRequest;

	@BeforeMethod
	public void initTestClass() {
		extractor = new HttpRequestParameterExtractor(new StringConstraint(Collections.<String, Object> singletonMap("stringLength", "20")));
	}

	@Test
	public void readParameters() {
		final String param1 = "p1";
		final String param2 = "p2";
		final String param1VReal = "I am a really long string that should be cropped in an meaningful way.";
		final String param2VReal1 = "value5";
		final String param2VReal2 = "value6";
		final String[] param1V = new String[] { param1VReal };
		final String[] param2V = new String[] { param2VReal1, param2VReal2 };
		final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		MapUtils.putAll(parameterMap, new Object[][] { { param1, param1V }, { param2, param2V } });

		when(httpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Map<String, String[]> result = extractor.getParameterMap(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result.size(), is(parameterMap.size()));
		assertThat(result, hasKey(param1));
		assertThat(result, hasKey(param2));
		assertThat("Value should be cropped!", result.get(param1), is(not(param1V)));
		assertThat(result.get(param2), is(param2V));
	}

	@Test
	public void readParametersNull() {
		Map<String, String[]> result = extractor.getParameterMap(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(nullValue()));
	}

	@Test
	public void readHeaders() {
		final String h1 = "h1";
		final String h2 = "h2";
		final String h1Value = "hValue1";
		final String h2Value = "hValue2";
		final Vector<String> headersList = new Vector<String>();
		Collections.addAll(headersList, h1, h2);

		final Enumeration<String> headers = headersList.elements();

		when(httpServletRequest.getHeaderNames()).thenReturn(headers);
		when(httpServletRequest.getHeader(h1)).thenReturn(h1Value);
		when(httpServletRequest.getHeader(h2)).thenReturn(h2Value);

		Map<String, String> result = extractor.getHeaders(httpServletRequest.getClass(), httpServletRequest);

		Map<String, String> expected = new HashMap<String, String>();
		MapUtils.putAll(expected, new Object[][] { { h1, h1Value }, { h2, h2Value } });
		assertThat(result, is(equalTo(expected)));

		// We only create a new instance of the element if we need to change it (e.g. crop)
		assertThat("No new instances", result.get(h1) == h1Value);
		assertThat("No new instances", result.get(h2) == h2Value);
	}

	@Test
	public void readHeadersCrop() {
		final String h1 = "h1";
		final String h2 = "h2";
		// this will be cropped!
		final String h1Value = "hValue1aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		final String h2Value = "hValue2";
		final Vector<String> headersList = new Vector<String>();
		Collections.addAll(headersList, h1, h2);

		final Enumeration<String> headers = headersList.elements();

		when(httpServletRequest.getHeaderNames()).thenReturn(headers);
		when(httpServletRequest.getHeader(h1)).thenReturn(h1Value);
		when(httpServletRequest.getHeader(h2)).thenReturn(h2Value);

		Map<String, String> result = extractor.getHeaders(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result.size(), is(2));

		// We only create a new instance of the element if we need to change it (e.g. crop)
		assertThat("No new instances", result.get(h1) != h1Value);
		assertThat("No new instances", result.get(h2) == h2Value);
	}

	@Test
	public void readHeadersNull() {
		Map<String, String> result = extractor.getHeaders(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(nullValue()));
	}

	@Test
	public void readAttributes() {
		final String att1 = "a1";
		final String att2 = "a2";
		final String att1Value = "aValue1";
		final String att2Value = "aValue2";
		final Vector<String> attributesList = new Vector<String>();
		Collections.addAll(attributesList, att1, att2);

		final Enumeration<String> attributes = attributesList.elements();

		when(httpServletRequest.getAttributeNames()).thenReturn(attributes);

		when(httpServletRequest.getAttribute(att1)).thenReturn(att1Value);
		when(httpServletRequest.getAttribute(att2)).thenReturn(att2Value);

		Map<String, String> result = extractor.getAttributes(httpServletRequest.getClass(), httpServletRequest);

		Map<String, String> expected = new HashMap<String, String>();
		MapUtils.putAll(expected, new Object[][] { { att1, att1Value }, { att2, att2Value } });

		assertThat(result, is(equalTo(expected)));
		// We only create a new instance of the element if we need to change it (e.g. crop)
		assertThat("No new instances", result.get(att1) == att1Value);
		assertThat("No new instances", result.get(att2) == att2Value);
	}

	@Test
	public void readArrayAttributes() {
		final String att1 = "a1";
		final String att2 = "a2";
		final String[] att1Value = { "attValue1", "attValue2", "attValue3" };
		final String[] att2Value = { "a1", "a2", "a3" };
		final Vector<String> attributesList = new Vector<String>();
		Collections.addAll(attributesList, att1, att2);
		final Enumeration<String> attributes = attributesList.elements();

		when(httpServletRequest.getAttributeNames()).thenReturn(attributes);
		when(httpServletRequest.getAttribute(att1)).thenReturn(att1Value);
		when(httpServletRequest.getAttribute(att2)).thenReturn(att2Value);

		Map<String, String> result = extractor.getAttributes(httpServletRequest.getClass(), httpServletRequest);

		final String extractedAttribute1Value = "[attValue1, attValue2, attValue3]".substring(0, 20) + "...";
		final String extractedAttribute2Value = "[a1, a2, a3]";
		Map<String, String> expected = new HashMap<String, String>();
		MapUtils.putAll(expected, new Object[][] { { att1, extractedAttribute1Value }, { att2, extractedAttribute2Value } });

		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void readArrayAttributesPrimitivesInteger() {
		final String att1 = "a1";
		final String att2 = "a2";
		final int[] att1Value = { 1, 2, 3 };
		final int[] att2Value = { 2, 3, 4 };
		final Vector<String> attributesList = new Vector<String>();
		Collections.addAll(attributesList, att1, att2);

		final Enumeration<String> attributes = attributesList.elements();

		when(httpServletRequest.getAttributeNames()).thenReturn(attributes);
		when(httpServletRequest.getAttribute(att1)).thenReturn(att1Value);
		when(httpServletRequest.getAttribute(att2)).thenReturn(att2Value);

		Map<String, String> result = extractor.getAttributes(httpServletRequest.getClass(), httpServletRequest);

		final String extractedAttribute1Value = "[1, 2, 3]";
		final String extractedAttribute2Value = "[2, 3, 4]";
		Map<String, String> expected = new HashMap<String, String>();
		MapUtils.putAll(expected, new Object[][] { { att1, extractedAttribute1Value }, { att2, extractedAttribute2Value } });

		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void readAttributesNull() {
		Map<String, String> result = extractor.getAttributes(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(nullValue()));
	}

	@Test
	public void readRequestUri() {
		final String uri = "URI";
		when(httpServletRequest.getRequestURI()).thenReturn(uri);

		String result = extractor.getRequestUri(httpServletRequest.getClass(), httpServletRequest);
		assertThat(result, is(equalTo(uri)));
		assertThat("Same instances", uri == result);
	}

	@Test
	public void readRequestUriNull() {
		String result = extractor.getRequestUri(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(HttpInfo.UNDEFINED));
	}

	@Test
	public void readRequestMethod() {
		final String method = "GET";
		when(httpServletRequest.getMethod()).thenReturn(method);

		String result = extractor.getRequestMethod(httpServletRequest.getClass(), httpServletRequest);
		assertThat(result, is(equalTo(method)));
		assertThat("Same istances", method == result);
	}

	@Test
	public void readRequestMethodNull() {
		String result = extractor.getRequestMethod(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(HttpInfo.UNDEFINED));
	}

	@Test
	public void sessionAttributesWithNoSession() {
		when(httpServletRequest.getSession(false)).thenReturn(null);

		Map<String, String> result = extractor.getSessionAttributes(httpServletRequest.getClass(), httpServletRequest);
		assertThat(result, is(nullValue()));
	}

	@Test
	public void sessionAttributesWithSession() {

		final String sa1 = "sa1";
		final String sa2 = "sa2";
		final String sa1Value = "saValue1";
		final String sa2Value = "saValue2";
		final Vector<String> sessionAttributesList = new Vector<String>();
		Collections.addAll(sessionAttributesList, sa1, sa2);
		final Enumeration<String> sessionAttributes = sessionAttributesList.elements();

		HttpSession session = Mockito.mock(HttpSession.class);
		when(session.getAttributeNames()).thenReturn(sessionAttributes);
		when(session.getAttribute(sa1)).thenReturn(sa1Value);
		when(session.getAttribute(sa2)).thenReturn(sa2Value);
		when(httpServletRequest.getSession(false)).thenReturn(session);

		Map<String, String> result = extractor.getSessionAttributes(httpServletRequest.getClass(), httpServletRequest);
		Map<String, String> expected = new HashMap<String, String>();
		MapUtils.putAll(expected, new Object[][] { { sa1, sa1Value }, { sa2, sa2Value } });

		assertThat(result, is(equalTo(expected)));
	}

	@Test
	public void sessionArrayAttributesWithSession() {
		final String sa1 = "sa1";
		final String sa2 = "sa2";
		final String[] sa1Value = { "saValue1", "saValue2", "saValue3" };
		final String[] sa2Value = { "s1", "s2", "s3" };
		final Vector<String> sessionAttributesList = new Vector<String>();
		Collections.addAll(sessionAttributesList, sa1, sa2);
		final Enumeration<String> sessionAttributes = sessionAttributesList.elements();

		HttpSession session = Mockito.mock(HttpSession.class);
		when(session.getAttributeNames()).thenReturn(sessionAttributes);
		when(session.getAttribute(sa1)).thenReturn(sa1Value);
		when(session.getAttribute(sa2)).thenReturn(sa2Value);
		when(httpServletRequest.getSession(false)).thenReturn(session);

		Map<String, String> result = extractor.getSessionAttributes(httpServletRequest.getClass(), httpServletRequest);

		final String extractedSa1Value = "[saValue1, saValue2, saValue3]".substring(0, 20) + "...";
		final String extractedSa2Value = "[s1, s2, s3]";
		Map<String, String> expected = new HashMap<String, String>();
		MapUtils.putAll(expected, new Object[][] { { sa1, extractedSa1Value }, { sa2, extractedSa2Value } });

		assertThat(result, is(equalTo(expected)));
	}
}
