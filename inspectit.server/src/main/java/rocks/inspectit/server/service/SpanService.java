package rocks.inspectit.server.service;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.dao.impl.BufferSpanDaoImpl;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;

/**
 * Implementation of the {@link ISpanService} that reads data from the buffer.
 *
 * @author Ivan Senic
 *
 */
@Service
public class SpanService implements ISpanService {

	/**
	 * {@link BufferSpanDaoImpl}.
	 */
	@Autowired
	private BufferSpanDaoImpl spanDao;

	/**
	 * Cached data service for comparators.
	 */
	@Autowired
	ICachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getRootSpans(int limit, Date fromDate, Date toDate, ResultComparator<AbstractSpan> resultComparator) {
		if (null != resultComparator) {
			resultComparator.setCachedDataService(cachedDataService);
		}
		return spanDao.getRootSpans(limit, fromDate, toDate, resultComparator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getSpans(long traceId) {
		return spanDao.getSpans(traceId);
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span get(SpanIdent spanIdent) {
		return spanDao.get(spanIdent);
	}

}
