package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;

import java.util.List;

/**
 * This DAO is used to handle all {@link MethodSensorTypeIdent} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface MethodSensorTypeIdentDao {

	/**
	 * Load a specific {@link MethodSensorTypeIdent} from the underlying storage by passing the id.
	 * 
	 * @param id
	 *            The id of the object.
	 * @return The found {@link MethodSensorTypeIdent} object.
	 */
	MethodSensorTypeIdent load(Long id);

	/**
	 * Saves or updates this {@link MethodSensorTypeIdent} in the underlying storage.
	 * 
	 * @param methodSensorTypeIdent
	 *            The {@link MethodSensorTypeIdent} object to save or update.
	 */
	void saveOrUpdate(MethodSensorTypeIdent methodSensorTypeIdent);

	/**
	 * Deletes this specific {@link MethodSensorTypeIdent} object.
	 * 
	 * @param methodSensorTypeIdent
	 *            The {@link MethodSensorTypeIdent} object to delete.
	 */
	void delete(MethodSensorTypeIdent methodSensorTypeIdent);

	/**
	 * Deletes all {@link MethodSensorTypeIdent} objects which are stored in the passed list.
	 * 
	 * @param methodSensorTypeIdents
	 *            The list containing the {@link MethodSensorTypeIdent} objects to delete.
	 */
	void deleteAll(List<MethodSensorTypeIdent> methodSensorTypeIdents);

	/**
	 * Returns all {@link MethodSensorTypeIdent} objects which are saved in the underlying storage.
	 * 
	 * @return Returns all stored {@link MethodSensorTypeIdent} objects.
	 */
	List<MethodSensorTypeIdent> findAll();

	/**
	 * Find the {@link MethodSensorTypeIdent} with given fully qualified sensor class name and
	 * platform ident.
	 * 
	 * @param fullyQualifiedClassName
	 *            FQN of sensor
	 * @param platformId
	 *            Platform ident id.
	 * @return List of existing objects.
	 */
	List<MethodSensorTypeIdent> findByClassNameAndPlatformId(String fullyQualifiedClassName, long platformId);

}
