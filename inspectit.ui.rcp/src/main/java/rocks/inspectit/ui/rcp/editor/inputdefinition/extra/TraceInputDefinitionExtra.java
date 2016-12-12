package rocks.inspectit.ui.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

/**
 * Input definition extra that provides the trace id.
 *
 * @author Ivan Senic
 *
 */
public class TraceInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * Trace id.
	 */
	private long traceId;

	/**
	 * Gets {@link #traceId}.
	 *
	 * @return {@link #traceId}
	 */
	public long getTraceId() {
		return this.traceId;
	}

	/**
	 * Sets {@link #traceId}.
	 *
	 * @param traceId
	 *            New value for {@link #traceId}
	 */
	public void setTraceId(long traceId) {
		this.traceId = traceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(traceId);
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

		TraceInputDefinitionExtra other = (TraceInputDefinitionExtra) obj;
		return Objects.equal(this.traceId, other.traceId);
	}
}
