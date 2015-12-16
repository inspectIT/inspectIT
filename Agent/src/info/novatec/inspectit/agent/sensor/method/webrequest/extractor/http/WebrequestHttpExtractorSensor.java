package info.novatec.inspectit.agent.sensor.method.webrequest.extractor.http;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The webrequest http sensor which initializes and returns the {@link WebrequestHttpExtractorHook}
 * class.
 * 
 * @author Thomas Kluge
 * 
 */
public class WebrequestHttpExtractorSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The hook.
	 */
	private WebrequestHttpExtractorHook hook;

	/**
	 * The ID manager.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * The timer used for accurate measuring.
	 */
	@Autowired
	private Timer timer;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public WebrequestHttpExtractorSensor() {

	}

	/**
	 * Constructor.
	 * 
	 * @param timer
	 *            the timer.
	 * @param idManager
	 *            the idmanager.
	 */
	public WebrequestHttpExtractorSensor(IdManager idManager, Timer timer) {
		this.idManager = idManager;
		this.timer = timer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		hook = new WebrequestHttpExtractorHook(idManager, timer, ManagementFactory.getThreadMXBean());

	}

	public IHook getHook() {
		return hook;
	}

}
