package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpringRestTemplateClientSensorTest extends TestBase {

	@InjectMocks
	SpringRestTemplateClientSensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	@Mock
	ClientHttpRequest httpRequest;

	public static class GetClientRequestAdapter extends SpringRestTemplateClientSensorTest {

		@Mock
		Headers headers;

		@Test
		public void properties() {
			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			assertThat(adapter.getReferenceType(), is(References.CHILD_OF));
			assertThat(adapter.getFormat(), is(Format.Builtin.HTTP_HEADERS));
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void spanStarting() {
			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			assertThat(adapter.startClientSpan(), is(true));
		}

		@Test
		public void url() throws Exception {
			String uri = "http://localhost";
			when(httpRequest.getURI()).thenReturn(new URI(uri));

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), uri));
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void urlUriNull() {
			when(httpRequest.getURI()).thenReturn(null);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void method() throws Exception {
			String method = "GET";
			when(httpRequest.getMethod()).thenReturn(method);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_METHOD.getKey(), method));
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void methodNull() {
			when(httpRequest.getMethod()).thenReturn(null);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void baggageInjection() {
			String key = "key";
			String value = "value";
			when(httpRequest.getHeaders()).thenReturn(headers);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);
			adapter.getCarrier().put(key, value);

			verify(headers).set(key, value);
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void baggageInjectionHeadersNull() {
			String key = "key";
			String value = "value";
			when(httpRequest.getHeaders()).thenReturn(null);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);
			adapter.getCarrier().put(key, value);

			verifyNoMoreInteractions(rsc);
		}
	}

	public static class GetClientResponseAdapter extends SpringRestTemplateClientSensorTest {

		@Mock
		ClientHttpResponse httpResponse;

		@Test
		public void exception() {
			ResponseAdapter adapter = sensor.getClientResponseAdapter(httpRequest, null, new NullPointerException(), true, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(2));
			assertThat(tags, hasEntry(Tags.ERROR.getKey(), String.valueOf(true)));
			assertThat(tags, hasEntry(ExtraTags.THROWABLE_TYPE, NullPointerException.class.getSimpleName()));
			verifyZeroInteractions(httpRequest, rsc);
		}

		@Test
		public void status() {
			int status = 200;
			when(httpResponse.getRawStatusCode()).thenReturn(status);

			ResponseAdapter adapter = sensor.getClientResponseAdapter(httpRequest, null, httpResponse, false, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_STATUS.getKey(), String.valueOf(status)));
			verifyZeroInteractions(httpRequest);
			verifyNoMoreInteractions(rsc);
		}

	}


	interface ClientHttpRequest {
		Object getURI();
		Object getMethod();
		Headers getHeaders();
	};

	interface Headers {
		void set(String key, String value);
		boolean containsKey(Object key);
	}

	interface ClientHttpResponse {
		int getRawStatusCode();
	};

}