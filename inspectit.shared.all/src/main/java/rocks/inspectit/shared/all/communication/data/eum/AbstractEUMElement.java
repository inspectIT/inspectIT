package rocks.inspectit.shared.all.communication.data.eum;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import rocks.inspectit.shared.all.communication.DefaultData;

// TODO: Implement the Sizeable interface correctly.

/**
 * Base class for all types of EUM Data which can be contained in an EUM beacon. Elements are
 * identifiable by a globally unique {@link EUMElementID}.
 *
 *
 * @author Jonas Kunz
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@Type(name = "pageLoadAction", value = PageLoadAction.class),
	@Type(name = "pageLoadRequest", value = PageLoadRequest.class),
	@Type(name = "resourceLoadRequest", value = ResourceLoadRequest.class),
	@Type(name = "ajaxRequest", value = AjaxRequest.class),
	@Type(name = "metaInfo", value = UserSessionInfo.class),
	@Type(name = "timerExecution", value = JSTimerExecution.class),
	@Type(name = "listenerExecution", value = JSEventListenerExecution.class),
	@Type(name = "domListenerExecution", value = JSDomEventListenerExecution.class)
})
public class AbstractEUMElement extends DefaultData implements Serializable {

	/**
	 * Serial version UUID.
	 */
	private static final long serialVersionUID = -9028143779219068642L;

	/**
	 * The ID globally unique ID of this element.
	 */
	@JsonIgnore
	private EUMElementID id;

	/**
	 * Default constructs, assigns a null-id to this object.
	 */
	public AbstractEUMElement() {
		id = new EUMElementID();
	}

	/**
	 * Sets the local ID part of the ID of this element. The local ID is uniquely identifies this
	 * elment within a tab.
	 *
	 * @param localId
	 *            the localID to set
	 */
	@JsonProperty(value = "id")
	public void setLocalID(long localId) {
		this.id.setLocalID(localId);
	}

	/**
	 * Sets the identifier of the tab within which this element was generated.
	 *
	 * @param tabId
	 *            the tabID of the tab within which this element was generated.
	 */
	@JsonIgnore
	public void setTabID(long tabId) {
		this.id.setTabID(tabId);
	}

	/**
	 * Sets the identifier of the session within which this element was generated.
	 *
	 * @param sessionId
	 *            the id of the session
	 */
	@JsonIgnore
	public void setSessionID(long sessionId) {
		this.id.setSessionID(sessionId);
	}

	/**
	 * Returns the complete ID of this element.
	 *
	 * @return the {@link EUMElementID} of this element.
	 */
	@JsonIgnore
	public EUMElementID getID() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
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
		AbstractEUMElement other = (AbstractEUMElement) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
