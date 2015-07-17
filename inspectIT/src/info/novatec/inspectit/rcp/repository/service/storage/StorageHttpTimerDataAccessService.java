package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.indexing.query.factory.impl.HttpTimerDataQueryFactory;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * {@link IHttpTimerDataAccessService} for storage purposes.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageHttpTimerDataAccessService extends AbstractStorageService<HttpTimerData> implements IHttpTimerDataAccessService {

	/**
	 * Comparator used to compare on the timestamp.
	 */
	private static final Comparator<HttpTimerData> TIMESTAMP_COMPARATOR = new Comparator<HttpTimerData>() {

		@Override
		public int compare(HttpTimerData o1, HttpTimerData o2) {
			return ObjectUtils.compare(o1.getTimeStamp(), o2.getTimeStamp());
		}

	};

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<HttpTimerData> indexingTree;

	/**
	 * Index query provider.
	 */
	private HttpTimerDataQueryFactory<StorageIndexQuery> httpDataQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, null, null);
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(true, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, null, null);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getTaggedAggregatedTimerData(HttpTimerData httpData, boolean includeRequestMethod, Date fromDate, Date toDate) {
		StorageIndexQuery query = httpDataQueryFactory.getFindAllTaggedHttpTimersQuery(httpData, fromDate, toDate);
		return super.executeQuery(query, new HttpTimerDataAggregator(false, includeRequestMethod));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<HttpTimerData> getChartingHttpTimerDataFromDateToDate(Collection<HttpTimerData> templates, Date fromDate, Date toDate, boolean retrieveByTag) {
		if (CollectionUtils.isNotEmpty(templates)) {
			StorageIndexQuery query = httpDataQueryFactory.getFindAllHttpTimersQuery(templates.iterator().next(), fromDate, toDate);

			if (!retrieveByTag) {
				Set<String> uris = new HashSet<String>();
				for (HttpTimerData httpTimerData : templates) {
					if (!HttpTimerData.UNDEFINED.equals(httpTimerData.getUri())) {
						uris.add(httpTimerData.getUri());
					}
				}
				query.addIndexingRestriction(IndexQueryRestrictionFactory.isInCollection("uri", uris));
			} else {
				Set<String> tags = new HashSet<String>();

				for (HttpTimerData httpTimerData : templates) {
					if (httpTimerData.hasInspectItTaggingHeader()) {
						tags.add(httpTimerData.getInspectItTaggingHeaderValue());
					}
				}
				query.addIndexingRestriction(IndexQueryRestrictionFactory.isInCollection("inspectItTaggingHeaderValue", tags));
			}

			return super.executeQuery(query, TIMESTAMP_COMPARATOR);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<HttpTimerData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<HttpTimerData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param httpDataQueryFactory
	 *            the httpDataQueryFactory to set
	 */
	public void setHttpDataQueryFactory(HttpTimerDataQueryFactory<StorageIndexQuery> httpDataQueryFactory) {
		this.httpDataQueryFactory = httpDataQueryFactory;
	}

}
