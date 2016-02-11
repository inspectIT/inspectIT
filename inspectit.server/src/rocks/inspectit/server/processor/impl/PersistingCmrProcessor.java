package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

/**
 * Processor that saves objects to database via {@link EntityManager}.
 * 
 * @author Ivan Senic
 * 
 */
public class PersistingCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * List of classes that should be saved by this simple saver.
	 */
	private List<Class<? extends DefaultData>> classes;

	/**
	 * Default constructor.
	 * 
	 * @param classes
	 *            List of classes that should be saved by this simple saver.
	 */
	public PersistingCmrProcessor(List<Class<? extends DefaultData>> classes) {
		this.classes = classes;
		if (null == this.classes) {
			this.classes = Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		defaultData.setId(0);
		entityManager.persist(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		if (null != defaultData) {
			return classes.contains(defaultData.getClass());
		}
		return false;
	}

}
