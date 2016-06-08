package rocks.inspectit.shared.cs.indexing.query.factory.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;
import rocks.inspectit.shared.all.communication.data.RemoteMQCallData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.query.factory.AbstractQueryFactory;
import rocks.inspectit.shared.cs.indexing.restriction.impl.IndexQueryRestrictionFactory;

/**
 * Factory for all queries for the {@link RemoteCallData}.
 *
 * @author Thomas Kluge
 *
 * @param <E>
 */
@Component
public class RemoteCallDataQueryFactory<E extends IIndexQuery> extends AbstractQueryFactory<E> {

	/**
	 * Returns the query for aggregating the {@link RemoteCallData}.
	 *
	 * @param remoteCallData
	 *            The template containing the platform id.
	 * @param fromDate
	 *            Date to include data from.
	 * @param toDate
	 *            Date to include data to.
	 * @return Query object.
	 */
	public E getRemoteCallDataQuery(RemoteCallData remoteCallData, Date fromDate, Date toDate) {
		E query = getIndexQueryProvider().getIndexQuery();
		query.setPlatformIdent(remoteCallData.getPlatformIdent());
		query.setMethodIdent(remoteCallData.getMethodIdent());

		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(RemoteHttpCallData.class);
		searchedClasses.add(RemoteMQCallData.class);
		query.setObjectClasses(searchedClasses);

		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		return query;
	}

	/**
	 * Returns the query for the given parameters.
	 *
	 * @param remotePlatformID
	 *            The platform id of the remote call.
	 * @param identification
	 *            The identification of this remote call.
	 * @param calling
	 *            If it is the remote call of the request (true) or the respone (false).
	 * @return Query object.
	 */
	public IIndexQuery getRemoteCallDataQuery(long remotePlatformID, long identification, boolean calling) {
		E query = getIndexQueryProvider().getIndexQuery();

		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(RemoteHttpCallData.class);
		searchedClasses.add(RemoteMQCallData.class);

		query.setObjectClasses(searchedClasses);
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("remotePlatformIdent", remotePlatformID));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("identification", identification));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("calling", calling));
		return query;
	}
}
