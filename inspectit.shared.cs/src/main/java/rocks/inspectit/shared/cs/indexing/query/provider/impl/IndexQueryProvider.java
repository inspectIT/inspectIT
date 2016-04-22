package rocks.inspectit.shared.cs.indexing.query.provider.impl;

import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.impl.IndexQuery;
import rocks.inspectit.shared.cs.indexing.query.provider.IIndexQueryProvider;

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
	@Override
	public IndexQuery getIndexQuery() {
		return createNewIndexQuery();
	}
}
