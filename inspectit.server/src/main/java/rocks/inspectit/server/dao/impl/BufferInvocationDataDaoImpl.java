package rocks.inspectit.server.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.InvocationDataDao;
import rocks.inspectit.shared.all.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.AbstractBranch;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.InvocationSequenceDataQueryFactory;

/**
 * Implementation of {@link InvocationDataDao} that works with the data from the buffer indexing
 * tree. <br>
 * The query-Method of {@link AbstractBranch} without fork&join is executed, because there isn't
 * much data expected.<br>
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
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Comparator<? super InvocationSequenceData> comparator) {
		return this.getInvocationSequenceOverview(platformId, methodId, limit, null, null, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Comparator<? super InvocationSequenceData> comparator) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate, Comparator<? super InvocationSequenceData> comparator) {
		return this.getInvocationSequenceOverview(platformId, 0, limit, fromDate, toDate, comparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate, Comparator<? super InvocationSequenceData> comparator) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, methodId, fromDate, toDate);
		List<InvocationSequenceData> resultWithChildren;
		if (null != comparator) {
			resultWithChildren = super.executeQuery(query, comparator, limit, false);
		} else {
			resultWithChildren = super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit, false);
		}
		List<InvocationSequenceData> realResults = new ArrayList<>(resultWithChildren.size());
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
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, invocationIdCollection, limit);
		List<InvocationSequenceData> resultWithChildren;
		if (null != comparator) {
			resultWithChildren = super.executeQuery(query, comparator, limit, false);
		} else {
			resultWithChildren = super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit, false);
		}
		List<InvocationSequenceData> realResults = new ArrayList<>(resultWithChildren.size());
		for (InvocationSequenceData invocationSequenceData : resultWithChildren) {
			realResults.add(invocationSequenceData.getClonedInvocationSequence());
		}
		return realResults;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Date fromDate, Date toDate, long minId, int limit, Comparator<? super InvocationSequenceData> comparator) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, fromDate, toDate, minId);
		List<InvocationSequenceData> resultWithChildren;
		if (null != comparator) {
			resultWithChildren = super.executeQuery(query, comparator, limit, false);
		} else {
			resultWithChildren = super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit, false);
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
	public InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template) {
		return super.getIndexingTree().get(template);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceDetail(long platformId, long methodId, int limit, Date fromDate, Date toDate, Comparator<? super InvocationSequenceData> comparator) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, methodId, fromDate, toDate);
		if (null != comparator) {
			return super.executeQuery(query, comparator, limit, false);
		} else {
			return super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit, false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InvocationSequenceData> getInvocationSequenceOverview(Long platformId, Date startDate, Date endDate, long minId, int limit, Long businessTrxId, Long applicationId,
			Comparator<? super InvocationSequenceData> comparator) {
		IIndexQuery query = invocationDataQueryFactory.getInvocationSequences(platformId, startDate, endDate, minId, businessTrxId, applicationId);
		List<InvocationSequenceData> resultWithChildren;
		if (null != comparator) {
			resultWithChildren = super.executeQuery(query, comparator, limit, false);
		} else {
			resultWithChildren = super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit, false);
		}
		List<InvocationSequenceData> realResults = new ArrayList<InvocationSequenceData>(resultWithChildren.size());
		for (InvocationSequenceData invocationSequenceData : resultWithChildren) {
			realResults.add(invocationSequenceData.getClonedInvocationSequence());
		}
		return realResults;
	}

}
