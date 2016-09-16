package rocks.inspectit.shared.all.communication.data.eum;

/**
 * Representing an AJAX request.
 *
 * @author David Monschein
 */
public class AjaxRequest extends Request {

	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = -2318566427302336923L;

	/**
	 * Start time of the Ajax request.
	 */
	private long startTime;

	/**
	 * End time of the Ajax request.
	 */
	private long endTime;

	/**
	 * Status with which the Ajax request was completed. (e.g. 200 for successful)
	 */
	private int status;

	/**
	 * Method which was used to send the Ajax request (e.g. GET or POST).
	 */
	private String method;

	/**
	 * The base URL of the ajax request.
	 */
	private String baseUrl;

	/**
	 * Gets {@link #startTime}.
	 *
	 * @return {@link #startTime}
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Sets {@link #startTime}.
	 *
	 * @param startTime
	 *            New value for {@link #startTime}
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets {@link #endTime}.
	 *
	 * @return {@link #endTime}
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Sets {@link #endTime}.
	 *
	 * @param endTime
	 *            New value for {@link #endTime}
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets {@link #method}.
	 *
	 * @return {@link #method}
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets {@link #method}.
	 *
	 * @param method
	 *            New value for {@link #method}
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestType getRequestType() {
		return RequestType.AJAX;
	}

	/**
	 * Gets {@link #status}.
	 *
	 * @return {@link #status}
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets {@link #status}.
	 *
	 * @param status
	 *            New value for {@link #status}
	 */
	public void setStatus(int status) {
		this.status = status;
	}

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
		AjaxRequest other = (AjaxRequest) obj;
		if (this.baseUrl == null) {
			if (other.baseUrl != null) {
				return false;
			}
		} else if (!this.baseUrl.equals(other.baseUrl)) {
			return false;
		}
		if (this.endTime != other.endTime) {
			return false;
		}
		if (this.method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!this.method.equals(other.method)) {
			return false;
		}
		if (this.startTime != other.startTime) {
			return false;
		}
		return this.status == other.status;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.baseUrl == null) ? 0 : this.baseUrl.hashCode());
		result = (prime * result) + (int) (this.endTime ^ (this.endTime >>> 32));
		result = (prime * result) + ((this.method == null) ? 0 : this.method.hashCode());
		result = (prime * result) + (int) (this.startTime ^ (this.startTime >>> 32));
		result = (prime * result) + this.status;
		return result;
	}
}
