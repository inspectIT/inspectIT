package rocks.inspectit.agent.java.sensor.method.remote.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.tracing.core.ServerInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerRequestAdapter;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

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
	ServerRequestAdapter requestAdapter;

	@Mock
	ResponseAdapter responseAdapter;

	@Mock
	IPlatformManager platformManager;

	@Mock
	Timer timer;

	@Mock
	Object object;

	@Mock
	Object result;

	@Mock
	RegisteredSensorConfig rsc;

	@Test
	public void happyPath() throws Exception {
		// ids
		long platformId = 1l;
		long methodId = 7l;
		long sensorId = 13l;
		// timer setup
		Double firstTimerValue = 1000.0d;
		Double secondTimerValue = 1323.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		// platform
		when(platformManager.getPlatformId()).thenReturn(platformId);
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		when(adapterProvider.getServerRequestAdapter(object, parameters, rsc)).thenReturn(requestAdapter);
		when(adapterProvider.getServerResponseAdapter(object, parameters, result, rsc)).thenReturn(responseAdapter);
		ServerSpan serverSpan = new ServerSpan();
		serverSpan.setSpanIdent(SpanIdent.build());
		when(serverInterceptor.handleRequest(requestAdapter)).thenReturn(serverSpan);
		when(serverInterceptor.handleResponse(responseAdapter)).thenReturn(serverSpan);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);

		ArgumentCaptor<ServerSpan> captor = ArgumentCaptor.forClass(ServerSpan.class);
		verify(coreService).addMethodSensorData(eq(sensorId), eq(methodId), eq(String.valueOf(serverSpan.getSpanIdent().getId())), captor.capture());
		assertThat(captor.getValue() == serverSpan, is(true));
		assertThat(serverSpan.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(serverSpan.getTimeStamp(), is(not(nullValue())));
		assertThat(serverSpan.getPlatformIdent(), is(platformId));
		assertThat(serverSpan.getSensorTypeIdent(), is(sensorId));
		assertThat(serverSpan.getMethodIdent(), is(methodId));

		// verify timer, interceptor and adapters
		verify(timer, times(2)).getCurrentTime();
		verify(serverInterceptor).handleRequest(requestAdapter);
		verify(serverInterceptor).handleResponse(responseAdapter);
		verify(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getServerResponseAdapter(object, parameters, result, rsc);
		verifyNoMoreInteractions(timer, adapterProvider, serverInterceptor, coreService);
	}

	@Test
	public void spansNull() throws Exception {
		// ids
		long platformId = 1l;
		long methodId = 7l;
		long sensorId = 13l;
		// timer setup
		Double firstTimerValue = 1000.0d;
		Double secondTimerValue = 1323.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		// platform
		when(platformManager.getPlatformId()).thenReturn(platformId);
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		when(adapterProvider.getServerRequestAdapter(object, parameters, rsc)).thenReturn(requestAdapter);
		when(adapterProvider.getServerResponseAdapter(object, parameters, result, rsc)).thenReturn(responseAdapter);
		when(serverInterceptor.handleRequest(requestAdapter)).thenReturn(null);
		when(serverInterceptor.handleResponse(responseAdapter)).thenReturn(null);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);

		// verify timer, interceptor and adapters
		verify(timer, times(2)).getCurrentTime();
		verify(serverInterceptor).handleRequest(requestAdapter);
		verify(serverInterceptor).handleResponse(responseAdapter);
		verify(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getServerResponseAdapter(object, parameters, result, rsc);
		verifyNoMoreInteractions(timer, adapterProvider, serverInterceptor, coreService);
		verifyZeroInteractions(coreService);
	}

	@Test
	public void twoCalls() throws Exception {
		// ids
		long platformId = 1l;
		long methodId = 7l;
		long sensorId = 13l;
		// timer setup
		Double firstTimerValue = 1000.0d;
		Double secondTimerValue = 1323.0d;
		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		// platform
		when(platformManager.getPlatformId()).thenReturn(platformId);
		// interceptor
		Object[] parameters = new String[] { "blah", "bla" };
		when(adapterProvider.getServerRequestAdapter(object, parameters, rsc)).thenReturn(requestAdapter);
		when(adapterProvider.getServerResponseAdapter(object, parameters, result, rsc)).thenReturn(responseAdapter);
		ServerSpan serverSpan = new ServerSpan();
		serverSpan.setSpanIdent(SpanIdent.build());
		when(serverInterceptor.handleRequest(requestAdapter)).thenReturn(serverSpan);
		when(serverInterceptor.handleResponse(responseAdapter)).thenReturn(serverSpan);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		// new call
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);
		// end new call
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);

		ArgumentCaptor<ServerSpan> captor = ArgumentCaptor.forClass(ServerSpan.class);
		verify(coreService).addMethodSensorData(eq(sensorId), eq(methodId), eq(String.valueOf(serverSpan.getSpanIdent().getId())), captor.capture());
		assertThat(captor.getValue() == serverSpan, is(true));
		assertThat(serverSpan.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(serverSpan.getTimeStamp(), is(not(nullValue())));
		assertThat(serverSpan.getPlatformIdent(), is(platformId));
		assertThat(serverSpan.getSensorTypeIdent(), is(sensorId));
		assertThat(serverSpan.getMethodIdent(), is(methodId));

		// verify timer, interceptor and adapters
		verify(timer, times(2)).getCurrentTime();
		verify(serverInterceptor).handleRequest(requestAdapter);
		verify(serverInterceptor).handleResponse(responseAdapter);
		verify(adapterProvider).getServerRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getServerResponseAdapter(object, parameters, result, rsc);
		verifyNoMoreInteractions(timer, adapterProvider, serverInterceptor, coreService);
	}

}
