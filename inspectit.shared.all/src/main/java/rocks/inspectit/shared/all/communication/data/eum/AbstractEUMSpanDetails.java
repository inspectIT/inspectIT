package rocks.inspectit.shared.all.communication.data.eum;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Adds details about the type and type specific data to {@link EUMSpan}s.
 *
 * @author Jonas Kunz
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = false)
@JsonSubTypes({ @Type(name = "pageLoadRequest", value = PageLoadRequest.class), @Type(name = "resourceLoadRequest", value = ResourceLoadRequest.class),
	@Type(name = "ajaxRequest", value = AjaxRequest.class), @Type(name = "timerExecution", value = JSTimerExecution.class),
	@Type(name = "listenerExecution", value = JSEventListenerExecution.class), @Type(name = "domEvent", value = JSDomEvent.class) })
// ignore properties which are handled by EumSpan already
@JsonIgnoreProperties({ "id", "parentId", "traceId", "enterTimestamp", "duration", "executionOrderIndex" })
public abstract class AbstractEUMSpanDetails extends DefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 2624170383673368019L;

	/**
	 * Reference to the owning span.
	 */
	@JsonIgnore
	private transient EUMSpan owner;

	/**
	 * @return true, if this call was asynchronous (non-blocking).
	 */
	public abstract boolean isAsyncCall();

	/**
	 * @return true, if this call left the browser.
	 */
	public abstract boolean isExternalCall();

	/**
	 * Invoked by the owning span to collect all information in form of tags.
	 *
	 * @param tags
	 *            the map to add the tags to.
	 */
	public abstract void collectTags(Map<String, String> tags);

	/**
	 * @return the propagation type of this span
	 */
	public abstract PropagationType getPropagationType();

	/**
	 * Called when the deserialization has been completed in order to clone data from the beacon,
	 * For example the active modules or the session and tab IDs.
	 *
	 * @param beacon
	 *            the beacon to which this details object belongs
	 */
	public void deserializationComplete(Beacon beacon) {
		// Nothing to do here, but subtypes are free to override this method
	}

	/**
	 * Gets {@link #owner}.
	 *
	 * @return {@link #owner}
	 */
	@JsonIgnore
	public EUMSpan getOwningSpan() {
		return this.owner;
	}

	/**
	 * Sets {@link #owner}.
	 *
	 * @param owner
	 *            New value for {@link #owner}
	 */
	@JsonIgnore
	public void setOwningSpan(EUMSpan owner) {
		this.owner = owner;
	}

}
