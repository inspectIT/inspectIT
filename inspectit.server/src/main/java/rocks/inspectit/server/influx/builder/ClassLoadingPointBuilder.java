package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;

/**
 * Point builder for the {@link ClassLoadingInformationData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class ClassLoadingPointBuilder extends DefaultDataPointBuilder<ClassLoadingInformationData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<ClassLoadingInformationData> getDataClass() {
		return ClassLoadingInformationData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.ClassLoadingInfomation.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(ClassLoadingInformationData data, Builder builder) {
		long count = data.getCount();
		long loadedClassCount = data.getTotalLoadedClassCount() / count;
		long totalLoadedClassCount = data.getTotalTotalLoadedClassCount() / count;
		long unloadedClassCount = data.getTotalUnloadedClassCount() / count;

		// fields
		builder.addField(Series.ClassLoadingInfomation.FIELD_LOADED_CLASSES, loadedClassCount);
		builder.addField(Series.ClassLoadingInfomation.FIELD_TOTAL_LOADED_CLASSES, totalLoadedClassCount);
		builder.addField(Series.ClassLoadingInfomation.FIELD_UNLOADED_CLASSES, unloadedClassCount);
	}

}
