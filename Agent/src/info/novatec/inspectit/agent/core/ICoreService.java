package info.novatec.inspectit.agent.core;

import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;

/**
 * Interface definition for the core service. The core service is the central point of the Agent
 * where all data is collected, triggered etc.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * 
 */
public interface ICoreService {

	/**
	 * Start this component.
	 */
	void start();

	/**
	 * Stop this component.
	 */
	void stop();

	/**
	 * Adds a new measurement from a method sensor to the value storage.
	 * 
	 * @param sensorTypeId
	 *            The id of the sensor type.
	 * @param methodId
	 *            The id of the method.
	 * @param prefix
	 *            An arbitrary prefix {@link String}.
	 * @param methodSensorData
	 *            The method sensor data.
	 */
	void addMethodSensorData(long sensorTypeId, long methodId, String prefix, MethodSensorData methodSensorData);

	/**
	 * Adds a new data object from the platform sensor to the value storage.
	 * 
	 * @param sensorTypeIdent
	 *            The id of the sensor type.
	 * @param systemSensorData
	 *            The system sensor data.
	 */
	void addPlatformSensorData(long sensorTypeIdent, SystemSensorData systemSensorData);

	/**
	 * Adds a new data object from the exception sensor to the value storage.
	 * 
	 * @param sensorTypeIdent
	 *            The id of the sensor type.
	 * @param throwableIdentityHashCode
	 *            The identityHashCode of the {@link ExceptionSensorData} object.
	 * @param exceptionSensorData
	 *            The exception sensor data.
	 */
	void addExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode, ExceptionSensorData exceptionSensorData);

	/**
	 * Adds a new object storage to the value storage. An object storage contains an instance of
	 * {@link IObjectStorage} which serves as a wrapper around a value object.
	 * 
	 * @param sensorTypeId
	 *            The id of the sensor type.
	 * @param methodId
	 *            The id of the method.
	 * @param prefix
	 *            An arbitrary prefix {@link String}.
	 * @param objectStorage
	 *            The object storage.
	 */
	void addObjectStorage(long sensorTypeId, long methodId, String prefix, IObjectStorage objectStorage);

	/**
	 * Triggers sending the buffered data.
	 */
	void sendData();

	/**
	 * Returns a saved measurement ({@link MethodSensorData}) for further processing.
	 * 
	 * @param sensorTypeIdent
	 *            The id of the sensor type to retrieve the measurement.
	 * @param methodIdent
	 *            The id of the method sensor to retrieve the measurement.
	 * @param prefix
	 *            An arbitrary prefix {@link String}.
	 * @return Returns a {@link MethodSensorData}.
	 */
	MethodSensorData getMethodSensorData(long sensorTypeIdent, long methodIdent, String prefix);

	/**
	 * Returns a saved data object for further processing.
	 * 
	 * @param sensorTypeIdent
	 *            The id of the sensor type to retrieve the data object.
	 * @return Returns a {@link SystemSensorData}
	 */
	SystemSensorData getPlatformSensorData(long sensorTypeIdent);

	/**
	 * Returns a saved data object for further processing.
	 * 
	 * @param sensorTypeIdent
	 *            The id of the sensor type to retrieve the data object.
	 * @param throwableIdentityHashCode
	 *            The identityHashCode of the data object to retrieve.
	 * @return Returns a {@link ExceptionSensorData}
	 */
	ExceptionSensorData getExceptionSensorData(long sensorTypeIdent, long throwableIdentityHashCode);

	/**
	 * Returns a saved object storage for further processing.
	 * 
	 * @param sensorTypeIdent
	 *            The id of the sensor type to retrieve the measurement.
	 * @param methodIdent
	 *            The id of the method sensor to retrieve the measurement.
	 * @param prefix
	 *            An arbitrary prefix {@link String}.
	 * @return Returns an {@link IObjectStorage}.
	 */
	IObjectStorage getObjectStorage(long sensorTypeIdent, long methodIdent, String prefix);

	/**
	 * Adds a new list listener.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	void addListListener(ListListener<?> listener);

	/**
	 * Removes a list listener.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	void removeListListener(ListListener<?> listener);

}
