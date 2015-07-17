package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.cmr.util.CacheIdGenerator;
import info.novatec.inspectit.communication.DefaultData;

import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;

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
	protected void processData(DefaultData defaultData, StatelessSession session) {
		cacheIdGenerator.assignObjectAnId(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return null != defaultData;
	}

}
