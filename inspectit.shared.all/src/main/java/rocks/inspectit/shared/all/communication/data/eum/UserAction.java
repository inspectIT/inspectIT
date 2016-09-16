package rocks.inspectit.shared.all.communication.data.eum;

import java.util.List;

/**
 * Represents an user action. This is either a Page load request or a click action.
 *
 * @author David Monschein
 */
public abstract class UserAction extends AbstractEUMData {

	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Contains the base URL of the user action.
	 */
	private String baseUrl;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.baseUrl == null) ? 0 : this.baseUrl.hashCode());
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
		UserAction other = (UserAction) obj;
		if (this.baseUrl == null) {
			if (other.baseUrl != null) {
				return false;
			}
		} else if (!this.baseUrl.equals(other.baseUrl)) {
			return false;
		}
		return true;
	}

}
