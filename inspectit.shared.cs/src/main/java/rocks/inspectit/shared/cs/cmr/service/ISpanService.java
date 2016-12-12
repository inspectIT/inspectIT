package rocks.inspectit.shared.cs.cmr.service;

import java.util.Collection;
import java.util.Date;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.Span;

/**
 * Data access service for {@link Span} retrieval.
 *
 * @author Ivan Senic
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface ISpanService {

	/**
	 * Returns all root spans existing in the system.
	 *
	 * @param limit
	 *            number of results returned by service. Value <code>-1</code> means no limit.
	 * @param fromDate
	 *            Date include invocation from.
	 * @param toDate
	 *            Date include invocation to.
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case default time-stamp sorting will be done.
	 *
	 * @return Returns all root spans existing in the system.
	 */
	Collection<? extends Span> getRootSpans(int limit, Date fromDate, Date toDate, ResultComparator<AbstractSpan> resultComparator);

	/**
	 * Get all spans belonging to the trace with the given ID.
	 *
	 * @param traceId
	 *            trace identification
	 * @return Collections of spans belonging to the given trace.
	 */
	Collection<? extends Span> getSpans(long traceId);
}
