package info.novatec.inspectit.indexing.query.factory.impl;

import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.AbstractQueryFactory;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Component;

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

		ArrayList<Class<?>> searchedClasses = new ArrayList<Class<?>>();
		searchedClasses.add(RemoteCallData.class);

		query.setObjectClasses(searchedClasses);
		query.setPlatformIdent(remoteCallData.getPlatformIdent());
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
		searchedClasses.add(RemoteCallData.class);

		query.setObjectClasses(searchedClasses);
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("remotePlatformIdent", remotePlatformID));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("identification", identification));
		query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("calling", calling));
		return query;
	}
}
