package rocks.inspectit.agent.java.sdk.opentracing.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.opentracing.SpanContext;
import rocks.inspectit.agent.java.sdk.opentracing.util.RandomUtils;

/**
 * Implementation of the {@link io.opentracing.SpanContext}. Keeps information about span id, trace
 * id and span parent id. Sampling flag is not included in the moment as it's not explicitly defined
 * by the opentracing API.
 *
 * @author Ivan Senic
 *
 */
public class SpanContextImpl implements SpanContext {

	/**
	 * Span id.
	 */
	private final long id;

	/**
	 * Trace id.
	 */
	private final long traceId;

	/**
	 * Parent span id.
	 */
	private final long parentId;

	/**
	 * Reference type to the parent context.
	 */
	private final String referenceType;

	/**
	 * Baggage.
	 */
	private final Map<String, String> baggage;

	/**
	 * Constructor.
	 *
	 * @param id
	 *            Unique ID of the span.
	 * @param traceId
	 *            ID of the trace that span belongs to.
	 * @param parentId
	 *            ID of the span's parent.
	 */
	public SpanContextImpl(long id, long traceId, long parentId) {
		this(id, traceId, parentId, null, Collections.<String, String> emptyMap());
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            Unique ID of the span.
	 * @param traceId
	 *            ID of the trace that span belongs to.
	 * @param parentId
	 *            ID of the span's parent.
	 * @param referenceType
	 *            Reference to the parent.
	 * @param baggage
	 *            Additional baggage
	 */
	public SpanContextImpl(long id, long traceId, long parentId, String referenceType, Map<String, String> baggage) {
		// ids
		this.id = id;
		this.traceId = traceId;
		this.parentId = parentId;
		this.referenceType = referenceType;
		// baggage handling
		if ((null != baggage) && !baggage.isEmpty()) {
			this.baggage = new HashMap<String, String>(baggage);
		} else {
			this.baggage = new HashMap<String, String>(0, 1f);
		}
	}

	/**
	 * Builds new {@link SpanContextImpl} as a child of given parent context. If parent context is
	 * <code>null</code> then {@link #build(Map)} will be used and new trace context will be
	 * created.
	 * <p>
	 * Passed baggage will be the baggage of this span context.
	 *
	 * @param parent
	 *            Context that will be use to determine to which trace/parent new context belongs.
	 *            Can be <code>null</code> to denote that the new trace context should be created.
	 * @param referenceType
	 *            Reference type to the parent context.
	 * @param baggage
	 *            Context baggage.
	 * @return {@link SpanContextImpl}. Never <code>null</code>.
	 */
	public static SpanContextImpl build(SpanContextImpl parent, String referenceType, Map<String, String> baggage) {
		if (null == parent) {
			return build(baggage);
		} else {
			long id = RandomUtils.randomLong();
			SpanContextImpl spanContextImpl = new SpanContextImpl(id, parent.getTraceId(), parent.getId(), referenceType, baggage);
			return spanContextImpl;
		}
	}

	/**
	 * Builds new {@link SpanContextImpl} with new trace context.
	 *
	 * @param baggage
	 *            Context baggage.
	 * @return {@link SpanContextImpl}. Never <code>null</code>.
	 */
	public static SpanContextImpl build(Map<String, String> baggage) {
		long id = RandomUtils.randomLong();
		return new SpanContextImpl(id, id, id, null, baggage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Entry<String, String>> baggageItems() {
		return Collections.unmodifiableMap(baggage).entrySet();
	}

	/**
	 * Sets baggage item.
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	public void setBaggageItem(String key, String value) {
		baggage.put(key, value);
	}

	/**
	 * Gets baggage item.
	 *
	 * @param key
	 *            key
	 * @return Baggage item or <code>null</code> if the one does not exist.
	 */
	public String getBaggageItem(String key) {
		return baggage.get(key);
	}

	/**
	 * Returns reference type to the parent if the one is set.
	 *
	 * @return Returns reference type to the parent if the one is set.
	 */
	public String getReferenceType() {
		return this.referenceType;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.baggage == null) ? 0 : this.baggage.hashCode());
		result = (prime * result) + (int) (this.id ^ (this.id >>> 32));
		result = (prime * result) + (int) (this.parentId ^ (this.parentId >>> 32));
		result = (prime * result) + ((this.referenceType == null) ? 0 : this.referenceType.hashCode());
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
		SpanContextImpl other = (SpanContextImpl) obj;
		if (this.baggage == null) {
			if (other.baggage != null) {
				return false;
			}
		} else if (!this.baggage.equals(other.baggage)) {
			return false;
		}
		if (this.id != other.id) {
			return false;
		}
		if (this.parentId != other.parentId) {
			return false;
		}
		if (this.referenceType == null) {
			if (other.referenceType != null) {
				return false;
			}
		} else if (!this.referenceType.equals(other.referenceType)) {
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
		return "SpanContextImpl [id=" + this.id + ", traceId=" + this.traceId + ", parentId=" + this.parentId + ", referenceType=" + this.referenceType + ", baggage=" + this.baggage + "]";
	}

}