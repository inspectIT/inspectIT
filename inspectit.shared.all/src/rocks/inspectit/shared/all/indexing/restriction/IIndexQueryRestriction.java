package info.novatec.inspectit.indexing.restriction;

import info.novatec.inspectit.indexing.IIndexQuery;

import java.util.List;

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
	 * Returns the name of the getter methods that needs to be invoked in order to get the object to
	 * check the restriction on. The first method is invoked on the object in the index, the second
	 * is invoked on the result of the first invocation, and so on.
	 * <p>
	 * The method names are complied with Java Bean naming standard, meaning that if the field name
	 * is foo, the name of the getter method is getFoo.
	 * 
	 * @return Getter methods that needs to be invoked in order to get the object to check the
	 *         restriction on
	 */
	List<String> getQualifiedMethodNames();

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
