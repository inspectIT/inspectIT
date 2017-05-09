package rocks.inspectit.agent.java.sensor.method.remote.server.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.SpanContext;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class JavaHttpRemoteServerSensorTest extends TestBase {

	@InjectMocks
	JavaHttpRemoteServerSensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	@Mock
	HttpServletRequest httpRequest;

	@Mock
	HttpServletResponse httpResponse;

	public static class GetServerRequestAdapter extends JavaHttpRemoteServerSensorTest {

		@Mock
		Object object;

		@Test
		public void properties() {
			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void uri() {
			String uri = "uri";
			when(httpRequest.getRequestURI()).thenReturn(uri);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), uri));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void uriNull() {
			when(httpRequest.getRequestURI()).thenReturn(null);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void method() {
			String method = "get";
			when(httpRequest.getMethod()).thenReturn(method);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_METHOD.getKey(), method));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void methodNull() {
			when(httpRequest.getMethod()).thenReturn(null);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void baggageExtraction() {
			String key = "key";
			String value = "value";
			when(httpRequest.getHeader(key)).thenReturn(value);
			doReturn(Collections.enumeration(Collections.singleton(key))).when(httpRequest).getHeaderNames();

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			Entry<String, String> next = adapter.getCarrier().iterator().next();
			assertThat(next.getKey(), is(key));
			assertThat(next.getValue(), is(value));
			assertThat(adapter.getCarrier().iterator().hasNext(), is(false));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void baggageExtractionEnumerationEmpty() throws Exception {
			doReturn(Collections.enumeration(Collections.emptyList())).when(httpRequest).getHeaderNames();

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			assertThat(adapter.getCarrier().iterator().hasNext(), is(false));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void baggageExtractionEnumerationNull() throws Exception {
			doReturn(null).when(httpRequest).getHeaderNames();

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			assertThat(adapter.getCarrier().iterator().hasNext(), is(false));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void contextStore() {
			SpanContext spanContext = mock(SpanContext.class);
			when(httpRequest.getAttribute(SpanContextStore.Constants.ID)).thenReturn(spanContext);

			ServerRequestAdapter<TextMap> adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			SpanContextStore spanContextStore = adapter.getSpanContextStore();
			SpanContext result = spanContextStore.getSpanContext();
			assertThat(result, is(spanContext));
			spanContextStore.setSpanContext(spanContext);
			verify(httpRequest).setAttribute(SpanContextStore.Constants.ID, spanContext);
			verifyZeroInteractions(object, httpResponse, rsc);
		}
	}

	public static class GetServertResponseAdapter extends JavaHttpRemoteServerSensorTest {

		@Mock
		Object object;

		@Test
		public void status() {
			int status = 200;
			when(httpResponse.getStatus()).thenReturn(status);

			ResponseAdapter adapter = sensor.getServerResponseAdapter(object, new Object[] { httpRequest, httpResponse }, null, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_STATUS.getKey(), String.valueOf(status)));
			verifyZeroInteractions(object, rsc);
		}

	}

}