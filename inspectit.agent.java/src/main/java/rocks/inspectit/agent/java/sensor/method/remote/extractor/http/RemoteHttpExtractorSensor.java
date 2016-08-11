package rocks.inspectit.agent.java.sensor.method.remote.extractor.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;

/**
 * The webrequest http sensor which initializes and returns the {@link RemoteHttpExtractorHook}
 * class.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteHttpExtractorSensor extends AbstractMethodSensor {

	/**
	 * The hook.
	 */
	private RemoteHttpExtractorHook hook;

	/**
	 * The ID manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * The extractor.
	 */
	@Autowired
	private RemoteHttpParameterExtractor extractor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initHook(Map<String, Object> parameter) {
		hook = new RemoteHttpExtractorHook(platformManager, extractor);
	}

	public IHook getHook() {
		return hook;
	}

}
