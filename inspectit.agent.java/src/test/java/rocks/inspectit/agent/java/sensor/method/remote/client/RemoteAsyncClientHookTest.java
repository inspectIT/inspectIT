package rocks.inspectit.agent.java.sensor.method.remote.client;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.math.RandomUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.tracing.core.ClientInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.listener.IAsyncSpanContextListener;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class RemoteAsyncClientHookTest extends TestBase {

	@InjectMocks
	RemoteAsyncClientHook hook;

	@Mock
	ICoreService coreService;

	@Mock
	ClientInterceptor clientInterceptor;

	@Mock
	AsyncClientAdapterProvider adapterProvider;

	@Mock
	AsyncClientRequestAdapter<?> requestAdapter;

	@Mock
	IAsyncSpanContextListener asyncContextListener;

	@Mock
	Object object;

	@Mock
	Object result;

	@Mock
	RegisteredSensorConfig rsc;

	@Test
	public void happyPath() throws Exception {
		// ids
		boolean exception = RandomUtils.nextBoolean();
		long methodId = 7l;
		long sensorId = 13l;
		long spanId = 17l;
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		doReturn(requestAdapter).when(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);

		SpanContextImpl context = mock(SpanContextImpl.class);
		when(context.getId()).thenReturn(spanId);
		SpanImpl spanImpl = mock(SpanImpl.class);
		when(spanImpl.context()).thenReturn(context);
		when(spanImpl.isClient()).thenReturn(true);
		when(clientInterceptor.handleAsyncRequest(requestAdapter)).thenReturn(spanImpl);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, exception, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, exception, rsc);

		verify(asyncContextListener).asyncSpanContextCreated(context);
		verify(spanImpl).setTag(ExtraTags.INSPECTT_METHOD_ID, methodId);
		verify(spanImpl).setTag(ExtraTags.INSPECTT_SENSOR_ID, sensorId);

		// verify timer, interceptor and adapters
		verify(clientInterceptor).handleAsyncRequest(requestAdapter);
		verify(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);
		verifyNoMoreInteractions(adapterProvider, clientInterceptor, coreService);
	}

	@Test
	public void spanNull() throws Exception {
		// ids
		long methodId = 7l;
		long sensorId = 13l;
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		doReturn(requestAdapter).when(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);
		when(clientInterceptor.handleAsyncRequest(requestAdapter)).thenReturn(null);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, false, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, false, rsc);

		// verify timer, interceptor and adapters
		verify(clientInterceptor).handleAsyncRequest(requestAdapter);
		verify(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);
		verifyNoMoreInteractions(adapterProvider, clientInterceptor, coreService);
		verifyZeroInteractions(coreService, asyncContextListener);
	}

	@Test
	public void requestAdapterNull() throws Exception {
		// ids
		long methodId = 7l;
		long sensorId = 13l;
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		doReturn(null).when(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, false, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, false, rsc);

		// verify timer, interceptor and adapters
		verify(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);
		verifyNoMoreInteractions(adapterProvider, clientInterceptor, coreService);
		verifyZeroInteractions(coreService, asyncContextListener);
	}

	@Test
	public void twoCalls() throws Exception {
		// ids
		long methodId = 7l;
		long sensorId = 13l;
		long spanId = 17l;
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		doReturn(requestAdapter).when(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);
		SpanContextImpl context = mock(SpanContextImpl.class);
		when(context.getId()).thenReturn(spanId);
		SpanImpl spanImpl = mock(SpanImpl.class);
		when(spanImpl.context()).thenReturn(context);
		when(spanImpl.isClient()).thenReturn(true);
		when(clientInterceptor.handleAsyncRequest(requestAdapter)).thenReturn(spanImpl);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		// new call
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, false, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, false, rsc);
		// end new call
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, false, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, false, rsc);

		verify(asyncContextListener).asyncSpanContextCreated(context);
		verify(spanImpl).setTag(ExtraTags.INSPECTT_METHOD_ID, methodId);
		verify(spanImpl).setTag(ExtraTags.INSPECTT_SENSOR_ID, sensorId);

		// verify timer, interceptor and adapters
		verify(clientInterceptor).handleAsyncRequest(requestAdapter);
		verify(adapterProvider).getAsyncClientRequestAdapter(object, parameters, rsc);
		verifyNoMoreInteractions(adapterProvider, clientInterceptor, coreService, asyncContextListener);
	}

}
