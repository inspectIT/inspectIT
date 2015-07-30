package info.novatec.inspectit.indexing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.CombinedStorageBranch;
/**
 * Queries through the branches and creates new tasks for each branch.
 * 
 * @author Tobias Angerstein
 *
 * @param <E> 
 */
public class CombinedStorageQueryTask<E> extends RecursiveTask<List<IStorageTreeComponent<E>>> {
	/**
	 * The given query.
	 */
	private IIndexQuery query;
	
	/**
	 * The branches, which have to be queried.
	 */
	private List<IStorageTreeComponent<E>> branchesToQuery;
	
	/**
	 * Default constructor. The current branch and the query is needed.
	 * 
	 * @param currentRoot the current Branch
	 * @param query the given query
	 */
	public CombinedStorageQueryTask(CombinedStorageBranch currentRoot, IIndexQuery query) {
		this.query = query;
		this.branchesToQuery = currentRoot.getBranchesToQuery(query);
	}

	/**
	 * Forks and queries all subbranches.
	 * {@inheritDoc}
	 */
	protected List<IStorageTreeComponent<E>> compute() {
		// List of Forks which will be started
		List<RecursiveTask<List<IStorageTreeComponent<E>>>> forks = new ArrayList<RecursiveTask<List<IStorageTreeComponent<E>>>>();
		List<IStorageTreeComponent<E>> resultList = new ArrayList<IStorageTreeComponent<E>>();
		for (IStorageTreeComponent<E> component : branchesToQuery) {
			// If Leaf, a LeafFork will be started
			// Else: Another QueryTask will be started
			component.getTaskForForkJoinQuery(query).fork();
			}
		for (RecursiveTask<List<IStorageTreeComponent<E>>> fork : forks) {
			resultList.addAll(fork.join());
		}
		return resultList;
	}
}
