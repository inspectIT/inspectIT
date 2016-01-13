package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.RemoteCallDataDao;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.impl.RemoteCallDataQueryFactory;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Provides <code>RemoteCallData</code> information from the CMR internal in memory buffer.
 * 
 * @author Thomas Kluge
 */
@Repository
public class BufferRemoteCallDataDaoImpl extends AbstractBufferDataDao<RemoteCallData> implements RemoteCallDataDao {

	/**
	 * Index query factory.
	 */
	@Autowired
	private RemoteCallDataQueryFactory<IIndexQuery> remoteCallDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RemoteCallData> getRemoteCallDataOverview(RemoteCallData remoteCallData) {
		IIndexQuery query = remoteCallDataQueryFactory.getRemoteCallDataQuery(remoteCallData, null, null);
		return super.executeQuery(query, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RemoteCallData> getRemoteCallDataOverview(RemoteCallData remoteCallData, Date fromDate, Date toDate) {
		IIndexQuery query = remoteCallDataQueryFactory.getRemoteCallDataQuery(remoteCallData, fromDate, toDate);
		return super.executeQuery(query, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoteCallData getRemoteCallDataByIdentification(long platformID, long identification, boolean calling) {
		IIndexQuery query = remoteCallDataQueryFactory.getRemoteCallDataQuery(platformID, identification, calling);

		List<RemoteCallData> result = super.executeQuery(query, false);
		if (result.size() == 1) {
			return result.get(0);
		}
		return null;
	}

}
