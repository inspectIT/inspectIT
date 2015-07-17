package info.novatec.inspectit.cmr.property.update;

/**
 * Interface for property update.
 * 
 * @author Ivan Senic
 * 
 * @param <V>
 *            Type of the property value.
 */
public interface IPropertyUpdate<V> {

	/**
	 * If this update is restore default update.
	 * 
	 * @return If this update is restore default update.
	 */
	boolean isRestoreDefault();

	/**
	 * Gets the update value.
	 * 
	 * @return Gets the update value.
	 */
	V getUpdateValue();

	/**
	 * Gets logical name of the updated property.
	 * 
	 * @return Gets logical name of the updated property.
	 */
	String getPropertyLogicalName();
}
