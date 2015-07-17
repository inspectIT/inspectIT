package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This classes enables joining of many storage indexing trees, so that all of them can be queried.
 * Note that this class provide read only operations. Thus call of the method
 * {@link #put(DefaultData)} and {@link #getAndRemove(DefaultData)} will throw and
 * {@link UnsupportedOperationException} because simply the class does not know where in which
 * branch the object belongs.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class CombinedStorageBranch<E extends DefaultData> implements IStorageTreeComponent<E> {

	/**
	 * List of combined branches.
	 */
	private List<IStorageTreeComponent<E>> branches;

	/**
	 * Default no-args constructor.
	 */
	public CombinedStorageBranch() {
		branches = new ArrayList<IStorageTreeComponent<E>>();
	}

	/**
	 * Constructor thats sets branches.
	 * 
	 * @param branches
	 *            Branches that are joined in one.
	 */
	public CombinedStorageBranch(List<IStorageTreeComponent<E>> branches) {
		this.branches = branches;
	}

	/**
	 * @return the branches
	 */
	public List<IStorageTreeComponent<E>> getBranches() {
		return branches;
	}

	/**
	 * @param branches
	 *            the branches to set
	 */
	public void setBranches(List<IStorageTreeComponent<E>> branches) {
		this.branches = branches;
	}

	/**
	 * Adds a branch to the combined branches list.
	 * 
	 * @param branch
	 *            {@link IStorageTreeComponent} to add.
	 */
	public void addBranch(IStorageTreeComponent<E> branch) {
		branches.add(branch);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Call to this method throws the {@link UnsupportedOperationException} cause combined branch
	 * should only be used for read operations.
	 */
	public IStorageDescriptor put(E element) throws IndexingException {
		throw new UnsupportedOperationException("Combined storage branch provides only read-only operations.");
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor get(E element) {
		for (IStorageTreeComponent<E> branch : branches) {
			IStorageDescriptor descriptor = branch.get(element);
			if (descriptor != null) {
				return descriptor;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Method returns combined results from all branches that are combined.
	 */
	public List<IStorageDescriptor> query(StorageIndexQuery query) {
		List<IStorageDescriptor> combinedResult = new ArrayList<IStorageDescriptor>();
		for (IStorageTreeComponent<E> branch : branches) {
			List<IStorageDescriptor> branchResult = branch.query(query);
			if (branchResult != null && !branchResult.isEmpty()) {
				combinedResult.addAll(branchResult);
			}
		}
		return combinedResult;
	}

	/**
	 * {@inheritDoc}
	 */
	public void preWriteFinalization() {
		for (IStorageTreeComponent<E> branch : branches) {
			branch.preWriteFinalization();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Method returns combined results from all branches that are combined.
	 */
	public List<IStorageDescriptor> query(IIndexQuery query) {
		List<IStorageDescriptor> combinedResult = new ArrayList<IStorageDescriptor>();
		for (IStorageTreeComponent<E> branch : branches) {
			List<IStorageDescriptor> branchResult = branch.query(query);
			if (branchResult != null && !branchResult.isEmpty()) {
				combinedResult.addAll(branchResult);
			}
		}
		return combinedResult;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Call to this method throws the {@link UnsupportedOperationException} cause combined branch
	 * should only be used for read operations.
	 */
	public IStorageDescriptor getAndRemove(E element) {
		throw new UnsupportedOperationException("Combined storage branch provides only read-only operations.");
	}

	/**
	 * {@inheritDoc}
	 */
	public long getComponentSize(IObjectSizes objectSizes) {
		long size = objectSizes.getSizeOfObjectHeader() + objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size += objectSizes.getSizeOf(branches);
		for (IStorageTreeComponent<E> branch : branches) {
			size += branch.getComponentSize(objectSizes);
		}
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("branches", branches);
		return toStringBuilder.toString();
	}
}
