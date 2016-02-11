package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;

/**
 * Processor that sets the correct exclusive time for {@link SqlStatementData} because it's always
 * known.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlExclusiveTimeCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		SqlStatementData sqlStatementData = (SqlStatementData) defaultData;
		sqlStatementData.setExclusiveCount(1L);
		sqlStatementData.setExclusiveDuration(sqlStatementData.getDuration());
		sqlStatementData.calculateExclusiveMax(sqlStatementData.getDuration());
		sqlStatementData.calculateExclusiveMin(sqlStatementData.getDuration());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof SqlStatementData;
	}

}
