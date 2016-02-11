package rocks.inspectit.server.service;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.dao.SqlDataDao;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.ISqlDataAccessService;

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
