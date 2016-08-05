package rocks.inspectit.server.dao;

import java.util.List;

import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

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
	 * This method returns a list containing {@link JmxDefinitionDataIdent} IDs which have an
	 * association to the given {@link PlatformIdent} object.
	 *
	 * @param platformId
	 *            The id of the platform.
	 * @param jmxDefinitionDataIdentExample
	 *            The {@link JmxDefinitionDataIdent} example object to look for similar object(s).
	 * @param updateTimestamp
	 *            As this method is used by the registration service there is an optional flag to
	 *            automatically update the time-stamp of the found {@link JmxDefinitionDataIdent} to
	 *            current time.
	 * @return A list containing the {@link JmxDefinitionDataIdent} objects IDs which are already in
	 *         an association with the passed {@link PlatformIdent} object and have identical fields
	 *         like the example object.
	 */
	List<Long> findIdForPlatformIdent(long platformId, JmxDefinitionDataIdent jmxDefinitionDataIdentExample, boolean updateTimestamp);
}
