package rocks.inspectit.server.influx.builder.eum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMSpanDetails;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
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
public class EUMCachingParentPointBuilder implements IPointBuilder<DefaultData> {

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
	Cache<AbstractEUMSpanDetails, Pair<Boolean, AbstractEUMPointBuilder>> pendingDataPoints;

	/**
	 * Holds all received AbstractEUMElements for which the session meta info is currently missing.
	 * The key is the sessionID.
	 */
	SetMultimap<Long, AbstractEUMSpanDetails> missingSessionInfosMap;

	/**
	 * Holds all received AbstractEUMElements for which the PageLoadRequest is currently missing.
	 * The key is the pair (sessionID, tabID) .
	 */
	SetMultimap<Pair<Long, Long>, AbstractEUMSpanDetails> missingPageLoadRequestsMap;

	/**
	 * Write queue for evicted entries.
	 */
	ConcurrentLinkedQueue<Builder> dataPointsToWrite;

	/**
	 * A list of all specific builder for the individual AbstractEUMSpanDetails types.
	 */
	Map<Class<? extends AbstractEUMSpanDetails>, AbstractEUMPointBuilder<?>> subBuilders;

	/**
	 * Constructor.
	 *
	 * @param availableSubBuilders
	 *            a List of all available builders for AbstractEUMSpanDetails Elements.
	 */
	@SuppressWarnings("rawtypes")
	@Autowired
	public EUMCachingParentPointBuilder(List<AbstractEUMPointBuilder<?>> availableSubBuilders) {
		sessionInfoCache = CacheBuilder.newBuilder().expireAfterAccess(MAX_SESSION_INACTIVE_SECONDS, TimeUnit.SECONDS).build();
		pageLoadRequestCache = CacheBuilder.newBuilder().expireAfterAccess(MAX_SESSION_INACTIVE_SECONDS, TimeUnit.SECONDS).build();

		dataPointsToWrite = new ConcurrentLinkedQueue<>();
		pendingDataPoints = CacheBuilder.newBuilder().expireAfterWrite(100, TimeUnit.SECONDS).removalListener(new RemovalListener<AbstractEUMSpanDetails, Pair<Boolean, AbstractEUMPointBuilder>>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onRemoval(RemovalNotification<AbstractEUMSpanDetails, Pair<Boolean, AbstractEUMPointBuilder>> notification) {

				AbstractEUMSpanDetails element = notification.getKey();
				synchronized (element) {
					AbstractEUMPointBuilder builder = notification.getValue().getSecond();

					Long sessId = element.getOwningSpan().getSessionId();
					Long tabId = element.getOwningSpan().getTabId();
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

		missingSessionInfosMap = Multimaps.synchronizedSetMultimap(HashMultimap.<Long, AbstractEUMSpanDetails> create());
		missingPageLoadRequestsMap = Multimaps.synchronizedSetMultimap(HashMultimap.<Pair<Long, Long>, AbstractEUMSpanDetails> create());

		subBuilders = new HashMap<>();

		for (AbstractEUMPointBuilder<?> builder : availableSubBuilders) {
			for (Class<? extends AbstractEUMSpanDetails> clazz : builder.getSupportedTypes()) {
				subBuilders.put(clazz, builder);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Class<? extends DefaultData>> getDataClasses() {
		return new HashSet<Class<? extends DefaultData>>(Arrays.asList(UserSessionInfo.class, EUMSpan.class));
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Builder> createBuilders(DefaultData element) {

		List<Builder> result = new ArrayList<>();

		if (element instanceof UserSessionInfo) {
			newSessionInfoReceived((UserSessionInfo) element);
		} else if (element instanceof EUMSpan) {
			EUMSpan span = (EUMSpan) element;

			boolean isBrowserInfoCaptured = span.getActiveAgentModules().contains(String.valueOf(JSAgentModule.BROWSERINFO_MODULE.getIdentifier()));
			AbstractEUMSpanDetails details = span.getDetails();
			if (details != null) {
				if (details instanceof PageLoadRequest) {
					newPageLoadRequestReceived((PageLoadRequest) details);
				}

				@SuppressWarnings("rawtypes")
				AbstractEUMPointBuilder responsibleBuilder = subBuilders.get(details.getClass());

				if (responsibleBuilder != null) {

					boolean requiredDataIsPresent = true;
					PageLoadRequest plr = null;
					UserSessionInfo sessInfo = null;

					Long sessId = span.getSessionId();
					Long tabId = span.getTabId();
					Pair<Long, Long> plrSessionTabId = new Pair<Long, Long>(sessId, tabId);

					if (responsibleBuilder.requiresPageLoadRequest()) {
						plr = pageLoadRequestCache.getIfPresent(plrSessionTabId);
						if (plr == null) { // data is not yet available
							requiredDataIsPresent = false;
							missingPageLoadRequestsMap.put(plrSessionTabId, details);
						}
					}
					if (responsibleBuilder.requiresSessionMetaInfo() && isBrowserInfoCaptured) {
						sessInfo = sessionInfoCache.getIfPresent(sessId);
						// data is not yet available but might come in the future
						if ((sessInfo == null)) {
							requiredDataIsPresent = false;
							missingSessionInfosMap.put(sessId, details);
						}
					}

					if (requiredDataIsPresent) {
						result.addAll(responsibleBuilder.build(sessInfo, plr, details));
					} else {
						pendingDataPoints.put(details, new Pair<>(isBrowserInfoCaptured, responsibleBuilder));
					}

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

		Long sessionID = plr.getOwningSpan().getSessionId();
		Long tabID = plr.getOwningSpan().getTabId();
		Pair<Long, Long> plrSessionTabId = new Pair<Long, Long>(sessionID, tabID);
		pageLoadRequestCache.put(plrSessionTabId, plr);

		Iterator<AbstractEUMSpanDetails> it = missingPageLoadRequestsMap.get(plrSessionTabId).iterator();
		while (it.hasNext()) {
			AbstractEUMSpanDetails next = it.next();
			it.remove();
			tryPointBuildingForPendingElement(next);
		}

	}

	/**
	 * handles the arrival of a new UserSessionInfo object.
	 *
	 * @param sessionInfo
	 *            the new session info
	 */
	private void newSessionInfoReceived(UserSessionInfo sessionInfo) {

		Long sessionID = sessionInfo.getSessionId();
		if (sessionInfoCache.getIfPresent(sessionID) != null) {
			return; // session info already present
		}

		sessionInfoCache.put(sessionID, sessionInfo);

		Iterator<AbstractEUMSpanDetails> it = missingSessionInfosMap.get(sessionID).iterator();
		while (it.hasNext()) {
			AbstractEUMSpanDetails next = it.next();
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
	private void tryPointBuildingForPendingElement(AbstractEUMSpanDetails element) {
		synchronized (element) {
			@SuppressWarnings("rawtypes")
			Pair<Boolean, AbstractEUMPointBuilder> pair = pendingDataPoints.getIfPresent(element);
			@SuppressWarnings("rawtypes")
			AbstractEUMPointBuilder responsibleBuilder = pair.getSecond();
			boolean isBrowserInfoCaptured = pair.getFirst();

			boolean requiredDataIsPresent = true;
			PageLoadRequest plr = null;
			UserSessionInfo sessInfo = null;

			Long sessId = element.getOwningSpan().getSessionId();
			Long tabId = element.getOwningSpan().getTabId();
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
