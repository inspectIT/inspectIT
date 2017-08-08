package rocks.inspectit.shared.cs.data.invocationtree;

import java.util.List;
import java.util.stream.Collectors;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
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
	 *
	 * @param tree
	 *            the top element of the tree to extract the data
	 * @return {@link List} of {@link InvocationSequenceData} and {@link Span}
	 */
	public static List<Object> getDataElements(InvocationTreeElement tree) {
		return tree.asStream().filter(e -> e.getType() != TreeElementType.MISSING_SPAN).map(InvocationTreeElement::getDataElement)
				.filter(o -> (o instanceof Span) || (((InvocationSequenceData) o).getParentSequence() == null)).collect(Collectors.toList());
	}

	/**
	 * Returns all {@link InvocationSequenceData} which is contained in the given
	 * {@link InvocationTreeElement} or in its child nodes. {@link InvocationTreeElement} of type
	 * {@link InvocationTreeElement.TreeElementType#MISSING_SPAN} or
	 * {@link InvocationTreeElement.TreeElementType#SPAN} will be ignored.
	 *
	 * @param tree
	 *            the top element of the tree to extract the data
	 * @return {@link List} of {@link InvocationSequenceData}
	 */
	public static List<InvocationSequenceData> getInvocationSequences(InvocationTreeElement tree) {
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
	public static String toString(InvocationTreeElement tree) {
		StringBuilder builder = new StringBuilder();

		toStringHelper(tree, builder, 0);

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
	private static void toStringHelper(InvocationTreeElement element, StringBuilder builder, int intent) {
		for (int i = 0; i < intent; i++) {
			builder.append("   ");
		}
		if (intent > 0) {
			builder.append("└──");
		}

		builder.append(element.toString()).append('\n');

		for (InvocationTreeElement ite : element.getChildren()) {
			toStringHelper(ite, builder, intent + 1);
		}
	}
}
