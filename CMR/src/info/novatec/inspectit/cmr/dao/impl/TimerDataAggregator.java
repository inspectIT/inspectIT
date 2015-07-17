package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.communication.data.DatabaseAggregatedTimerData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Aggregator for the {@link TimerData} objects that need to be persisted to the DB.
 * <p>
 * The class is marked as final because in the constructor it starts a thread.
 * 
 * @author Ivan Senic
 * @see https://confluence.novatec-gmbh.de/display/INSPECTIT/TimerData+Aggregator
 * 
 */
@Repository
public class TimerDataAggregator extends HibernateDaoSupport {

	/**
	 * Period of time in which all timer data should be aggregated. In milliseconds.
	 */
	@Value("${cmr.aggregationPeriod}")
	long aggregationPeriod;

	/**
	 * Max elements in the cache.
	 */
	@Value("${cmr.maxElements}")
	int maxElements;

	/**
	 * Sleeping period for thread that is cleaning the cache (persisting the objects). In
	 * milliseconds.
	 */
	@Value("${cmr.cacheCleanSleepingPeriod}")
	long cacheCleanSleepingPeriod;

	/**
	 * Current element count in cache.
	 */
	private AtomicInteger elementCount;

	/**
	 * Map for caching.
	 */
	private Map<Integer, TimerData> map;

	/**
	 * Queue for knowing the order.
	 */
	private ConcurrentLinkedQueue<TimerData> queue;

	/**
	 * List of objects that are out of the cache and need to be persisted.
	 */
	private ConcurrentLinkedQueue<TimerData> persistList;

	/**
	 * Lock for persist all.
	 */
	private ReentrantLock persistAllLock;

	/**
	 * Pointer to the most recently added {@link TimerData} to the cache.
	 */
	private volatile TimerData mostRecentlyAdded;

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 *            the hibernate session factory.
	 */
	@Autowired
	public TimerDataAggregator(SessionFactory sessionFactory) {
		elementCount = new AtomicInteger(0);
		map = new HashMap<Integer, TimerData>();
		queue = new ConcurrentLinkedQueue<TimerData>();
		persistList = new ConcurrentLinkedQueue<TimerData>();
		persistAllLock = new ReentrantLock();

		setSessionFactory(sessionFactory);
	}

	/**
	 * Aggregates the {@link TimerData} object and updates the cache. Note that the given object
	 * will not be modified by this method.
	 * 
	 * @param timerData
	 *            {@link TimerData} that holds values to be aggregated.
	 */
	public void processTimerData(TimerData timerData) {
		long aggregationTimestamp = getAlteredTimestamp(timerData);
		int cacheHash = getCacheHash(timerData.getPlatformIdent(), timerData.getMethodIdent(), aggregationTimestamp);

		persistAllLock.lock();
		try {
			TimerData aggTimerData = map.get(cacheHash);
			if (aggTimerData == null) {
				// we create a DB aggregated timer data because we don't want to alter objects that
				// are in the memory
				aggTimerData = new DatabaseAggregatedTimerData(new Timestamp(aggregationTimestamp), timerData.getPlatformIdent(), timerData.getSensorTypeIdent(), timerData.getMethodIdent());
				map.put(cacheHash, aggTimerData);
				queue.add(aggTimerData);
				mostRecentlyAdded = aggTimerData;

				int count = elementCount.incrementAndGet();
				// remove oldest as long as number of elements is higher than maximum
				while (maxElements < count) {
					TimerData oldest = queue.poll();
					if (null != oldest) {
						map.remove(getCacheHash(oldest.getPlatformIdent(), oldest.getMethodIdent(), oldest.getTimeStamp().getTime()));
						persistList.add(oldest);
						count = elementCount.decrementAndGet();
					}
				}
			}
			aggTimerData.aggregateTimerData(timerData);
		} finally {
			persistAllLock.unlock();
		}
	}

	/**
	 * Clears the cache and persists all the data inside.
	 */
	public void removeAndPersistAll() {
		if (!queue.isEmpty()) {
			persistAllLock.lock();
			try {
				StatelessSession session = getHibernateTemplate().getSessionFactory().openStatelessSession();
				Transaction tx = session.beginTransaction();

				TimerData oldest = queue.poll();
				while (oldest != null) {
					map.remove(getCacheHash(oldest.getPlatformIdent(), oldest.getMethodIdent(), oldest.getTimeStamp().getTime()));
					session.insert(oldest);
					elementCount.decrementAndGet();

					oldest = queue.poll();
				}

				tx.commit();
				session.close();
			} finally {
				persistAllLock.unlock();
			}
		}
	}

	/**
	 * Persists all objects in the persistence list.
	 */
	void saveAllInPersistList() {
		if (!persistList.isEmpty()) {
			StatelessSession session = getHibernateTemplate().getSessionFactory().openStatelessSession();
			Transaction tx = session.beginTransaction();

			TimerData last = persistList.poll();
			while (last != null) {
				last.finalizeData();
				session.insert(last);
				last = persistList.poll();
			}

			tx.commit();
			session.close();
		}
	}

	/**
	 * Returns the cache hash code.
	 * 
	 * @param platformIdent
	 *            Platform ident.
	 * @param methodIdent
	 *            Method ident.
	 * @param timestampValue
	 *            Time stamp value as long.
	 * @return Cache hash for the given set of values.
	 */
	private int getCacheHash(long platformIdent, long methodIdent, long timestampValue) {
		final int prime = 31;
		int result = 0;
		result = prime * result + (int) (platformIdent ^ (platformIdent >>> 32));
		result = prime * result + (int) (methodIdent ^ (methodIdent >>> 32));
		result = prime * result + (int) (timestampValue ^ (timestampValue >>> 32));
		return result;
	}

	/**
	 * Returns the value of the time stamp based on a aggregation period.
	 * 
	 * @param timerData
	 *            {@link TimerData} to get aggregation time stamp.
	 * @return Aggregation time stamp.
	 */
	private long getAlteredTimestamp(TimerData timerData) {
		long timestampValue = timerData.getTimeStamp().getTime();
		long newTimestampValue = timestampValue - timestampValue % aggregationPeriod;
		return newTimestampValue;
	}

	/**
	 * Starting the thread in post construct, not in constructor.
	 */
	@PostConstruct
	public void postConstruct() {
		CacheCleaner cacheCleaner = new CacheCleaner();
		cacheCleaner.start();
	}

	/**
	 * @return the aggregationPeriod
	 */
	public long getAggregationPeriod() {
		return aggregationPeriod;
	}

	/**
	 * @return the maxElements
	 */
	public int getMaxElements() {
		return maxElements;
	}

	/**
	 * @return the cacheCleanSleepingPeriod
	 */
	public long getCacheCleanSleepingPeriod() {
		return cacheCleanSleepingPeriod;
	}

	/**
	 * Gets {@link #elementCount}.
	 * 
	 * @return {@link #elementCount}
	 */
	public int getElementCount() {
		return elementCount.get();
	}

	/**
	 * Cache cleaner, or thread that is constantly checking if there is something to be persisted.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class CacheCleaner extends Thread {

		/**
		 * Element that is last checked by thread. If this element is same as most recently added
		 * element, all elements in the cache will be persisted.
		 */
		private TimerData lastChecked;

		/**
		 * Constructor. Set thread as daemon and gives it minimum priority.
		 */
		public CacheCleaner() {
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
				TimerData timerData = mostRecentlyAdded;
				if (timerData != null) {
					if (timerData.equals(lastChecked)) {
						removeAndPersistAll();
					}
					lastChecked = timerData;
				}
				saveAllInPersistList();
				try {
					Thread.sleep(cacheCleanSleepingPeriod);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
		}
	}

}
