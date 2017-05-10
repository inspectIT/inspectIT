package rocks.inspectit.ui.rcp.repository.service.storage;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.SpanQueryFactory;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

/**
 * Storage span service that always returns empty results. This will be changed in future when spans
 * are also saved to storage.
 *
 * @author Ivan Senic
 *
 */
public class StorageSpanService extends AbstractStorageService<AbstractSpan> implements ISpanService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<AbstractSpan> indexingTree;

	/**
	 * Index query provider.
	 */
	private SpanQueryFactory<StorageIndexQuery> spanQueryFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getRootSpans(int limit, Date fromDate, Date toDate, ResultComparator<AbstractSpan> resultComparator) {
		StorageIndexQuery query = spanQueryFactory.getRootSpansQuery(fromDate, toDate);
		if (null != resultComparator) {
			resultComparator.setCachedDataService(getStorageRepositoryDefinition().getCachedDataService());
			return super.executeQuery(query, resultComparator, limit);
		} else {
			return super.executeQuery(query, DefaultDataComparatorEnum.TIMESTAMP, limit);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getSpans(long traceId) {
		StorageIndexQuery query = spanQueryFactory.getSpans(traceId);
		return super.executeQuery(query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Span get(SpanIdent spanIdent) {
		StorageIndexQuery query = spanQueryFactory.get(spanIdent);
		List<AbstractSpan> spans = super.executeQuery(query);
		if (CollectionUtils.isNotEmpty(spans)) {
			return spans.iterator().next();
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStorageTreeComponent<AbstractSpan> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * Sets {@link #indexingTree}.
	 *
	 * @param indexingTree
	 *            New value for {@link #indexingTree}
	 */
	public void setIndexingTree(IStorageTreeComponent<AbstractSpan> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * Sets {@link #spanQueryFactory}.
	 *
	 * @param spanQueryFactory
	 *            New value for {@link #spanQueryFactory}
	 */
	public void setSpanQueryFactory(SpanQueryFactory<StorageIndexQuery> spanQueryFactory) {
		this.spanQueryFactory = spanQueryFactory;
	}

}
