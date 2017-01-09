package rocks.inspectit.agent.java.sensor.method.remote.server;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.tracing.core.ServerInterceptor;
import rocks.inspectit.agent.java.tracing.core.adapter.ServerAdapterProvider;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Abstract class for all remote server sensors that can read tracing information on the start of
 * the request. Subclasses must implement {@link #getServerAdapterProvider()} that is passed to the
 * {@link RemoteServerHook} during initialization.
 *
 * @author Ivan Senic
 *
 */
public abstract class RemoteServerSensor extends AbstractMethodSensor {

	/**
	 * One reflection cache for all instances of all remote server sensors.
	 */
	protected static final ReflectionCache CACHE = new ReflectionCache();

	/**
	 * The Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * Server interceptor.
	 */
	@Autowired
	private ServerInterceptor serverInterceptor;

	/**
	 * Hook.
	 */
	private RemoteServerHook hook;

	/**
	 * Sub-classes should provide the correct requestAdapter provider based on the technology and framework
	 * they are targeting.
	 *
	 * @return {@link ServerAdapterProvider}.
	 */
	protected abstract ServerAdapterProvider getServerAdapterProvider();

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
		ServerAdapterProvider serverAdapterProvider = getServerAdapterProvider();
		hook = new RemoteServerHook(serverInterceptor, serverAdapterProvider, platformManager);
	}

}
