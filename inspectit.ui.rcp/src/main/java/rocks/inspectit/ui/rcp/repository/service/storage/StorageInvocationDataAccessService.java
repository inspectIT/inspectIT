package rocks.inspectit.ui.rcp.repository.service.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.InvocationSequenceDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

/**
 * {@link IInvocationDataAccessService} for storage purposes.
 *
 * @author Ivan Senic
 *
 */
public class StorageInvocationDataAccessService extends AbstractStorageService<InvocationSequenceData> implements IInvocationDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<InvocationSequenceData> indexingTree;

	/**
	 * Index query provider.
	 */
	private InvocationSequenceDataQueryFactory<StorageIndexQuery> invocationDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		return this.getInvocationSequenceOverview(platformId, methodId, limit, null, null, resultComparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, resultComparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate,
			ResultComparator<InvocationSequenceData> resultComparator) {
		StorageIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, methodId, fromDate, toDate);
		query.setOnlyInvocationsWithoutChildren(true);
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
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate, ResultComparator<InvocationSequenceData> resultComparator) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, fromDate, toDate, resultComparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		StorageIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, invocationIdCollection, limit);
		query.setOnlyInvocationsWithoutChildren(true);
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
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(Long platformId, int limit, Date startDate, Date endDate, Long minId, int businessTrxId, int applicationId, // NOCHK
			ResultComparator<InvocationSequenceData> resultComparator) {
		StorageIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, startDate, endDate, minId, businessTrxId, applicationId);
		query.setOnlyInvocationsWithoutChildren(true);
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
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(String alertId, int limit, ResultComparator<InvocationSequenceData> resultComparator) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		// here we need to create new query since this one does not exist in factory
		StorageIndexQuery query = invocationDataQueryFactory.getIndexQueryProvider().getIndexQuery();
		ArrayList<Class<?>> searchedClasses = new ArrayList<>();
		searchedClasses.add(InvocationSequenceData.class);
		query.setObjectClasses(searchedClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		query.setMethodIdent(template.getMethodIdent());
		query.setSensorTypeIdent(template.getSensorTypeIdent());
		query.setOnlyInvocationsWithoutChildren(false);
		ArrayList<Long> includeIds = new ArrayList<>();
		includeIds.add(template.getId());
		query.setIncludeIds(includeIds);
		List<InvocationSequenceData> results = super.executeQuery(query);
		if (results.size() == 1) {
			return results.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<InvocationSequenceData> getInvocationSequenceDetail(long traceId) {
		StorageIndexQuery query = invocationDataQueryFactory.getInvocationSequences(traceId);
		query.setOnlyInvocationsWithoutChildren(false);
		return super.executeQuery(query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStorageTreeComponent<InvocationSequenceData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<InvocationSequenceData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param invocationDataQueryFactory
	 *            the invocationDataQueryFactory to set
	 */
	public void setInvocationDataQueryFactory(InvocationSequenceDataQueryFactory<StorageIndexQuery> invocationDataQueryFactory) {
		this.invocationDataQueryFactory = invocationDataQueryFactory;
	}

}
