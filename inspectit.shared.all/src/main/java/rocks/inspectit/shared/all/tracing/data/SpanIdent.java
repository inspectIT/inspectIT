package rocks.inspectit.shared.all.tracing.data;

import java.io.Serializable;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.Sizeable;

/**
 * Holds identification for any span. Each span identification has unique ID. There are also trace
 * and parent ID fields. Span is considered to be root if both parent and trace IDs are same as the
 * span ID.
 *
 * @author Ivan Senic
 *
 */
public class SpanIdent implements Sizeable, Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4529002759213057498L;

	/**
	 * Unique ID of the span.
	 */
	private final long id;

	/**
	 * ID of the trace that span belongs to. Can be same as {@link #id} when it's a root span.
	 */
	private final long traceId;

	/**
	 * ID of the span's parent. Can be same as {@link #id} to denote that there is no parent.
	 */
	private final long parentId;

	/**
	 * No-arg constructor.
	 */
	protected SpanIdent() {
		this(0, 0, 0);
	}

	/**
	 * Default constructor.
	 *
	 * @param id
	 *            Unique ID of the span.
	 * @param traceId
	 *            ID of the trace that span belongs to. Can be same as {@link #id}.
	 * @param parentId
	 *            ID of the span's parent. Can be <code>0</code> to denote that there is no parent.
	 */
	public SpanIdent(long id, long traceId, long parentId) {
		this.id = id;
		this.traceId = traceId;
		this.parentId = parentId;
	}

	/**
	 * If this is span identification for a root span.
	 *
	 * @return If this is span identification for a root span.
	 */
	public boolean isRoot() {
		// TODO kick the other part to support EUM
		return (this.parentId == this.id) && (this.traceId == this.id);
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Gets {@link #traceId}.
	 *
	 * @return {@link #traceId}
	 */
	public long getTraceId() {
		return this.traceId;
	}

	/**
	 * Gets {@link #parentId}.
	 *
	 * @return {@link #parentId}
	 */
	public long getParentId() {
		return this.parentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes) {
		return getObjectSize(objectSizes, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(0, 0, 0, 0, 3, 0);

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
		int result = 1;
		result = (prime * result) + (int) (this.id ^ (this.id >>> 32));
		result = (prime * result) + (int) (this.parentId ^ (this.parentId >>> 32));
		result = (prime * result) + (int) (this.traceId ^ (this.traceId >>> 32));
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SpanIdent other = (SpanIdent) obj;
		if (this.id != other.id) {
			return false;
		}
		if (this.parentId != other.parentId) {
			return false;
		}
		if (this.traceId != other.traceId) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "SpanIdent [id=" + this.id + ", traceId=" + this.traceId + ", parentId=" + this.parentId + ", isRoot()=" + this.isRoot() + "]";
	}

}
