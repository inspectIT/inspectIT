package rocks.inspectit.shared.all.communication.data.eum;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.opentracing.tag.Tags;

/**
 * EUM element representing an AJAX request.
 *
 * @author David Monschein, Jonas Kunz
 */
public class AjaxRequest extends AbstractRequest {

	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = -2318566427302336923L;

	/**
	 * Status with which the Ajax request was completed. (e.g. 200 for successful)
	 */
	@JsonProperty
	private int status;

	/**
	 * Method which was used to send the Ajax request (e.g. GET or POST).
	 */
	@JsonProperty
	private String method;

	/**
	 * A flag indicating whether this request was a non-blocking one, meaning that it was issued
	 * asynchronously.
	 */
	@JsonProperty
	private boolean isAsync; // NOPMD

	/**
	 * Stores the current URL of the main page when the request was recorded.
	 */
	@JsonProperty
	private String baseUrl;

	/**
	 * Gets {@link #status}.
	 *
	 * @return {@link #status}
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Gets {@link #method}.
	 *
	 * @return {@link #method}
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAsyncCall() {
		return isAsync;
	}

	/**
	 * Gets {@link #baseUrl}.
	 *
	 * @return {@link #baseUrl}
	 */
	public String getBaseUrl() {
		return this.baseUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deserializationComplete(Beacon beacon) {
		super.resolveRelativeUrl(baseUrl);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void collectTags(Map<String, String> tags) {
		super.collectTags(tags);
		tags.put(Tags.HTTP_STATUS.getKey(), String.valueOf(status));
		tags.put(Tags.HTTP_METHOD.getKey(), method);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.baseUrl == null) ? 0 : this.baseUrl.hashCode());
		result = (prime * result) + (this.isAsync ? 1231 : 1237);
		result = (prime * result) + ((this.method == null) ? 0 : this.method.hashCode());
		result = (prime * result) + this.status;
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
		AjaxRequest other = (AjaxRequest) obj;
		if (this.baseUrl == null) {
			if (other.baseUrl != null) {
				return false;
			}
		} else if (!this.baseUrl.equals(other.baseUrl)) {
			return false;
		}
		if (this.isAsync != other.isAsync) {
			return false;
		}
		if (this.method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!this.method.equals(other.method)) {
			return false;
		}
		if (this.status != other.status) {
			return false;
		}
		return true;
	}

}
