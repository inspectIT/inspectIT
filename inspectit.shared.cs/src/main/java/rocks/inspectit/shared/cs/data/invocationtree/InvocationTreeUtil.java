package rocks.inspectit.shared.cs.data.invocationtree;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.opentracing.References;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeElement.TreeElementType;

/**
 * Utility class to simplify working with {@link InvocationTreeElement}s.
 *
 * @author Marius Oehler
 *
 */
public final class InvocationTreeUtil {

	/**
	 * Hidden constructor.
	 */
	private InvocationTreeUtil() {
	}

	/**
	 * Returns all data elements (of type {@link InvocationSequenceData} or {@link Span}) which are
	 * contained in the given {@link InvocationTreeElement} or in its child nodes.
	 * {@link InvocationTreeElement} of type
	 * {@link InvocationTreeElement.TreeElementType#MISSING_SPAN} will be ignored.
	 * <p>
	 * Note: If <code>includeNestedInvocationSequences</code> is set to false, all nested invocation
	 * sequences will be ignored and not added to the resulting list, thus, the invocation sequences
	 * in the returned list will be only the root invocation sequences.
	 *
	 * @param tree
	 *            the top element of the tree to extract the data
	 * @param includeNestedInvocationSequences
	 *            whether nested invocation sequences should also be referenced in the returned list
	 * @return {@link List} of {@link InvocationSequenceData} and {@link Span}
	 */
	public static List<Object> getDataElements(InvocationTreeElement tree, boolean includeNestedInvocationSequences) {
		if (tree == null) {
			return Collections.emptyList();
		}

		return tree.asStream().filter(e -> e.getType() != TreeElementType.MISSING_SPAN).map(InvocationTreeElement::getDataElement)
				.filter(o -> includeNestedInvocationSequences || (o instanceof Span) || (((InvocationSequenceData) o).getParentSequence() == null)).collect(Collectors.toList());
	}

	/**
	 * Returns all {@link InvocationSequenceData} which is contained in the given
	 * {@link InvocationTreeElement} or in its child nodes. {@link InvocationTreeElement} of type
	 * {@link InvocationTreeElement.TreeElementType#MISSING_SPAN} or
	 * {@link InvocationTreeElement.TreeElementType#SPAN} will be ignored.
	 * <p>
	 * Note: the returned list will contain only root invocation sequences
	 *
	 * @param tree
	 *            the top element of the tree to extract the data
	 * @return {@link List} of {@link InvocationSequenceData}
	 */
	public static List<InvocationSequenceData> getInvocationSequences(InvocationTreeElement tree) {
		if (tree == null) {
			return Collections.emptyList();
		}
		return tree.asStream().filter(e -> e.getType() == TreeElementType.INVOCATION_SEQUENCE).map(e -> (InvocationSequenceData) e.getDataElement()).filter(i -> i.getParentSequence() == null)
				.collect(Collectors.toList());
	}

	/**
	 * Creates a string representation of the tree starting the given {@link InvocationTreeElement}.
	 *
	 * @param tree
	 *            starting element of the tree
	 * @return string representation of the tree
	 */
	public static String stringify(InvocationTreeElement tree) {
		StringBuilder builder = new StringBuilder();

		stringifyHelper(tree, builder, 0);

		return builder.toString();
	}

	/**
	 * Helper method to stringify a tree consisting of {@link InvocationTreeElement}s.
	 *
	 * @param element
	 *            most top element
	 * @param builder
	 *            the builder to add the text
	 * @param intent
	 *            the current level of intent
	 */
	private static void stringifyHelper(InvocationTreeElement element, StringBuilder builder, int intent) {
		for (int i = 0; i < intent; i++) {
			builder.append("   ");
		}
		if (intent > 0) {
			builder.append("└──");
		}

		builder.append(element.toString()).append('\n');

		for (InvocationTreeElement ite : element.getChildren()) {
			stringifyHelper(ite, builder, intent + 1);
		}
	}

	/**
	 * Returns exclusive duration of the Span contained in the given {@link InvocationTreeElement}.
	 * Note: This method works only with {@link InvocationTreeElement} of type
	 * {@link TreeElementType#SPAN}.
	 *
	 * @param element
	 *            the {@link InvocationTreeElement} to calculate the exclusive duration for
	 * @return Returns exclusive duration.
	 */
	public static double calculateSpanExclusiveDuration(InvocationTreeElement element) {
		if (element == null) {
			return Double.NaN;
		}

		if (!element.isSpan()) {
			return 0D;
		}

		double childrenDuration = element.getChildren().stream().mapToDouble(InvocationTreeUtil::calculateSpanParentRelativeDuration).sum();

		double exclusiveDuration = ((Span) element.getDataElement()).getDuration() - childrenDuration;

		return Math.max(exclusiveDuration, 0D);
	}

	/**
	 * Returns the relative duration of the span contained in the given element. If the element is
	 * an asynchronous span, the method will return <code>0</code> because we cannot specify a
	 * relative duration of an asynchronous span relative to its parent.
	 * <p>
	 * If the element is not of type {@link TreeElementType#SPAN} the sum of all direct child spans
	 * are calculated. This means that when the child spans are not direct children in the tree but
	 * are connected via several invocation sequence to the parent span we handle them as well.
	 *
	 * @param element
	 *            the element to calculate the duration
	 * @return the relative duration of the span or the sum of all direct child spans
	 */
	private static double calculateSpanParentRelativeDuration(InvocationTreeElement element) {
		if (element.isSpan()) {
			if (InvocationTreeUtil.isConsideredAsync(element)) {
				return 0D;
			} else {
				return ((Span) element.getDataElement()).getDuration();
			}
		}

		// This case is only necessary in full trees which do not consist of span elements only but
		// multiple spans are connected together over several invocation sequences. In a tree
		// consisting only of span elements, this case can never happen.
		double nestedDuration = element.getChildren().stream().mapToDouble(InvocationTreeUtil::calculateSpanParentRelativeDuration).sum();

		return nestedDuration;
	}

	/**
	 * Returns exclusive duration as a percentage relative to its parents. Note: This method works
	 * only with {@link InvocationTreeElement} of type {@link TreeElementType#SPAN}.
	 *
	 * @param element
	 *            the {@link InvocationTreeElement} to calculate the exclusive percentage for
	 * @return Returns exclusive duration.
	 */
	public static double calculateSpanExclusivePercentage(InvocationTreeElement element) {
		if (element == null) {
			return Double.NaN;
		}

		InvocationTreeElement topElement = element;
		while ((topElement.getParent() != null) && !isConsideredAsync(topElement)) {
			topElement = topElement.getParent();
		}

		if (topElement.isSpan()) {
			return InvocationTreeUtil.calculateSpanExclusiveDuration(element) / ((Span) topElement.getDataElement()).getDuration();
		}
		return 0D;
	}

	/**
	 * Returns the root element of the tree where the given element belongs to.
	 *
	 * @param element
	 *            the {@link InvocationTreeElement} to find the root for
	 * @return the root {@link InvocationTreeElement} of the current tree
	 */
	public static InvocationTreeElement getRoot(InvocationTreeElement element) {
		if (element == null) {
			return null;
		}

		while (element.getParent() != null) {
			element = element.getParent();
		}
		return element;
	}

	/**
	 * Builds the {@link #lookupMap} map of the given tree. This allows direct access to the tree
	 * elements via data objects.
	 *
	 * @param tree
	 *            the tree
	 * @return the lookup map for the given tree
	 */
	public static Map<Object, InvocationTreeElement> buildLookupMap(InvocationTreeElement tree) {
		return InvocationTreeUtil.getRoot(tree).asStream().collect(Collectors.toMap(e -> calculateLookupKey(e.getDataElement()), Function.identity()));
	}

	/**
	 * Calculates a key which is used for the lookup map and identifying
	 * {@link InvocationTreeElement} based on the contained data object.
	 *
	 * @param object
	 *            the object to generate a key for
	 * @return the key of the given object
	 */
	public static String calculateLookupKey(Object object) {
		if (object == null) {
			return null;
		}

		if (object instanceof Span) {
			return "span_" + ((Span) object).getSpanIdent().getId();
		} else if (object instanceof InvocationSequenceData) {
			return "isd_" + ((InvocationSequenceData) object).getId();
		} else if (object instanceof SpanIdent) {
			return "span_" + ((SpanIdent) object).getId();
		}
		return null;
	}

	/**
	 * Returns the {@link InvocationTreeElement} of the given lookup map which contains the given
	 * data object. <code>null</code> is returned if no {@link InvocationTreeElement} matches.
	 *
	 * @param lookupMap
	 *            the lookup map
	 * @param object
	 *            the data object to search
	 * @return the {@link InvocationTreeElement} related to the given data object
	 */
	public static InvocationTreeElement lookupTreeElement(Map<Object, InvocationTreeElement> lookupMap, Object object) {
		if (lookupMap == null) {
			return null;
		}
		return lookupMap.get(calculateLookupKey(object));
	}

	/**
	 * Returns whether the given {@link InvocationTreeElement} is a child of a span or the parent is
	 * a span respectively.
	 *
	 * @param element
	 *            the element to check
	 * @return <code>true</code> when the parent is a span
	 */
	public static boolean isChildOfSpan(InvocationTreeElement element) {
		if (element == null) {
			throw new IllegalArgumentException("The given InvocationTreeElement must not be null.");
		}
		return (element.getParent() != null) && element.getParent().isSpan();
	}

	/**
	 * Returns whether the given element is considered as asynchronous. It will return always
	 * <code>false</code> if the type is not {@link TreeElementType#SPAN}.
	 *
	 * @param element
	 *            the element to check
	 * @return <code>true</code> if the element is considered as asynchronous
	 */
	public static boolean isConsideredAsync(InvocationTreeElement element) {
		if (!element.isSpan()) {
			return false;
		}
		return References.FOLLOWS_FROM.equals(((Span) element.getDataElement()).getReferenceType());
	}
}
