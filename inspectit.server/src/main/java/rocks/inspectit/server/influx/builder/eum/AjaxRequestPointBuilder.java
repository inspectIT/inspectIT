package rocks.inspectit.server.influx.builder.eum;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMElement;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;

/**
 * Point Builder for Ajax requests.
 *
 * @author Jonas Kunz
 *
 */
@Component
public class AjaxRequestPointBuilder extends AbstractEUMRequestPointBuilder<AjaxRequest> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean requiresPageLoadRequest() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Builder> build(UserSessionInfo sessionInfo, PageLoadRequest plr, AjaxRequest ajax) {

		Builder builder = Point.measurement(Series.EumAjax.NAME);
		addBasicRequestData(builder, sessionInfo, ajax);

		builder.tag(Series.EumAjax.TAG_BASE_URL, ajax.getBaseUrl());

		builder.time(Math.round(ajax.getEnterTimestamp()), TimeUnit.MILLISECONDS);
		builder.addField(Series.EumAjax.FIELD_DURATION, ajax.getExitTimestamp() - ajax.getEnterTimestamp());
		builder.addField(Series.EumAjax.FIELD_METHOD, ajax.getMethod());
		builder.addField(Series.EumAjax.FIELD_STATUS, ajax.getStatus());

		return Collections.singleton(builder);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Class<? extends AbstractEUMElement>> getSupportedTypes() {
		return Collections.<Class<? extends AbstractEUMElement>> singleton(AjaxRequest.class);
	}

}
