package rocks.inspectit.shared.all.communication.data.eum;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * JSDomEventListenerExecution objects represent the execution of JS listeners attached to DOM
 * elements, e.g. button clicks or other input events.
 *
 * @author Jonas Kunz
 *
 */
public class JSDomEvent extends AbstractEUMSpanDetails {

	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 6055013772645221629L;

	/**
	 * The name of the event which was captured, e.g. 'click'.
	 */
	@JsonProperty
	private String eventType;

	/**
	 * Stores the current URL of the main page when this event occurred.
	 */
	@JsonProperty
	private String baseUrl;

	/**
	 * True, if this Event became relevant because a {@link EumDomEventSelector} matched.
	 */
	@JsonProperty
	private boolean relevantThroughSelector;

	/**
	 * Information about the DOM element on which the event occurred. This information is extracted
	 * based on the selectors configured in the UI.
	 */
	@JsonProperty
	@JsonInclude(value = Include.NON_NULL)
	private Map<String, String> elementInfo;

	/**
	 * Gets {@link #eventType}.
	 *
	 * @return {@link #eventType}
	 */
	public String getEventType() {
		return this.eventType;
	}

	/**
	 * Gets {@link #relevantThroughSelector}.
	 *
	 * @return {@link #relevantThroughSelector}
	 */
	public boolean isRelevantThroughSelector() {
		return this.relevantThroughSelector;
	}

	/**
	 * Gets {@link #elementInfo}.
	 *
	 * @return {@link #elementInfo}
	 */
	public Map<String, String> getElementInfo() {
		return this.elementInfo;
	}

	/**
	 * Gets {@link #baseUrl}.
	 *
	 * @return {@link #baseUrl}
	 */
	public String getBaseUrl() {
		return this.baseUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAsyncCall() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExternalCall() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return PropagationType.JAVASCRIPT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void collectTags(Map<String, String> tags) {
		tags.put(ExtraTags.OPERATION_NAME, getReadableName());
		for (Map.Entry<String, String> info : elementInfo.entrySet()) {
			tags.put(ExtraTags.INSPECTT_DOM_ELEMENT_PREFIX + info.getKey(), info.getValue());
		}
	}

	/**
	 * @return a human-readable name showing details about this event
	 */
	private String getReadableName() {
		StringBuilder nameBuilder = new StringBuilder(eventType);
		nameBuilder.append(' ');
		if (elementInfo.containsKey("tagName")) {
			nameBuilder.append(elementInfo.get("tagName"));
		}
		if (elementInfo.containsKey("id")) {
			nameBuilder.append('#').append(elementInfo.get("id"));
		}
		String descriptionText = null;
		if (elementInfo.containsKey("text")) {
			descriptionText = elementInfo.get("text");
		} else if (elementInfo.containsKey("innerText")) {
			descriptionText = elementInfo.get("innerText");
		} else if (elementInfo.containsKey("$label")) {
			descriptionText = elementInfo.get("$label");
		}
		if (descriptionText != null) {
			nameBuilder.append(" '").append(descriptionText.trim()).append('\'');
		}
		return nameBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.baseUrl == null) ? 0 : this.baseUrl.hashCode());
		result = (prime * result) + ((this.elementInfo == null) ? 0 : this.elementInfo.hashCode());
		result = (prime * result) + ((this.eventType == null) ? 0 : this.eventType.hashCode());
		result = (prime * result) + (this.relevantThroughSelector ? 1231 : 1237);
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
		JSDomEvent other = (JSDomEvent) obj;
		if (this.baseUrl == null) {
			if (other.baseUrl != null) {
				return false;
			}
		} else if (!this.baseUrl.equals(other.baseUrl)) {
			return false;
		}
		if (this.elementInfo == null) {
			if (other.elementInfo != null) {
				return false;
			}
		} else if (!this.elementInfo.equals(other.elementInfo)) {
			return false;
		}
		if (this.eventType == null) {
			if (other.eventType != null) {
				return false;
			}
		} else if (!this.eventType.equals(other.eventType)) {
			return false;
		}
		if (this.relevantThroughSelector != other.relevantThroughSelector) {
			return false;
		}
		return true;
	}



}
