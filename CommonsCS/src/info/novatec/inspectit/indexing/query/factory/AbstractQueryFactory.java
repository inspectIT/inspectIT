package info.novatec.inspectit.indexing.query.factory;

import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.provider.IIndexQueryProvider;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract query factory, has only the instance to the {@link IIndexQueryProvider}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public abstract class AbstractQueryFactory<E extends IIndexQuery> {

	/**
	 * {@link IIndexQueryProvider}.
	 */
	@Autowired
	private IIndexQueryProvider<E> indexQueryProvider;

	/**
	 * @return the indexQueryProvider
	 */
	public IIndexQueryProvider<E> getIndexQueryProvider() {
		return indexQueryProvider;
	}

	/**
	 * @param indexQueryProvider
	 *            the indexQueryProvider to set
	 */
	public void setIndexQueryProvider(IIndexQueryProvider<E> indexQueryProvider) {
		this.indexQueryProvider = indexQueryProvider;
	}

}
