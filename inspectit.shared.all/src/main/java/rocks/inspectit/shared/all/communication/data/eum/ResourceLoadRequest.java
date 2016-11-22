package rocks.inspectit.shared.all.communication.data.eum;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request which contains informations about a single resource load. (e.g. CSS file)
 *
 * @author David Monschein, Jonas Kunz
 */
public class ResourceLoadRequest extends AbstractRequest {

	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = 583794863578163599L;

	/**
	 * Determines from what the resource loading got triggered.
	 */
	@JsonProperty
	private String initiatorType;

	/**
	 * The size in octets of the resource.
	 */
	@JsonProperty
	private long transferSize;

	/**
	 * Stores the current URL of the main page when the request was recorded.
	 */
	@JsonProperty
	private String baseUrl;

	/**
	 * Gets {@link #initiatorType}.
	 *
	 * @return {@link #initiatorType}
	 */
	public String getInitiatorType() {
		return this.initiatorType;
	}

	/**
	 * Gets {@link #transferSize}.
	 *
	 * @return {@link #transferSize}
	 */
	public long getTransferSize() {
		return this.transferSize;
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
	public boolean isAsyncCall() {
		return true;
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.baseUrl == null) ? 0 : this.baseUrl.hashCode());
		result = (prime * result) + ((this.initiatorType == null) ? 0 : this.initiatorType.hashCode());
		result = (prime * result) + (int) (this.transferSize ^ (this.transferSize >>> 32));
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
		ResourceLoadRequest other = (ResourceLoadRequest) obj;
		if (this.baseUrl == null) {
			if (other.baseUrl != null) {
				return false;
			}
		} else if (!this.baseUrl.equals(other.baseUrl)) {
			return false;
		}
		if (this.initiatorType == null) {
			if (other.initiatorType != null) {
				return false;
			}
		} else if (!this.initiatorType.equals(other.initiatorType)) {
			return false;
		}
		if (this.transferSize != other.transferSize) {
			return false;
		}
		return true;
	}

}
