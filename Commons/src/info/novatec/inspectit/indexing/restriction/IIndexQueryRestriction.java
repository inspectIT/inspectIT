package info.novatec.inspectit.indexing.restriction;

import info.novatec.inspectit.indexing.IIndexQuery;

/**
 * Interface that defines the restriction behavior for indexing query when searching for objects in
 * the indexing tree. Restrictions are part of the {@link IIndexQuery} and allow additional
 * specification of search properties, than the ones that are defined directly in the
 * {@link IIndexQuery} don not provide.
 * 
 * @author Ivan Senic
 * 
 */
public interface IIndexQueryRestriction {

	/**
	 * Returns the name of the object's field that is aimed with this restriction.
	 * 
	 * @return Object's field name that restriction is bounded to.
	 */
	String getFieldName();

	/**
	 * Returns the name of the getter method that returns the field indexing restriction is aiming.
	 * The method name is complied with Java Bean naming standard, meaning that if the field name is
	 * foo, the name of the getter method is getFoo.
	 * 
	 * @return Getter method name for object's field that restriction is bounded to.
	 */
	String getQualifiedMethodName();

	/**
	 * Checks if the restriction is fulfilled with given object value.
	 * 
	 * @param fieldValue
	 *            The field value of checked object defined with {@link #getFieldName()}
	 * 
	 * @return True if restriction is fulfilled, false otherwise.
	 */
	boolean isFulfilled(Object fieldValue);
}
