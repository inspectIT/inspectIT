package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.InvocationDataDao;
import info.novatec.inspectit.communication.comparator.DefaultDataComparatorEnum;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.query.factory.impl.InvocationSequenceDataQueryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link InvocationDataDao} that works with the data from the buffer indexing
 * tree.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class BufferInvocationDataDaoImpl extends AbstractBufferDataDao<InvocationSequenceData> implements InvocationDataDao {

	/**
	 * Index query provider.
	 */
	@Autowired
	private InvocationSequenceDataQueryFactory<IIndexQuery> invocationDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Comparator<? super InvocationSequenceData> comparator) {
		return this.getInvocationSequenceOverview(platformId, methodId, limit, null, null, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Comparator<? super InvocationSequenceData> comparator) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate, Comparator<? super InvocationSequenceData> comparator) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, fromDate, toDate, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate, Comparator<? super InvocationSequenceData> comparator) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, methodId, limit, fromDate, toDate);
		List<InvocationSequenceData> resultWithChildren;
		if (null != comparator) {
			resultWithChildren = super.executeQuery(query, comparator, limit);
		} else {
			resultWithChildren = super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit);
		}
		List<InvocationSequenceData> realResults = new ArrayList<InvocationSequenceData>(resultWithChildren.size());
		for (InvocationSequenceData invocationSequenceData : resultWithChildren) {
			realResults.add(invocationSequenceData.getClonedInvocationSequence());
		}
		return realResults;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit, Comparator<? super InvocationSequenceData> comparator) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequenceOverview(platformId, invocationIdCollection, limit);
		List<InvocationSequenceData> resultWithChildren;
		if (null != comparator) {
			resultWithChildren = super.executeQuery(query, comparator, limit);
		} else {
			resultWithChildren = super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit);
		}
		List<InvocationSequenceData> realResults = new ArrayList<InvocationSequenceData>(resultWithChildren.size());
		for (InvocationSequenceData invocationSequenceData : resultWithChildren) {
			realResults.add(invocationSequenceData.getClonedInvocationSequence());
		}
		return realResults;
	}

	/**
	 * {@inheritDoc}
	 */
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		return super.getIndexingTree().get(template);
	}

}
