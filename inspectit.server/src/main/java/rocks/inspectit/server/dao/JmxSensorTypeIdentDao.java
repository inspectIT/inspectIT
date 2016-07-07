package rocks.inspectit.server.dao;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

import rocks.inspectit.shared.all.cmr.model.JmxSensorTypeIdent;

/**
 * This DAO is used to handle all {@link JmxSensorTypeIdent} objects.
 *
 * @author Alfred Krauss
 *
 */
public interface JmxSensorTypeIdentDao {

	/**
	 * Load a specific {@link JmxSensorTypeIdent} from the underlying storage by passing the id.
	 *
	 * @param id
	 *            The id of the object.
	 * @return The found {@link JmxSensorTypeIdent} object.
	 */
	JmxSensorTypeIdent load(Long id);

	/**
	 * Saves or updates this {@link JmxSensorTypeIdent} in the underlying storage.
	 *
	 * @param jmxSensorTypeIdent
	 *            The {@link JmxSensorTypeIdent} object to save or update.
	 */
	void saveOrUpdate(JmxSensorTypeIdent jmxSensorTypeIdent);

	/**
	 * Execute a findByExample query against the underlying storage and returns the ID of the found
	 * element.
	 *
	 * @param platformId
	 *            Platform ID sensor should belong to.
	 * @param jmxSensorTypeIdent
	 *            The {@link JmxSensorTypeIdent} object which serves as the example.
	 * @return The list of {@link JmxSensorTypeIdent} objects IDs which have the same contents as
	 *         the passed example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<Long> findIdByExample(long platformId, JmxSensorTypeIdent jmxSensorTypeIdent);
}
