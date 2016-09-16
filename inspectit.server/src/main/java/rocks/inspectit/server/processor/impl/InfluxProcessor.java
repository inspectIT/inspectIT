package rocks.inspectit.server.processor.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import rocks.inspectit.server.influx.builder.IPointBuilder;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * The simple influx processor. Processor knows all available influx point builders. When the
 * default data comes the processor with check if influx is online and if point builder exists for
 * the given data type. If so an influx point will be created and inserted to {@link #influxDbDao}.
 *
 * @author Ivan Senic
 *
 */
public class InfluxProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link InfluxDBDao} to write to.
	 */
	private InfluxDBDao influxDbDao;

	/**
	 * Map of all builders.
	 */
	private Map<Class<? extends DefaultData>, IPointBuilder<DefaultData>> builderMap;

	/**
	 * Default constructor.
	 *
	 * @param influxDbDao
	 *            {@link InfluxDBDao}
	 * @param builders
	 *            All available influx point builders.
	 */
	@Autowired
	public InfluxProcessor(InfluxDBDao influxDbDao, List<IPointBuilder<DefaultData>> builders) {
		this.influxDbDao = influxDbDao;
		if (CollectionUtils.isEmpty(builders)) {
			builderMap = Collections.emptyMap();
		} else {
			builderMap = new HashMap<>();
			for (IPointBuilder<DefaultData> builder : builders) {
				for (Class<? extends DefaultData> clazz : builder.getDataClasses()) {
					builderMap.put(clazz, builder);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		IPointBuilder<DefaultData> defaultDataPointBuilder = builderMap.get(defaultData.getClass());
		Collection<Builder> builders = defaultDataPointBuilder.createBuilders(defaultData);
		for (Builder builder : builders) {
			influxDbDao.insert(builder.build());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return influxDbDao.isConnected() && builderMap.containsKey(defaultData.getClass()) && isValidData(defaultData);
	}

	/**
	 * Check if the data if valid. We currently have following constraints:
	 *
	 * <ul>
	 * <li>If {@link TimerData} instance then charting must be set
	 * <li>If {@link JmxSensorValueData} instance then it must be boolean or numeric
	 * </ul>
	 *
	 * @param defaultData
	 *            Data to check.
	 * @return True if data should be used with the pointer and sent to influx.
	 */
	private boolean isValidData(DefaultData defaultData) {
		if ((defaultData instanceof TimerData) && !((TimerData) defaultData).isCharting()) {
			return false;
		}

		if ((defaultData instanceof JmxSensorValueData) && !((JmxSensorValueData) defaultData).isBooleanOrNumeric()) {
			return false;
		}

		if ((defaultData instanceof InvocationSequenceData) && (((InvocationSequenceData) defaultData).getParentSequence() != null)) {
			return false;
		}

		return true;
	}

}
