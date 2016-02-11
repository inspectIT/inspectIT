package info.novatec.inspectit.indexing.impl;


import java.util.List;
import java.util.concurrent.RecursiveTask;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.CombinedStorageBranch;

/**
 * This class is a test class which returns the given resultList, to test the functionality of the
 * query(query, forkJoinPool) method in{@link CombinedStorageBranch}.
 * <p>
 * The class is used in {@link StorageIndexingTest}.queryCombinedStorageBranch().
 * 
 * @author Tobias Angerstein
 *
 * @param <E>
 */
public class CombinedStorageQueryTestTask<E extends DefaultData> extends RecursiveTask<List<IStorageDescriptor>> {
	
	/**
	 * the defined resultList 
	 */
	List<IStorageDescriptor> resultList;
	
	
	public CombinedStorageQueryTestTask(List<IStorageDescriptor> resultList){
		this.resultList = resultList;
	}
	/**
	 * {@inheritDoc}
	 * <p>
	 * Forks and queries all subbranches.
	 */
	protected List<IStorageDescriptor> compute() {	
		return resultList;
	}
}