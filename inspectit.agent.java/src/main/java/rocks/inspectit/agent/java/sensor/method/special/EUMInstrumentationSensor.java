package rocks.inspectit.agent.java.sensor.method.special;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.eum.data.IDataHandler;
import rocks.inspectit.agent.java.eum.instrumentation.JSAgentBuilder;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;

/**
 *
 * Sensor for EUM instrumentation.
 *
 * @author Jonas Kunz
 *
 */
public class EUMInstrumentationSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The runtime linker for creating proxies.
	 */
	@Autowired
	private IRuntimeLinker linker;

	/**
	 * The tracer for correlating front and back end traces.
	 */
	@Autowired
	private TracerImpl tracer;

	/**
	 * Handles the data which we get from the JS agent.
	 */
	@Autowired
	private IDataHandler dataHandler;

	/**
	 * Configuration Storage.
	 */
	@Autowired
	private IConfigurationStorage config;

	/**
	 * Java Script Agent builder.
	 */
	@Autowired
	private JSAgentBuilder agentBuilder;

	/**
	 * The EUM instrumentation hook initialised by this sensor.
	 */
	private EUMInstrumentationHook hook;

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
		hook = new EUMInstrumentationHook(linker, tracer, dataHandler, config, agentBuilder);
	}

}
