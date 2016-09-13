package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * Point builder for the {@link TimerData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class TimerPointBuilder extends DefaultDataPointBuilder<TimerData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<TimerData> getDataClass() {
		return TimerData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.Methods.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTags(TimerData data, Builder builder) {
		super.addTags(data, builder);

		MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

		if (null != methodIdent) {
			builder.tag(Series.Methods.TAG_METHOD_NAME, methodIdent.getMethodName());
			builder.tag(Series.Methods.TAG_CLASS_FQN, methodIdent.getFQN());
			builder.tag(Series.Methods.TAG_METHOD_SIGNATURE, methodIdent.getFullyQualifiedMethodSignature());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(TimerData data, Builder builder) {
		// fields
		builder.addField(Series.Methods.FIELD_MIN_DURATION, data.getMin());
		builder.addField(Series.Methods.FIELD_DURATION, data.getAverage());
		builder.addField(Series.Methods.FIELD_MAX_DURATION, data.getMax());
		builder.addField(Series.Methods.FIELD_MIN_CPU_TIME, data.getCpuMin());
		builder.addField(Series.Methods.FIELD_CPU_TIME, data.getCpuAverage());
		builder.addField(Series.Methods.FIELD_MAX_CPU_TIME, data.getCpuMax());
	}

}
