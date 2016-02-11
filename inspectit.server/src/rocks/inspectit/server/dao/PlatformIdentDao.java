package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.PlatformIdent;

import java.util.List;

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
	 * Executes the same query as {@link #findAll()} but initialized the lazy collections
	 * afterwards. Only for one agent.
	 * 
	 * @param id
	 *            Id of wanted agent.
	 * 
	 * @return Returns one {@link PlatformIdent} object and initializes the collections.
	 */
	PlatformIdent findInitialized(long id);

	/**
	 * Finds agent(s) that are registered with given agent name. Same as calling
	 * {@link #findByNameAndIps(String, null)}.
	 * 
	 * @param agentName
	 *            Name of the agent to search for.
	 * @return The list of {@link PlatformIdent} objects.
	 */
	List<PlatformIdent> findByName(String agentName);

	/**
	 * Finds agent(s) that are registered with given agent name and IP addresses. If passed
	 * addresses are <code>null</code>, they will not be taken into consideration.
	 * 
	 * @param agentName
	 *            Name of the agent to search for.
	 * @param definedIps
	 *            List of defined IP addresses.
	 * @return The list of {@link PlatformIdent} objects.
	 */
	List<PlatformIdent> findByNameAndIps(String agentName, List<String> definedIps);

}
