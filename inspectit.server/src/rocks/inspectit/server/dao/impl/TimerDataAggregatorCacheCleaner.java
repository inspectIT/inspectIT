package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.communication.data.TimerData;

/**
 * Cache cleaner, or thread that is constantly checking if there is something to be persisted.
 * 
 * @author Ivan Senic
 * 
 */
class TimerDataAggregatorCacheCleaner extends Thread {

	/**
	 * {@link TimerDataAggregator} to work on.
	 */
	private final TimerDataAggregator timerDataAggregator;

	/**
	 * Pointer to the most recently added {@link TimerData} to the cache.
	 */
	private volatile TimerData mostRecentlyAdded;

	/**
	 * Element that is last checked by thread. If this element is same as most recently added
	 * element, all elements in the cache will be persisted.
	 */
	private TimerData lastChecked;

	/**
	 * Constructor. Set thread as daemon and gives it minimum priority.
	 * 
	 * @param timerDataAggregator
	 *            {@link TimerDataAggregator} to work on.
	 */
	public TimerDataAggregatorCacheCleaner(TimerDataAggregator timerDataAggregator) {
		this.timerDataAggregator = timerDataAggregator;
		setName("timer-data-aggregator-cache-cleaner-thread");
		setDaemon(true);
		setPriority(MIN_PRIORITY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (true) {
			TimerData timerData = this.mostRecentlyAdded;
			if (timerData != null) {
				if (timerData == lastChecked) { // NOPMD
					this.timerDataAggregator.removeAndPersistAll();
				}
				lastChecked = timerData;
			}
			this.timerDataAggregator.saveAllInPersistList();
			try {
				Thread.sleep(this.timerDataAggregator.cacheCleanSleepingPeriod);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
	}

	/**
	 * Sets {@link #mostRecentlyAdded}.
	 * 
	 * @param mostRecentlyAdded
	 *            New value for {@link #mostRecentlyAdded}
	 */
	public void setMostRecentlyAdded(TimerData mostRecentlyAdded) {
		this.mostRecentlyAdded = mostRecentlyAdded;
	}

}