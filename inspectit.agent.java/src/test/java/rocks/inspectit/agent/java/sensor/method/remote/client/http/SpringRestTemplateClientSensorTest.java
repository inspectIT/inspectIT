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

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.Tag;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ReferenceType;

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
			when(rsc.getTargetMethodName()).thenReturn("execute");

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			assertThat(adapter.getReferenceType(), is(ReferenceType.CHILD_OF));
			verify(rsc).getTargetMethodName();
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void propertiesAsync() {
			when(rsc.getTargetMethodName()).thenReturn("executeAsync");

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			assertThat(adapter.getReferenceType(), is(ReferenceType.FOLLOW_FROM));
			verify(rsc).getTargetMethodName();
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void uri() throws Exception {
			String uri = "http://localhost";
			when(httpRequest.getURI()).thenReturn(new URI(uri));
			when(rsc.getTargetMethodName()).thenReturn("execute");

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Http.URL, uri));
			verify(rsc).getTargetMethodName();
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void uriNull() {
			when(httpRequest.getURI()).thenReturn(null);
			when(rsc.getTargetMethodName()).thenReturn("execute");

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verify(rsc).getTargetMethodName();
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void baggageInjection() {
			String key = "key";
			String value = "value";
			when(httpRequest.getHeaders()).thenReturn(headers);
			when(rsc.getTargetMethodName()).thenReturn("execute");

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);
			adapter.getBaggageInjectAdapter().putBaggageItem(key, value);

			verify(headers).set(key, value);
			verify(rsc).getTargetMethodName();
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void baggageInjectionHeadersNull() {
			String key = "key";
			String value = "value";
			when(httpRequest.getHeaders()).thenReturn(null);
			when(rsc.getTargetMethodName()).thenReturn("execute");

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(httpRequest, null, rsc);
			adapter.getBaggageInjectAdapter().putBaggageItem(key, value);

			verify(rsc).getTargetMethodName();
			verifyNoMoreInteractions(rsc);
		}
	}

	public static class GetClientResponseAdapter extends SpringRestTemplateClientSensorTest {

		@Mock
		ClientHttpResponse httpResponse;

		@Test
		public void status() {
			int status = 200;
			when(httpResponse.getRawStatusCode()).thenReturn(status);
			when(rsc.getTargetMethodName()).thenReturn("execute");

			ResponseAdapter adapter = sensor.getClientResponseAdapter(httpRequest, null, httpResponse, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Http.STATUS, String.valueOf(status)));
			verify(rsc).getTargetMethodName();
			verifyZeroInteractions(httpRequest);
			verifyNoMoreInteractions(rsc);
		}

		@Test
		public void statusAsync() {
			when(rsc.getTargetMethodName()).thenReturn("executeAsync");

			ResponseAdapter adapter = sensor.getClientResponseAdapter(httpRequest, null, null, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verify(rsc).getTargetMethodName();
			verifyZeroInteractions(httpRequest);
			verifyNoMoreInteractions(rsc);
		}

	}


	interface ClientHttpRequest {
		URI getURI();
		Headers getHeaders();
	};

	interface Headers {
		void set(String key, String value);
	}

	interface ClientHttpResponse {
		int getRawStatusCode();
	};

}