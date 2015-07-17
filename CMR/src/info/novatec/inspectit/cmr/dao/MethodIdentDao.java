package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This DAO is used to handle all {@link MethodIdent} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface MethodIdentDao {

	/**
	 * Load a specific {@link MethodIdent} from the underlying storage by passing the id.
	 * 
	 * @param id
	 *            The id of the object.
	 * @return The found {@link MethodIdent} object.
	 */
	MethodIdent load(Long id);

	/**
	 * Execute a findByExample query against the underlying storage.
	 * 
	 * @param methodIdent
	 *            The {@link MethodIdent} object which serves as the example.
	 * @return The list of {@link MethodIdent} objects which have the same contents as the passed
	 *         example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<MethodIdent> findByExample(MethodIdent methodIdent);

	/**
	 * Saves or updates this {@link MethodIdent} in the underlying storage.
	 * 
	 * @param methodIdent
	 *            The {@link MethodIdent} object to save or update.
	 */
	void saveOrUpdate(MethodIdent methodIdent);

	/**
	 * Deletes this specific {@link MethodIdent} object.
	 * 
	 * @param methodIdent
	 *            The {@link MethodIdent} object to delete.
	 */
	void delete(MethodIdent methodIdent);

	/**
	 * Deletes all {@link MethodIdent} objects which are stored in the passed list.
	 * 
	 * @param methodIdents
	 *            The list containing the {@link MethodIdent} objects to delete.
	 */
	void deleteAll(List<MethodIdent> methodIdents);

	/**
	 * This method returns a list containing {@link MethodIdent} objects which have an association
	 * to the given {@link PlatformIdent} object.
	 * 
	 * @param platformId
	 *            The id of the platform.
	 * @param methodIdentExample
	 *            The {@link MethodIdent} example object to look for similar object(s).
	 * @return A list containing the {@link MethodIdent} objects which are already in an association
	 *         with the passed {@link PlatformIdent} object and have identical fields like the
	 *         example object.
	 */
	List<MethodIdent> findForPlatformIdent(long platformId, MethodIdent methodIdentExample);

	/**
	 * Returns all {@link MethodIdent} objects which are saved in the underlying storage.
	 * 
	 * @return Returns all stored {@link MethodIdent} objects.
	 */
	List<MethodIdent> findAll();

}
