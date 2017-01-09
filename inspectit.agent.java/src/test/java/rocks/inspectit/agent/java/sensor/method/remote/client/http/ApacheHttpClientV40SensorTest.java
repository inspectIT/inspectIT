package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
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
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ApacheHttpClientV40SensorTest extends TestBase {

	@InjectMocks
	ApacheHttpClientV40Sensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	public static class GetClientRequestAdapter extends ApacheHttpClientV40SensorTest {

		@Mock
		Object object;

		@Mock
		HttpRequest httpRequest;

		@Mock
		RequestLine requestLine;

		@BeforeMethod
		public void setup() {
			when(httpRequest.getRequestLine()).thenReturn(requestLine);
		}

		@Test
		public void properties() {
			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			assertThat(adapter.getReferenceType(), is(References.CHILD_OF));
			assertThat(adapter.getFormat(), is(Format.Builtin.HTTP_HEADERS));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void uri() {
			String uri = "uri";
			when(requestLine.getUri()).thenReturn(uri);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), uri));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void method() {
			String method = "get";
			when(requestLine.getMethod()).thenReturn(method);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_METHOD.getKey(), method));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void nullRequestLine() {
			when(httpRequest.getRequestLine()).thenReturn(null);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageInjection() {
			String key = "key";
			String value = "value";

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);
			adapter.getCarrier().put(key, value);

			verify(httpRequest).setHeader(key, value);
			verifyZeroInteractions(object, rsc);
		}
	}

	public static class GetClientResponseAdapter extends ApacheHttpClientV40SensorTest {

		@Mock
		Object object;

		@Mock
		HttpResponse httpResponse;

		@Mock
		StatusLine statusLine;

		@Test
		public void status() {
			int status = 200;
			when(statusLine.getStatusCode()).thenReturn(status);
			when(httpResponse.getStatusLine()).thenReturn(statusLine);

			ResponseAdapter adapter = sensor.getClientResponseAdapter(object, null, httpResponse, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_STATUS.getKey(), String.valueOf(status)));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void statusNullStatusLine() {
			when(httpResponse.getStatusLine()).thenReturn(null);

			ResponseAdapter adapter = sensor.getClientResponseAdapter(object, null, httpResponse, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

	}


	interface HttpRequest {
		RequestLine getRequestLine();
		void setHeader(String key, String value);
	};

	interface HttpResponse {
		StatusLine getStatusLine();
	};

	interface RequestLine {
		String getUri();
		String getMethod();
	};

	interface StatusLine {
		int getStatusCode();
	};

}