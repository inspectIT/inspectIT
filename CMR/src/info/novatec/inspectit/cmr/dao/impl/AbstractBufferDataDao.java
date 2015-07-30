package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Abstract class for all buffer data DAO service.
 * 
 * @param <E>
 *            Type of the data to be queried.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractBufferDataDao<E extends DefaultData> {

	/**
	 * Indexing tree to search for data.
	 */
	@Autowired
	private IBufferTreeComponent<E> indexingTree;
	
	/**
	 * ForkJoinPool to manage the forks.
	 */
	@Autowired
	@Qualifier("indexingTreeForkJoinPool")
	private ForkJoinPool forkJoinPool;
	
	/**
	 * Executes the query on the indexing tree.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, boolean useForkJoin) {
		return this.executeQuery(indexQuery, null, null, -1, useForkJoin);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator, boolean useForkJoin) {
		return this.executeQuery(indexQuery, aggregator, null, -1, useForkJoin);
	}

	/**
	 * Executes the query on the indexing tree. Results can be sorted by comparator.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, Comparator<E> comparator, boolean useForkJoin) {
		return this.executeQuery(indexQuery, null, comparator, -1, useForkJoin);
	}

	/**
	 * Executes the query on the indexing tree. Furthermore the result list can be limited.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, int limit, boolean useForkJoin) {
		return this.executeQuery(indexQuery, null, null, limit, useForkJoin);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator, Comparator<? super E> comparator, boolean useForkJoin) {
		return this.executeQuery(indexQuery, aggregator, comparator, -1, useForkJoin);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}. Furthermore the
	 * result list can be limited.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator, int limit, boolean useForkJoin) {
		return this.executeQuery(indexQuery, aggregator, null, limit, useForkJoin);
	}

	/**
	 * Executes the query on the indexing tree. Results can be sorted by comparator. Furthermore the
	 * result list can be limited.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * 
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, Comparator<? super E> comparator, int limit, boolean useForkJoin) {
		return this.executeQuery(indexQuery, null, comparator, limit, useForkJoin);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}. Results can be
	 * sorted by comparator. Furthermore the result list can be limited.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @param useForkJoin
	 * 			  true, if forkJoinPool should be used
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator, Comparator<? super E> comparator, int limit, boolean useForkJoin) {
		List<E> data;
		
		if (useForkJoin) {
			data = indexingTree.query(indexQuery, forkJoinPool);
		} else {
			data = indexingTree.query(indexQuery);
		}
		if (null != aggregator) {
			AggregationPerformer<E> aggregationPerformer = new AggregationPerformer<E>(aggregator);
			aggregationPerformer.processCollection(data);
			data = aggregationPerformer.getResultList();
		}

		if (null != comparator) {
			Collections.sort(data, comparator);
		}

		if (limit > -1 && data.size() > limit) {
			data = new ArrayList<E>(data.subList(0, limit));
		}

		return data;
	}

	/**
	 * Gets {@link #indexingTree}.
	 * 
	 * @return {@link #indexingTree}
	 */
	protected IBufferTreeComponent<E> getIndexingTree() {
		return indexingTree;
	}

}
