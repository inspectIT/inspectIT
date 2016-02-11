package info.novatec.inspectit.communication.data.cmr;

import info.novatec.inspectit.storage.StorageData;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This POJO joins several recording information data.
 * 
 * @author Ivan Senic
 * 
 */
public class RecordingData implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -4409533435016692576L;

	/**
	 * {@link WritingStatus} of the recording storage writer.
	 */
	private WritingStatus recordingWritingStatus;

	/**
	 * Storage that is used for recording.
	 */
	private StorageData recordingStorage;

	/**
	 * Date when the recording started.
	 */
	private Date recordStartDate;

	/**
	 * Date when the recording should be stopped.
	 */
	private Date recordEndDate;

	/**
	 * No-arguments constructor.
	 */
	public RecordingData() {
	}

	/**
	 * @param recordingWritingStatus
	 *            Sets the {@link #recordingWritingStatus}.
	 * @param recordingStorage
	 *            Sets the {@link #recordingStorage}.
	 * @param recordStartDate
	 *            Date when the recording started.
	 * @param recordEndDate
	 *            Date when the recording should be stopped.
	 */
	public RecordingData(WritingStatus recordingWritingStatus, StorageData recordingStorage, Date recordStartDate, Date recordEndDate) {
		super();
		this.recordingWritingStatus = recordingWritingStatus;
		this.recordingStorage = recordingStorage;
		this.recordStartDate = recordStartDate;
		this.recordEndDate = recordEndDate;
	}

	/**
	 * Gets {@link #recordingWritingStatus}.
	 * 
	 * @return {@link #recordingWritingStatus}
	 */
	public WritingStatus getRecordingWritingStatus() {
		return recordingWritingStatus;
	}

	/**
	 * Sets {@link #recordingWritingStatus}.
	 * 
	 * @param recordingWritingStatus
	 *            New value for {@link #recordingWritingStatus}
	 */
	public void setRecordingWritingStatus(WritingStatus recordingWritingStatus) {
		this.recordingWritingStatus = recordingWritingStatus;
	}

	/**
	 * Gets {@link #recordingStorage}.
	 * 
	 * @return {@link #recordingStorage}
	 */
	public StorageData getRecordingStorage() {
		return recordingStorage;
	}

	/**
	 * Sets {@link #recordingStorage}.
	 * 
	 * @param recordingStorage
	 *            New value for {@link #recordingStorage}
	 */
	public void setRecordingStorage(StorageData recordingStorage) {
		this.recordingStorage = recordingStorage;
	}

	/**
	 * Gets {@link #recordStartDate}.
	 * 
	 * @return {@link #recordStartDate}
	 */
	public Date getRecordStartDate() {
		return recordStartDate;
	}

	/**
	 * Sets {@link #recordStartDate}.
	 * 
	 * @param recordStartDate
	 *            New value for {@link #recordStartDate}
	 */
	public void setRecordStartDate(Date recordStartDate) {
		this.recordStartDate = recordStartDate;
	}

	/**
	 * Gets {@link #recordEndDate}.
	 * 
	 * @return {@link #recordEndDate}
	 */
	public Date getRecordEndDate() {
		return recordEndDate;
	}

	/**
	 * Sets {@link #recordEndDate}.
	 * 
	 * @param recordEndDate
	 *            New value for {@link #recordEndDate}
	 */
	public void setRecordEndDate(Date recordEndDate) {
		this.recordEndDate = recordEndDate;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(recordEndDate);
		hashCodeBuilder.append(recordStartDate);
		hashCodeBuilder.append(recordingStorage);
		hashCodeBuilder.append(recordingWritingStatus);
		return hashCodeBuilder.toHashCode();
	}

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
		RecordingData other = (RecordingData) obj;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(recordEndDate, other.recordEndDate);
		equalsBuilder.append(recordStartDate, other.recordStartDate);
		equalsBuilder.append(recordingStorage, other.recordingStorage);
		equalsBuilder.append(recordingWritingStatus, other.recordingWritingStatus);
		return equalsBuilder.isEquals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("recordingWritingStatus", recordingWritingStatus);
		toStringBuilder.append("recordingStorage", recordingStorage);
		toStringBuilder.append("recordStartDate", recordStartDate);
		toStringBuilder.append("recordEndDate", recordEndDate);
		return toStringBuilder.toString();
	}

}
