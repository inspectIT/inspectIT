package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;

/**
 * Point builder for the {@link JmxSensorValueData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class JmxPointBuilder extends DefaultDataPointBuilder<JmxSensorValueData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<JmxSensorValueData> getDataClass() {
		return JmxSensorValueData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.Jmx.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTags(JmxSensorValueData data, Builder builder) {
		super.addTags(data, builder);

		JmxDefinitionDataIdent jmxDefinitionDataIdent = cachedDataService.getJmxDefinitionDataIdentForId(data.getJmxSensorDefinitionDataIdentId());
		if (null != jmxDefinitionDataIdent) {
			builder.tag(Series.Jmx.TAG_JMX_ATTRIBUTE_FULL_NAME, jmxDefinitionDataIdent.getDerivedFullName());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(JmxSensorValueData data, Builder builder) {
		// fields
		builder.addField(Series.Jmx.FIELD_VALUE, data.getValueAsDouble());
	}

}
