package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

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
