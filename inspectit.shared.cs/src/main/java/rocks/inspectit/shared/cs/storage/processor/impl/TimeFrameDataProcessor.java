package rocks.inspectit.shared.cs.storage.processor.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.storage.processor.AbstractChainedDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;

/**
 * This processor serves as a time frame restriction processor. It will check if the time stamp of
 * the {@link DefaultData} to be processed is in the given time frame and if it is, processor will
 * pass the data to the other processor that are defined under.
 *
 * @author Ivan Senic
 *
 */
public class TimeFrameDataProcessor extends AbstractChainedDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -787983119053084482L;

	/**
	 * From date.
	 */
	private Date fromDate;

	/**
	 * To date.
	 */
	private Date toDate;

	/**
	 * No-arg constructor.
	 */
	public TimeFrameDataProcessor() {
		super(Collections.<AbstractDataProcessor> emptyList());
	}

	/**
	 * Default constructor.
	 *
	 * @param fromDate
	 *            From date.
	 * @param toDate
	 *            To date.
	 * @param dataProcessors
	 *            List of the processors this processor will pass the {@link DefaultData} its time
	 *            stamp is in the given time frame.
	 */
	public TimeFrameDataProcessor(Date fromDate, Date toDate, List<AbstractDataProcessor> dataProcessors) {
		super(dataProcessors);
		if ((null != fromDate) && (null != toDate) && fromDate.after(toDate)) {
			throw new IllegalArgumentException("Time frame not specified correctly. From date (" + fromDate + ") is after to date (" + toDate + ") value.");
		}
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean shouldBePassedToChainedProcessors(DefaultData defaultData) {
		return isInTimeframe(defaultData);
	}

	/**
	 * Gets {@link #fromDate}.
	 *
	 * @return {@link #fromDate}
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * Gets {@link #toDate}.
	 *
	 * @return {@link #toDate}
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * Is the default data's time stamp in the time frame of this processor.
	 *
	 * @param defaultData
	 *            {@link DefaultData} to check.
	 * @return true if it is in the time frame.
	 */
	private boolean isInTimeframe(DefaultData defaultData) {
		if (null != fromDate) {
			if (fromDate.after(defaultData.getTimeStamp())) {
				return false;
			}
		}
		if (null != toDate) {
			if (toDate.before(defaultData.getTimeStamp())) {
				return false;
			}
		}
		return true;
	}

}
