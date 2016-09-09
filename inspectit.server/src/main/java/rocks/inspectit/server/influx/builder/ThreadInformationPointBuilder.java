package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;

/**
 * Point builder for the {@link ThreadInformationData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class ThreadInformationPointBuilder extends DefaultDataPointBuilder<ThreadInformationData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<ThreadInformationData> getDataClass() {
		return ThreadInformationData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.ThreadInformation.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(ThreadInformationData data, Builder builder) {
		int count = data.getCount();
		long liveThreadCount = data.getTotalThreadCount() / count;
		long daemonThreadCount = data.getTotalDaemonThreadCount() / count;
		long totalStartedThreadCount = data.getTotalTotalStartedThreadCount() / count;
		long peakThreadCount = data.getTotalPeakThreadCount() / count;

		// fields
		builder.addField(Series.ThreadInformation.FIELD_LIVE_THREAD_COUNT, liveThreadCount);
		builder.addField(Series.ThreadInformation.FIELD_DEAMON_THREAD_COUNT, daemonThreadCount);
		builder.addField(Series.ThreadInformation.FIELD_TOTAL_STARTED_THREAD_COUNT, totalStartedThreadCount);
		builder.addField(Series.ThreadInformation.FIELD_PEAK_THREAD_COUNT, peakThreadCount);
	}

}
