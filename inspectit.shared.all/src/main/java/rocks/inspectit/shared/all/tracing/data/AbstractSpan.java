package rocks.inspectit.shared.all.tracing.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.MethodSensorData;

/**
 * Base span. This span holds all information that all other types of spans do.
 * <p>
 * The relationship in the span can be described in two ways:
 * <ul>
 * <li>1. The caller or called attribute. Reference span can either call another reference span or
 * be called.
 * <li>2. Relationship type describes what are time constraints between two spans. Can be Child-Of
 * (caller wait for callee) or Follow-From (caller does not wait for the callee). More info or the
 * relationships can be read in the class {@link ReferenceType}.
 * </ul>
 * <p>
 * We also capture the {@link PropagationType} that provides information if propagation was inter-
 * or cross-process and what technology was used (for example HTTP).
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractSpan extends MethodSensorData implements Span {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -8144017430549409409L;

	/**
	 * Identifier of this span.
	 */
	private SpanIdent spanIdent;

	/**
	 * Duration of this span in milliseconds. We keep same resolution here as for the duration of
	 * our other monitoring data.
	 */
	private double duration;

	/**
	 * Propagation type.
	 *
	 * @see PropagationType
	 */
	private PropagationType propagationType;

	/**
	 * Reference type.
	 */
	private String referenceType;

	/**
	 * ID of the span's parent. Can be 0 to denote that there is no parent.
	 */
	private long parentSpanId;

	/**
	 * Defined tags.
	 */
	private Map<String, String> tags;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract boolean isCaller();

	/**
	 * Gets {@link #spanIdent}.
	 *
	 * @return {@link #spanIdent}
	 */
	@Override
	public SpanIdent getSpanIdent() {
		return this.spanIdent;
	}

	/**
	 * Sets {@link #spanIdent}.
	 *
	 * @param spanIdent
	 *            New value for {@link #spanIdent}
	 */
	public void setSpanIdent(SpanIdent spanIdent) {
		this.spanIdent = spanIdent;
	}

	/**
	 * Gets {@link #duration}.
	 *
	 * @return {@link #duration}
	 */
	@Override
	public double getDuration() {
		return this.duration;
	}

	/**
	 * Sets {@link #duration}.
	 *
	 * @param duration
	 *            New value for {@link #duration}
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * Gets {@link #propagationType}.
	 *
	 * @return {@link #propagationType}
	 */
	@Override
	public PropagationType getPropagationType() {
		return this.propagationType;
	}

	/**
	 * Sets {@link #propagationType}.
	 *
	 * @param propagationType
	 *            New value for {@link #propagationType}
	 */
	public void setPropagationType(PropagationType propagationType) {
		this.propagationType = propagationType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReferenceType() {
		return referenceType;
	}

	/**
	 * Sets {@link #ReferenceType}.
	 *
	 * @param referenceType
	 *            New value for {@link #ReferenceType}
	 */
	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

	/**
	 * Gets {@link #parentSpanId}.
	 *
	 * @return {@link #parentSpanId}
	 */
	@Override
	public long getParentSpanId() {
		return this.parentSpanId;
	}

	/**
	 * Sets {@link #parentSpanId}.
	 *
	 * @param parentId
	 *            New value for {@link #parentSpanId} or 0 to make this span a root.
	 */
	public void setParentSpanId(long parentId) {
		this.parentSpanId = parentId;
	}

	/**
	 * If this is span is a root span.
	 *
	 * @return If this is span identification for a root span.
	 */
	@Override
	public boolean isRoot() {
		return this.parentSpanId == 0;
	}

	/**
	 * Same as {@link #isRoot()}. Needed for querying.
	 *
	 * @return {@link #isRoot()}
	 */
	public boolean getRoot() { // NOPMD
		return isRoot();
	}

	/**
	 * Adds tag to this span.
	 *
	 * @param tag
	 *            {@link Tag}, must not be <code>null</code>.
	 * @param value
	 *            String value, must not be <code>null</code>.
	 * @return Old value associated with same tag.
	 */
	@Override
	public String addTag(String tag, String value) {
		if (null == tags) {
			tags = new HashMap<String, String>(1, 1f);
		}
		return tags.put(tag, value);
	}

	/**
	 * Adds all tags from the given map to the tags of this span.
	 *
	 * @param otherTags
	 *            Map of tags to add.
	 */
	@Override
	public void addAllTags(Map<String, String> otherTags) {
		if (null == tags) {
			tags = new HashMap<String, String>(otherTags.size(), 1f);
		}
		tags.putAll(otherTags);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTags() {
		if (null == tags) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(tags);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(4, 0, 0, 0, 1, 1);
		size += objectSizes.getSizeOf(spanIdent);
		if (null != tags) {
			int tagsSize = tags.size();
			size += objectSizes.getSizeOfHashMap(tagsSize);
			size += tagsSize + objectSizes.getSizeOfIntegerObject();
			size += objectSizes.getSizeOf(tags.values().toArray(new String[tagsSize]));
		}

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(this.duration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + (int) (this.parentSpanId ^ (this.parentSpanId >>> 32));
		result = (prime * result) + ((this.propagationType == null) ? 0 : this.propagationType.hashCode());
		result = (prime * result) + ((this.referenceType == null) ? 0 : this.referenceType.hashCode());
		result = (prime * result) + ((this.spanIdent == null) ? 0 : this.spanIdent.hashCode());
		result = (prime * result) + ((this.tags == null) ? 0 : this.tags.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractSpan other = (AbstractSpan) obj;
		if (Double.doubleToLongBits(this.duration) != Double.doubleToLongBits(other.duration)) {
			return false;
		}
		if (this.parentSpanId != other.parentSpanId) {
			return false;
		}
		if (this.propagationType != other.propagationType) {
			return false;
		}
		if (this.referenceType == null) {
			if (other.referenceType != null) {
				return false;
			}
		} else if (!this.referenceType.equals(other.referenceType)) {
			return false;
		}
		if (this.spanIdent == null) {
			if (other.spanIdent != null) {
				return false;
			}
		} else if (!this.spanIdent.equals(other.spanIdent)) {
			return false;
		}
		if (this.tags == null) {
			if (other.tags != null) {
				return false;
			}
		} else if (!this.tags.equals(other.tags)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AbstractSpan [isCaller()=" + this.isCaller() + ", getSpanIdent()=" + this.getSpanIdent() + ", getDuration()=" + this.getDuration() + ", getPropagationType()="
				+ this.getPropagationType() + ", getReferenceType()=" + this.getReferenceType() + ", getParentSpanId()=" + this.getParentSpanId() + ", isRoot()=" + this.isRoot() + ", getTags()="
				+ this.getTags() + "]";
	}

}
