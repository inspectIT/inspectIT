package rocks.inspectit.agent.java.sensor.exception;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;

/**
 * The {@link ExceptionSensor} which initializes and returns the {@link ExceptionSensorHook} class.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class ExceptionSensor extends AbstractMethodSensor implements IExceptionSensor {

	/**
	 * The Platform manager.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * The used exception sensor hook.
	 */
	private ExceptionSensorHook exceptionSensorHook = null;

	/**
	 * No-arg constructor needed for Spring.
	 */
	public ExceptionSensor() {
	}

	/**
	 * The default constructor which needs 3 parameter for initialization.
	 *
	 * @param platformManager
	 *            The Platform manager.
	 */
	public ExceptionSensor(IPlatformManager platformManager) {
		this.platformManager = platformManager;
	}

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return exceptionSensorHook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		exceptionSensorHook = new ExceptionSensorHook(platformManager, parameters);
	}

}
