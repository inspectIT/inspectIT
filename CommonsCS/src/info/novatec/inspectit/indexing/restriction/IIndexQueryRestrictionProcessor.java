package info.novatec.inspectit.indexing.restriction;

import java.util.List;

/**
 * The index query restriction processor is responsible for checking if the all restrictions
 * belonging to one index query are fulfilled for different objects that are queried.
 * 
 * @author Ivan Senic
 * 
 */
public interface IIndexQueryRestrictionProcessor {

	/**
	 * If all given restrictions are fulfilled for supplied object.
	 * 
	 * @param object
	 *            Object that restrictions should be checked against.
	 * @param restrictions
	 *            List of restrictions.
	 * @return True if all restrictions are fulfilled, otherwise false.
	 */
	boolean areAllRestrictionsFulfilled(Object object, List<IIndexQueryRestriction> restrictions);

}
