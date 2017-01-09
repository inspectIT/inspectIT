package rocks.inspectit.agent.java.sensor.method.remote.client.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class JettyHttpClientV61SensorTest extends TestBase {

	@InjectMocks
	JettyHttpClientV61Sensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	public static class GetClientRequestAdapter extends JettyHttpClientV61SensorTest {

		@Mock
		Object object;

		@Mock
		HttpExchange httpExchange;

		@Mock
		Scheme scheme;

		@Test
		public void properties() {
			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			assertThat(adapter.getReferenceType(), is(References.FOLLOWS_FROM));
			assertThat(adapter.getFormat(), is(Format.Builtin.HTTP_HEADERS));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void spanStarting() {
			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			assertThat(adapter.startClientSpan(), is(true));
		}

		@Test
		public void url() {
			String uri = "/test";
			String schemeString = "http";
			String address = "localhost:8080";
			when(scheme.array()).thenReturn(schemeString.getBytes());
			when(httpExchange.getScheme()).thenReturn(scheme);
			when(httpExchange.getAddress()).thenReturn(address);
			when(httpExchange.getRequestURI()).thenReturn(uri);


			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), schemeString + "://" + address + uri));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void urlUriNull() {
			String schemeString = "http";
			String address = "localhost:8080";
			when(scheme.array()).thenReturn(schemeString.getBytes());
			when(httpExchange.getScheme()).thenReturn(scheme);
			when(httpExchange.getAddress()).thenReturn(address);
			when(httpExchange.getRequestURI()).thenReturn(null);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), schemeString + "://" + address));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void urlAddressNull() {
			String uri = "/test";
			String schemeString = "http";
			when(scheme.array()).thenReturn(schemeString.getBytes());
			when(httpExchange.getScheme()).thenReturn(scheme);
			when(httpExchange.getAddress()).thenReturn(null);
			when(httpExchange.getRequestURI()).thenReturn(uri);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), schemeString + "://" + uri));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void urlSchemeNull() {
			String uri = "/test";
			String address = "localhost:8080";
			when(httpExchange.getScheme()).thenReturn(null);
			when(httpExchange.getAddress()).thenReturn(address);
			when(httpExchange.getRequestURI()).thenReturn(uri);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), address + uri));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void urlSchemeBytesNull() {
			String uri = "/test";
			String address = "localhost:8080";
			when(scheme.array()).thenReturn(null);
			when(httpExchange.getScheme()).thenReturn(scheme);
			when(httpExchange.getAddress()).thenReturn(address);
			when(httpExchange.getRequestURI()).thenReturn(uri);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_URL.getKey(), address + uri));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void urlAllNull() {
			when(httpExchange.getScheme()).thenReturn(null);
			when(httpExchange.getAddress()).thenReturn(null);
			when(httpExchange.getRequestURI()).thenReturn(null);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void method() {
			String method = "get";
			when(httpExchange.getMethod()).thenReturn(method);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry(Tags.HTTP_METHOD.getKey(), method));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void methodNull() {
			when(httpExchange.getMethod()).thenReturn(null);

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageInjection() {
			String key = "key";
			String value = "value";

			ClientRequestAdapter<TextMap> adapter = sensor.getClientRequestAdapter(object, new Object[] { httpExchange }, rsc);
			adapter.getCarrier().put(key, value);

			verify(httpExchange).setRequestHeader(key, value);
			verifyZeroInteractions(object, rsc);
		}
	}

	public static class GetClientResponseAdapter extends JettyHttpClientV61SensorTest {

		@Mock
		Object object;

		@Mock
		Object result;

		@Test
		public void empty() {
			ResponseAdapter adapter = sensor.getClientResponseAdapter(object, null, result, rsc);

			assertThat(adapter, is(not(nullValue())));
			assertThat(adapter.getTags().size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

	}

	interface HttpExchange {
		Scheme getScheme();
		Object getAddress();
		Object getRequestURI();
		String getMethod();
		void setRequestHeader(String key, String value);
	}

	interface Scheme {
		byte[] array();
	}

}