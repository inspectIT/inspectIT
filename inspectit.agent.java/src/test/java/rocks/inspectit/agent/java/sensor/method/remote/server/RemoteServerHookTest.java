package rocks.inspectit.agent.java.sensor.method.remote.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.math.RandomUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.tracing.core.ServerInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class RemoteServerHookTest extends TestBase {

	@InjectMocks
	RemoteServerHook hook;

	@Mock
	ICoreService coreService;

	@Mock
	ServerInterceptor serverInterceptor;

	@Mock
	ServerAdapterProvider adapterProvider;

	@Mock
	ServerRequestAdapter<?> requestAdapter;

	@Mock
	ResponseAdapter responseAdapter;

	@Mock
	IPlatformManager platformManager;

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
		long platformId = 1l;
		long methodId = 7l;
		long sensorId = 13l;
		long spanId = 17l;
		// platform
		when(platformManager.getPlatformId()).thenReturn(platformId);
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		doReturn(requestAdapter).when(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		doReturn(responseAdapter).when(adapterProvider).getServerResponseAdapter(object, parameters, result, exception, rsc);

		SpanContextImpl context = mock(SpanContextImpl.class);
		when(context.getId()).thenReturn(spanId);
		SpanImpl spanImpl = mock(SpanImpl.class);
		when(spanImpl.context()).thenReturn(context);
		when(spanImpl.isClient()).thenReturn(false);
		when(serverInterceptor.handleRequest(requestAdapter)).thenReturn(spanImpl);
		when(serverInterceptor.handleResponse(spanImpl, responseAdapter)).thenReturn(spanImpl);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, exception, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, exception, rsc);

		ArgumentCaptor<ServerSpan> captor = ArgumentCaptor.forClass(ServerSpan.class);
		verify(coreService).addDefaultData(captor.capture());
		assertThat(captor.getValue().getPlatformIdent(), is(platformId));
		assertThat(captor.getValue().getSensorTypeIdent(), is(sensorId));
		assertThat(captor.getValue().getMethodIdent(), is(methodId));

		// verify timer, interceptor and adapters
		verify(serverInterceptor).handleRequest(requestAdapter);
		verify(serverInterceptor).handleResponse(spanImpl, responseAdapter);
		verify(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getServerResponseAdapter(object, parameters, result, exception, rsc);
		verifyNoMoreInteractions(adapterProvider, serverInterceptor, coreService);
	}

	@Test
	public void spanNull() throws Exception {
		// ids
		long platformId = 1l;
		long methodId = 7l;
		long sensorId = 13l;
		// platform
		when(platformManager.getPlatformId()).thenReturn(platformId);
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		doReturn(requestAdapter).when(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		doReturn(responseAdapter).when(adapterProvider).getServerResponseAdapter(object, parameters, result, false, rsc);
		when(serverInterceptor.handleRequest(requestAdapter)).thenReturn(null);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, false, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, false, rsc);

		// verify timer, interceptor and adapters
		verify(serverInterceptor).handleRequest(requestAdapter);
		verify(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		verifyNoMoreInteractions(adapterProvider, serverInterceptor, coreService);
		verifyZeroInteractions(coreService);
	}

	@Test
	public void twoCalls() throws Exception {
		// ids
		long platformId = 1l;
		long methodId = 7l;
		long sensorId = 13l;
		long spanId = 17l;
		// platform
		when(platformManager.getPlatformId()).thenReturn(platformId);
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		doReturn(requestAdapter).when(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		doReturn(responseAdapter).when(adapterProvider).getServerResponseAdapter(object, parameters, result, false, rsc);

		SpanContextImpl context = mock(SpanContextImpl.class);
		when(context.getId()).thenReturn(spanId);
		SpanImpl spanImpl = mock(SpanImpl.class);
		when(spanImpl.context()).thenReturn(context);
		when(spanImpl.isClient()).thenReturn(false);
		when(serverInterceptor.handleRequest(requestAdapter)).thenReturn(spanImpl);
		when(serverInterceptor.handleResponse(spanImpl, responseAdapter)).thenReturn(spanImpl);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		// new call
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, false, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, false, rsc);
		// end new call
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, false, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, false, rsc);

		ArgumentCaptor<ServerSpan> captor = ArgumentCaptor.forClass(ServerSpan.class);
		verify(coreService).addDefaultData(captor.capture());
		assertThat(captor.getValue().getPlatformIdent(), is(platformId));
		assertThat(captor.getValue().getSensorTypeIdent(), is(sensorId));
		assertThat(captor.getValue().getMethodIdent(), is(methodId));

		// verify timer, interceptor and adapters
		verify(serverInterceptor).handleRequest(requestAdapter);
		verify(serverInterceptor).handleResponse(spanImpl, responseAdapter);
		verify(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getServerResponseAdapter(object, parameters, result, false, rsc);
		verifyNoMoreInteractions(adapterProvider, serverInterceptor, coreService);
	}

}
