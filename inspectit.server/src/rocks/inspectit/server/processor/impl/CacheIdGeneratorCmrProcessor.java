package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.util.CacheIdGenerator;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Simple {@link AbstractCmrDataProcessor} that can assign the ID for the {@link DefaultData} using
 * {@link CacheIdGenerator}.
 * 
 * @author Ivan Senic
 * 
 */
public class CacheIdGeneratorCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link CacheIdGenerator}.
	 */
	@Autowired
	CacheIdGenerator cacheIdGenerator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		cacheIdGenerator.assignObjectAnId(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return null != defaultData;
	}

	/**
	 * Sets {@link #cacheIdGenerator}.
	 * 
	 * @param cacheIdGenerator
	 *            New value for {@link #cacheIdGenerator}
	 */
	public void setCacheIdGenerator(CacheIdGenerator cacheIdGenerator) {
		this.cacheIdGenerator = cacheIdGenerator;
	}

}
