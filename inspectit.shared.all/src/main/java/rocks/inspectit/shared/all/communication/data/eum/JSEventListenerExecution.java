package rocks.inspectit.shared.all.communication.data.eum;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import rocks.inspectit.shared.all.tracing.constants.ExtraTags;

/**
 * Represents the execution of JS Event listener function.
 *
 * @author Jonas Kunz
 *
 */
public class JSEventListenerExecution extends JSFunctionExecution {

	/**
	 * the serial version UID.
	 */
	private static final long serialVersionUID = -3061447548235961509L;

	/**
	 * The type of the event which occurred, e.g. "click".
	 */
	@JsonProperty
	private String eventType;

	/**
	 * A flag indicating whether this event handler is executed synchronously to the event occurence
	 * or asynchronously.
	 */
	@JsonProperty
	@JsonInclude(value = Include.NON_DEFAULT)
	private boolean isAsyncEvent = true; // NOPMD

	/**
	 * Gets {@link #eventType}.
	 *
	 * @return {@link #eventType}
	 */
	public String getEventType() {
		return this.eventType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAsyncCall() {
		return isAsyncEvent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void collectTags(Map<String, String> tags) {
		super.collectTags(tags);

		StringBuilder name = new StringBuilder();
		name.append(eventType).append("-listener");
		String fn = getFunctionName();
		if (!fn.isEmpty()) {
			name.append(" (").append(fn).append(')');
		}
		tags.put(ExtraTags.OPERATION_NAME, name.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.eventType == null) ? 0 : this.eventType.hashCode());
		result = (prime * result) + (this.isAsyncEvent ? 1231 : 1237);
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
		JSEventListenerExecution other = (JSEventListenerExecution) obj;
		if (this.eventType == null) {
			if (other.eventType != null) {
				return false;
			}
		} else if (!this.eventType.equals(other.eventType)) {
			return false;
		}
		if (this.isAsyncEvent != other.isAsyncEvent) {
			return false;
		}
		return true;
	}
}
