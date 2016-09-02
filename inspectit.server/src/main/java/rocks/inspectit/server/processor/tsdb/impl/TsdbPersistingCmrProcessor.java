package rocks.inspectit.server.processor.tsdb.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractChainedCmrDataProcessor;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.IInfluxDBService;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * This ChainedProcessor encapsulates all processors that write data to the timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class TsdbPersistingCmrProcessor extends AbstractChainedCmrDataProcessor {

	/**
	 * Default value for unavailable values.
	 */
	public static final String VALUE_NOT_AVAILABLE = "n/a";

	/**
	 * {@link IInfluxDBService} to check availability of the timeseries database service.
	 */
	@Autowired
	IInfluxDBService influxDbService;

	/**
	 * Constructor.
	 *
	 * @param dataProcessors
	 *            List of chained processors.
	 */
	public TsdbPersistingCmrProcessor(List<AbstractCmrDataProcessor> dataProcessors) {
		super(dataProcessors);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean shouldBePassedToChainedProcessors(DefaultData defaultData) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return influxDbService.isOnline();
	}

}
