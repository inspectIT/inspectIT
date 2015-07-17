package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.hooking.IConstructorHook;

/**
 * This hook records the meta data information of a connection and stores this into a map for later
 * retrieval.
 * 
 * @author Stefan Siegl
 * 
 */
public class ConnectionMetaDataHook implements IConstructorHook {

	/**
	 * Storage for connection meta data.
	 */
	private final ConnectionMetaDataStorage connectionMetaDataStorage;

	/**
	 * The only constructor which needs the {@link ConnectionMetaDataStorage}.
	 * 
	 * @param connectionMetaDataStorage
	 *            the connection storage.
	 */
	public ConnectionMetaDataHook(ConnectionMetaDataStorage connectionMetaDataStorage) {
		this.connectionMetaDataStorage = connectionMetaDataStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		connectionMetaDataStorage.add(object);
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc) {
		// nothing
	}
}