package rocks.inspectit.shared.all.communication.data.eum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Stores information about an user session. This element will be sent for every new tab openend,
 * however as the information is consistent across tabs duplciate userSessionInfos received can be
 * safely ignored.
 *
 * @author David Monschein, Jonas Kunz
 */
@JsonIgnoreProperties({ "type", "id" })
public class UserSessionInfo extends DefaultData implements EUMBeaconElement {

	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = 2607499843063635013L;

	/**
	 * The session ID.
	 */
	@JsonIgnore
	private long sessionId;

	/**
	 * The browser name.
	 */
	@JsonProperty
	private String browser;

	/**
	 * The device name.
	 */
	@JsonProperty
	private String device;

	/**
	 * The browser language.
	 */
	@JsonProperty
	private String language;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultData asDefaultData() {
		return this;
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
	 * Gets {@link #browser}.
	 *
	 * @return {@link #browser}
	 */
	public String getBrowser() {
		return this.browser;
	}

	/**
	 * Gets {@link #device}.
	 *
	 * @return {@link #device}
	 */
	public String getDevice() {
		return this.device;
	}

	/**
	 * Gets {@link #language}.
	 *
	 * @return {@link #language}
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deserializationComplete(Beacon beacon) {
		this.sessionId = beacon.getSessionID();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.browser == null) ? 0 : this.browser.hashCode());
		result = (prime * result) + ((this.device == null) ? 0 : this.device.hashCode());
		result = (prime * result) + ((this.language == null) ? 0 : this.language.hashCode());
		result = (prime * result) + (int) (this.sessionId ^ (this.sessionId >>> 32));
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
		UserSessionInfo other = (UserSessionInfo) obj;
		if (this.browser == null) {
			if (other.browser != null) {
				return false;
			}
		} else if (!this.browser.equals(other.browser)) {
			return false;
		}
		if (this.device == null) {
			if (other.device != null) {
				return false;
			}
		} else if (!this.device.equals(other.device)) {
			return false;
		}
		if (this.language == null) {
			if (other.language != null) {
				return false;
			}
		} else if (!this.language.equals(other.language)) {
			return false;
		}
		if (this.sessionId != other.sessionId) {
			return false;
		}
		return true;
	}

}
