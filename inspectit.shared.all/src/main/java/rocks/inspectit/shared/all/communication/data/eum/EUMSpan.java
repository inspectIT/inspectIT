package rocks.inspectit.shared.all.communication.data.eum;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.opentracing.References;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;

/**
 * A span element which ccured at the End User's browser.
 *
 * @author Jonas Kunz
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = EumSpanDeserializer.class)
public class EUMSpan extends AbstractSpan implements EUMBeaconElement {

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = 6684931260549760417L;

	/**
	 * Stores further information about this trace element. One-to-One relationship.
	 */
	private AbstractEUMSpanDetails details;

	/**
	 * The session ID identifying the user session.
	 */
	private long sessionId;

	/**
	 * The ID of the window within the session.
	 */
	private long tabId;

	/**
	 * Stores which agent modules were active when this element was collected.
	 */
	private String activeAgentModules;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultData asDefaultData() {
		return this;
	}

	/**
	 * Sets teh details object. Should only be called once!
	 *
	 * @param eumDetails
	 *            the details.
	 */
	void setDetails(AbstractEUMSpanDetails eumDetails) {
		this.details = eumDetails;
		eumDetails.setOwningSpan(this);

		if (eumDetails.isAsyncCall() && !getSpanIdent().isRoot()) {
			setReferenceType(References.FOLLOWS_FROM);
		} else {
			setReferenceType(References.CHILD_OF);
		}

		this.setPropagationType(eumDetails.getPropagationType());

	}

	/**
	 * @return the details about this EUM span
	 */
	public AbstractEUMSpanDetails getDetails() {
		return details;
	}

	/**
	 * Gets {@link #sessionId}.
	 *
	 * @return {@link #sessionId}
	 */
	public long getSessionId() {
		return this.sessionId;
	}

	/**
	 * Gets {@link #tabId}.
	 *
	 * @return {@link #tabId}
	 */
	public long getTabId() {
		return this.tabId;
	}

	/**
	 * Gets {@link #activeAgentModules}.
	 *
	 * @return {@link #activeAgentModules}
	 */
	public String getActiveAgentModules() {
		return this.activeAgentModules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCaller() {
		return details.isExternalCall();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deserializationComplete(Beacon beacon) {
		sessionId = beacon.getSessionID();
		tabId = beacon.getTabID();
		activeAgentModules = beacon.getActiveAgentModules();

		details.deserializationComplete(beacon);

		Map<String, String> allTags = new HashMap<String, String>();
		details.collectTags(allTags);
		this.addAllTags(allTags);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.activeAgentModules == null) ? 0 : this.activeAgentModules.hashCode());
		result = (prime * result) + ((this.details == null) ? 0 : this.details.hashCode());
		result = (prime * result) + (int) (this.sessionId ^ (this.sessionId >>> 32));
		result = (prime * result) + (int) (this.tabId ^ (this.tabId >>> 32));
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
		EUMSpan other = (EUMSpan) obj;
		if (this.activeAgentModules == null) {
			if (other.activeAgentModules != null) {
				return false;
			}
		} else if (!this.activeAgentModules.equals(other.activeAgentModules)) {
			return false;
		}
		if (this.details == null) {
			if (other.details != null) {
				return false;
			}
		} else if (!this.details.equals(other.details)) {
			return false;
		}
		if (this.sessionId != other.sessionId) {
			return false;
		}
		if (this.tabId != other.tabId) {
			return false;
		}
		return true;
	}

}
