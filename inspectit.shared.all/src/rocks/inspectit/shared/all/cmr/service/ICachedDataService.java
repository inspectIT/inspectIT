package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;

/**
 * Interface for the cached data service. Provides platform, sensor and method ident from the cache.
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
}