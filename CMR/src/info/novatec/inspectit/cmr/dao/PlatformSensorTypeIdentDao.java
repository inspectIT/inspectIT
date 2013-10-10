package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;

import java.util.List;

/**
 * This DAO is used to handle all {@link PlatformSensorTypeIdent} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface PlatformSensorTypeIdentDao {

	/**
	 * Load a specific {@link PlatformSensorTypeIdent} from the underlying storage by passing the
	 * id.
	 * 
	 * @param id
	 *            The id of the object.
	 * @return The found {@link PlatformSensorTypeIdent} object.
	 */
	PlatformSensorTypeIdent load(Long id);

	/**
	 * Saves or updates this {@link PlatformSensorTypeIdent} in the underlying storage.
	 * 
	 * @param platformSensorTypeIdent
	 *            The {@link PlatformSensorTypeIdent} object to save or update.
	 */
	void saveOrUpdate(PlatformSensorTypeIdent platformSensorTypeIdent);

	/**
	 * Deletes this specific {@link PlatformSensorTypeIdent} object.
	 * 
	 * @param platformSensorTypeIdent
	 *            The {@link PlatformSensorTypeIdent} object to delete.
	 */
	void delete(PlatformSensorTypeIdent platformSensorTypeIdent);

	/**
	 * Deletes all {@link PlatformSensorTypeIdent} objects which are stored in the passed list.
	 * 
	 * @param platformSensorTypeIdents
	 *            The list containing the {@link PlatformSensorTypeIdent} objects to delete.
	 */
	void deleteAll(List<PlatformSensorTypeIdent> platformSensorTypeIdents);

	/**
	 * Returns all {@link PlatformSensorTypeIdent} objects which are saved in the underlying
	 * storage.
	 * 
	 * @return Returns all stored {@link PlatformSensorTypeIdent} objects.
	 */
	List<PlatformSensorTypeIdent> findAll();

	/**
	 * Find the {@link PlatformSensorTypeIdent} with given fully qualified sensor class name and
	 * platform ident.
	 * 
	 * @param fullyQualifiedClassName
	 *            FQN of sensor
	 * @param platformId
	 *            Platform ident id.
	 * @return List of existing objects.
	 */
	List<PlatformSensorTypeIdent> findByClassNameAndPlatformId(String fullyQualifiedClassName, long platformId);

}
