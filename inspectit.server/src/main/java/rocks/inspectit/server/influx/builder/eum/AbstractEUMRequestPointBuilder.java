package rocks.inspectit.server.influx.builder.eum;

import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.eum.AbstractRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;


/**
 * Abstract super class for all point builders which consume requests.
 *
 * @author Jonas Kunz
 *
 * @param <R>
 *            the request type handled by this Point Builder.
 */
public abstract class AbstractEUMRequestPointBuilder<R extends AbstractRequest> extends AbstractEUMPointBuilder<R> {

	/**
	 * {@link ICachedDataService} for resolving all needed names.
	 */
	@Autowired
	protected ICachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean requiresSessionMetaInfo() {
		return true;
	}

	/**
	 *
	 * Adds the basic data from {@link Series} and {@link Series#EUMBasicRequestSeries} to the
	 * builder. Does not set the time of the point, this has to be done by the caller!
	 *
	 * @param sessionInfo
	 *            the sessionInfo belonging to the given request, can be null if unavailable
	 * @param builder
	 *            the point builder for the record.
	 * @param request
	 *            the request for which teh data point is built
	 */
	protected void addBasicRequestData(Builder builder, UserSessionInfo sessionInfo, R request) {
		PlatformIdent platformIdent = cachedDataService.getPlatformIdentForId(request.getPlatformIdent());
		builder.tag(Series.TAG_AGENT_ID, String.valueOf(request.getPlatformIdent()));
		if (null != platformIdent) {
			builder.tag(Series.TAG_AGENT_NAME, platformIdent.getAgentName());
		}
		if (sessionInfo != null) {
			builder.tag(Series.EUMBasicRequestSeries.TAG_BROWSER, sessionInfo.getBrowser());
			builder.tag(Series.EUMBasicRequestSeries.TAG_DEVICE, sessionInfo.getDevice());
			builder.tag(Series.EUMBasicRequestSeries.TAG_LANGUAGE, sessionInfo.getLanguage());
		}

		builder.tag(Series.EUMBasicRequestSeries.TAG_URL, request.getUrl());
	}

}
