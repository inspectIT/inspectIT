package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.dao.impl.TimerDataAggregator;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.spring.logger.Log;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
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
	 * Log for this class.
	 */
	@Log
	Logger log;

	/**
	 * {@link TimerDataAggregator} for {@link TimerData} aggregation.
	 */
	@Autowired
	TimerDataAggregator timerDataAggregator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		if (defaultData instanceof HttpTimerData) {
			try {
				HttpTimerData clone = (HttpTimerData) ((HttpTimerData) defaultData).clone();
				clone.setId(0);
				entityManager.persist(clone);
			} catch (CloneNotSupportedException e) {
				log.warn("TimerDataChartingCmrProcessor failed to clone the given HttpTimerData", e);
			}
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
