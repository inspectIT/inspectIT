package info.novatec.inspectit.agent.sensor.exception;

import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

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
