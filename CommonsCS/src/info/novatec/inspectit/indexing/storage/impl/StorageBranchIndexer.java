package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;
import info.novatec.inspectit.indexing.storage.IStorageBranchIndexer;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.storage.util.StorageUtil;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Implementation of the indexer for the {@link IStorageTreeComponent}. This indexer delegate the
 * key creation to the {@link IBranchIndexer}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of element indexed by indexer.
 */
public class StorageBranchIndexer<E extends DefaultData> implements IStorageBranchIndexer<E> {

	/**
	 * Id that will be passed to the child indexers/leafs if {@link #passId} is true.
	 */
	private int id;

	/**
	 * The delegate indexer for keys creation.
	 */
	private IBranchIndexer<E> delegateIndexer;

	/**
	 * The indexer that is next in the hierarchy.
	 */
	private StorageBranchIndexer<E> childIndexer;

	/**
	 * If pass ID mode is on. With this mode, all sub-components created with this indexer will have
	 * same ID, thus write will be done in same file.
	 */
	private boolean passId;

	/**
	 * No-args constructor.
	 */
	public StorageBranchIndexer() {
	}

	/**
	 * This constructor will generate unique ID and will not set child indexer.
	 * 
	 * @param delegateIndexer
	 *            Provides delegate indexer with a constructor.
	 * @param passId
	 *            If pass ID mode is on. With this mode, all sub-components created with this
	 *            indexer will have same ID, thus write will be done in same file.
	 */
	public StorageBranchIndexer(IBranchIndexer<E> delegateIndexer, boolean passId) {
		this(delegateIndexer, null, StorageUtil.getRandomInt(), passId);
	}

	/**
	 * This constructor allows setting of all properties except ID that will be uniquely generated.
	 * 
	 * @param delegateIndexer
	 *            Provides delegate indexer with a constructor.
	 * @param childIndexer
	 *            Provides child indexer.
	 * @param passId
	 *            If pass ID mode is on. With this mode, all sub-components created with this
	 *            indexer will have same ID, thus write will be done in same file.
	 */
	public StorageBranchIndexer(IBranchIndexer<E> delegateIndexer, StorageBranchIndexer<E> childIndexer, boolean passId) {
		this(delegateIndexer, childIndexer, StorageUtil.getRandomInt(), passId);
	}

	/**
	 * This constructor allows setting of all properties.
	 * 
	 * @param delegateIndexer
	 *            Provides delegate indexer with a constructor.
	 * @param childIndexer
	 *            Provides child indexer.
	 * @param id
	 *            Id given to this indexer.
	 * @param sharedId
	 *            If shared ID mode is on. With this mode, all components created with this indexer
	 *            will have same ID, thus write will be done in same file.
	 */
	public StorageBranchIndexer(IBranchIndexer<E> delegateIndexer, StorageBranchIndexer<E> childIndexer, int id, boolean sharedId) {
		this.delegateIndexer = delegateIndexer;
		this.childIndexer = childIndexer;
		this.id = id;
		this.passId = sharedId;
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
	public boolean sharedInstance() {
		return delegateIndexer.sharedInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageBranchIndexer<E> getNewInstance() {
		IBranchIndexer<E> branchIndexer = null;
		if (sharedInstance()) {
			branchIndexer = delegateIndexer;
		} else {
			branchIndexer = delegateIndexer.getNewInstance();
		}

		StorageBranchIndexer<E> storageBranchIndexer = new StorageBranchIndexer<E>(branchIndexer, childIndexer, passId);
		return storageBranchIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageTreeComponent<E> getNextTreeComponent(E object) {
		if (null != childIndexer) {
			// if there is child indexer we need to create a branch

			if (!childIndexer.isPassId() && !passId) {
				// if child is not shared and we don't need to pass id
				// just create new branch with child indexer
				return new StorageBranch<E>(childIndexer);
			} else {
				// create new instance of child indexer and pass id if necessary
				IStorageBranchIndexer<E> indexer = childIndexer.getNewInstance();
				if (passId) {
					indexer.setId(id);
				}
				return new StorageBranch<E>(indexer);
			}
		} else {
			// if not we need to create Leaf, and pass id is necessary
			if (object instanceof InvocationSequenceData) {
				// for invocations ArrayBasedStorageLeaf
				if (passId) {
					return new ArrayBasedStorageLeaf<E>(id);
				} else {
					return new ArrayBasedStorageLeaf<E>();
				}
			} else {
				// for everything else LeafWithNoDescriptors
				if (passId) {
					return new LeafWithNoDescriptors<E>(id);
				} else {
					return new LeafWithNoDescriptors<E>();
				}
			}
		}
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets {@link #passId}.
	 * 
	 * @return {@link #passId}
	 */
	public boolean isPassId() {
		return passId;
	}

	/**
	 * Sets {@link #passId}.
	 * 
	 * @param sharedId
	 *            New value for {@link #passId}
	 */
	public void setPassId(boolean sharedId) {
		this.passId = sharedId;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childIndexer == null) ? 0 : childIndexer.hashCode());
		result = prime * result + ((delegateIndexer == null) ? 0 : delegateIndexer.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StorageBranchIndexer<E> other = (StorageBranchIndexer<E>) obj;
		if (childIndexer == null) {
			if (other.childIndexer != null) {
				return false;
			}
		} else if (!childIndexer.equals(other.childIndexer)) {
			return false;
		}
		if (delegateIndexer == null) {
			if (other.delegateIndexer != null) {
				return false;
			}
		} else if (!delegateIndexer.equals(other.delegateIndexer)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("id", id);
		toStringBuilder.append("delegateIndexer", delegateIndexer);
		toStringBuilder.append("childIndexer", childIndexer);
		toStringBuilder.append("passId", passId);
		return toStringBuilder.toString();
	}

}
