package rocks.inspectit.agent.java.core;

import java.util.concurrent.ScheduledExecutorService;

import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;

/**
 * Interface definition for the core service. The core service is the central point of the Agent
 * where all data is collected, triggered etc.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Alfred Krauss
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
	 * Adds a new data object from the jmx sensor to the value storage.
	 * 
	 * @param sensorTypeIdent
	 *            The id of the sensor type.
	 * @param objectName
	 *            The name of the mBean
	 * @param attributeName
	 * 			  The attributeName of the Attribute.
	 * @param jmxSensorValueData
	 *            Part of the jmx sensor data.
	 */
	void addJmxSensorValueData(long sensorTypeIdent, String objectName, String attributeName, JmxSensorValueData jmxSensorValueData);

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

	/**
	 * Returns the scheduled executor-service.
	 * 
	 * @return a {@link ScheduledExecutorService}
	 */
	ScheduledExecutorService getExecutorService();
}
