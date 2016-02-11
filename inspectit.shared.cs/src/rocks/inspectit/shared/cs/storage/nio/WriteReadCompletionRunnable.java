package info.novatec.inspectit.storage.nio;

import info.novatec.inspectit.storage.nio.read.ReadingCompletionHandler;
import info.novatec.inspectit.storage.nio.write.WritingCompletionHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Completion runnable that know if the IO operation was successful.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class WriteReadCompletionRunnable implements Runnable {

	/**
	 * Size that was tried to be read/written.
	 */
	private long attemptedWriteReadSize;

	/**
	 * Position at which it was tried to be read/written.
	 */
	private long attemptedWriteReadPosition;

	/**
	 * How much times a {@link #markSuccess()} and {@link #markFailed()} has to be called so that
	 * this runnable will be run.
	 */
	private int completeMarks;

	/**
	 * Track of the number of success marks.
	 */
	private AtomicInteger successMarks = new AtomicInteger(0);

	/**
	 * Track of the number of failed marks.
	 */
	private AtomicInteger failedMarks = new AtomicInteger(0);

	/**
	 * Default constructor. Sets {@link #completeMarks} to 1.
	 */
	public WriteReadCompletionRunnable() {
		this(1);
	}

	/**
	 * Constructor that sets the number of {@link #completeMarks}.
	 * 
	 * @param completeMarks
	 *            How much times a {@link #markSuccess()} and {@link #markFailed()} has to be called
	 *            so that this runnable will be run.
	 */
	public WriteReadCompletionRunnable(int completeMarks) {
		this.completeMarks = completeMarks;
	}

	/**
	 * Marks as success.
	 */
	public void markSuccess() {
		successMarks.incrementAndGet();
	}

	/**
	 * Marks as failed.
	 */
	public void markFailed() {
		failedMarks.incrementAndGet();
	}

	/**
	 * Denotes if the number of reported succeeded and failed operations is same as the number of
	 * expected marks.
	 * 
	 * @return True if the {@link #run()} can be executed.
	 */
	public boolean isFinished() {
		return successMarks.get() + failedMarks.get() == completeMarks;
	}

	/**
	 * Denotes if the write/read operation was completed successfully.
	 * <p>
	 * Note that this will always be correctly set before {@link #run()} is executed on the
	 * {@link WritingCompletionHandler} or {@link ReadingCompletionHandler}, thus it can be used in
	 * run to determine if the IO operation failed.
	 * 
	 * @return If the write/read operation was completed successfully.
	 */
	public boolean isCompleted() {
		return successMarks.get() == completeMarks;
	}

	/**
	 * Denotes if the write/read operation failed.
	 * <p>
	 * Note that this will always be correctly set before {@link #run()} is executed on the
	 * {@link WritingCompletionHandler} or {@link ReadingCompletionHandler}, thus it can be used in
	 * run to determine if the IO operation failed.
	 * 
	 * @return If the write/read operation failed.
	 */
	public boolean isFailed() {
		return failedMarks.get() > 0;
	}

	/**
	 * Gets {@link #attemptedWriteReadSize}.
	 * 
	 * @return {@link #attemptedWriteReadSize}
	 */
	public long getAttemptedWriteReadSize() {
		return attemptedWriteReadSize;
	}

	/**
	 * Sets {@link #attemptedWriteReadSize}.
	 * 
	 * @param attemptedWriteReadSize
	 *            New value for {@link #attemptedWriteReadSize}
	 */
	public void setAttemptedWriteReadSize(long attemptedWriteReadSize) {
		this.attemptedWriteReadSize = attemptedWriteReadSize;
	}

	/**
	 * Gets {@link #attemptedWriteReadPosition}.
	 * 
	 * @return {@link #attemptedWriteReadPosition}
	 */
	public long getAttemptedWriteReadPosition() {
		return attemptedWriteReadPosition;
	}

	/**
	 * Sets {@link #attemptedWriteReadPosition}.
	 * 
	 * @param attemptedWriteReadPosition
	 *            New value for {@link #attemptedWriteReadPosition}
	 */
	public void setAttemptedWriteReadPosition(long attemptedWriteReadPosition) {
		this.attemptedWriteReadPosition = attemptedWriteReadPosition;
	}

}
