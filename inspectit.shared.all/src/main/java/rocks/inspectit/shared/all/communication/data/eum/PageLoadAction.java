package rocks.inspectit.shared.all.communication.data.eum;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Pageloadaction which is an user action.
 *
 * @author David Monschein
 */
public class PageLoadAction extends UserAction {
	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = 3793073633735309262L;

	/**
	 * Belonging Pageload request with 1 to 1 relationship.
	 */
	private PageLoadRequest pageLoadRequest;

	/**
	 * List of all subrequests.
	 */
	private List<Request> requests;

	/**
	 * Creates a new page load action with no child requests.
	 */
	public PageLoadAction() {
		this.requests = new ArrayList<Request>(0);
	}

	/**
	 * Gets {@link #pageLoadRequest}.
	 *
	 * @return {@link #pageLoadRequest}
	 */
	public PageLoadRequest getPageLoadRequest() {
		return pageLoadRequest;
	}

	/**
	 * Sets {@link #pageLoadRequest}.
	 *
	 * @param pageLoadRequest
	 *            New value for {@link #pageLoadRequest}
	 */
	public void setPageLoadRequest(PageLoadRequest pageLoadRequest) {
		this.pageLoadRequest = pageLoadRequest;
	}

	/**
	 * Gets {@link #requests}.
	 *
	 * @return {@link #requests}
	 */
	public List<Request> getRequests() {
		return requests;
	}

	/**
	 * Sets {@link #requests}.
	 *
	 * @param requests
	 *            New value for {@link #requests}
	 */
	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}

	/**
	 * Adds a single request to this action.
	 *
	 * @param r
	 *            the request which should belong to this action.
	 */
	public void addRequest(Request r) {
		this.requests.add(r);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Request> getChildRequests() {
		List<Request> copy = new ArrayList<Request>(this.requests);
		if (pageLoadRequest != null) {
			copy.add(pageLoadRequest);
		}
		return copy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.pageLoadRequest == null) ? 0 : this.pageLoadRequest.hashCode());
		result = (prime * result) + ((this.requests == null) ? 0 : this.requests.hashCode());
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
		PageLoadAction other = (PageLoadAction) obj;
		if (this.pageLoadRequest == null) {
			if (other.pageLoadRequest != null) {
				return false;
			}
		} else if (!this.pageLoadRequest.equals(other.pageLoadRequest)) {
			return false;
		}
		if (this.requests == null) {
			if (other.requests != null) {
				return false;
			}
		} else if (!this.requests.equals(other.requests)) {
			return false;
		}
		return true;
	}

}
