package rocks.inspectit.ui.rcp.repository.service.storage;

import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.cmr.service.ITimerDataAccessService;
import rocks.inspectit.shared.cs.indexing.aggregation.Aggregators;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.TimerDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

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
