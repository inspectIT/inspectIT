package rocks.inspectit.ui.rcp.editor.tree.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import io.opentracing.References;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.shared.cs.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;

/**
 * Model to easier represent spans in the tree view.
 *
 * @author Ivan Senic
 *
 */
public final class TraceTreeData implements Comparable<TraceTreeData> {

	/**
	 * Span that belongs to this data.
	 */
	private final Span span;

	/**
	 * Parent.
	 */
	private TraceTreeData parent;

	/**
	 * Children of the data.
	 */
	private List<TraceTreeData> children = new ArrayList<>(0);

	/**
	 * Invocations that belong to this trace part.
	 */
	private List<InvocationSequenceData> invocations = new ArrayList<>(0);

	/**
	 * Default constructor. User {@link #buildModel(Collection, Collection)}.
	 *
	 * @param span
	 *            Span
	 */
	private TraceTreeData(Span span) {
		if (null == span) {
			throw new IllegalArgumentException("Span must not be null.");
		}

		this.span = span;
	}

	/**
	 * Builds the model form the collection of spans and invocations.
	 *
	 * @param spans
	 *            Spans. Must include at least one root span. It's expected that all given spans
	 *            belong to the same trace.
	 * @param invocations
	 *            Invocation that belong to the same trace as given spans.
	 * @return {@link TraceTreeData} containing complete model.
	 */
	public static TraceTreeData buildModel(Collection<? extends Span> spans, Collection<InvocationSequenceData> invocations) {
		Span root = null;
		Multimap<Long, Span> parentToSpanMap = MultimapBuilder.hashKeys().arrayListValues().build();
		for (Span span : spans) {
			if (span.isRoot()) {
				root = span;
			} else {
				parentToSpanMap.put(span.getParentSpanId(), span);
			}
		}

		if (null == root) {
			throw new IllegalArgumentException("Can not construct model without the root.");
		}

		TraceTreeData rootData = new TraceTreeData(root);
		combine(rootData, root, parentToSpanMap);

		for (InvocationSequenceData invocation : invocations) {
			rootData.processInvocation(invocation);
		}

		return rootData;
	}

	/**
	 * Combines the Spans from the map with the given trace tree data / span.
	 *
	 * @param rootData
	 *            {@link TraceTreeData}
	 * @param ref
	 *            Span contained in the rootData
	 * @param parentToSpanMap
	 *            map
	 */
	private static void combine(TraceTreeData rootData, Span ref, Multimap<Long, Span> parentToSpanMap) {
		Collection<Span> spans = parentToSpanMap.removeAll(ref.getSpanIdent().getId());
		if (CollectionUtils.isNotEmpty(spans)) {
			for (Span span : spans) {
				TraceTreeData data = new TraceTreeData(span);
				rootData.addChild(data);
				combine(data, span, parentToSpanMap);
			}
		}
	}

	/**
	 * Collects all invocations in this trace data and it's children and add's it to the given list.
	 *
	 * @param root
	 *            {@link TraceTreeData} to start from.
	 * @param list
	 *            List to add invoc to.
	 * @return returns given list for easier connecting.
	 */
	public static List<InvocationSequenceData> collectInvocations(TraceTreeData root, List<InvocationSequenceData> list) {
		if (null == root) {
			return list;
		}

		list.addAll(root.getInvocations());

		for (TraceTreeData child : root.getChildren()) {
			collectInvocations(child, list);
		}

		return list;
	}

	/**
	 * Collects all spans in this trace data and it's children and add's it to the given list.
	 *
	 * @param root
	 *            {@link TraceTreeData} to start from.
	 * @param list
	 *            List to add spans to.
	 * @return returns given list for easier connecting.
	 */
	public static List<Span> collectSpans(TraceTreeData root, List<Span> list) {
		if (null == root) {
			return list;
		}

		list.add(root.getSpan());

		for (TraceTreeData child : root.getChildren()) {
			collectSpans(child, list);
		}

		return list;
	}

	/**
	 * Searches the {@link TraceTreeData} for the given span ident, going to the trace children in
	 * order to locate the ident.
	 *
	 * @param root
	 *            {@link TraceTreeData} to start from.
	 * @param spanIdent
	 *            ident
	 * @return Found data or <code>null</code>
	 */
	public static TraceTreeData getForSpanIdent(TraceTreeData root, SpanIdent spanIdent) {
		if (null == root) {
			return null;
		}

		if (root.hasSpanIdent(spanIdent)) {
			return root;
		}

		for (TraceTreeData child : root.getChildren()) {
			TraceTreeData containing = getForSpanIdent(child, spanIdent);
			if (null != containing) {
				return containing;
			}
		}

		return null;
	}

	/**
	 * If this trace part is considered to be asynchronous.
	 *
	 * @return If this trace part is considered to be asynchronous.
	 */
	public boolean isConsideredAsync() {
		return References.FOLLOWS_FROM.equals(span.getReferenceType());
	}

	/**
	 * Returns exclusive duration.
	 *
	 * @return Returns exclusive duration.
	 */
	public double getExclusiveDuration() {
		double d = 0;
		for (TraceTreeData child : children) {
			d += child.getParentRelativeDuration();
		}
		double exclusive = span.getDuration() - d;
		// ensure we never return negative values here
		return Math.max(exclusive, 0d);
	}

	/**
	 * Returns exclusive duration.
	 *
	 * @return Returns exclusive duration.
	 */
	public double getExclusivePercentage() {
		// go up in the tree
		double d = span.getDuration();
		TraceTreeData data = this;
		while (((null != data.getParent()) && !data.isConsideredAsync())) {
			d = data.getParent().getSpan().getDuration();
			data = data.getParent();
		}
		return getExclusiveDuration() / d;
	}

	/**
	 * Returns the duration of this trace part relative to it's parent.
	 *
	 * @return Returns the duration of this trace part relative to it's parent.
	 */
	public double getParentRelativeDuration() {
		if (isConsideredAsync()) {
			return 0;
		} else {
			return span.getDuration();
		}
	}

	/**
	 * Returns true if any of the spans in this {@link TraceTreeData} has given span ident.
	 *
	 * @param ident
	 *            Trace tree data.
	 * @return Returns true if any of the spans in this {@link TraceTreeData} has given span ident.
	 */
	public boolean hasSpanIdent(SpanIdent ident) {
		return Objects.equals(ident, span.getSpanIdent());
	}

	/**
	 * Returns true if any of the {@link #invocations} has nested sqls.
	 *
	 * @return Returns true if any of the {@link #invocations} has nested sqls.
	 */
	public boolean hasSqlsInInvocations() {
		for (InvocationSequenceData invoc : invocations) {
			if (InvocationSequenceDataHelper.hasNestedSqlStatements(invoc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any of the {@link #invocations} has nested sqls.
	 *
	 * @return Returns true if any of the {@link #invocations} has nested sqls.
	 */
	public boolean hasExceptionsInInvocations() {
		for (InvocationSequenceData invoc : invocations) {
			if (InvocationSequenceDataHelper.hasNestedExceptions(invoc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds child to this trace data. Takes care of back-reference.
	 *
	 * @param data
	 *            child
	 */
	private void addChild(TraceTreeData data) {
		children.add(data);
		data.setParent(this);
		Collections.sort(children);
	}

	/**
	 * Processes the invocation by checking if the invocation belongs to this trace data. If not
	 * delegated to the {@link #children}.
	 *
	 * @param invocation
	 *            {@link InvocationSequenceData}
	 * @return <code>true</code> if invocation was added, false otherwise
	 */
	private boolean processInvocation(InvocationSequenceData invocation) {
		if (spanIdentMacthes(span, invocation.getSpanIdent())) {
			addInvocation(invocation);
			return true;
		}

		for (TraceTreeData child : children) {
			if (child.processInvocation(invocation)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Adds one invocation to the {@link #invocations}.
	 *
	 * @param invocation
	 *            {@link InvocationSequenceData}
	 */
	private void addInvocation(InvocationSequenceData invocation) {
		invocations.add(invocation);
		Collections.sort(invocations, DefaultDataComparatorEnum.TIMESTAMP);
	}

	/**
	 * Returns true if the span ident matches the span.
	 *
	 * @param span
	 *            span
	 * @param spanIdent
	 *            span ident
	 * @return Returns true if the span ident matches the span.
	 */
	private static boolean spanIdentMacthes(Span span, SpanIdent spanIdent) {
		if (null != span) {
			return Objects.equals(span.getSpanIdent(), spanIdent);
		}
		return false;
	}

	/**
	 * Gets {@link #parent}.
	 *
	 * @return {@link #parent}
	 */
	public TraceTreeData getParent() {
		return this.parent;
	}

	/**
	 * Sets {@link #parent}.
	 *
	 * @param parent
	 *            New value for {@link #parent}
	 */
	private void setParent(TraceTreeData parent) {
		this.parent = parent;
	}

	/**
	 * Gets {@link #invocations}.
	 *
	 * @return {@link #invocations}
	 */
	public List<InvocationSequenceData> getInvocations() {
		return this.invocations;
	}

	/**
	 * Gets {@link #children}.
	 *
	 * @return {@link #children}
	 */
	public List<TraceTreeData> getChildren() {
		return this.children;
	}

	/**
	 * Gets {@link #span}.
	 *
	 * @return {@link #span}
	 */
	public Span getSpan() {
		return this.span;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(TraceTreeData o) {
		return ObjectUtils.compare(this.span.getTimeStamp(), o.span.getTimeStamp());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.span == null) ? 0 : this.span.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TraceTreeData other = (TraceTreeData) obj;
		if (this.span == null) {
			if (other.span != null) {
				return false;
			}
		} else if (!this.span.equals(other.span)) {
			return false;
		}
		return true;
	}

}
