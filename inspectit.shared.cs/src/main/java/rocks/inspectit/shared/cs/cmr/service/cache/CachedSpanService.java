package rocks.inspectit.shared.cs.cmr.service.cache;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;

/**
 * Wrapper for the {@link ISpanService} that caches spans so that we don't need to have constant
 * round-trips to the server to load span by span.
 *
 * @author Ivan Senic
 *
 */
public class CachedSpanService implements ISpanService {

	/**
	 * Logger of this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CachedSpanService.class);

	/**
	 * Real {@link ISpanService} to load data from.
	 */
	private ISpanService service;

	/**
	 * Span cache for avoiding reloading.
	 */
	private LoadingCache<SpanIdent, Span> cache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<SpanIdent, Span>() {
		@Override
		public Span load(SpanIdent ident) throws Exception {
			// collect all from same trace as we expect them to be asked in same point of time
			// this way we don't do round-trips to only get one span
			Collection<? extends Span> spans = service.getSpans(ident.getTraceId());

			// add other spans from the trace to the cache and returned the one we tried to load
			if (CollectionUtils.isNotEmpty(spans)) {
				Span value = null;
				for (Span span : spans) {
					if (Objects.equals(ident, span.getSpanIdent())) {
						value = span;
					} else {
						cache.put(span.getSpanIdent(), span);
					}
				}

				// return asked one
				if (null != value) {
					return value;
				}
			}

			// if we can not locate, throw exception as per loading cache api
			throw new Exception("Span with ident " + ident + " can not be found.");
		}
	});

	/**
	 * Default constructor.
	 *
	 * @param service
	 *            Real {@link ISpanService} to load data from.
	 */
	public CachedSpanService(ISpanService service) {
		this.service = service;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getRootSpans(int limit, Date fromDate, Date toDate, ResultComparator<AbstractSpan> resultComparator) {
		// call service
		Collection<? extends Span> spans = service.getRootSpans(limit, fromDate, toDate, resultComparator);

		// cache results
		for (Span span : spans) {
			cache.put(span.getSpanIdent(), span);
		}

		// then return
		return spans;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Span> getSpans(long traceId) {
		// call service
		// note we can not go via the cache here as we are not sure that cache holds all spans
		// belonging to the trace (due to eviction and/or spans coming to the server from several
		// agents in different time)
		Collection<? extends Span> spans = service.getSpans(traceId);

		// cache results
		for (Span span : spans) {
			cache.put(span.getSpanIdent(), span);
		}

		// then return
		return spans;
	}

	/**
	 * Returns the span with given span ident. This method will look into the cache first and only
	 * if span is not cached try to load it from the original span service.
	 *
	 * @param spanIdent
	 *            {@link SpanIdent} that identifies the span.
	 * @return {@link Span} or <code>null</code> if span is not in a cache and not found on the
	 *         server.
	 */
	@Override
	public Span get(SpanIdent spanIdent) {
		try {
			return cache.get(spanIdent);
		} catch (ExecutionException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Span with span ident " + spanIdent.toString() + " can not be loaded.", e);
			}
			return null;
		}
	}

	/**
	 * Gets {@link #cache}.
	 *
	 * @return {@link #cache}
	 */
	public LoadingCache<SpanIdent, Span> getCache() {
		return this.cache;
	}

}
