package info.novatec.inspectit.indexing.query.provider.impl;

import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.impl.IndexQuery;
import info.novatec.inspectit.indexing.query.provider.IIndexQueryProvider;

/**
 * Class that is used for providing the correct instance of {@link IIndexQuery} via Spring
 * framework.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class IndexQueryProvider implements IIndexQueryProvider<IndexQuery> {

	/**
	 * 
	 * @return Returns the correctly instated instance of {@link IIndexQuery} that can be used in
	 *         for querying the indexing tree.
	 */
	public abstract IndexQuery createNewIndexQuery();

	/**
	 * {@inheritDoc}
	 */
	public IndexQuery getIndexQuery() {
		return createNewIndexQuery();
	}
}
