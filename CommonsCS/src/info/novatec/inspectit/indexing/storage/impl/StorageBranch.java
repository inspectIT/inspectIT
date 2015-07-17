package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.AbstractBranch;
import info.novatec.inspectit.indexing.ITreeComponent;
import info.novatec.inspectit.indexing.storage.IStorageBranchIndexer;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

/**
 * Storage branch type that implements the {@link IStorageTreeComponent}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class StorageBranch<E extends DefaultData> extends AbstractBranch<IStorageDescriptor, E> implements IStorageTreeComponent<E> {

	/**
	 * Storage branch indexer.
	 */
	private IStorageBranchIndexer<E> storageBranchIndexer;

	/**
	 * No-args constructor for testing.
	 */
	public StorageBranch() {
		super(null);
	}

	/**
	 * Default constructor.
	 * 
	 * @param storageBranchIndexer
	 *            Indexer to be used in the branch.
	 */
	public StorageBranch(IStorageBranchIndexer<E> storageBranchIndexer) {
		super(storageBranchIndexer);
		this.storageBranchIndexer = storageBranchIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	protected ITreeComponent<IStorageDescriptor, E> getNextTreeComponent(E element) {
		return storageBranchIndexer.getNextTreeComponent(element);
	}

	/**
	 * {@inheritDoc}
	 */
	public void preWriteFinalization() {
		for (ITreeComponent<IStorageDescriptor, E> storageTreeComponent : getComponentMap().values()) {
			if (storageTreeComponent instanceof IStorageTreeComponent) {
				((IStorageTreeComponent<E>) storageTreeComponent).preWriteFinalization();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getComponentSize(IObjectSizes objectSizes) {
		long size = super.getComponentSize(objectSizes);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * Gets {@link #storageBranchIndexer}.
	 * 
	 * @return {@link #storageBranchIndexer}
	 */
	IStorageBranchIndexer<E> getStorageBranchIndexer() {
		return storageBranchIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((storageBranchIndexer == null) ? 0 : storageBranchIndexer.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
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
		StorageBranch<E> other = (StorageBranch<E>) obj;
		if (storageBranchIndexer == null) {
			if (other.storageBranchIndexer != null) {
				return false;
			}
		} else if (!storageBranchIndexer.equals(other.storageBranchIndexer)) {
			return false;
		}
		return true;
	}

}
