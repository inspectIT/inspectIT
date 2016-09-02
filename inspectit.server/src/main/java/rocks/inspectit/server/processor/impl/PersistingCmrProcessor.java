package rocks.inspectit.server.processor.impl;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Processor that saves objects to database via {@link EntityManager}.
 *
 * @author Ivan Senic
 *
 */
public class PersistingCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * List of classes that must be saved by this simple saver.
	 */
	private List<Class<? extends DefaultData>> mandatoryClasses;

	/**
	 * List of classes that can be saved by this simple saver.
	 */
	private List<Class<? extends DefaultData>> optionalClasses;

	/**
	 * Indicates whether optional data shell be persisted.
	 */
	@Value("${timeseries.dataPersistence.active}")
	boolean persistOptionalData;

	/**
	 * Default constructor.
	 *
	 * @param mandatoryClasses
	 *            List of classes that must be saved by this simple saver.
	 * @param optionalClasses
	 *            List of classes that can be saved by this simple saver.
	 */
	public PersistingCmrProcessor(List<Class<? extends DefaultData>> mandatoryClasses, List<Class<? extends DefaultData>> optionalClasses) {
		this.mandatoryClasses = mandatoryClasses;
		if (null == this.mandatoryClasses) {
			this.mandatoryClasses = Collections.emptyList();
		}

		this.optionalClasses = optionalClasses;
		if (null == this.optionalClasses) {
			this.optionalClasses = Collections.emptyList();
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
			return mandatoryClasses.contains(defaultData.getClass()) || (persistOptionalData && optionalClasses.contains(defaultData.getClass()));
		}
		return false;
	}

}
