package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;

import java.util.List;

/**
 * This DAO is used to handle all {@link JmxDefinitionDataIdent} objects.
 * 
 * @author Alfred Krauss
 * 
 */
public interface JmxDefinitionDataIdentDao {

	/**
	 * Load a specific {@link JmxDefinitionDataIdent} from the underlying storage by passing the id.
	 * 
	 * @param id
	 *            The id of the object.
	 * @return The found {@link JmxDefinitionDataIdent} object.
	 */
	JmxDefinitionDataIdent load(Long id);

	/**
	 * Saves or updates this {@link JmxDefinitionDataIdent} in the underlying storage.
	 * 
	 * @param jmxDefinitionDataIdent
	 *            The {@link JmxDefinitionDataIdent} object to save or update.
	 */
	void saveOrUpdate(JmxDefinitionDataIdent jmxDefinitionDataIdent);

	/**
	 * This method returns a list containing {@link JmxDefinitionDataIdent} objects which have an
	 * association to the given {@link PlatformIdent} object.
	 * 
	 * @param platformId
	 *            The id of the platform.
	 * @param jmxDefinitionDataIdentExample
	 *            The {@link JmxDefinitionDataIdent} example object to look for similar object(s).
	 * @return A list containing the {@link JmxDefinitionDataIdent} objects which are already in an
	 *         association with the passed {@link PlatformIdent} object and have identical fields
	 *         like the example object.
	 */
	List<JmxDefinitionDataIdent> findForPlatformIdent(long platformId, JmxDefinitionDataIdent jmxDefinitionDataIdentExample);
}
