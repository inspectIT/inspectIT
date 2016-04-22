package rocks.inspectit.server.dao;

import rocks.inspectit.shared.all.cmr.model.MethodIdentToSensorType;

/**
 * This DAO is used to handle all {@link MethodIdentToSensorType} objects.
 *
 * @author Ivan Senic
 *
 */
public interface MethodIdentToSensorTypeDao {

	/**
	 * Load a specific {@link MethodIdentToSensorType} from the underlying storage by passing the
	 * id.
	 *
	 * @param id
	 *            The id of the object.
	 * @return The found {@link MethodIdentToSensorType} object.
	 */
	MethodIdentToSensorType load(Long id);

	/**
	 * Find the {@link MethodIdentToSensorType} for given method id and method sensor type id.
	 *
	 * @param methodId
	 *            Id of the method ident.
	 * @param methodSensorTypeId
	 *            Id of the method sensor type ident.
	 * @return Returns {@link MethodIdentToSensorType} object or <code>null</code> if the one does
	 *         not exists for the given method id and methos sensor type id combination.
	 */
	MethodIdentToSensorType find(long methodId, long methodSensorTypeId);

	/**
	 * Saves or updates this {@link MethodIdentToSensorType} in the underlying storage.
	 *
	 * @param methodIdentToSensorType
	 *            The {@link MethodIdentToSensorType} object to save or update.
	 */
	void saveOrUpdate(MethodIdentToSensorType methodIdentToSensorType);
}
