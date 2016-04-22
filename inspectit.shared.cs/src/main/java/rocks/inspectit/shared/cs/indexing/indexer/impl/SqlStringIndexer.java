package rocks.inspectit.shared.cs.indexing.indexer.impl;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.IBranchIndexer;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

/**
 * Indexer that indexes SQLs based on the query string. All other objects types are indexed with
 * same key.
 *
 * @author Ivan Senic
 *
 * @param <E>
 */
public class SqlStringIndexer<E extends DefaultData> extends AbstractSharedInstanceBranchIndexer<E> implements IBranchIndexer<E> {

	/**
	 * Maximum amount of branches/leaf that can be created by this indexer. Negative values means no
	 * limit.
	 */
	private final int maxKeys;

	/**
	 * Default constructor. Adds no limit on the maximum amount of keys created.
	 */
	public SqlStringIndexer() {
		this(-1);
	}

	/**
	 * Additional constructor. Sets the amount of maximum keys that will be created. If unlimited
	 * keys should be used, construct object with no-arg constructor or pass the non-positive
	 * number.
	 *
	 * @param maxKeys
	 *            Max keys that can be created by this indexer.
	 */
	public SqlStringIndexer(int maxKeys) {
		this.maxKeys = maxKeys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getKey(E element) {
		if (element instanceof SqlStatementData) {
			SqlStatementData sqlStatementData = (SqlStatementData) element;
			if (null != sqlStatementData.getSql()) {
				return getInternalHash(sqlStatementData.getSql().hashCode());
			}
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getKeys(IIndexQuery query) {
		if (query instanceof StorageIndexQuery) {
			if (null != ((StorageIndexQuery) query).getSql()) {
				Object[] keys = new Object[1];
				keys[0] = getInternalHash(((StorageIndexQuery) query).getSql().hashCode());
				return keys;
			}
		}
		return new Object[0];
	}

	/**
	 * Internal hash function depending on the {@link #maxKeys}.
	 *
	 * @param hashCode
	 *            Hash code to transform.
	 * @return Key that can be used.
	 */
	private Integer getInternalHash(int hashCode) {
		if (maxKeys > 0) {
			return Integer.valueOf(Math.abs(hashCode % maxKeys));
		} else {
			return hashCode;
		}
	}

}
