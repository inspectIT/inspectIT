package rocks.inspectit.server.influx.builder.eum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.ClickAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.Request;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserAction;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;

/**
 * Creates Points based on the different types of user actions.
 *
 * @author Jonas Kunz
 *
 */
@Component
public class UserActionPointBuilder extends SessionAwarePointBuilder<UserAction> {

	/**
	 * {@link ICachedDataService} for resolving all needed names.
	 */
	@Autowired
	protected ICachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Class<? extends UserAction>> getDataClasses() {
		return Arrays.asList(PageLoadAction.class, ClickAction.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Builder> createBuildersWithSession(UserSessionInfo session, UserAction action) {
		List<Builder> builders = new ArrayList<>();
		for (Request req : action.getChildRequests()) {
			if (req instanceof PageLoadRequest) {
				builders.add(createPageLoadPoint(session, (PageLoadRequest) req, action));
			} else if (req instanceof ResourceLoadRequest) {
				builders.add(createResourceLoadPoint(session, (ResourceLoadRequest) req));
			} else if (req instanceof AjaxRequest) {
				builders.add(createAjaxPoint(session, (AjaxRequest) req));
			}
		}
		return builders;
	}

	/**
	 * @param session
	 *            the session attached to this request
	 * @param plReq
	 *            the pageload request to build an influx point for.
	 * @param action
	 *            the action owning this request
	 * @return the resulting influx point
	 */
	private Builder createPageLoadPoint(UserSessionInfo session, PageLoadRequest plReq, UserAction action) {
		int resourceCount = 0;
		for (Request req : action.getChildRequests()) {
			if (req instanceof ResourceLoadRequest) {
				resourceCount++;
			}
		}
		Builder builder = getDefaultBuilder(plReq, session, Series.EumPageLoad.NAME);
		builder.addField(Series.EumPageLoad.FIELD_NAVIGATION_START, plReq.getNavigationStartW()).addField(Series.EumPageLoad.FIELD_CONNECT_END, plReq.getConnectEndW())
		.addField(Series.EumPageLoad.FIELD_CONNECT_START, plReq.getConnectStartW()).addField(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_START, plReq.getDomContentLoadedEventStartW())
		.addField(Series.EumPageLoad.FIELD_DOM_CONTENT_LOADED_EVENT_END, plReq.getDomContentLoadedEventEndW()).addField(Series.EumPageLoad.FIELD_DOM_INTERACTIVE, plReq.getDomInteractiveW())
		.addField(Series.EumPageLoad.FIELD_DOM_LOADING, plReq.getDomLoadingW()).addField(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_START, plReq.getDomainLookupStartW())
		.addField(Series.EumPageLoad.FIELD_DOMAIN_LOOKUP_END, plReq.getDomainLookupEndW()).addField(Series.EumPageLoad.FIELD_FETCH_START, plReq.getFetchStartW())
		.addField(Series.EumPageLoad.FIELD_LOAD_EVENT_START, plReq.getLoadEventStartW()).addField(Series.EumPageLoad.FIELD_LOAD_EVENT_END, plReq.getLoadEventEndW())
		.addField(Series.EumPageLoad.FIELD_REDIRECT_START, plReq.getRedirectStartW()).addField(Series.EumPageLoad.FIELD_REDIRECT_END, plReq.getRedirectEndW())
		.addField(Series.EumPageLoad.FIELD_REQUEST_START, plReq.getRequestStartW()).addField(Series.EumPageLoad.FIELD_RESPONSE_START, plReq.getResponseStartW())
		.addField(Series.EumPageLoad.FIELD_RESPONSE_END, plReq.getResponseEndW()).addField(Series.EumPageLoad.FIELD_UNLOAD_EVENT_START, plReq.getUnloadEventStartW())
		.addField(Series.EumPageLoad.FIELD_UNLOAD_EVENT_END, plReq.getUnloadEventEndW()).addField(Series.EumPageLoad.FIELD_SPEEDINDEX, plReq.getSpeedindex())
		.addField(Series.EumPageLoad.FIELD_FIRSTPAINT, plReq.getFirstpaint()).addField(Series.EumPageLoad.FIELD_RESOURCE_COUNT, resourceCount);

		return builder;
	}

	/**
	 * @param session
	 *            the session attached to this request
	 * @param rlReq
	 *            the resource load request to build an influx point for.
	 * @return the resulting influx point
	 */
	private Builder createResourceLoadPoint(UserSessionInfo session, ResourceLoadRequest rlReq) {
		Builder builder = getDefaultBuilder(rlReq, session, Series.EumResourceLoad.NAME);
		builder.tag(Series.EumResourceLoad.TAG_INITIATOR_URL, rlReq.getInitiatorUrl()).tag(Series.EumResourceLoad.TAG_INITIATOR_TYPE, rlReq.getInitiatorType())
		.addField(Series.EumResourceLoad.FIELD_DURATION, rlReq.getEndTime() - rlReq.getStartTime()).addField(Series.EumResourceLoad.FIELD_TRANSFER_SIZE, rlReq.getTransferSize());

		return builder;
	}

	/**
	 * @param session
	 *            the session attached to this request
	 * @param ajaxReq
	 *            the ajax request to build an influx point for.
	 * @return the resulting influx point
	 */
	private Builder createAjaxPoint(UserSessionInfo session, AjaxRequest ajaxReq) {
		Builder builder = getDefaultBuilder(ajaxReq, session, Series.EumAjax.NAME);
		builder.tag(Series.EumAjax.TAG_BASE_URL, ajaxReq.getBaseUrl()).addField(Series.EumAjax.FIELD_DURATION, ajaxReq.getEndTime() - ajaxReq.getStartTime())
		.addField(Series.EumAjax.FIELD_METHOD, ajaxReq.getMethod()).addField(Series.EumAjax.FIELD_STATUS, ajaxReq.getStatus());

		return builder;
	}

	/**
	 * Creates a builder with all tags and fields common for each point type.
	 *
	 * @param req
	 *            the request
	 * @param session
	 *            the session of this request
	 * @param seriesName
	 *            the name of the series to palce the point in
	 * @return a builder having all standard fields and tags set
	 */
	private Builder getDefaultBuilder(Request req, UserSessionInfo session, String seriesName) {
		Builder builder = Point.measurement(seriesName);
		builder.time(req.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);
		PlatformIdent platformIdent = cachedDataService.getPlatformIdentForId(req.getPlatformIdent());
		builder.tag(Series.TAG_AGENT_ID, String.valueOf(req.getPlatformIdent()));
		if (null != platformIdent) {
			builder.tag(Series.TAG_AGENT_NAME, platformIdent.getAgentName());
		}
		builder.tag(Series.EUMBasicRequestSeries.TAG_BROWSER, session.getBrowser());
		builder.tag(Series.EUMBasicRequestSeries.TAG_DEVICE, session.getDevice());
		builder.tag(Series.EUMBasicRequestSeries.TAG_LANGUAGE, session.getLanguage());
		builder.tag(Series.EUMBasicRequestSeries.TAG_URL, req.getUrl());
		builder.addField(Series.EUMBasicRequestSeries.FIELD_SESSIONID, req.getSessionId());
		return builder;
	}

}
