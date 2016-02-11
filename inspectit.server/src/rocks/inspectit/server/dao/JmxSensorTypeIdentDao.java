package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.cmr.model.JmxSensorTypeIdent;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;

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
	 * Execute a findByExample query against the underlying storage.
	 *
	 * @param platformId
	 *            Platform ID sensor should belong to.
	 * @param jmxSensorTypeIdent
	 *            The {@link JmxSensorTypeIdent} object which serves as the example.
	 * @return The list of {@link JmxSensorTypeIdent} objects which have the same contents as the
	 *         passed example object.
	 * @see HibernateTemplate#findByExample(Object)
	 */
	List<JmxSensorTypeIdent> findByExample(long platformId, JmxSensorTypeIdent jmxSensorTypeIdent);

	/**
	 * Saves or updates this {@link JmxSensorTypeIdent} in the underlying storage.
	 *
	 * @param jmxSensorTypeIdent
	 *            The {@link JmxSensorTypeIdent} object to save or update.
	 */
	void saveOrUpdate(JmxSensorTypeIdent jmxSensorTypeIdent);
}
