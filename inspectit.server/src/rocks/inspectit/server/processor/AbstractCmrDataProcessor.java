package info.novatec.inspectit.cmr.processor;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;

import java.util.Collection;

import javax.persistence.EntityManager;

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
	 * @param entityManager
	 *            {@link EntityManager} to save data in DB if needed.
	 */
	public void process(Collection<? extends DefaultData> defaultDatas, EntityManager entityManager) {
		for (DefaultData defaultData : defaultDatas) {
			process(defaultData, entityManager);
		}
	}

	/**
	 * Processes one {@link DefaultData} object. This method will check is
	 * {@link #canBeProcessed(DefaultData)} is true, and then delegate the processing to the
	 * {@link #processData(DefaultData)} method.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @param entityManager
	 *            {@link EntityManager} to save data in DB if needed.
	 */
	public void process(DefaultData defaultData, EntityManager entityManager) {
		if (canBeProcessed(defaultData)) {
			processData(defaultData, entityManager);
		}
	}

	/**
	 * Concrete method for processing. Implemented by sub-classes.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @param entityManager
	 *            {@link EntityManager} to save data in DB if needed.
	 */
	protected abstract void processData(DefaultData defaultData, EntityManager entityManager);

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
