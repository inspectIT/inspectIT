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

import org.springframework.beans.factory.annotation.Autowired;

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
	 * Executes the query on the indexing tree.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * 
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery) {
		return this.executeQuery(indexQuery, null, null, -1);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator) {
		return this.executeQuery(indexQuery, aggregator, null, -1);
	}

	/**
	 * Executes the query on the indexing tree. Results can be sorted by comparator.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, Comparator<E> comparator) {
		return this.executeQuery(indexQuery, null, comparator, -1);
	}

	/**
	 * Executes the query on the indexing tree. Furthermore the result list can be limited.
	 * 
	 * @param indexQuery
	 *            Index query to execute.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, int limit) {
		return this.executeQuery(indexQuery, null, null, limit);
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
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator, Comparator<? super E> comparator) {
		return this.executeQuery(indexQuery, aggregator, comparator, -1);
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
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator, int limit) {
		return this.executeQuery(indexQuery, aggregator, null, limit);
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
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, Comparator<? super E> comparator, int limit) {
		return this.executeQuery(indexQuery, null, comparator, limit);
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
	 * @return Result list.
	 */
	protected List<E> executeQuery(IIndexQuery indexQuery, IAggregator<E> aggregator, Comparator<? super E> comparator, int limit) {
		List<E> data = indexingTree.query(indexQuery);

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
