/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Containing informations about an user session. A session should be unique for every user.
 *
 * @author David Monschein
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSession extends DefaultData {

	/**
	 *
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
	 * The session id.
	 */
	private String sessionId;

	/**
	 * All useractions which belong to a certain session.
	 */
	private List<UserAction> userActions;

	/**
	 * Creates a new user session containing no information about the user.
	 */
	public UserSession() {
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
	public UserSession(String browser, String device, String lang, String id) {
		this.browser = browser;
		this.device = device;
		this.language = lang;
		this.sessionId = id;

		this.userActions = new ArrayList<UserAction>();
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
	 * Gets {@link #sessionId}.
	 *
	 * @return {@link #sessionId}
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets {@link #sessionId}.
	 *
	 * @param sessionId
	 *            New value for {@link #sessionId}
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Gets {@link #userActions}.
	 *
	 * @return {@link #userActions}
	 */
	public List<UserAction> getUserActions() {
		return userActions;
	}

	/**
	 * Sets {@link #userActions}.
	 *
	 * @param userActions
	 *            New value for {@link #userActions}
	 */
	public void setUserActions(List<UserAction> userActions) {
		this.userActions = userActions;
	}

	/**
	 * Adds a single user action to this session.
	 *
	 * @param action
	 *            the user action which should be added to this user session.
	 */
	public void addUserAction(UserAction action) {
		this.userActions.add(action);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other instanceof UserSession) {
			return ((UserSession) other).getSessionId().equals(this.sessionId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return sessionId.hashCode();
	}

}
