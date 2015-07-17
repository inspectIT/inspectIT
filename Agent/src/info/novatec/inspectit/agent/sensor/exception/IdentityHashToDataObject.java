package info.novatec.inspectit.agent.sensor.exception;

import info.novatec.inspectit.communication.data.ExceptionSensorData;

/**
 * This class maps the identity hashcode of a {@link Throwable} object to the
 * {@link ExceptionSensorData} object.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class IdentityHashToDataObject {
	/**
	 * The identity hashcode of the {@link Throwable} object.
	 */
	private Long identityHash;

	/**
	 * The {@link ExceptionSensorData} object containing all information.
	 */
	private ExceptionSensorData exceptionSensorData;

	/**
	 * Default no-arg constructor.
	 */
	public IdentityHashToDataObject() {
	}

	/**
	 * Constructor taking the identity hashcode and the {@link ExceptionSensorData} object for
	 * initialization.
	 * 
	 * @param identityHash
	 *            The identity hashcode of the {@link Throwable} object.
	 * @param exceptionSensorData
	 *            The {@link ExceptionSensorData} object containing the information.
	 */
	public IdentityHashToDataObject(Long identityHash, ExceptionSensorData exceptionSensorData) {
		this.identityHash = identityHash;
		this.exceptionSensorData = exceptionSensorData;
	}

	/**
	 * Returns the identity hashcode of the {@link Throwable} object.
	 * 
	 * @return The identity hashcode of the {@link Throwable} object.
	 */
	public Long getIdentityHash() {
		return identityHash;
	}

	/**
	 * Sets the identity hashcode of the {@link Throwable} object.
	 * 
	 * @param identityHash
	 *            The identity hashcode of the {@link Throwable} object.
	 */
	public void setIdentityHash(Long identityHash) {
		this.identityHash = identityHash;
	}

	/**
	 * Returns the {@link ExceptionSensorData} object containing the information.
	 * 
	 * @return The {@link ExceptionSensorData} object containing the information.
	 */
	public ExceptionSensorData getExceptionSensorData() {
		return exceptionSensorData;
	}

	/**
	 * Sets the {@link ExceptionSensorData} object.
	 * 
	 * @param exceptionSensorData
	 *            The {@link ExceptionSensorData} object containing the information.
	 */
	public void setExceptionSensorData(ExceptionSensorData exceptionSensorData) {
		this.exceptionSensorData = exceptionSensorData;
	}
}
