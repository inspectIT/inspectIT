package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.Aggregators;
import info.novatec.inspectit.indexing.query.factory.impl.TimerDataQueryFactory;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.util.Date;
import java.util.List;

/**
 * {@link ITimerDataAccessService} for storage purposes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageTimerDataAccessService extends AbstractStorageService<TimerData> implements ITimerDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<TimerData> indexingTree;

	/**
	 * Index query provider.
	 */
	private TimerDataQueryFactory<StorageIndexQuery> timerDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<TimerData> getAggregatedTimerData(TimerData timerData) {
		return this.getAggregatedTimerData(timerData, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<TimerData> getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate) {
		StorageIndexQuery query = timerDataQueryFactory.getAggregatedTimerDataQuery(timerData, fromDate, toDate);
		return super.executeQuery(query, Aggregators.TIMER_DATA_AGGREGATOR);
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<TimerData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<TimerData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param timerDataQueryFactory
	 *            the timerDataQueryFactory to set
	 */
	public void setTimerDataQueryFactory(TimerDataQueryFactory<StorageIndexQuery> timerDataQueryFactory) {
		this.timerDataQueryFactory = timerDataQueryFactory;
	}

}
