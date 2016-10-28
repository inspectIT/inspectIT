package rocks.inspectit.agent.java.sensor.method.remote.server.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.Tag;
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
			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			assertThat(adapter.getPropagationType(), is(PropagationType.HTTP));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void uri() {
			String uri = "uri";
			when(httpRequest.getRequestURI()).thenReturn(uri);

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Http.URL, uri));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void uriNull() {
			when(httpRequest.getRequestURI()).thenReturn(null);

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(0));
			verifyZeroInteractions(object, httpResponse, rsc);
		}

		@Test
		public void baggageExtraction() {
			String key = "key";
			String value = "value";
			when(httpRequest.getHeader(key)).thenReturn(value);

			ServerRequestAdapter adapter = sensor.getServerRequestAdapter(object, new Object[] { httpRequest, httpResponse }, rsc);

			assertThat(adapter.getBaggageExtractAdapter().getBaggageItem(key), is(value));
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

			Map<Tag, String> tags = adapter.getTags();
			assertThat(tags.size(), is(1));
			assertThat(tags, hasEntry((Tag) Tag.Http.STATUS, String.valueOf(status)));
			verifyZeroInteractions(object, rsc);
		}

	}

}