package rocks.inspectit.shared.all.communication.data.eum;

/**
 * Request which contains informations about a single resource load. (e.g. CSS file)
 *
 * @author David Monschein
 */
public class ResourceLoadRequest extends Request {

	/**
	 * serial Version UID.
	 */
	private static final long serialVersionUID = 583794863578163599L;

	/**
	 * Start time of the resource load request.
	 */
	private double startTime;

	/**
	 * End time of the resource load request.
	 */
	private double endTime;

	/**
	 * Determines from what the resource loading got triggered.
	 */
	private String initiatorType;

	/**
	 * The size in octets of the resource.
	 */
	private long transferSize;

	/**
	 * The URL which triggered the resource load.
	 */
	private String initiatorUrl;

	/**
	 * Gets {@link #startTime}.
	 *
	 * @return {@link #startTime}
	 */
	public double getStartTime() {
		return this.startTime;
	}

	/**
	 * Sets {@link #startTime}.
	 *
	 * @param startTime
	 *            New value for {@link #startTime}
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets {@link #endTime}.
	 *
	 * @return {@link #endTime}
	 */
	public double getEndTime() {
		return this.endTime;
	}

	/**
	 * Sets {@link #endTime}.
	 *
	 * @param endTime
	 *            New value for {@link #endTime}
	 */
	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets {@link #initiatorType}.
	 *
	 * @return {@link #initiatorType}
	 */
	public String getInitiatorType() {
		return this.initiatorType;
	}

	/**
	 * Sets {@link #initiatorType}.
	 *
	 * @param initiatorType
	 *            New value for {@link #initiatorType}
	 */
	public void setInitiatorType(String initiatorType) {
		this.initiatorType = initiatorType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestType getRequestType() {
		return RequestType.RESOURCELOAD;
	}

	/**
	 * Gets {@link #transferSize}.
	 *
	 * @return {@link #transferSize}
	 */
	public long getTransferSize() {
		return transferSize;
	}

	/**
	 * Sets {@link #transferSize}.
	 *
	 * @param transferSize
	 *            New value for {@link #transferSize}
	 */
	public void setTransferSize(long transferSize) {
		this.transferSize = transferSize;
	}

	/**
	 * Gets {@link #initiatorUrl}.
	 *
	 * @return {@link #initiatorUrl}
	 */
	public String getInitiatorUrl() {
		return initiatorUrl;
	}

	/**
	 * Sets {@link #initiatorUrl}.
	 *
	 * @param initiatorUrl
	 *            New value for {@link #initiatorUrl}
	 */
	public void setInitiatorUrl(String initiatorUrl) {
		this.initiatorUrl = initiatorUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(this.endTime);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((this.initiatorType == null) ? 0 : this.initiatorType.hashCode());
		result = (prime * result) + ((this.initiatorUrl == null) ? 0 : this.initiatorUrl.hashCode());
		temp = Double.doubleToLongBits(this.startTime);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
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
		if (Double.doubleToLongBits(this.endTime) != Double.doubleToLongBits(other.endTime)) {
			return false;
		}
		if (this.initiatorType == null) {
			if (other.initiatorType != null) {
				return false;
			}
		} else if (!this.initiatorType.equals(other.initiatorType)) {
			return false;
		}
		if (this.initiatorUrl == null) {
			if (other.initiatorUrl != null) {
				return false;
			}
		} else if (!this.initiatorUrl.equals(other.initiatorUrl)) {
			return false;
		}
		if (Double.doubleToLongBits(this.startTime) != Double.doubleToLongBits(other.startTime)) {
			return false;
		}
		if (this.transferSize != other.transferSize) {
			return false;
		}
		return true;
	}

}
