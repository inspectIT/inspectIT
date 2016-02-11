package info.novatec.inspectit.storage.recording;

import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class for holding the recording properties.
 * 
 * @author Ivan Senic
 * 
 */
public class RecordingProperties implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8838581567710175793L;

	/**
	 * Date when the recording started.
	 */
	private Date recordStartDate;

	/**
	 * Date when the recording should be stopped.
	 */
	private Date recordEndDate;

	/**
	 * Collection of recording processors. if supplied this processors will be used to process
	 * recording data.
	 */
	private Collection<AbstractDataProcessor> recordingDataProcessors;

	/**
	 * Start delay of recording in milliseconds.
	 */
	private long startDelay;

	/**
	 * Represents time in milliseconds of how long the recording should last.
	 */
	private long recordDuration;

	/**
	 * If storage should be auto-finalized after the recording.
	 */
	private boolean autoFinalize;

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

	/**
	 * Gets {@link #recordingDataProcessors}.
	 * 
	 * @return {@link #recordingDataProcessors}
	 */
	public Collection<AbstractDataProcessor> getRecordingDataProcessors() {
		return recordingDataProcessors;
	}

	/**
	 * Sets {@link #recordingDataProcessors}.
	 * 
	 * @param recordingDataProcessors
	 *            New value for {@link #recordingDataProcessors}
	 */
	public void setRecordingDataProcessors(Collection<AbstractDataProcessor> recordingDataProcessors) {
		this.recordingDataProcessors = recordingDataProcessors;
	}

	/**
	 * Gets {@link #startDelay}.
	 * 
	 * @return {@link #startDelay}
	 */
	public long getStartDelay() {
		return startDelay;
	}

	/**
	 * Sets {@link #startDelay}.
	 * 
	 * @param startDelay
	 *            New value for {@link #startDelay}
	 */
	public void setStartDelay(long startDelay) {
		this.startDelay = startDelay;
	}

	/**
	 * Gets {@link #recordDuration}.
	 * 
	 * @return {@link #recordDuration}
	 */
	public long getRecordDuration() {
		return recordDuration;
	}

	/**
	 * Sets {@link #recordDuration}.
	 * 
	 * @param recordDuration
	 *            New value for {@link #recordDuration}
	 */
	public void setRecordDuration(long recordDuration) {
		this.recordDuration = recordDuration;
	}

	/**
	 * Gets {@link #autoFinalize}.
	 * 
	 * @return {@link #autoFinalize}
	 */
	public boolean isAutoFinalize() {
		return autoFinalize;
	}

	/**
	 * Sets {@link #autoFinalize}.
	 * 
	 * @param autoFinalize
	 *            New value for {@link #autoFinalize}
	 */
	public void setAutoFinalize(boolean autoFinalize) {
		this.autoFinalize = autoFinalize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("recordStartDate", recordStartDate);
		toStringBuilder.append("recordEndDate", recordEndDate);
		toStringBuilder.append("recordingDataProcessors", recordingDataProcessors);
		toStringBuilder.append("startDelay", startDelay);
		toStringBuilder.append("autoFinalize", autoFinalize);
		return toStringBuilder.toString();
	}

}
