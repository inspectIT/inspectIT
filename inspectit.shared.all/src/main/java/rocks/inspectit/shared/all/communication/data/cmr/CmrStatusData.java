package rocks.inspectit.shared.all.communication.data.cmr;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.externalservice.ExternalServiceStatus;
import rocks.inspectit.shared.all.externalservice.ExternalServiceType;

/**
 * Class that hold all information about a CMR status.
 *
 * @author Ivan Senic
 *
 */
public class CmrStatusData implements Serializable {

	/**
	 * generated UID.
	 */
	private static final long serialVersionUID = 126245907015200153L;

	/**
	 * Current buffer occupancy in bytes.
	 */
	private long currentBufferSize;

	/**
	 * Maximum buffer occupancy in bytes.
	 */
	private long maxBufferSize;

	/**
	 * Oldest element in buffer.
	 */
	private DefaultData bufferOldestElement;

	/**
	 * Newest element in the buffer.
	 */
	private DefaultData bufferNewestElement;

	/**
	 * Amount of bytes that has left for storage data.
	 */
	private long storageDataSpaceLeft;

	/**
	 * Amount of maximum bytes storage space that can be occupied.
	 */
	private long storageMaxDataSpace;

	/**
	 * If warning about low storage data space is active.
	 */
	private boolean warnSpaceLeftActive;

	/**
	 * If CMR has enough space to continue writing data.
	 */
	private boolean canWriteMore;

	/**
	 * Running time of CMR in millis.
	 */
	private long upTime;

	/**
	 * Date CMR was started.
	 */
	private Date dateStarted;

	/**
	 * Size of the database, <code>null</code> represents no information available.
	 */
	private Long databaseSize;

	/**
	 * The connection status of external services.
	 */
	private final Map<ExternalServiceType, ExternalServiceStatus> externalServiceStatusMap = new HashMap<ExternalServiceType, ExternalServiceStatus>(0);

	/**
	 * Gets {@link #externalServiceStatusMap}.
	 *
	 * @return {@link #externalServiceStatusMap}
	 */
	public Map<ExternalServiceType, ExternalServiceStatus> getExternalServiceStatusMap() {
		return this.externalServiceStatusMap;
	}

	/**
	 * Gets {@link #currentBufferSize}.
	 *
	 * @return {@link #currentBufferSize}
	 */
	public long getCurrentBufferSize() {
		return currentBufferSize;
	}

	/**
	 * Sets {@link #currentBufferSize}.
	 *
	 * @param currentBufferSize
	 *            New value for {@link #currentBufferSize}
	 */
	public void setCurrentBufferSize(long currentBufferSize) {
		this.currentBufferSize = currentBufferSize;
	}

	/**
	 * Gets {@link #maxBufferSize}.
	 *
	 * @return {@link #maxBufferSize}
	 */
	public long getMaxBufferSize() {
		return maxBufferSize;
	}

	/**
	 * Sets {@link #maxBufferSize}.
	 *
	 * @param maxBufferSize
	 *            New value for {@link #maxBufferSize}
	 */
	public void setMaxBufferSize(long maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	/**
	 * Gets {@link #bufferOldestElement}.
	 *
	 * @return {@link #bufferOldestElement}
	 */
	public DefaultData getBufferOldestElement() {
		return bufferOldestElement;
	}

	/**
	 * Sets {@link #bufferOldestElement}.
	 *
	 * @param bufferOldestElement
	 *            New value for {@link #bufferOldestElement}
	 */
	public void setBufferOldestElement(DefaultData bufferOldestElement) {
		this.bufferOldestElement = bufferOldestElement;
	}

	/**
	 * Gets {@link #bufferNewestElement}.
	 *
	 * @return {@link #bufferNewestElement}
	 */
	public DefaultData getBufferNewestElement() {
		return bufferNewestElement;
	}

	/**
	 * Sets {@link #bufferNewestElement}.
	 *
	 * @param bufferNewestElement
	 *            New value for {@link #bufferNewestElement}
	 */
	public void setBufferNewestElement(DefaultData bufferNewestElement) {
		this.bufferNewestElement = bufferNewestElement;
	}

	/**
	 * Gets {@link #storageDataSpaceLeft}.
	 *
	 * @return {@link #storageDataSpaceLeft}
	 */
	public long getStorageDataSpaceLeft() {
		return storageDataSpaceLeft;
	}

	/**
	 * Sets {@link #storageDataSpaceLeft}.
	 *
	 * @param storageDataSpaceLeft
	 *            New value for {@link #storageDataSpaceLeft}
	 */
	public void setStorageDataSpaceLeft(long storageDataSpaceLeft) {
		this.storageDataSpaceLeft = storageDataSpaceLeft;
	}

	/**
	 * Gets {@link #storageMaxDataSpace}.
	 *
	 * @return {@link #storageMaxDataSpace}
	 */
	public long getStorageMaxDataSpace() {
		return storageMaxDataSpace;
	}

	/**
	 * Sets {@link #storageMaxDataSpace}.
	 *
	 * @param storageMaxDataSpace
	 *            New value for {@link #storageMaxDataSpace}
	 */
	public void setStorageMaxDataSpace(long storageMaxDataSpace) {
		this.storageMaxDataSpace = storageMaxDataSpace;
	}

	/**
	 * Gets {@link #warnSpaceLeftActive}.
	 *
	 * @return {@link #warnSpaceLeftActive}
	 */
	public boolean isWarnSpaceLeftActive() {
		return warnSpaceLeftActive;
	}

	/**
	 * Sets {@link #warnSpaceLeftActive}.
	 *
	 * @param warnSpaceLeftActive
	 *            New value for {@link #warnSpaceLeftActive}
	 */
	public void setWarnSpaceLeftActive(boolean warnSpaceLeftActive) {
		this.warnSpaceLeftActive = warnSpaceLeftActive;
	}

	/**
	 * Gets {@link #canWriteMore}.
	 *
	 * @return {@link #canWriteMore}
	 */
	public boolean isCanWriteMore() {
		return canWriteMore;
	}

	/**
	 * Sets {@link #canWriteMore}.
	 *
	 * @param canWriteMore
	 *            New value for {@link #canWriteMore}
	 */
	public void setCanWriteMore(boolean canWriteMore) {
		this.canWriteMore = canWriteMore;
	}

	/**
	 * Gets {@link #upTime}.
	 *
	 * @return {@link #upTime}
	 */
	public long getUpTime() {
		return upTime;
	}

	/**
	 * Sets {@link #upTime}.
	 *
	 * @param upTime
	 *            New value for {@link #upTime}
	 */
	public void setUpTime(long upTime) {
		this.upTime = upTime;
	}

	/**
	 * Gets {@link #dateStarted}.
	 *
	 * @return {@link #dateStarted}
	 */
	public Date getDateStarted() {
		return dateStarted;
	}

	/**
	 * Sets {@link #dateStarted}.
	 *
	 * @param dateStarted
	 *            New value for {@link #dateStarted}
	 */
	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	/**
	 * Gets {@link #databaseSize}.
	 *
	 * @return {@link #databaseSize}
	 */
	public Long getDatabaseSize() {
		return databaseSize;
	}

	/**
	 * Sets {@link #databaseSize}.
	 *
	 * @param databaseSize
	 *            New value for {@link #databaseSize}
	 */
	public void setDatabaseSize(Long databaseSize) {
		this.databaseSize = databaseSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((bufferNewestElement == null) ? 0 : bufferNewestElement.hashCode());
		result = (prime * result) + ((bufferOldestElement == null) ? 0 : bufferOldestElement.hashCode());
		result = (prime * result) + (canWriteMore ? 1231 : 1237);
		result = (prime * result) + (int) (currentBufferSize ^ (currentBufferSize >>> 32));
		result = (prime * result) + ((databaseSize == null) ? 0 : databaseSize.hashCode());
		result = (prime * result) + ((dateStarted == null) ? 0 : dateStarted.hashCode());
		result = (prime * result) + (int) (maxBufferSize ^ (maxBufferSize >>> 32));
		result = (prime * result) + (int) (storageDataSpaceLeft ^ (storageDataSpaceLeft >>> 32));
		result = (prime * result) + (int) (storageMaxDataSpace ^ (storageMaxDataSpace >>> 32));
		result = (prime * result) + (int) (upTime ^ (upTime >>> 32));
		result = (prime * result) + (warnSpaceLeftActive ? 1231 : 1237);
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CmrStatusData other = (CmrStatusData) obj;
		if (bufferNewestElement == null) {
			if (other.bufferNewestElement != null) {
				return false;
			}
		} else if (!bufferNewestElement.equals(other.bufferNewestElement)) {
			return false;
		}
		if (bufferOldestElement == null) {
			if (other.bufferOldestElement != null) {
				return false;
			}
		} else if (!bufferOldestElement.equals(other.bufferOldestElement)) {
			return false;
		}
		if (canWriteMore != other.canWriteMore) {
			return false;
		}
		if (currentBufferSize != other.currentBufferSize) {
			return false;
		}
		if (databaseSize == null) {
			if (other.databaseSize != null) {
				return false;
			}
		} else if (!databaseSize.equals(other.databaseSize)) {
			return false;
		}
		if (dateStarted == null) {
			if (other.dateStarted != null) {
				return false;
			}
		} else if (!dateStarted.equals(other.dateStarted)) {
			return false;
		}
		if (maxBufferSize != other.maxBufferSize) {
			return false;
		}
		if (storageDataSpaceLeft != other.storageDataSpaceLeft) {
			return false;
		}
		if (storageMaxDataSpace != other.storageMaxDataSpace) {
			return false;
		}
		if (upTime != other.upTime) {
			return false;
		}
		if (warnSpaceLeftActive != other.warnSpaceLeftActive) {
			return false;
		}
		return true;
	}

}
