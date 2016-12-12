package rocks.inspectit.shared.cs.tracing.comparator;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.shared.cs.communication.comparator.IDataComparator;

/**
 * Comparator for the span.
 *
 * @author Ivan Senic
 *
 */
public enum SpanComparator implements IDataComparator<AbstractSpan> {

	/**
	 * Sort by propagation type.
	 */
	PROPAGATION_TYPE,

	/**
	 * Sort by span duration.
	 */
	DURATION;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(AbstractSpan o1, AbstractSpan o2, ICachedDataService cachedDataService) {
		switch (this) {
		case PROPAGATION_TYPE:
			return ObjectUtils.compare(o1.getPropagationType(), o2.getPropagationType());
		case DURATION:
			return Double.compare(o1.getDuration(), o2.getDuration());
		default:
			return 0;
		}
	}

}
