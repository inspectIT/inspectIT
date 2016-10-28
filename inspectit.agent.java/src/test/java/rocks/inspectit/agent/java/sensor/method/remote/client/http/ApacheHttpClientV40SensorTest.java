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
			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			assertThat(adapter.getReferenceType(), is(ReferenceType.CHILD_OF));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void uri() {
			String uri = "uri";
			when(requestLine.getUri()).thenReturn(uri);

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Http.URL, uri));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void uriNullRequestLine() {
			when(httpRequest.getRequestLine()).thenReturn(null);

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageInjection() {
			String key = "key";
			String value = "value";

			ClientRequestAdapter adapter = sensor.getClientRequestAdapter(object, new Object[] { null, httpRequest }, rsc);
			adapter.getBaggageInjectAdapter().putBaggageItem(key, value);

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

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Http.STATUS, String.valueOf(status)));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void statusNullStatusLine() {
			when(httpResponse.getStatusLine()).thenReturn(null);

			ResponseAdapter adapter = sensor.getClientResponseAdapter(object, null, httpResponse, rsc);

			Map<Tag, String> tags = adapter.getTags();
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
	};

	interface StatusLine {
		int getStatusCode();
	};

}