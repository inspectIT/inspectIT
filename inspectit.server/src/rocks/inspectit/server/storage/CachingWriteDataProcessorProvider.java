package rocks.inspectit.server.storage;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.aggregation.Aggregators;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.ExceptionSensorDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.SqlStatementDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.TimerDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.query.provider.impl.StorageIndexQueryProvider;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;
import rocks.inspectit.shared.cs.storage.processor.write.AbstractWriteDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.write.impl.QueryCachingDataProcessor;

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
