package info.novatec.inspectit.indexing.query.provider;

import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.impl.IndexQuery;

/**
 * Common interface for all query providers.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of query it provides.
 */
public interface IIndexQueryProvider<E extends IIndexQuery> {

	/**
	 * @return Returns the {@link IndexQuery} that can be used for querying.
	 */
	E getIndexQuery();

}
