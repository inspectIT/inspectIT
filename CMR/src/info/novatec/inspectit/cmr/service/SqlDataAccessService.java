package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.SqlDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.spring.logger.Log;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Patrice Bouillet
 * 
 */
@Service
public class SqlDataAccessService implements ISqlDataAccessService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * The sql DAO.
	 */
	@Autowired
	private SqlDataDao sqlDataDao;

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		List<SqlStatementData> result = sqlDataDao.getAggregatedSqlStatements(sqlStatementData);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<SqlStatementData> getAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		List<SqlStatementData> result = sqlDataDao.getAggregatedSqlStatements(sqlStatementData, fromDate, toDate);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<SqlStatementData> getParameterAggregatedSqlStatements(SqlStatementData sqlStatementData) {
		List<SqlStatementData> result = sqlDataDao.getParameterAggregatedSqlStatements(sqlStatementData);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<SqlStatementData> getParameterAggregatedSqlStatements(SqlStatementData sqlStatementData, Date fromDate, Date toDate) {
		List<SqlStatementData> result = sqlDataDao.getParameterAggregatedSqlStatements(sqlStatementData, fromDate, toDate);
		return result;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-SQL Data Access Service active...");
		}
	}

}
