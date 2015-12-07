package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessTransactionDefinition;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;

/**
 * Interface for the cached data service. Provides platform, sensor, method ident and business
 * context from the cache.
 * 
 * @author Ivan Senic
 * 
 */
public interface ICachedDataService {

	/**
	 * Returns the mapped {@link PlatformIdent} object for the passed platform id.
	 * 
	 * @param platformId
	 *            The long value.
	 * @return The {@link PlatformIdent} object.
	 */
	PlatformIdent getPlatformIdentForId(long platformId);

	/**
	 * Returns the mapped {@link SensorTypeIdent} object for the passed sensor type id.
	 * 
	 * @param sensorTypeId
	 *            The long value.
	 * @return The {@link SensorTypeIdent} object.
	 */
	SensorTypeIdent getSensorTypeIdentForId(long sensorTypeId);

	/**
	 * Returns the mapped {@link MethodIdent} object for the passed method id.
	 * 
	 * @param methodId
	 *            The long value.
	 * @return The {@link MethodIdent} object.
	 */
	MethodIdent getMethodIdentForId(long methodId);

	/**
	 * Returns the mapped {@link JmxDefinitionDataIdent} object for the passed jmDefinitionData id.
	 * 
	 * @param jmxDefinitionDataId
	 *            The long value.
	 * @return The {@link JmxDefinitionDataIdent} object.
	 */
	JmxDefinitionDataIdent getJmxDefinitionDataIdentForId(long jmxDefinitionDataId);
	
	/**
	 * Retrieves the {@link IApplicationDefinition} for the given identifier.
	 * 
	 * @param id
	 *            unique identifier of the application definition
	 * @return Returns the application definition for the given id or null if no applicaiton
	 *         definition for the id exists.
	 */
	IApplicationDefinition getApplicationDefinitionForId(long id);

	/**
	 * Retrieves the {@link IBusinessTransactionDefinition} for the given application and business
	 * transaction identifiers.
	 * 
	 * @param appId
	 *            unique identifier of the application definition
	 * @param businessTxId
	 *            unique identifier of the business transaction definition
	 * @return Returns the business transaction definition or null if no business transaction
	 *         definition for the given pair of identifiers exists.
	 */
	IBusinessTransactionDefinition getBusinessTransactionDefinitionForId(long appId, long businessTxId);

	/**
	 * Invalidates the cache for the business context.
	 */
	void invalidateBusinessContextCache();
}