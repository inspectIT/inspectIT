package rocks.inspectit.agent.java.stats;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Very basic component for statistics logging. Other components can use methods provided to denote
 * different events, while the logger decides when will something be printed.
 * <P>
 * For now only can log the number of data dropped.
 *
 * @author Ivan Senic
 *
 */
@Component
public class AgentStatsLogger {

	/**
	 * Log strategy for the {@link #droppedDataCount}.
	 */
	private static final CountingLogStrategy DROPPED_DATA_LOG_STRATEGY = new CountingLogStrategy(1, 10, 100, 1000);

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * Count how much data are we dropping.
	 */
	private AtomicLong droppedDataCount = new AtomicLong(0);

	/**
	 * Signals data drop.
	 *
	 * @param count
	 *            How many points have we dropped and not sent to the server.
	 */
	public void dataDropped(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Dropped data must be positive number.");
		}

		long dropped = droppedDataCount.addAndGet(count);

		// log on first, tenth, hundredth and then on every one thousand elements dropped
		if (log.isWarnEnabled() && DROPPED_DATA_LOG_STRATEGY.shouldLog(dropped - count, dropped)) {
			log.warn("Monitoring data is dropped due to buffer capacity reached or connection failure. Current count of dropped data is " + dropped + ".");
		}
	}

	/**
	 * Small utility for log based on the count updates.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class CountingLogStrategy {

		/**
		 * Boundaries to log when they are exceeded. if [1,10,100] is passed then strategy will
		 * return true after passing 1 and 10 and then every time after passing 100 elements (so
		 * including 200, 300, etc).
		 */
		private long[] boundaries;

		/**
		 * @param boundaries
		 *            Boundaries to log when they are exceeded. if [1,10,100] is passed then
		 *            strategy will return true after passing 1 and 10 and then every time after
		 *            passing 100 elements (so including 200, 300, etc).
		 */
		CountingLogStrategy(long... boundaries) {
			if ((null == boundaries) || (boundaries.length < 2)) {
				throw new IllegalArgumentException("At least one boundary needs to be specified.");
			}

			Arrays.sort(boundaries);
			this.boundaries = boundaries;
		}

		/**
		 * If count update should be logged.
		 *
		 * @param previous
		 *            Previous state of the counter.
		 * @param current
		 *            Updated state of the counter.
		 * @return If should log or not.
		 * @see #boundaries
		 */
		boolean shouldLog(long previous, long current) {
			for (int i = 0; i < (boundaries.length - 1); i++) {
				if ((previous < boundaries[i]) && (current >= boundaries[i])) {
					return true;
				}
			}

			long last = boundaries[boundaries.length - 1];
			return ((previous / last) < (current / last));
		}

	}

}
