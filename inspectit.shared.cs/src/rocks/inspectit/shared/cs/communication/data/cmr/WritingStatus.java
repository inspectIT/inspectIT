package info.novatec.inspectit.communication.data.cmr;

/**
 * Status of the writing.
 * 
 * @author Ivan Senic
 * 
 */
public enum WritingStatus {

	/**
	 * Writing is going on smoothly.
	 */
	GOOD,

	/**
	 * Writing is having a small lack.
	 */
	MEDIUM,

	/**
	 * Writing can not manage to write all data received.
	 */
	BAD;

	/**
	 * Number in percentage until what the good status is active.
	 * 
	 * @see #getWritingStatus(long, long)
	 */
	private static final double GOOD_STATUS_END = 0.1;

	/**
	 * Number in percentage until what the medium status is active.
	 * 
	 * @see #getWritingStatus(long, long)
	 */
	private static final double MEDIUM_STATUS_END = 0.35;

	/**
	 * Returns the writing status based on the amount of arrived and finished tasks in the writer
	 * for the same period of time. Status is calculated as following:
	 * 
	 * <ul>
	 * <li>- {@link #GOOD} - if the amount of arrived tasks is smaller than finished or bigger for
	 * {@value #GOOD_STATUS_END} (in percents).</li>
	 * <li>- {@link #MEDIUM} - if the amount of arrived tasks is from {@value #GOOD_STATUS_END} to
	 * {@value #MEDIUM_STATUS_END} (in percents) bigger than finished tasks.</li>
	 * <li>- {@link #BAD} - if the amount of arrived tasks is bigger for {@value #MEDIUM_STATUS_END}
	 * (in percents).</li>
	 * </ul>
	 * 
	 * @param arrivedTasks
	 *            Number of arrived tasks.
	 * @param finishedTasks
	 *            Number of finished tasks.
	 * @return Writing status.
	 */
	public static WritingStatus getWritingStatus(long arrivedTasks, long finishedTasks) {
		if (arrivedTasks == 0) {
			return GOOD;
		}
		double ratio = (double) arrivedTasks / finishedTasks;
		if (ratio < 1 + GOOD_STATUS_END) {
			return GOOD;
		} else if (ratio < 1 + MEDIUM_STATUS_END) {
			return MEDIUM;
		} else {
			return BAD;
		}
	}

}
