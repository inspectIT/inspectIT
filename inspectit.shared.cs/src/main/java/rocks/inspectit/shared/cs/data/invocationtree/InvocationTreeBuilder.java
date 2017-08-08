package rocks.inspectit.shared.cs.data.invocationtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private Map<Long, InvocationTreeElement> spansExistingInTree = new HashMap<>();

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
	 * Builds the tree.
	 *
	 * @return The root {@link InvocationTreeElement} of the resulting tree. If no tree could be
	 *         built, e.g. when finding no root element <code>null</code> will be returned.
	 */
	public InvocationTreeElement build() {
		prepare();

		tree = findRoot();
		if (tree == null) {
			return null;
		}

		resolve();

		sortChildren(tree);

		// load invocation sequences for calculating span details
		if (mode == Mode.ONLY_SPANS_WITH_SDK) {
			invocationSequences = loadInvocationSequences();
		}

		// creating a mapping from span ids to invocation sequences - when two spans refers the same
		// trace, we are trying to return the trace which follows the span.
		Stream<InvocationSequenceData> invocationStream = InvocationSequenceDataHelper.asStream(invocationSequences);
		spanToInvocationSequenceMap = invocationStream.filter(i -> i.getSpanIdent() != null).collect(Collectors.toMap(i -> i.getSpanIdent().getId(), Function.identity(), (i1, i2) -> {
			if (i1.getParentSequence() == null) {
				return i1;
			} else {
				return i2;
			}
		}));

		// resolves the span details (like has nested exceptions..)
		tree.asStream().filter(e -> e.getType() == TreeElementType.SPAN).forEach(this::resolveSpanDetails);

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
		if (!currentSpan.isCaller()) {
			// server span - expect invocation sequence
			for (InvocationSequenceData data : invocationSequences) {
				if (InvocationSequenceDataHelper.hasSpanIdent(data) && data.getSpanIdent().equals(currentSpan.getSpanIdent())) {
					InvocationTreeElement child = createTreeElement(data, parent);
					resolveInvocationSequence(child);
				}
			}
		}

		for (Span span : spans) {
			// if span is already in the tree, skip it
			if (isSpanInTree(span.getSpanIdent())) {
				continue;
			}

			if (span.getParentSpanId() == currentSpan.getSpanIdent().getId()) {
				InvocationTreeElement child = createTreeElement(span, parent);
				resolveSpan(child);
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
		if (InvocationSequenceDataHelper.hasSpanIdent(currentData) && !isSpanInTree(currentData.getSpanIdent())) {
			Optional<Span> optionalSpan = getSpan(currentData.getSpanIdent());

			if (optionalSpan.isPresent()) {
				Span span = optionalSpan.get();
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
	 * Returns whether the current tree contains already a span with an id equals to the one of the
	 * given span ident.
	 *
	 * @param spanIdent
	 *            span ident to check
	 * @return <code>true</code> if the tree contains a span with the id of the given span ident
	 */
	private boolean isSpanInTree(SpanIdent spanIdent) {
		return spansExistingInTree.containsKey(spanIdent.getId());
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

			// loading all spans related to the set invocation sequence
			if (spanService != null) {
				Stream<InvocationSequenceData> invocationStream = InvocationSequenceDataHelper.asStream(invocationSequence);
				spans = invocationStream.map(i -> i.getSpanIdent()).filter(Objects::nonNull).map(spanService::get).filter(Objects::nonNull).collect(Collectors.toList());
			} else {
				spans = new ArrayList<>();
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
	 * Tryies to get the span with the given {@link SpanIdent}.
	 *
	 * @param ident
	 *            the span identifier to search for
	 * @return an {@link Optional} which may contain the {@link Span} with belongs to the given
	 *         {@link SpanIdent}
	 */
	private Optional<Span> getSpan(SpanIdent ident) {
		return spans.stream().filter(s -> s.getSpanIdent().equals(ident)).findAny();
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

			// put the span into the `spansExistingInTree` map in order to mark that it is existing
			// in the tree, now
			spansExistingInTree.put(span.getSpanIdent().getId(), element);
		}

		return element;
	}

	/**
	 * Finds and creates the {@link InvocationTreeElement} which represents the root of the tree.
	 * Note: This element must not be the root of a trace!
	 *
	 * @return returns the root {@link InvocationTreeElement}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private InvocationTreeElement findRoot() {
		if (mode == Mode.SINGLE) {
			InvocationSequenceData rootSequence = invocationSequence;
			while (rootSequence.getParentSequence() != null) {
				rootSequence = rootSequence.getParentSequence();
			}

			Object rootDataElement;

			if (InvocationSequenceDataHelper.hasSpanIdent(rootSequence)) {
				Optional span = getSpan(rootSequence.getSpanIdent());
				rootDataElement = span.orElse(rootSequence);
			} else {
				rootDataElement = rootSequence;
			}

			InvocationTreeElement element = createTreeElement(rootDataElement);
			element.setRoot(true);
			return element;
		}

		// find root span
		Optional<Span> rootSpan = spans.stream().filter(Span::isRoot).findAny();
		if (rootSpan.isPresent()) {
			InvocationTreeElement element = createTreeElement(rootSpan.get());
			element.setRoot(true);
			return element;
		}

		// no root span has been found - trying to find a root invocation sequence
		// strategy: time based
		Optional<InvocationSequenceData> rootSequence = invocationSequences.stream().min((i1, i2) -> i1.getTimeStamp().compareTo(i2.getTimeStamp()));

		if (rootSequence.isPresent()) {
			InvocationTreeElement element = createTreeElement(rootSequence.get());
			element.setRoot(true);
			return element;
		}

		return null;
	}
}
