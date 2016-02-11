package info.novatec.inspectit.indexing.buffer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferBranchIndexer;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Implementation of branch indexer for the {@link IBufferTreeComponent}. This indexer is delegating
 * generation of the indexing keys to the {@link IBranchIndexer}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of the elements indexed.
 */
public class BufferBranchIndexer<E extends DefaultData> implements IBufferBranchIndexer<E> {

	/**
	 * Delegate indexer.
	 */
	private IBranchIndexer<E> delegateIndexer;

	/**
	 * Child indexer.
	 */
	private BufferBranchIndexer<E> childBufferIndexer;

	/**
	 * Default constructor.
	 * 
	 * @param delegateIndexer
	 *            Delegate indexer that should generate keys.
	 */
	public BufferBranchIndexer(IBranchIndexer<E> delegateIndexer) {
		this(delegateIndexer, null);
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param delegateIndexer
	 *            Type of the delegate indexer that will actually generate keys for objects.
	 * @param childBufferIndexer
	 *            Indexer to be used in the child branch.
	 */
	public BufferBranchIndexer(IBranchIndexer<E> delegateIndexer, BufferBranchIndexer<E> childBufferIndexer) {
		this.delegateIndexer = delegateIndexer;
		this.childBufferIndexer = childBufferIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		return delegateIndexer.getKey(element);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		return delegateIndexer.getKeys(query);
	}

	/**
	 * {@inheritDoc}
	 */
	public IBufferBranchIndexer<E> getChildIndexer() {
		return childBufferIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean sharedInstance() {
		return delegateIndexer.sharedInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBufferBranchIndexer<E> getNewInstance() {
		if (!sharedInstance()) {
			BufferBranchIndexer<E> bufferBranchIndexer = new BufferBranchIndexer<E>(delegateIndexer.getNewInstance(), childBufferIndexer);
			return bufferBranchIndexer;
		} else {
			throw new UnsupportedOperationException("Method getNewInstance() called on the Indexer that has a shared instance.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IBufferTreeComponent<E> getNextTreeComponent() {
		if (null != childBufferIndexer) {
			if (childBufferIndexer.sharedInstance()) {
				return new Branch<E>(childBufferIndexer);
			} else {
				return new Branch<E>(childBufferIndexer.getNewInstance());
			}
		} else {
			return new Leaf<E>();
		}
	}

	/**
	 * Gets {@link #delegateIndexer}.
	 * 
	 * @return {@link #delegateIndexer}
	 */
	IBranchIndexer<E> getDelegateIndexer() {
		return delegateIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("delegateIndexer", delegateIndexer);
		return toStringBuilder.toString();
	}

}
