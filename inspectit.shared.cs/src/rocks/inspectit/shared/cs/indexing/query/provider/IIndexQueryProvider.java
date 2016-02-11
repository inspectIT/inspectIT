package rocks.inspectit.shared.cs.indexing.query.provider;

import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.impl.IndexQuery;

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
