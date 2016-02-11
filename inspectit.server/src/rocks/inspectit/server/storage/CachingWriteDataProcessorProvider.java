package info.novatec.inspectit.cmr.storage;

import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.Aggregators;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.query.factory.impl.ExceptionSensorDataQueryFactory;
import info.novatec.inspectit.indexing.query.factory.impl.SqlStatementDataQueryFactory;
import info.novatec.inspectit.indexing.query.factory.impl.TimerDataQueryFactory;
import info.novatec.inspectit.indexing.query.provider.impl.StorageIndexQueryProvider;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;
import info.novatec.inspectit.storage.processor.write.AbstractWriteDataProcessor;
import info.novatec.inspectit.storage.processor.write.impl.QueryCachingDataProcessor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 * Configuration class for specifying the caching processors for the storage writer.
 * <p>
 * These will be autowired to each storage writer.
 * 
 * @author Ivan Senic
 * 
 */
@Configuration
public class CachingWriteDataProcessorProvider {

	/**
	 * {@link StorageIndexQueryProvider} is needed here because we are caching storage queries.
	 */
	@Autowired
	private StorageIndexQueryProvider storageIndexQueryProvider;

	/**
	 * {@link TimerDataQueryFactory}.
	 */
	private TimerDataQueryFactory<StorageIndexQuery> timerDataQueryFactory;

	/**
	 * {@link SqlStatementDataQueryFactory}.
	 */
	private SqlStatementDataQueryFactory<StorageIndexQuery> sqlStatementDataQueryFactory;

	/**
	 * {@link ExceptionSensorDataQueryFactory}.
	 */
	private ExceptionSensorDataQueryFactory<StorageIndexQuery> exceptionSensorDataQueryFactory;

	/**
	 * @return Returns {@link AbstractWriteDataProcessor} for caching the {@link TimerData} view.
	 */
	@Bean
	@Lazy
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public AbstractWriteDataProcessor getTimerDataCachingDataProcessor() {
		IIndexQuery query = timerDataQueryFactory.getAggregatedTimerDataQuery(new TimerData(), null, null);
		IAggregator<TimerData> aggregator = Aggregators.TIMER_DATA_AGGREGATOR;
		return new QueryCachingDataProcessor<>(query, aggregator);
	}

	/**
	 * @return Returns {@link AbstractWriteDataProcessor} for caching the {@link SqlStatementData}
	 *         view.
	 */
	@Bean
	@Lazy
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public AbstractWriteDataProcessor getSqlDataCachingDataProcessor() {
		IIndexQuery query = sqlStatementDataQueryFactory.getAggregatedSqlStatementsQuery(new SqlStatementData(), null, null);
		IAggregator<SqlStatementData> aggregator = Aggregators.SQL_STATEMENT_DATA_AGGREGATOR;
		return new QueryCachingDataProcessor<>(query, aggregator);
	}

	/**
	 * @return Returns {@link AbstractWriteDataProcessor} for caching the
	 *         {@link ExceptionSensorData} grouped view.
	 */
	@Bean
	@Lazy
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public AbstractWriteDataProcessor getGroupedExceptionsDataCachingDataProcessor() {
		IIndexQuery query = exceptionSensorDataQueryFactory.getDataForGroupedExceptionOverviewQuery(new ExceptionSensorData(), null, null);
		IAggregator<ExceptionSensorData> aggregator = Aggregators.GROUP_EXCEPTION_OVERVIEW_AGGREGATOR;
		return new QueryCachingDataProcessor<>(query, aggregator);
	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void initFactories() {
		timerDataQueryFactory = new TimerDataQueryFactory<>();
		timerDataQueryFactory.setIndexQueryProvider(storageIndexQueryProvider);

		sqlStatementDataQueryFactory = new SqlStatementDataQueryFactory<>();
		sqlStatementDataQueryFactory.setIndexQueryProvider(storageIndexQueryProvider);

		exceptionSensorDataQueryFactory = new ExceptionSensorDataQueryFactory<>();
		exceptionSensorDataQueryFactory.setIndexQueryProvider(storageIndexQueryProvider);
	}

}
