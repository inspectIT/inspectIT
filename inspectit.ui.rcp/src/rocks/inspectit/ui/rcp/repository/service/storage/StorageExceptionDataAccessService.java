package rocks.inspectit.ui.rcp.repository.service.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.communication.data.AggregatedExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.cs.cmr.service.IExceptionDataAccessService;
import rocks.inspectit.shared.cs.indexing.aggregation.Aggregators;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.ExceptionSensorDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

/**
 * {@link IExceptionDataAccessService} for storage purposes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageExceptionDataAccessService extends AbstractStorageService<ExceptionSensorData> implements IExceptionDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<ExceptionSensorData> indexingTree;

	/**
	 * Index query provider.
	 */
	private ExceptionSensorDataQueryFactory<StorageIndexQuery> exceptionSensorDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, ResultComparator<ExceptionSensorData> resultComparator) {
		return this.getUngroupedExceptionOverview(template, limit, null, null, resultComparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, int limit, Date fromDate, Date toDate, ResultComparator<ExceptionSensorData> resultComparator) {
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getUngroupedExceptionOverviewQuery(template, limit, fromDate, toDate);
		if (null != resultComparator) {
			resultComparator.setCachedDataService(getStorageRepositoryDefinition().getCachedDataService());
			return super.executeQuery(query, resultComparator, limit);
		} else {
			return super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, ResultComparator<ExceptionSensorData> resultComparator) {
		return this.getUngroupedExceptionOverview(template, -1, null, null, resultComparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getUngroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate, ResultComparator<ExceptionSensorData> resultComparator) {
		return this.getUngroupedExceptionOverview(template, -1, fromDate, toDate, resultComparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ExceptionSensorData> getExceptionTree(ExceptionSensorData template) {
		// here we have a problem because we have to de-serialize every exception to find the right
		// one, we need to check if we can change this method
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getExceptionTreeQuery(template);
		List<ExceptionSensorData> results = super.executeQuery(query);
		Collections.reverse(results);
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template) {
		return this.getDataForGroupedExceptionOverview(template, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AggregatedExceptionSensorData> getDataForGroupedExceptionOverview(ExceptionSensorData template, Date fromDate, Date toDate) {
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getDataForGroupedExceptionOverviewQuery(template, fromDate, toDate);
		List<ExceptionSensorData> resultList = super.executeQuery(query, Aggregators.GROUP_EXCEPTION_OVERVIEW_AGGREGATOR);
		List<AggregatedExceptionSensorData> filterList = new ArrayList<AggregatedExceptionSensorData>(resultList.size());
		for (ExceptionSensorData data : resultList) {
			if (data instanceof AggregatedExceptionSensorData) {
				filterList.add((AggregatedExceptionSensorData) data);
			}
		}
		return filterList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ExceptionSensorData> getStackTraceMessagesForThrowableType(ExceptionSensorData template) {
		// same problem again, we need to de-serialize all exceptions
		StorageIndexQuery query = exceptionSensorDataQueryFactory.getStackTraceMessagesForThrowableTypeQuery(template);
		return super.executeQuery(query, Aggregators.DISTINCT_STACK_TRACES_AGGREGATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<ExceptionSensorData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<ExceptionSensorData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param exceptionSensorDataQueryFactory
	 *            the exceptionSensorDataQueryFactory to set
	 */
	public void setExceptionSensorDataQueryFactory(ExceptionSensorDataQueryFactory<StorageIndexQuery> exceptionSensorDataQueryFactory) {
		this.exceptionSensorDataQueryFactory = exceptionSensorDataQueryFactory;
	}

}
