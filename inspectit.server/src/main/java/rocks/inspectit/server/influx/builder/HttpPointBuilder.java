package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.influx.constants.Tags;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;

/**
 * Point builder for the {@link HttpTimerData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class HttpPointBuilder extends DefaultDataPointBuilder<HttpTimerData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<HttpTimerData> getDataClass() {
		return HttpTimerData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.Http.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTags(HttpTimerData data, Builder builder) {
		super.addTags(data, builder);

		HttpInfo httpInfo = data.getHttpInfo();

		if (null != httpInfo.getUri()) {
			builder.tag(Tags.URI, httpInfo.getUri());
		}
		if (httpInfo.hasInspectItTaggingHeader()) {
			builder.tag(Tags.INSPECTIT_TAGGING_HEADER, httpInfo.getInspectItTaggingHeaderValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(HttpTimerData data, Builder builder) {
		// fields
		builder.addField(Series.Http.FIELD_DURATION, data.getDuration());
	}

}
