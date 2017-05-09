package rocks.inspectit.agent.java.sensor.method.remote.server.manual;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import io.opentracing.tag.Tags;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.SpanContextStore;
import rocks.inspectit.agent.java.tracing.core.adapter.store.NoopSpanContextStore;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ManualRemoteServerSensorTest extends TestBase {

	@InjectMocks
	ManualRemoteServerSensor sensor;

	@Mock
	RegisteredSensorConfig rsc;

	@Mock
	Object object;

	public static class GetServerRequestAdapter extends ManualRemoteServerSensorTest {

		@Test
		public void empty() {
			ServerRequestAdapter<?> adapter = sensor.getServerRequestAdapter(object, null, rsc);

			assertThat(adapter.getPropagationType(), is(nullValue()));
			assertThat(adapter.getTags().size(), is(0));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void baggageExtraction() {
			ServerRequestAdapter<?> adapter = sensor.getServerRequestAdapter(object, null, rsc);

			assertThat(adapter.getCarrier(), is(nullValue()));
			assertThat(adapter.getFormat(), is(nullValue()));
			verifyZeroInteractions(object, rsc);
		}

		@Test
		public void contextStore() {
			ServerRequestAdapter<?> adapter = sensor.getServerRequestAdapter(object, null, rsc);

			SpanContextStore spanContextStore = adapter.getSpanContextStore();
			assertThat(spanContextStore, is(not(nullValue())));
			assertThat(spanContextStore, is(instanceOf(NoopSpanContextStore.class)));
			verifyZeroInteractions(object, rsc);
		}

	}

	public static class GetServerResponseAdapter extends ManualRemoteServerSensorTest {

		@Mock
		Object result;

		@Test
		public void empty() {
			ResponseAdapter adapter = sensor.getServerResponseAdapter(object, null, result, false, rsc);

			assertThat(adapter.getTags().size(), is(0));
			verifyZeroInteractions(object, result, rsc);
		}

		@Test
		public void exception() {
			ResponseAdapter adapter = sensor.getServerResponseAdapter(object, null, new NullPointerException(), true, rsc);

			Map<String, String> tags = adapter.getTags();
			assertThat(tags.size(), is(2));
			assertThat(tags, hasEntry(Tags.ERROR.getKey(), String.valueOf(true)));
			assertThat(tags, hasEntry(ExtraTags.THROWABLE_TYPE, NullPointerException.class.getSimpleName()));
			verifyZeroInteractions(object, rsc);
		}

	}
}
