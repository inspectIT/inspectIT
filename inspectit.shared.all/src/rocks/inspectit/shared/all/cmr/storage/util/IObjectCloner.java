package info.novatec.inspectit.cmr.storage.util;

/**
 * Interface for the object cloning utility.
 * 
 * @author Ivan Senic
 * 
 */
public interface IObjectCloner {

	/**
	 * Clones the the given object and removes the persistent Hibernate collections.
	 * 
	 * @param object
	 *            Object to clone.
	 * @param <E>
	 *            Type of the object.
	 * @return Clone.
	 */
	<E> E clone(E object);

}