package rocks.inspectit.agent.java.sensor.method.remote.inserter.mq;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.remote.inserter.RemoteIdentificationManager;

/**
 * The remote mq sensor which initializes and returns the {@link RemoteMQInserterHook} class.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteMQInserterSensor extends AbstractMethodSensor {

	/**
	 * The hook.
	 */
	private RemoteMQInserterHook hook = null;

	/**
	 * The Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * The remoteIdentificationManager provides a unique identification for each remote call.
	 */
	@Autowired
	private RemoteIdentificationManager remoteIdentificationManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initHook(Map<String, Object> parameters) {
		hook = new RemoteMQInserterHook(platformManager, remoteIdentificationManager);
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return hook;
	}

}
