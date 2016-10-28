package rocks.inspectit.agent.java.sensor.method.remote.client;

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
import rocks.inspectit.agent.java.tracing.core.ClientInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientRequestAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.ResponseAdapter;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class RemoteClientHookTest extends TestBase {

	@InjectMocks
	RemoteClientHook hook;

	@Mock
	ICoreService coreService;

	@Mock
	ClientInterceptor clientInterceptor;

	@Mock
	ClientAdapterProvider adapterProvider;

	@Mock
	ClientRequestAdapter requestAdapter;

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
		when(adapterProvider.getClientRequestAdapter(object, parameters, rsc)).thenReturn(requestAdapter);
		when(adapterProvider.getClientResponseAdapter(object, parameters, result, rsc)).thenReturn(responseAdapter);
		ClientSpan clientSpan = new ClientSpan();
		clientSpan.setSpanIdent(SpanIdent.build());
		when(clientInterceptor.handleRequest(requestAdapter)).thenReturn(clientSpan);
		when(clientInterceptor.handleResponse(responseAdapter)).thenReturn(clientSpan);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);

		ArgumentCaptor<ClientSpan> captor = ArgumentCaptor.forClass(ClientSpan.class);
		verify(coreService).addMethodSensorData(eq(sensorId), eq(methodId), eq(String.valueOf(clientSpan.getSpanIdent().getId())), captor.capture());
		assertThat(captor.getValue() == clientSpan, is(true));
		assertThat(clientSpan.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(clientSpan.getTimeStamp(), is(not(nullValue())));
		assertThat(clientSpan.getPlatformIdent(), is(platformId));
		assertThat(clientSpan.getSensorTypeIdent(), is(sensorId));
		assertThat(clientSpan.getMethodIdent(), is(methodId));

		// verify timer, interceptor and adapters
		verify(timer, times(2)).getCurrentTime();
		verify(clientInterceptor).handleRequest(requestAdapter);
		verify(clientInterceptor).handleResponse(responseAdapter);
		verify(adapterProvider).getClientRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getClientResponseAdapter(object, parameters, result, rsc);
		verifyNoMoreInteractions(timer, adapterProvider, clientInterceptor, coreService);
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
		when(adapterProvider.getClientRequestAdapter(object, parameters, rsc)).thenReturn(requestAdapter);
		when(adapterProvider.getClientResponseAdapter(object, parameters, result, rsc)).thenReturn(responseAdapter);
		when(clientInterceptor.handleRequest(requestAdapter)).thenReturn(null);
		when(clientInterceptor.handleResponse(responseAdapter)).thenReturn(null);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);

		// verify timer, interceptor and adapters
		verify(timer, times(2)).getCurrentTime();
		verify(clientInterceptor).handleRequest(requestAdapter);
		verify(clientInterceptor).handleResponse(responseAdapter);
		verify(adapterProvider).getClientRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getClientResponseAdapter(object, parameters, result, rsc);
		verifyNoMoreInteractions(timer, adapterProvider, clientInterceptor, coreService);
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
		when(adapterProvider.getClientRequestAdapter(object, parameters, rsc)).thenReturn(requestAdapter);
		when(adapterProvider.getClientResponseAdapter(object, parameters, result, rsc)).thenReturn(responseAdapter);
		ClientSpan clientSpan = new ClientSpan();
		clientSpan.setSpanIdent(SpanIdent.build());
		when(clientInterceptor.handleRequest(requestAdapter)).thenReturn(clientSpan);
		when(clientInterceptor.handleResponse(responseAdapter)).thenReturn(clientSpan);

		// execute calls
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		// new call
		hook.beforeBody(methodId, sensorId, object, parameters, rsc);
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);
		// end new call
		hook.firstAfterBody(methodId, sensorId, object, parameters, result, rsc);
		hook.secondAfterBody(coreService, methodId, sensorId, object, parameters, result, rsc);

		ArgumentCaptor<ClientSpan> captor = ArgumentCaptor.forClass(ClientSpan.class);
		verify(coreService).addMethodSensorData(eq(sensorId), eq(methodId), eq(String.valueOf(clientSpan.getSpanIdent().getId())), captor.capture());
		assertThat(captor.getValue() == clientSpan, is(true));
		assertThat(clientSpan.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(clientSpan.getTimeStamp(), is(not(nullValue())));
		assertThat(clientSpan.getPlatformIdent(), is(platformId));
		assertThat(clientSpan.getSensorTypeIdent(), is(sensorId));
		assertThat(clientSpan.getMethodIdent(), is(methodId));

		// verify timer, interceptor and adapters
		verify(timer, times(2)).getCurrentTime();
		verify(clientInterceptor).handleRequest(requestAdapter);
		verify(clientInterceptor).handleResponse(responseAdapter);
		verify(adapterProvider).getClientRequestAdapter(object, parameters, rsc);
		verify(adapterProvider).getClientResponseAdapter(object, parameters, result, rsc);
		verifyNoMoreInteractions(timer, adapterProvider, clientInterceptor, coreService);
	}

}
