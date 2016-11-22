package rocks.inspectit.shared.all.communication.data.eum;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Stores information about an user session. This element will be sent for every new tab openend,
 * however as the ifnormation is consistent across tabs duplciate userSessionInfos received can be
 * safely ignored.
 *
 * @author David Monschein, Jonas Kunz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSessionInfo extends AbstractEUMElement {

	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = 2607499843063635013L;

	/**
	 * The browser name.
	 */
	private String browser;

	/**
	 * The device name.
	 */
	private String device;

	/**
	 * The browser language.
	 */
	private String language;

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

	@Override
	@JsonIgnore
	public void setLocalID(long localId) {
		// ignores calls as a UserSessionInfo does not really have a local id.
	}

	@Override
	@JsonIgnore
	public void setTabID(long sessionId) {
		// ignores calls as a UserSessionInfo does not really have a tab id.
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
		return true;
	}

}
