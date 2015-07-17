package info.novatec.inspectit.cmr.processor;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.util.Collection;

import org.hibernate.StatelessSession;

/**
 * Abstract processor class for CMR data.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractCmrDataProcessor {

	/**
	 * Processes many {@link DefaultData} objects.
	 * 
	 * @param defaultDatas
	 *            Default data objects.
	 * @param session
	 *            {@link StatelessSession} to save data in DB if needed.
	 */
	public void process(Collection<? extends DefaultData> defaultDatas, StatelessSession session) {
		for (DefaultData defaultData : defaultDatas) {
			process(defaultData, session);
		}
	}

	/**
	 * Processes one {@link DefaultData} object. This method will check is
	 * {@link #canBeProcessed(DefaultData)} is true, and then delegate the processing to the
	 * {@link #processData(DefaultData)} method.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @param session
	 *            {@link StatelessSession} to save data in DB if needed.
	 */
	public void process(DefaultData defaultData, StatelessSession session) {
		if (canBeProcessed(defaultData)) {
			processData(defaultData, session);
		}
	}

	/**
	 * Concrete method for processing. Implemented by sub-classes.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @param session
	 *            {@link StatelessSession} to save data in DB if needed.
	 */
	protected abstract void processData(DefaultData defaultData, StatelessSession session);

	/**
	 * Returns if the {@link DefaultData} object can be processed by this
	 * {@link AbstractDataProcessor}.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @return True if data can be processed, false otherwise.
	 */
	public abstract boolean canBeProcessed(DefaultData defaultData);
}
