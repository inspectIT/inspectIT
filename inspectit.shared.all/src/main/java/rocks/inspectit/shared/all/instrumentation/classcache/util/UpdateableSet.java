package rocks.inspectit.shared.all.instrumentation.classcache.util;

import java.util.Set;

/**
 * Set that has a {@link #addOrUpdate(Object)} method. This is a special type of the add method that
 * if the element e2 exists in the set and element e is equal to it, the replace will occur if these
 * are not the same objects in terms of reference address. This can be useful when the objects are
 * equal in terms of equalTo, but not with == terms.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of element in the set.
 */
public interface UpdateableSet<E> extends Set<E> {

	/**
	 * Adds the element to the set if it's not existing. if the element e2 exists in the set and
	 * element e is equal to it, the replace will occur if these are not the same objects in terms
	 * of reference address. This can be useful when the objects are equal in terms of equalTo, but
	 * not with == terms.
	 * 
	 * @param e
	 *            element
	 */
	void addOrUpdate(E e);

}
