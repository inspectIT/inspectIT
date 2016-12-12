package rocks.inspectit.ui.rcp.repository.service.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;

/**
 * Storage span service that always returns empty results. This will be changed in future when spans
 * are also saved to storage.
 *
 * @author Ivan Senic
 *
 */
public class StorageSpanService implements ISpanService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getRootSpans(int limit, Date fromDate, Date toDate, ResultComparator<AbstractSpan> resultComparator) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getSpans(long traceId) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span get(SpanIdent spanIdent) {
		return null;
	}

}
