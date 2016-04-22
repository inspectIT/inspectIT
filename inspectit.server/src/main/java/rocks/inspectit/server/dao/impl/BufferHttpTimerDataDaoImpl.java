package rocks.inspectit.server.dao.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.HttpTimerDataDao;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.HttpTimerDataAggregator;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.HttpTimerDataQueryFactory;

/**
 * Provides <code>HttpTimerData</code> information from the CMR internal in memory buffer. Fork&join
 * isn't used, because only one HTTP data per invocation is expected.
 *
 * @author Stefan Siegl
 *
 */
@Repository
public class BufferHttpTimerDataDaoImpl extends AbstractBufferDataDao<HttpTimerData> implements HttpTimerDataDao {

	/**
	 * Index query factory.
	 */
	@Autowired
	private HttpTimerDataQueryFactory<IIndexQuery> httpDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		IIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, null, null);
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		IIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		IIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, null, null);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		IIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod), false);
	}

}
