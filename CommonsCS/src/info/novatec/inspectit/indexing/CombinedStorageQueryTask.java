package info.novatec.inspectit.indexing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.CombinedStorageBranch;
/**
 * Queries through the branches and creates new tasks for each branch.
 * 
 * @author Tobias Angerstein
 *
 * @param <E>
 */
public class CombinedStorageQueryTask<E extends DefaultData> extends RecursiveTask<List<IStorageDescriptor>> {
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
	public CombinedStorageQueryTask(CombinedStorageBranch<E> currentRoot, IIndexQuery query) {
		this.query = query;
		this.branchesToQuery = currentRoot.getBranchesToQuery(query);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Forks and queries all subbranches.
	 */
	protected List<IStorageDescriptor> compute() {
		// List of Forks which will be started
		List<RecursiveTask<List<IStorageDescriptor>>> forks = new ArrayList<RecursiveTask<List<IStorageDescriptor>>>();
		List<IStorageDescriptor> resultList = new ArrayList<IStorageDescriptor>();
		for (IStorageTreeComponent<E> component : branchesToQuery) {
			// New fork is being started
			RecursiveTask<List<IStorageDescriptor>> task = component.getTaskForForkJoinQuery(query);
			forks.add(task);
			task.fork();
		}
		for (RecursiveTask<List<IStorageDescriptor>> fork : forks) {
			resultList.addAll(fork.join());
		}
		return resultList;
	}
}
