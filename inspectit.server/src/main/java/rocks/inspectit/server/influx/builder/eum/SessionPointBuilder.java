package rocks.inspectit.server.influx.builder.eum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import rocks.inspectit.server.influx.builder.IPointBuilder;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMData;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.util.Pair;

/**
 * This class is responsible for handling the matching of session meta data to the data object
 * requiring this meta data for their point building. THis is done by caching incoming session
 * objects and allowing other point builders to query them. Additionally, point builders requiring
 * session information about a session which is not available yet can enqueue themselves to be
 * notifies as soon as this information arrives.
 *
 * @author Jonas Kunz
 *
 */
@Component
public class SessionPointBuilder implements IPointBuilder<UserSessionInfo> {

	/**
	 * Keep values in the cache for a max of two hours.
	 */
	private static final long MAX_SESSION_INACTIVE_SECONDS = 6 * 60 * 2;

	/**
	 * Holds the currently open sessions with doubled expiration time to avoid data loss due to
	 * delayed requests.
	 */
	Cache<String, UserSessionInfo> sessionInfoCache;

	/**
	 * Holds the data points whose session info is missing in combination with the corresponding
	 * point builder.
	 */
	@SuppressWarnings("rawtypes")
	Cache<String, ConcurrentLinkedQueue<Pair<AbstractEUMData, SessionAwarePointBuilder>>> pendingDataPoints;

	/**
	 * Write queue for evicted entries.
	 */
	ConcurrentLinkedQueue<Builder> dataPointsToWrite;

	/**
	 * Constructor.
	 */
	@SuppressWarnings("rawtypes")
	public SessionPointBuilder() {
		sessionInfoCache = CacheBuilder.newBuilder().expireAfterAccess(MAX_SESSION_INACTIVE_SECONDS, TimeUnit.SECONDS).build();
		dataPointsToWrite = new ConcurrentLinkedQueue<>();
		pendingDataPoints = CacheBuilder.newBuilder().expireAfterWrite(100, TimeUnit.SECONDS)
				.removalListener(new RemovalListener<String, ConcurrentLinkedQueue<Pair<AbstractEUMData, SessionAwarePointBuilder>>>() {
					@SuppressWarnings("unchecked")
					@Override
					public void onRemoval(RemovalNotification<String, ConcurrentLinkedQueue<Pair<AbstractEUMData, SessionAwarePointBuilder>>> notification) {
						if (notification.getCause() != RemovalCause.EXPLICIT) {
							String sessionId = notification.getKey();
							UserSessionInfo unknownSession = new UserSessionInfo("unknown", "unknown", "unknown", sessionId);
							for (Pair<AbstractEUMData, SessionAwarePointBuilder> val : notification.getValue()) {
								dataPointsToWrite.addAll(val.getSecond().createBuildersWithSession(unknownSession, val.getFirst()));
							}
						}
					}
				}).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Class<? extends UserSessionInfo>> getDataClasses() {
		return Collections.singleton(UserSessionInfo.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<Builder> createBuilders(UserSessionInfo session) {
		sessionInfoCache.put(session.getSessionId(), session);

		ArrayList<Builder> pointsToWrite = new ArrayList<>();
		Queue<Pair<AbstractEUMData, SessionAwarePointBuilder>> pending = pendingDataPoints.getIfPresent(session.getSessionId());
		while ((pending != null) && !pending.isEmpty()) {
			Pair<AbstractEUMData, SessionAwarePointBuilder> pair = pending.poll();
			pointsToWrite.addAll(pair.getSecond().createBuildersWithSession(session, pair.getFirst()));
		}

		// append evicted points
		Builder it = dataPointsToWrite.poll();
		while (it != null) {
			pointsToWrite.add(it);
			it = dataPointsToWrite.poll();
		}

		return pointsToWrite;
	}

	/**
	 * @param sessionId
	 *            the sessionID
	 * @return return the session info for the given id, or null if it is not available
	 */
	public UserSessionInfo getSessionInfo(String sessionId) {
		return sessionInfoCache.getIfPresent(sessionId);
	}

	/**
	 * Adds a pending data element for which the session info is not yet available. As soon as the
	 * corresponding session info arrives, this SessionAwarePointBuilder wil lbe executed for the
	 * given data element.
	 *
	 * @param dataObject
	 *            the elemetn requiring a session info to be added to influx
	 * @param eumSessionAwarePointBuilder
	 *            the point builder responsible for this type of data elemetns
	 */
	@SuppressWarnings("rawtypes")
	public void addPendingPoints(AbstractEUMData dataObject, SessionAwarePointBuilder eumSessionAwarePointBuilder) {
		String session = dataObject.getSessionId();
		try {
			pendingDataPoints.get(session, new Callable<ConcurrentLinkedQueue<Pair<AbstractEUMData, SessionAwarePointBuilder>>>() {

				@Override
				public ConcurrentLinkedQueue<Pair<AbstractEUMData, SessionAwarePointBuilder>> call() throws Exception {
					return new ConcurrentLinkedQueue<>();
				}
			}).add(new Pair<>(dataObject, eumSessionAwarePointBuilder));
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

}
