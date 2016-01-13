package info.novatec.inspectit.agent.sensor.method.remote.extractor.http;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The webrequest http sensor which initializes and returns the {@link RemoteHttpExtractorHook}
 * class.
 * 
 * @author Thomas Kluge
 * 
 */
public class RemoteHttpExtractorSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The hook.
	 */
	private RemoteHttpExtractorHook hook;

	/**
	 * The ID manager.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * The extractor.
	 */
	@Autowired
	private RemoteHttpParameterExtractor extractor;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public RemoteHttpExtractorSensor() {

	}

	/**
	 * Constructor.
	 * 
	 * @param idManager
	 *            the idmanager.
	 */
	public RemoteHttpExtractorSensor(IdManager idManager) {
		this.idManager = idManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		hook = new RemoteHttpExtractorHook(idManager, extractor);
	}

	public IHook getHook() {
		return hook;
	}

}
