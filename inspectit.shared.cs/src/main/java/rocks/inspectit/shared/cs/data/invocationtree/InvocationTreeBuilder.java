package rocks.inspectit.shared.cs.data.invocationtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeElement.TreeElementType;

/**
 * This class represents a builder to create trace trees consisting of
 * {@link InvocationTreeElement}s.
 *
 * @author Marius Oehler
 *
 */
public class InvocationTreeBuilder {

	/**
	 * Cache which is used by the builder to store already created trees.
	 */
	private static Cache<String, InvocationTreeElement> treeCache = CacheBuilder.newBuilder().maximumSize(25).build();

	/**
	 * The modes in which the builder can be used.
	 *
	 * @author Marius Oehler
	 *
	 */
	public enum Mode {
		/**
		 * The builder creates a tree containing all spans and invocations of a certain trace but no
		 * SDK spans.
		 */
		ALL,

		/**
		 * The builder creates a tree containing all spans of a certain trace including SDK spans
		 * but no invocations.
		 */
		ONLY_SPANS_WITH_SDK,

		/**
		 * The builder creates a tree containing all spans which are directly related to a certain
		 * span or its nested spans. No SDK spans will be existing in the tree.
		 */
		SINGLE;
	}

	/**
	 * The span service.
	 */
	private ISpanService spanService;

	/**
	 * The invocation service.
	 */
	private IInvocationDataAccessService invocationService;

	/**
	 * List of spans to use in the tree.
	 */
	private List<Span> spans = Collections.emptyList();

	/**
	 * List of invocation sequences to use in the tree.
	 */
	private Collection<InvocationSequenceData> invocationSequences = Collections.emptyList();

	/**
	 * Lookup map for mapping span ids to {@link InvocationTreeElement}s. Elements existing in this
	 * map are already put in the resulting tree.
	 */
	private Map<Long, InvocationTreeElement> spanLookupMap = new HashMap<>();

	/**
	 * Lookup map for mapping span ids to {@link InvocationTreeElement}s.
	 */
	private Map<Long, InvocationTreeElement> temporarySpanLookupMap = new HashMap<>();

	/**
	 * Lookup map for mapping span ids to {@link InvocationSequenceData}.
	 */
	private Map<Long, InvocationSequenceData> spanToInvocationSequenceMap = new HashMap<>();

	/**
	 * The tree which is build.
	 */
	private InvocationTreeElement tree;
	/**
	 * The trace id of the span to build.
	 */
	private Long traceId;

	/**
	 * The current builder mode.
	 */
	private Mode mode = Mode.ALL;

	/**
	 * The invocation sequence which should be used to build the tree. (only in SINGLE mode)
	 */
	private InvocationSequenceData invocationSequence;

	/**
	 * Whether the cache should be checked if the tree is already existing.
	 */
	private boolean useCache = true;

	/**
	 * Sets the span service to use.
	 *
	 * @param spanService
	 *            the span service to use
	 * @return this builder
	 */
	public InvocationTreeBuilder setSpanService(ISpanService spanService) {
		this.spanService = spanService;
		return this;
	}

	/**
	 * Sets the invocation data service to use.
	 *
	 * @param invocationService
	 *            the invocation data service to use
	 * @return this builder
	 */
	public InvocationTreeBuilder setInvocationService(IInvocationDataAccessService invocationService) {
		this.invocationService = invocationService;
		return this;
	}

	/**
	 * The id of the trace to build.
	 *
	 * @param traceId
	 *            the trace id to use
	 * @return this builder
	 */
	public InvocationTreeBuilder setTraceId(long traceId) {
		this.traceId = traceId;
		return this;
	}

	/**
	 * The builder mode which should be used.
	 *
	 * @param mode
	 *            the mode to use
	 * @return this builder
	 */
	public InvocationTreeBuilder setMode(Mode mode) {
		this.mode = mode;
		return this;
	}

	/**
	 * The invocation sequence to build the tree. (only necessary in SINGLE mode)
	 *
	 * @param invocationSequence
	 *            the invocation sequence to use
	 * @return this builder
	 */
	public InvocationTreeBuilder setInvocationSequence(InvocationSequenceData invocationSequence) {
		this.invocationSequence = invocationSequence;
		return this;
	}

	/**
	 * Specifies whether the cache should be used. If a tree has already been built, it will be
	 * queried from the cache if possible.
	 *
	 * @param useCache
	 *            whether the cache should be used
	 * @return this builder
	 */
	public InvocationTreeBuilder setUseCache(boolean useCache) {
		this.useCache = useCache;
		return this;
	}

	/**
	 * Builds the tree.
	 *
	 * @return the root {@link InvocationTreeElement} of the resulting tree
	 */
	public InvocationTreeElement build() {
		if (useCache) {
			InvocationTreeElement cachedTree = treeCache.getIfPresent(getCacheKey());
			if (cachedTree != null) {
				return cachedTree;
			}
		}

		prepare();

		tree = findRoot();

		resolve();

		integrateRemainingSpans();

		sortChildren(tree);

		// load invocation sequences for calculating span details
		if (mode == Mode.ONLY_SPANS_WITH_SDK) {
			invocationSequences = loadInvocationSequences();
		}

		// creating a mapping from span ids to invocation sequences
		InvocationSequenceDataHelper.asStream(invocationSequences).filter(i -> i.getSpanIdent() != null).forEach(i -> spanToInvocationSequenceMap.put(i.getSpanIdent().getId(), i));

		// resolves the span details (like has nested exceptions..)
		tree.asStream().filter(e -> e.getType() == TreeElementType.SPAN).forEach(this::resolveSpanDetails);

		// always put the tree to the cache
		treeCache.put(getCacheKey(), tree);

		return tree;
	}

	/**
	 * ################################################################
	 *
	 * The following methods are used by the builder internally.
	 *
	 * ################################################################
	 */

	/**
	 * Starts resolving the tree elements.
	 */
	private void resolve() {
		if (tree.isSpan()) {
			resolveSpan(tree);
		} else {
			resolveInvocationSequence(tree);
		}
	}

	/**
	 * Resolves a span element.
	 *
	 * @param parent
	 *            a {@link InvocationTreeElement} from type {@link TreeElementType#SPAN}.
	 */
	private void resolveSpan(InvocationTreeElement parent) {
		Span currentSpan = (Span) parent.getDataElement();
		if (currentSpan.isCaller()) {
			// client span - expect server span
			for (Span span : spans) {
				if (spanLookupMap.containsKey(span.getSpanIdent().getId())) {
					continue;
				}

				if (span.getParentSpanId() == currentSpan.getSpanIdent().getId()) {
					InvocationTreeElement child = createTreeElement(span, parent);
					resolveSpan(child);
				}
			}
		} else {
			// server span - expect invocation sequence
			for (InvocationSequenceData data : invocationSequences) {
				if ((data.getSpanIdent() != null) && (data.getSpanIdent().getId() == currentSpan.getSpanIdent().getId())) {
					InvocationTreeElement child = createTreeElement(data, parent);
					resolveInvocationSequence(child);
				}
			}
		}
	}

	/**
	 * Resolves a invocation sequence element.
	 *
	 * @param parent
	 *            a {@link InvocationTreeElement} from type
	 *            {@link TreeElementType#INVOCATION_SEQUENCE}.
	 */
	private void resolveInvocationSequence(InvocationTreeElement parent) {
		InvocationSequenceData currentData = (InvocationSequenceData) parent.getDataElement();
		if ((currentData.getSpanIdent() != null) && !spanLookupMap.containsKey(currentData.getSpanIdent().getId())) {
			Span span = getSpan(currentData.getSpanIdent());
			if (span != null) {
				InvocationTreeElement child = null;
				if (span.isCaller()) {
					// client span
					child = createTreeElement(span, parent);
				} else {
					// server span - only add if parent of invocation is not already the span
					if ((parent.getParent() == null) || !parent.getParent().isSpan() || (((Span) parent.getParent().getDataElement()).getSpanIdent().getId() != span.getSpanIdent().getId())) {
						child = createTreeElement(span, parent);
					}
				}
				if (child != null) {
					resolveSpan(child);
				}
			} else {
				// span is missing
				if (parent.isRoot()) {
					// change root element
					InvocationTreeElement newRoot = createTreeElement(currentData.getSpanIdent());
					newRoot.addChild(parent);

					newRoot.setRoot(true);
					parent.setRoot(false);

					tree = newRoot;
				} else {
					createTreeElement(currentData.getSpanIdent(), parent);
				}
			}
		}

		// add all nested invocations as children
		for (InvocationSequenceData data : currentData.getNestedSequences()) {
			InvocationTreeElement child = createTreeElement(data, parent);
			resolveInvocationSequence(child);
		}
	}

	/**
	 * Resolves additional span details (e.g. has nested exception or SQL data) for the given
	 * {@link InvocationTreeElement}.
	 *
	 * @param element
	 *            the {@link InvocationTreeElement} which details should be resolved
	 */
	private void resolveSpanDetails(InvocationTreeElement element) {
		if (!element.isSpan()) {
			return;
		}
		InvocationSequenceData sequenceData = spanToInvocationSequenceMap.get(((Span) element.getDataElement()).getSpanIdent().getId());
		if (sequenceData != null) {
			element.setHasNestedSqls((sequenceData.isNestedSqlStatements() != null) && sequenceData.isNestedSqlStatements());
			element.setHasNestedExceptions((sequenceData.isNestedExceptions() != null) && sequenceData.isNestedExceptions());
		}
	}

	/**
	 * Calculates a string which can be used as a key for caching this tree.
	 *
	 * @return key for caching the tree
	 */
	private String getCacheKey() {
		StringBuilder builder = new StringBuilder();

		builder.append(mode.toString()).append('/');
		builder.append(traceId);
		if (invocationSequence != null) {
			builder.append('/').append(invocationSequence.getId());
		}

		return builder.toString();
	}

	/**
	 * Recursively iterating through the given {@link InvocationTreeElement} and its child elements
	 * and sorting all child collections from each {@link InvocationTreeElement}.
	 *
	 * @param element
	 *            the {@link InvocationTreeElement} where the iteration is starting
	 */
	private void sortChildren(InvocationTreeElement element) {
		if (element.hasChildren()) {
			Collections.sort(element.getChildren());
			element.getChildren().stream().forEach(this::sortChildren);
		}
	}

	/**
	 * Loads all the data which is required for building the tree.
	 */
	private void prepare() {
		if (mode == Mode.SINGLE) {
			invocationSequences = Arrays.asList(invocationSequence);
			spans = new ArrayList<>();

			// loading all spans related to the set invocation sequence
			if (spanService != null) {
				InvocationSequenceDataHelper.asStream(invocationSequence).map(i -> i.getSpanIdent()).filter(Objects::nonNull).map(spanService::get).filter(Objects::nonNull).forEach(spans::add);
			}
		} else {
			if (traceId == null) {
				throw new IllegalStateException("A trace id have to be specified when a span tree has to be build and the mode is not set to SINGLE.");
			}

			spans = loadSpans();
			if (mode == Mode.ALL) {
				invocationSequences = loadInvocationSequences();
			}
		}

		if (mode != Mode.ONLY_SPANS_WITH_SDK) {
			spans.removeIf(s -> (s.getPropagationType() == null) && !s.isRoot());
		}
	}

	/**
	 * Loads all spans which are related to the set trace.
	 *
	 * @return a {@link List} containing {@link Span}s
	 */
	private List<Span> loadSpans() {
		if (spanService == null) {
			return Collections.emptyList();
		}

		return new ArrayList<>(spanService.getSpans(traceId));
	}

	/**
	 * Loads all invocation sequences which are related to the set trace.
	 *
	 * @return a {@link List} containing {@link InvocationSequenceData}
	 */
	private List<InvocationSequenceData> loadInvocationSequences() {
		if (invocationService == null) {
			return Collections.emptyList();
		}

		return new ArrayList<>(invocationService.getInvocationSequenceDetail(traceId));
	}

	/**
	 * Gets the span with the given {@link SpanIdent}.
	 *
	 * @param ident
	 *            the span identifier to search for
	 * @return the {@link Span} with belongs to the given {@link SpanIdent}
	 */
	private Span getSpan(SpanIdent ident) {
		return spans.stream().filter(s -> s.getSpanIdent().equals(ident)).findAny().orElse(null);
	}

	/**
	 * Creates a new {@link InvocationTreeElement} without a parent.
	 *
	 * @param dataElement
	 *            the data element of the created {@link InvocationTreeElement}
	 * @return the newly created {@link InvocationTreeElement}
	 */
	private InvocationTreeElement createTreeElement(Object dataElement) {
		return createTreeElement(dataElement, null);
	}

	/**
	 * Creates a new {@link InvocationTreeElement} which is related to the given parent and contains
	 * the given data objet.
	 *
	 * @param dataElement
	 *            the data element of the created {@link InvocationTreeElement}
	 * @param parent
	 *            the parent element
	 * @return the newly created {@link InvocationTreeElement}
	 */
	private InvocationTreeElement createTreeElement(Object dataElement, InvocationTreeElement parent) {
		InvocationTreeElement element = new InvocationTreeElement(dataElement);

		if (parent != null) {
			parent.addChild(element);
		}

		if (dataElement instanceof Span) {
			Span span = (Span) dataElement;
			spanLookupMap.put(span.getSpanIdent().getId(), element);
		}

		return element;
	}

	/**
	 * Integrates all spans which belongs to the current trace but have not a direct reference via
	 * an existing invocation sequence (thus, have not added to the tree yet) into the tree.
	 */
	private void integrateRemainingSpans() {
		for (Span span : spans) {
			if (spanLookupMap.containsKey(span.getSpanIdent().getId())) {
				continue;
			}

			InvocationTreeElement parentElement = spanLookupMap.get(span.getParentSpanId());

			InvocationTreeElement element;
			if (parentElement == null) {
				// parent does not existing in the tree yet
				element = createTreeElement(span);
				temporarySpanLookupMap.put(span.getSpanIdent().getId(), element);

				// check if parent exists in temporary map
				parentElement = temporarySpanLookupMap.get(span.getParentSpanId());
				if (parentElement != null) {
					parentElement.addChild(element);
				}
			} else {
				element = createTreeElement(span, parentElement);
			}

			linkExistingTemporarySpans(element);
			resolveSpan(element);
		}
	}

	/**
	 * Checks whether {@link InvocationTreeElement}s exist which are not in the tree but belongs to
	 * the given {@link InvocationTreeElement}.
	 *
	 * @param parent
	 *            the parent {@link InvocationTreeElement}
	 */
	private void linkExistingTemporarySpans(InvocationTreeElement parent) {
		long parentSpanId = ((Span) parent.getDataElement()).getSpanIdent().getId();
		temporarySpanLookupMap.values().stream().filter(e -> ((Span) e.getDataElement()).getParentSpanId() == parentSpanId).forEach(parent::addChild);
	}

	/**
	 * Finds and creates the {@link InvocationTreeElement} which represents the root of the tree.
	 * Note: This element must not be the root of a trace!
	 *
	 * @return returns the root {@link InvocationTreeElement}
	 */
	private InvocationTreeElement findRoot() {
		if (mode == Mode.SINGLE) {
			InvocationSequenceData rootSequence = invocationSequence;
			while (rootSequence.getParentSequence() != null) {
				rootSequence = rootSequence.getParentSequence();
			}

			Object rootDataElement;

			if (rootSequence.getSpanIdent() == null) {
				rootDataElement = rootSequence;
			} else {
				Span span = getSpan(rootSequence.getSpanIdent());
				if (span == null) {
					rootDataElement = rootSequence;
				} else {
					rootDataElement = span;
				}
			}

			InvocationTreeElement element = createTreeElement(rootDataElement);
			element.setRoot(true);
			return element;
		}

		// find root span
		Span rootSpan = spans.stream().filter(Span::isRoot).findAny().orElse(null);
		if (rootSpan != null) {
			InvocationTreeElement element = createTreeElement(rootSpan);
			element.setRoot(true);
			return element;
		}

		// no root span has been found - trying to find a root invocation sequence
		// strategy: time based
		InvocationSequenceData rootSequence = invocationSequences.stream().min((i1, i2) -> i1.getTimeStamp().compareTo(i2.getTimeStamp())).orElse(null);

		if (rootSequence != null) {
			InvocationTreeElement element = createTreeElement(rootSequence);
			element.setRoot(true);
			return element;
		}

		throw new IllegalStateException("Cannot find any root element.");
	}
}
