package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.dao.impl.TimerDataAggregator;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.impl.SerializationManager;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import javax.annotation.PostConstruct;
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
	 * Serialization manager provider for getting the {@link SerializationManager}.
	 */
	@Autowired
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * {@link SerializationManager} for cloning.
	 */
	SerializationManager serializationManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		if (defaultData instanceof HttpTimerData) {
			try {
				HttpTimerData clone = getClone((HttpTimerData) defaultData);
				entityManager.persist(clone);
			} catch (SerializationException e) {
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

	/**
	 * Creates the cloned {@link HttpTimerData} by using the kryo and {@link #serializationManager}.
	 * Sets id of the clone to zero.
	 * 
	 * @param original
	 *            Data to be cloned.
	 * @return Cloned {@link HttpTimerData} with id zero.
	 * @throws SerializationException
	 *             If serialization fails.
	 */
	private synchronized HttpTimerData getClone(HttpTimerData original) throws SerializationException {
		HttpTimerData httpTimerData = serializationManager.copy(original);
		httpTimerData.setId(0L);
		return httpTimerData;
	}

	/**
	 * Post construct.
	 */
	@PostConstruct
	protected void init() {
		serializationManager = serializationManagerProvider.createSerializer();
	}

}
