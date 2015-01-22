package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IRemoteCallDataAccessService;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.indexing.query.factory.impl.RemoteCallDataQueryFactory;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * /** {@link IRemoteCallDataAccessService} for storage purposes.
 * 
 * @author Thomas Kluge
 * 
 */
public class StorageRemoteDataAccessService extends AbstractStorageService<RemoteCallData> implements IRemoteCallDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<RemoteCallData> indexingTree;

	/**
	 * Index query provider.
	 */
	private RemoteCallDataQueryFactory<StorageIndexQuery> remoteCallDataQueryFactory;

	/**
	 * @return indexingTree the indexingTree to set
	 */
	@Override
	protected IStorageTreeComponent<RemoteCallData> getIndexingTree() {
		return indexingTree;
	}

	@Override
	public List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData) {
		return getRemoteCallData(remoteCallData, null, null);
	}

	@Override
	public List<RemoteCallData> getRemoteCallData(RemoteCallData remoteCallData, Date fromDate, Date toDate) {
		StorageIndexQuery query = remoteCallDataQueryFactory.getRemoteCallDataQuery(remoteCallData, fromDate, toDate);
		return super.executeQuery(query);
	}

	@Override
	public RemoteCallData getRemoteCallData(long remotePlatformID, long identification, boolean calling) {
		StorageIndexQuery query = remoteCallDataQueryFactory.getIndexQueryProvider().getIndexQuery();
		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(RemoteCallData.class);
		query.setObjectClasses(searchedClasses);
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("remotePlatformIdent", remotePlatformID));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("identification", identification));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("calling", calling));

		List<RemoteCallData> result = super.executeQuery(query);
		if (result.size() == 1) {
			return result.get(0);

		}
		return null;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<RemoteCallData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param remoteCallDataQueryFactory
	 *            the remoteCallDataQueryFactory to set
	 */
	public void setInvocationDataQueryFactory(RemoteCallDataQueryFactory<StorageIndexQuery> remoteCallDataQueryFactory) {
		this.remoteCallDataQueryFactory = remoteCallDataQueryFactory;
	}

}
