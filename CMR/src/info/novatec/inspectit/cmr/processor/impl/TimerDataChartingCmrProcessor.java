package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.dao.impl.TimerDataAggregator;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;

import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor that saves {@link TimerData} or {@link HttpTimerData} to database correctly if the
 * charting is on.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataChartingCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link TimerDataAggregator} for {@link TimerData} aggregation.
	 */
	@Autowired
	TimerDataAggregator timerDataAggregator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, StatelessSession session) {
		if (defaultData instanceof HttpTimerData) {
			long bufferId = defaultData.getId();
			defaultData.setId(0);
			session.insert(defaultData);
			defaultData.setId(bufferId);
		} else {
			timerDataAggregator.processTimerData((TimerData) defaultData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof TimerData && ((TimerData) defaultData).isCharting();
	}

}
