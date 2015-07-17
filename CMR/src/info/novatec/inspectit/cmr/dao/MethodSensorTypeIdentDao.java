package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

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
	 * Execute a findByExample query against the underlying storage.
	 * 
	 * @param platformId
	 *            Platform ID sensor should belong to.
	 * @param methodSensorTypeIdent
	 *            The {@link MethodSensorTypeIdent} object which serves as the example.
	 * @return The list of {@link MethodSensorTypeIdent} objects which have the same contents as the
	 *         passed example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<MethodSensorTypeIdent> findByExample(long platformId, MethodSensorTypeIdent methodSensorTypeIdent);

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

}
