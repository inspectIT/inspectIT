package rocks.inspectit.server.influx.builder.eum;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMElement;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;

/**
 * Point builder responsible for {@link ResourceLoadRequest}s.
 *
 * @author Jonas Kunz
 *
 */
@Component
public class ResourceLoadRequestPointBuilder extends AbstractEUMRequestPointBuilder<ResourceLoadRequest> {


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
	public Collection<Builder> build(UserSessionInfo sessionInfo, PageLoadRequest plr, ResourceLoadRequest res) {

		Builder builder = Point.measurement(Series.EumResourceLoad.NAME);

		super.addBasicRequestData(builder, sessionInfo, res);

		builder.tag(Series.EumResourceLoad.TAG_INITIATOR_URL, res.getBaseUrl());

		builder.time(Math.round(res.getEnterTimestamp()), TimeUnit.MILLISECONDS);
		builder.addField(Series.EumAjax.FIELD_DURATION, res.getExitTimestamp() - res.getEnterTimestamp());
		builder.tag(Series.EumResourceLoad.TAG_INITIATOR_TYPE, res.getInitiatorType());
		builder.addField(Series.EumResourceLoad.FIELD_TRANSFER_SIZE, res.getTransferSize());

		return Collections.singleton(builder);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Class<? extends AbstractEUMElement>> getSupportedTypes() {
		return Collections.<Class<? extends AbstractEUMElement>> singleton(ResourceLoadRequest.class);
	}

}
