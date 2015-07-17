package info.novatec.inspectit.jmeter;

import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.jmeter.data.AggregatedSQLResult;
import info.novatec.inspectit.jmeter.data.ResultBase;

import java.util.List;

/**
 * Sampler that requests the aggregated SQL view.
 * 
 * @author Stefan Siegl
 */
public class InspectITAggregatedSQL extends InspectITSamplerBase {

	/** query template. */
	private SqlStatementData sqlTemplate;
	/** platformId. */
	private Long platformId;
	/** Result. */
	private List<SqlStatementData> sqlsResult;

	@Override
	public Configuration[] getRequiredConfig() {
		return new Configuration[] { Configuration.PLATFORM_ID };
	}

	@Override
	public void setup() {
		sqlTemplate = new SqlStatementData();
		platformId = getValue(Configuration.PLATFORM_ID);
		sqlTemplate.setPlatformIdent(platformId);
	}

	@Override
	public ResultBase getResult() {
		return new AggregatedSQLResult(platformId, sqlsResult.size());
	}

	@Override
	public void run() {
		sqlsResult = repository.getSqlDataAccessService().getAggregatedSqlStatements(sqlTemplate);
	}
}
