package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;

/**
 * Point builder for the {@link CpuInformationData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class CpuInformationPointBuilder extends DefaultDataPointBuilder<CpuInformationData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<CpuInformationData> getDataClass() {
		return CpuInformationData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.CpuInformation.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(CpuInformationData data, Builder builder) {
		float cpuAverage = data.getTotalCpuUsage() / data.getCount();

		// fields
		builder.addField(Series.CpuInformation.FIELD_AVG_CPU_UTIL, cpuAverage);
		builder.addField(Series.CpuInformation.FIELD_MIN_CPU_UTIL, data.getMinCpuUsage());
		builder.addField(Series.CpuInformation.FIELD_MAX_CPU_UTIL, data.getMaxCpuUsage());
	}

}
