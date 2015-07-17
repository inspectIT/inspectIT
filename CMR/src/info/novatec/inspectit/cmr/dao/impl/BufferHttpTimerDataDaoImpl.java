package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.HttpTimerDataDao;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.indexing.query.factory.impl.HttpTimerDataQueryFactory;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Provides <code>HttpTimerData</code> information from the CMR internal in memory buffer.
 * 
 * @author Stefan Siegl
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
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		IIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		IIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, null, null);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getTaggedAggregatedHttpTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		IIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod));
	}

}
