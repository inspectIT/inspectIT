package rocks.inspectit.agent.java.sensor.method.remote.extractor.mq;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;

/**
 * The remote mq sensor which initializes and returns the {@link RemoteMQConsumerExtractorHook}
 * class.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteMQConsumerExtractorSensor extends AbstractMethodSensor {

	/**
	 * The hook.
	 */
	private RemoteMQConsumerExtractorHook hook = null;

	/**
	 * The ID manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * The extractor.
	 */
	@Autowired
	private RemoteMQParameterExtractor extractor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initHook(Map<String, Object> parameters) {
		hook = new RemoteMQConsumerExtractorHook(platformManager, extractor);
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
