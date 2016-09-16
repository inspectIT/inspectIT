/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

import java.util.List;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Represents an user action. This is either a Page load request or a click action.
 *
 * @author David Monschein
 */
public abstract class UserAction extends DefaultData {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Contains the belonging user session.
	 */
	private UserSession userSession;

	/**
	 * Contains the base URL of the user action.
	 */
	private String baseUrl;

	/**
	 * Gets {@link #userSession}.
	 *
	 * @return {@link #userSession}
	 */
	public UserSession getUserSession() {
		return userSession;
	}

	/**
	 * Sets {@link #userSession}.
	 *
	 * @param userSession
	 *            New value for {@link #userSession}
	 */
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	/**
	 * Gets all child requests which correspond to this user action.
	 *
	 * @return child requests triggered by this user action.
	 */
	public abstract List<Request> getChildRequests();

	/**
	 * Gets {@link #baseUrl}.
	 *
	 * @return {@link #baseUrl}
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Sets {@link #baseUrl}.
	 *
	 * @param baseUrl
	 *            New value for {@link #baseUrl}
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

}
