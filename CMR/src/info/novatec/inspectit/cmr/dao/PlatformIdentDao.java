package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.PlatformIdent;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * This DAO is used to handle all {@link PlatformIdent} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface PlatformIdentDao {

	/**
	 * Load a specific {@link PlatformIdent} from the underlying storage by passing the id.
	 * 
	 * @param id
	 *            The id of the object.
	 * @return The found {@link PlatformIdent} object.
	 */
	PlatformIdent load(Long id);

	/**
	 * Execute a findByExample query against the underlying storage.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object which serves as the example.
	 * @return The list of {@link PlatformIdent} objects which have the same contents as the passed
	 *         example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<PlatformIdent> findByExample(PlatformIdent platformIdent);

	/**
	 * Saves or updates this {@link PlatformIdent} in the underlying storage.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object to save or update.
	 */
	void saveOrUpdate(PlatformIdent platformIdent);

	/**
	 * Deletes this specific {@link PlatformIdent} object.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object to delete.
	 */
	void delete(PlatformIdent platformIdent);

	/**
	 * Deletes all {@link PlatformIdent} objects which are stored in the passed list.
	 * 
	 * @param platformIdents
	 *            The list containing the {@link PlatformIdent} objects to delete.
	 */
	void deleteAll(List<PlatformIdent> platformIdents);

	/**
	 * Returns all {@link PlatformIdent} objects which are saved in the underlying storage.
	 * <p>
	 * Object will be sorted by agent name.
	 * 
	 * @return Returns all stored {@link PlatformIdent} objects.
	 */
	List<PlatformIdent> findAll();

	/**
	 * Evicts the passed {@link PlatformIdent} object from the session.
	 * 
	 * @param platformIdent
	 *            The {@link PlatformIdent} object to evict from the session.
	 */
	void evict(PlatformIdent platformIdent);

	/**
	 * Evicts all {@link PlatformIdent} objects from the session.
	 * 
	 * @param platformIdents
	 *            The list of {@link PlatformIdent} objects to evict from the session.
	 */
	void evictAll(List<PlatformIdent> platformIdents);

	/**
	 * Executes the same query as {@link #findAll()} but initialized the lazy collections
	 * afterwards. Only for one agent.
	 * 
	 * @param id
	 *            Id of wanted agent.
	 * 
	 * @return Returns one {@link PlatformIdent} object and initializes the collections.
	 */
	PlatformIdent findInitialized(long id);

}
