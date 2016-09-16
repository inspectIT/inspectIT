package rocks.inspectit.shared.all.communication.data.eum;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Containing informations about an user session. A session should be unique for every user.
 *
 * @author David Monschein
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSessionInfo extends AbstractEUMData {

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
	 * Creates a new user session containing no information about the user.
	 */
	public UserSessionInfo() {
	}

	/**
	 * Creates a new user session with all information about the user initialized.
	 *
	 * @param browser
	 *            the browser which is used
	 * @param device
	 *            the device of the user
	 * @param lang
	 *            the language of he users browser
	 * @param id
	 *            an unique id representing this user
	 */
	public UserSessionInfo(String browser, String device, String lang, String id) {
		super(id);
		this.browser = browser;
		this.device = device;
		this.language = lang;
	}

	/**
	 * Gets {@link #browser}.
	 *
	 * @return {@link #browser}
	 */
	public String getBrowser() {
		return browser;
	}

	/**
	 * Sets {@link #browser}.
	 *
	 * @param browser
	 *            New value for {@link #browser}
	 */
	public void setBrowser(String browser) {
		this.browser = browser;
	}

	/**
	 * Gets {@link #device}.
	 *
	 * @return {@link #device}
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * Sets {@link #device}.
	 *
	 * @param device
	 *            New value for {@link #device}
	 */
	public void setDevice(String device) {
		this.device = device;
	}

	/**
	 * Gets {@link #language}.
	 *
	 * @return {@link #language}
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets {@link #language}.
	 *
	 * @param language
	 *            New value for {@link #language}
	 */
	public void setLanguage(String language) {
		this.language = language;
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
