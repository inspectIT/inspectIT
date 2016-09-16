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
		this.requests = new ArrayList<Request>();
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
		copy.add(pageLoadRequest);
		return copy;
	}
}
