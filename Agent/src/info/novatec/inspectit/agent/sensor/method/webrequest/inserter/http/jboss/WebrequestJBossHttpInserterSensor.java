package info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.jboss;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.webrequest.inserter.http.WebrequestDefaultHttpInserterHook;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The webrequest http sensor which initializes and returns the
 * {@link WebrequestJBossHttpInserterHook} class.
 * 
 * @author Thomas Kluge
 * 
 */
public class WebrequestJBossHttpInserterSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The hook.
	 */
	private WebrequestDefaultHttpInserterHook hook = null;

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
	 * The remoteIdentificationManager provides a unique identification for each remote call.
	 */
	@Autowired
	private RemoteIdentificationManager remoteIdentificationManager;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public WebrequestJBossHttpInserterSensor() {

	}

	/**
	 * Constructor. *
	 * 
	 * @param timer
	 *            the timer.
	 * @param idManager
	 *            the idmanager.
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager
	 */
	public WebrequestJBossHttpInserterSensor(IdManager idManager, Timer timer, RemoteIdentificationManager remoteIdentificationManager) {
		this.idManager = idManager;
		this.timer = timer;
		this.remoteIdentificationManager = remoteIdentificationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameters) {
		hook = new WebrequestJBossHttpInserterHook(idManager, timer, remoteIdentificationManager, ManagementFactory.getThreadMXBean());
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
