package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Isabel Vico Peinado
 *
 */
@SuppressWarnings("PMD")
public class ApacheAsyncHttpClientSensorTest extends TestBase {

	@InjectMocks
	ApacheAsyncHttpClientSensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	public static class GetAsyncClientRequestAdapter extends ApacheAsyncHttpClientSensorTest {

		@Mock
		Object object;

		@Mock
		HttpRequest httpRequest;

		@Mock
		RequestLine requestLine;

		@Mock
		HttpContext httpContext;

		@Mock
		SpanStore spanStore;

		@BeforeMethod
		public void init() {
			when(httpRequest.getRequestLine()).thenReturn(requestLine);
		}

		@Test
		public void mustHaveTheProperProperties() {
			AsyncClientRequestAdapter<TextMap> adapter = sensor.getAsyncClientRequestAdapter(object, new Object[] { null, httpRequest, httpContext, null }, rsc);

			assertThat("The propagation type must be HTTP.", adapter.getPropagationType(), is(PropagationType.HTTP));
			assertThat("The reference type must be FOLLOWS FROM.", adapter.getReferenceType(), is(References.FOLLOWS_FROM));
			assertThat("The format must be HTTP HEADER.", adapter.getFormat(), is(Format.Builtin.HTTP_HEADERS));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void mustStartClientProperlyWhenTheRequestDoesNotContainsHeader() {
			when(httpRequest.containsHeader(anyString())).thenReturn(false);

			AsyncClientRequestAdapter<TextMap> adapter = sensor.getAsyncClientRequestAdapter(object, new Object[] { null, httpRequest, httpContext, null }, rsc);

			assertThat("The client must be started.", adapter.startClientSpan(), is(true));
		}

		@Test
		public void mustHaveOneTagWithTheExpectedUri() {
			String uri = "uri";
			when(requestLine.getUri()).thenReturn(uri);

			AsyncClientRequestAdapter<TextMap> adapter = sensor.getAsyncClientRequestAdapter(object, new Object[] { null, httpRequest, httpContext, null }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat("Tags map size must be 1.", tags.size(), is(1));
			assertThat("Tags map must have the expected uri.", tags, hasEntry(Tags.HTTP_URL.getKey(), uri));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void mustHaveOneTagWithTheExpectedMethod() {
			String methodName = "get";
			when(requestLine.getMethod()).thenReturn(methodName);

			AsyncClientRequestAdapter<TextMap> adapter = sensor.getAsyncClientRequestAdapter(object, new Object[] { null, httpRequest, httpContext, null }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat("Tags map size must be 1.", tags.size(), is(1));
			assertThat("Tags map must have the expected method name.", tags, hasEntry(Tags.HTTP_METHOD.getKey(), methodName));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void mustNotHaveAnyTagWhenRequestLineIsNull() {
			when(httpRequest.getRequestLine()).thenReturn(null);

			AsyncClientRequestAdapter<TextMap> adapter = sensor.getAsyncClientRequestAdapter(object, new Object[] { null, httpRequest, httpContext, null }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat("Tags map size must be 0.", tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

	}

	interface HttpRequest {
		RequestLine getRequestLine();

		void setHeader(String key, String value);

		boolean containsHeader(String key);
	};


	interface RequestLine {
		String getUri();

		String getMethod();
	};

	interface HttpContext {
		Object getAttribute(String id);

		void setAttribute(String id, Object obj);

		Object removeAttribute(String id);
	}

}
