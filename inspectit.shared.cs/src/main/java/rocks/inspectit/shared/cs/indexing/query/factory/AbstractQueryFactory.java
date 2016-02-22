package rocks.inspectit.shared.cs.indexing.query.factory;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.query.provider.IIndexQueryProvider;

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
