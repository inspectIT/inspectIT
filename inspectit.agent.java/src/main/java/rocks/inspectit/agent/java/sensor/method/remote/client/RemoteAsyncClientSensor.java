package rocks.inspectit.agent.java.sensor.method.remote.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.tracing.core.ClientInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.AsyncClientAdapterProvider;
import rocks.inspectit.agent.java.tracing.core.listener.IAsyncSpanContextListener;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Abstract class for all remote async client sensors. Subclasses must implement
 * {@link #getAsyncClientAdapterProvider()} that is passed to the {@link RemoteAsyncClientHook}
 * during initialization.
 * <p>
 * Note that all remote async client sensors class names should be added to the
 * {@link rocks.inspectit.agent.java.sensor.method.invocationsequence.InvocationSequenceHook}, as we
 * don't want additional invocation children to be created if remote sensor did not create any
 * tracing data.
 *
 * @author Ivan Senic
 *
 */
public abstract class RemoteAsyncClientSensor extends AbstractMethodSensor {

	/**
	 * One reflection cache for all instances of all remote client sensors.
	 */
	protected static final ReflectionCache CACHE = new ReflectionCache();

	/**
	 * Client interceptor.
	 */
	@Autowired
	private ClientInterceptor clientInterceptor;

	/**
	 * Listener for firing async spans.
	 */
	@Autowired
	private IAsyncSpanContextListener asyncSpanContextListener;

	/**
	 * Hook.
	 */
	private RemoteAsyncClientHook hook;

	/**
	 * Sub-classes should provide the correct requestAdapter provider based on the technology and
	 * framework they are targeting.
	 *
	 * @return {@link AsyncClientAdapterProvider}.
	 */
	protected abstract AsyncClientAdapterProvider getAsyncClientAdapterProvider();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHook getHook() {
		return hook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		AsyncClientAdapterProvider clientAdapterProvider = getAsyncClientAdapterProvider();
		hook = new RemoteAsyncClientHook(clientInterceptor, clientAdapterProvider, asyncSpanContextListener);
	}

}
