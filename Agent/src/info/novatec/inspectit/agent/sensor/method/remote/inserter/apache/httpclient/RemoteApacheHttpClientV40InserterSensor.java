package info.novatec.inspectit.agent.sensor.method.remote.inserter.apache.httpclient;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteIdentificationManager;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteDefaultHttpInserterHook;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The webrequest http sensor which initializes and returns the
 * {@link RemoteApacheHttpClientV40InserterHook} class.
 * 
 * @author Thomas Kluge
 * 
 */
public class RemoteApacheHttpClientV40InserterSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * The hook.
	 */
	private RemoteDefaultHttpInserterHook hook = null;

	/**
	 * The ID manager.
	 */
	@Autowired
	private IIdManager idManager;

	/**
	 * The remoteIdentificationManager provides a unique identification for each remote call.
	 */
	@Autowired
	private RemoteIdentificationManager remoteIdentificationManager;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public RemoteApacheHttpClientV40InserterSensor() {

	}

	/**
	 * Constructor. *
	 * 
	 * @param idManager
	 *            the idmanager.
	 * @param remoteIdentificationManager
	 *            the remoteIdentificationManager.
	 */
	public RemoteApacheHttpClientV40InserterSensor(IdManager idManager, RemoteIdentificationManager remoteIdentificationManager) {
		this.idManager = idManager;
		this.remoteIdentificationManager = remoteIdentificationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameters) {
		hook = new RemoteApacheHttpClientV40InserterHook(idManager, remoteIdentificationManager);
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
