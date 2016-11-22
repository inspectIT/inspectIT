package rocks.inspectit.server.influx.builder.eum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import rocks.inspectit.server.influx.builder.IPointBuilder;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMElement;
import rocks.inspectit.shared.all.communication.data.eum.Beacon;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.util.Pair;

/**
 * Basic Point Builder for EUM data, which delegates to the specialized
 * {@link AbstractEUMPointBuilder}s.
 *
 * This class is responsible for handling the matching of session meta data to the data object
 * requiring this meta data for their point building. This is done by caching incoming session
 * objects and allowing other point builders to query them. Additionally, point builders requiring
 * session information about a session which is not available yet can enqueue themselves to be
 * notifies as soon as this information arrives.
 *
 * @author Jonas Kunz
 *
 */
@Component
public class EUMCachingParentPointBuilder implements IPointBuilder<Beacon> {

	/**
	 * Keep values in the cache for a max of two hours.
	 */
	private static final long MAX_SESSION_INACTIVE_SECONDS = 6 * 60 * 2;

	/**
	 * Holds the currently open sessions with doubled expiration time to avoid data loss due to
	 * delayed requests. The key is the sessionID.
	 */
	Cache<Long, UserSessionInfo> sessionInfoCache;

	/**
	 * Maps the (sessionId, tabID) pair to their corresponding page load action.
	 */
	Cache<Pair<Long, Long>, PageLoadRequest> pageLoadRequestCache;

	/**
	 * Holds the data points for whom the session information is missing in combination with the
	 * responsible point builder. The stored boolean represents whether this session will provide
	 * session meta information in the future.
	 */
	@SuppressWarnings("rawtypes")
	Cache<AbstractEUMElement, Pair<Boolean, AbstractEUMPointBuilder>> pendingDataPoints;

	/**
	 * Holds all received AbstractEUMElements for which the session meta info is currently missing.
	 * The key is the sessionID.
	 */
	SetMultimap<Long, AbstractEUMElement> missingSessionInfosMap;

	/**
	 * Holds all received AbstractEUMElements for which the PageLoadRequest is currently missing.
	 * The key is the pair (sessionID, tabID) .
	 */
	SetMultimap<Pair<Long, Long>, AbstractEUMElement> missingPageLoadRequestsMap;

	/**
	 * Write queue for evicted entries.
	 */
	ConcurrentLinkedQueue<Builder> dataPointsToWrite;

	/**
	 * A list of all specific buidler for the individual AbstractEUMElement types.
	 */
	Map<Class<? extends AbstractEUMElement>, AbstractEUMPointBuilder<?>> subBuilders;


	/**
	 * Constructor.
	 *
	 * @param availableSubBuilders
	 *            a List of all available builders for AbstractEUM Elements.
	 */
	@SuppressWarnings("rawtypes")
	@Autowired
	public EUMCachingParentPointBuilder(List<AbstractEUMPointBuilder<?>> availableSubBuilders) {
		sessionInfoCache = CacheBuilder.newBuilder().expireAfterAccess(MAX_SESSION_INACTIVE_SECONDS, TimeUnit.SECONDS).build();
		pageLoadRequestCache = CacheBuilder.newBuilder().expireAfterAccess(MAX_SESSION_INACTIVE_SECONDS, TimeUnit.SECONDS).build();

		dataPointsToWrite = new ConcurrentLinkedQueue<>();
		pendingDataPoints = CacheBuilder.newBuilder().expireAfterWrite(100, TimeUnit.SECONDS)
				.removalListener(new RemovalListener<AbstractEUMElement, Pair<Boolean, AbstractEUMPointBuilder>>() {
					@SuppressWarnings("unchecked")
					@Override
					public void onRemoval(RemovalNotification<AbstractEUMElement, Pair<Boolean, AbstractEUMPointBuilder>> notification) {

						AbstractEUMElement element = notification.getKey();
						synchronized (element) {
							AbstractEUMPointBuilder builder = notification.getValue().getSecond();

							Long sessId = element.getID().getSessionID();
							Long tabId = element.getID().getTabID();
							Pair<Long, Long> plrSessionTabId = new Pair<Long, Long>(sessId, tabId);

							missingSessionInfosMap.remove(sessId, element);
							missingPageLoadRequestsMap.remove(plrSessionTabId, element);

							// Force a write even if data is still missing in case of an cache
							// eviction because of the timeout
							if (notification.getCause() != RemovalCause.EXPLICIT) {

								PageLoadRequest plr = pageLoadRequestCache.getIfPresent(plrSessionTabId);
								UserSessionInfo sessionInfo = sessionInfoCache.getIfPresent(sessId);

								dataPointsToWrite.addAll(builder.build(sessionInfo, plr, element));
							}
						}
					}
				}).build();

		missingSessionInfosMap = Multimaps.synchronizedSetMultimap(HashMultimap.<Long, AbstractEUMElement> create());
		missingPageLoadRequestsMap = Multimaps.synchronizedSetMultimap(HashMultimap.<Pair<Long, Long>, AbstractEUMElement> create());

		subBuilders = new HashMap<>();

		for (AbstractEUMPointBuilder<?> builder : availableSubBuilders) {
			for (Class<? extends AbstractEUMElement> clazz : builder.getSupportedTypes()) {
				subBuilders.put(clazz, builder);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Class<Beacon>> getDataClasses() {
		return Collections.singleton(Beacon.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Builder> createBuilders(Beacon beacon) {

		List<Builder> result = new ArrayList<>();

		boolean isBrowserInfoCaptured = beacon.getActiveAgentModules().toLowerCase().contains(String.valueOf(JSAgentModule.BROWSERINFO_MODULE.getIdentifier()));

		for (AbstractEUMElement element : beacon.getData()) {

			if (element instanceof UserSessionInfo) {
				newSessionInfoReceived((UserSessionInfo) element);
			} else if (element instanceof PageLoadRequest) {
				newPageLoadRequestReceived((PageLoadRequest) element);
			}

			@SuppressWarnings("rawtypes")
			AbstractEUMPointBuilder responsibleBuilder = subBuilders.get(element.getClass());

			if (responsibleBuilder != null) {

				boolean requiredDataIsPresent = true;
				PageLoadRequest plr = null;
				UserSessionInfo sessInfo = null;

				Long sessId = element.getID().getSessionID();
				Long tabId = element.getID().getTabID();
				Pair<Long, Long> plrSessionTabId = new Pair<Long, Long>(sessId, tabId);

				if (responsibleBuilder.requiresPageLoadRequest()) {
					plr = pageLoadRequestCache.getIfPresent(plrSessionTabId);
					if (plr == null) { // data is not yet available
						requiredDataIsPresent = false;
						missingPageLoadRequestsMap.put(plrSessionTabId, element);
					}
				}
				if (responsibleBuilder.requiresSessionMetaInfo() && isBrowserInfoCaptured) {
					sessInfo = sessionInfoCache.getIfPresent(sessId);
					// data is not yet available but might come in the future
					if ((sessInfo == null)) {
						requiredDataIsPresent = false;
						missingSessionInfosMap.put(sessId, element);
					}
				}

				if (requiredDataIsPresent) {
					result.addAll(responsibleBuilder.build(sessInfo, plr, element));
				} else {
					pendingDataPoints.put(element, new Pair<>(isBrowserInfoCaptured, responsibleBuilder));
				}

			}
		}

		// append evicted points
		// this done using manual iteration to avoid race conditions, as only poll atomically gets
		// and removed an element
		Builder it = dataPointsToWrite.poll();
		while (it != null) {
			result.add(it);
			it = dataPointsToWrite.poll();
		}

		return result;
	}

	/**
	 * Handles teh arrival of a new PageLoadRequest.
	 *
	 * @param plr
	 *            the received pageloadrequest
	 */
	private void newPageLoadRequestReceived(PageLoadRequest plr) {

		Long sessionID = plr.getID().getSessionID();
		Pair<Long, Long> plrSessionTabId = new Pair<Long, Long>(sessionID, plr.getID().getTabID());
		pageLoadRequestCache.put(plrSessionTabId, plr);

		Iterator<AbstractEUMElement> it = missingPageLoadRequestsMap.get(plrSessionTabId).iterator();
		while (it.hasNext()) {
			AbstractEUMElement next = it.next();
			it.remove();
			tryPointBuildingForPendingElement(next);
		}

	}

	/**
	 * handles teh arrival of a new UserSessionInfo object.
	 *
	 * @param sessionInfo
	 *            the new session info
	 */
	private void newSessionInfoReceived(UserSessionInfo sessionInfo) {

		Long sessionID = sessionInfo.getID().getSessionID();
		if (sessionInfoCache.getIfPresent(sessionID) != null) {
			return; // session info already present
		}

		sessionInfoCache.put(sessionID, sessionInfo);

		Iterator<AbstractEUMElement> it = missingSessionInfosMap.get(sessionID).iterator();
		while (it.hasNext()) {
			AbstractEUMElement next = it.next();
			it.remove();
			tryPointBuildingForPendingElement(next);
		}
	}

	/**
	 * Tries to build the points for the corresponding element, which previously depended on
	 * unavailable data.
	 *
	 * @param element
	 *            the element to try to process
	 */
	@SuppressWarnings("unchecked")
	private void tryPointBuildingForPendingElement(AbstractEUMElement element) {
		synchronized (element) {
			@SuppressWarnings("rawtypes")
			Pair<Boolean, AbstractEUMPointBuilder> pair = pendingDataPoints.getIfPresent(element);
			@SuppressWarnings("rawtypes")
			AbstractEUMPointBuilder responsibleBuilder = pair.getSecond();
			boolean isBrowserInfoCaptured = pair.getFirst();

			boolean requiredDataIsPresent = true;
			PageLoadRequest plr = null;
			UserSessionInfo sessInfo = null;

			Long sessId = element.getID().getSessionID();
			Long tabId = element.getID().getTabID();
			Pair<Long, Long> plrSessionTabId = new Pair<Long, Long>(sessId, tabId);

			if (responsibleBuilder.requiresPageLoadRequest()) {
				plr = pageLoadRequestCache.getIfPresent(plrSessionTabId);
				if (plr == null) { // data is not yet available
					requiredDataIsPresent = false;
				}
			}
			if (responsibleBuilder.requiresSessionMetaInfo() && isBrowserInfoCaptured) {
				sessInfo = sessionInfoCache.getIfPresent(sessId);
				if ((sessInfo == null)) { // data is not yet available
					requiredDataIsPresent = false;
				}
			}

			if (requiredDataIsPresent) {
				dataPointsToWrite.addAll(responsibleBuilder.build(sessInfo, plr, element));
				pendingDataPoints.invalidate(element);
			}
		}
	}

}
