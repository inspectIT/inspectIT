package rocks.inspectit.agent.java.sensor.exception;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.core.IIdManager;
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
	 * The ID manager.
	 */
	@Autowired
	private IIdManager idManager;

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
	 * @param idManager
	 *            The ID manager.
	 */
	public ExceptionSensor(IIdManager idManager) {
		this.idManager = idManager;
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
	public void init(Map<String, Object> parameter) {
		exceptionSensorHook = new ExceptionSensorHook(idManager, parameter);
	}

}
