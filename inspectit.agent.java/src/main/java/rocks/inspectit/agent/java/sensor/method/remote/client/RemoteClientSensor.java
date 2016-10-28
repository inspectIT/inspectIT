package rocks.inspectit.agent.java.sensor.method.remote.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.tracing.core.ClientInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ClientAdapterProvider;
import rocks.inspectit.agent.java.util.ReflectionCache;
import rocks.inspectit.agent.java.util.Timer;

/**
 * Abstract class for all remote client sensors. Subclasses must implement
 * {@link #getClientAdapterProvider()} that is passed to the {@link RemoteClientHook} during
 * initialization.
 *
 * @author Ivan Senic
 *
 */
public abstract class RemoteClientSensor extends AbstractMethodSensor {

	/**
	 * One reflection cache for all instances of all remote client sensors.
	 */
	protected static final ReflectionCache CACHE = new ReflectionCache();

	/**
	 * Timer to measure time.
	 */
	@Autowired
	private Timer timer;

	/**
	 * The Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * Client interceptor.
	 */
	@Autowired
	private ClientInterceptor clientInterceptor;

	/**
	 * Hook.
	 */
	private RemoteClientHook hook;

	/**
	 * Sub-classes should provide the correct requestAdapter provider based on the technology and framework
	 * they are targeting.
	 *
	 * @return {@link ClientAdapterProvider}.
	 */
	protected abstract ClientAdapterProvider getClientAdapterProvider();

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
		ClientAdapterProvider clientAdapterProvider = getClientAdapterProvider();
		hook = new RemoteClientHook(clientInterceptor, clientAdapterProvider, platformManager, timer);
	}

}
