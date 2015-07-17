package info.novatec.inspectit.agent.buffer.impl;

import info.novatec.inspectit.agent.buffer.AbstractBufferStrategy;
import info.novatec.inspectit.agent.buffer.IBufferStrategy;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;

/**
 * The simplest version of a buffer strategy contains just the reference to one measurement list.
 * Every time a new one is added, the old one is thrown away.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SimpleBufferStrategy extends AbstractBufferStrategy<MethodSensorData> implements IBufferStrategy<MethodSensorData> {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * Stores the reference to the last given measurements.
	 */
	private List<MethodSensorData> measurements;

	/**
	 * True if measurements were added and available.
	 */
	private volatile boolean newMeasurements = false;

	/**
	 * {@inheritDoc}
	 */
	public final void addMeasurements(final List<MethodSensorData> measurements) {
		if (null == measurements) {
			throw new IllegalArgumentException("Measurements cannot be null!");
		}
		synchronized (this) {
			if (newMeasurements) {
				// if the measurements already exist, this buffer strategy will simply drop the old
				// ones, because we can not let the data pile up if the sending of the data is not
				// fast enough
				if (log.isDebugEnabled()) {
					log.debug("Possible data loss due to the excessive data creation on the Agent!");
				}
			}
			this.measurements = measurements;
			newMeasurements = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean hasNext() {
		return newMeasurements;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<MethodSensorData> next() {
		synchronized (this) {
			if (newMeasurements) {
				newMeasurements = false;
				return measurements;
			}
		}

		throw new NoSuchElementException();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void init(final Map<String, String> settings) {
		// nothing to do
	}

}
