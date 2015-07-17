package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SqlStatementData;

import org.hibernate.StatelessSession;

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
	protected void processData(DefaultData defaultData, StatelessSession session) {
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
