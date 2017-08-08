package rocks.inspectit.shared.cs.data.invocationtree;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.opentracing.References;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * This class is used to build trees of traces. The tree is based on invocation sequences, spans and
 * span identifiers (in case the actual span cannot be loaded/is missing).
 *
 * @author Marius Oehler
 *
 */
public class InvocationTreeElement implements Comparable<InvocationTreeElement> {

	/**
	 * Types a {@link InvocationTreeElement} can be.
	 *
	 * @author Marius Oehler
	 *
	 */
	public enum TreeElementType {
		/**
		 * The data is of type {@link Span}.
		 */
		SPAN,

		/**
		 * The data is of type {@link InvocationSequenceData}.
		 */
		INVOCATION_SEQUENCE,

		/**
		 * The data is of type {@link SpanIdent}.
		 */
		MISSING_SPAN;
	}

	/**
	 * The data object.
	 */
	private final Object dataElement;

	/**
	 * The type of this element.
	 */
	private final TreeElementType type;

	/**
	 * The parent element.
	 */
	private InvocationTreeElement parent;

	/**
	 * The child elements.
	 */
	private final List<InvocationTreeElement> children = new ArrayList<>();

	/**
	 * Whether this element is the root of the tree. (NOTE: this is not relating to the root of a
	 * trace but only the current tree)
	 */
	private boolean root = false;

	/**
	 * If this element or child elements contains nested exceptions.
	 */
	private boolean nestedExceptions;

	/**
	 * If this element or child elements contains nested SQL statements.
	 */
	private boolean nestedSqls;

	/**
	 * Lookup map of all elements in the current tree.
	 */
	private Map<Object, InvocationTreeElement> lookupMap;

	/**
	 * Constructor.
	 *
	 * @param dataElement
	 *            the data object of this tree element
	 */
	public InvocationTreeElement(Object dataElement) {
		if (dataElement instanceof Span) {
			type = TreeElementType.SPAN;
		} else if (dataElement instanceof InvocationSequenceData) {
			type = TreeElementType.INVOCATION_SEQUENCE;
		} else if (dataElement instanceof SpanIdent) {
			type = TreeElementType.MISSING_SPAN;
		} else {
			throw new IllegalArgumentException("A data element of type '" + dataElement.getClass().getName() + "' is currently not supported.");
		}

		this.dataElement = dataElement;
	}

	/**
	 * Returns the tree (starting from this element) as a stream of {@link InvocationTreeElement}.
	 *
	 * @return returns a {@link Stream} of {@link InvocationTreeElement}s
	 */
	public Stream<InvocationTreeElement> asStream() {
		return Stream.concat(Stream.of(this), children.stream().flatMap(InvocationTreeElement::asStream));
	}

	/**
	 * Builds the {@link #lookupMap} map of the current tree.
	 */
	private void buildLookupMap() {
		lookupMap = new HashMap<>();

		// building the lookup map beginning at the root element
		getRootElement().asStream().forEach(e -> lookupMap.put(calculateKey(e.getDataElement()), e));
	}

	/**
	 * Calculates a key which is used for the lookup map and identifying
	 * {@link InvocationTreeElement} based on the contained data object.
	 *
	 * @param object
	 *            the object to generate a key for
	 * @return the key of the given object
	 */
	private String calculateKey(Object object) {
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
	 * The comparison is based on the elements' timestamps.
	 * <p>
	 * {@inheritDoc}
	 *
	 */
	@Override
	public int compareTo(InvocationTreeElement o) {
		if ((getTimeStamp() == null) && (o.getTimeStamp() == null)) {
			return 0;
		}
		if ((getTimeStamp() == null) && (o.getTimeStamp() != null)) {
			return 1;
		}
		if ((getTimeStamp() != null) && (o.getTimeStamp() == null)) {
			return -1;
		}

		return getTimeStamp().compareTo(o.getTimeStamp());
	}

	/**
	 * Gets {@link #children}.
	 *
	 * @return {@link #children}
	 */
	public List<InvocationTreeElement> getChildren() {
		return this.children;
	}

	/**
	 * Gets {@link #dataElement}.
	 *
	 * @return {@link #dataElement}
	 */
	public Object getDataElement() {
		return this.dataElement;
	}

	/**
	 * Returns exclusive duration.
	 *
	 * @return Returns exclusive duration.
	 */
	public double getExclusiveDuration() {
		if (!isSpan()) {
			return 0D;
		}

		double childrenDuration = 0;
		for (InvocationTreeElement child : getChildren()) {
			childrenDuration += child.getParentRelativeDuration();
		}

		double exclusiveDuration = ((Span) getDataElement()).getDuration() - childrenDuration;

		return Math.max(exclusiveDuration, 0D);
	}

	/**
	 * Returns exclusive duration.
	 *
	 * @return Returns exclusive duration.
	 */
	public double getExclusivePercentage() {
		InvocationTreeElement topElement = this;
		while ((topElement.getParent() != null) && !topElement.isConsideredAsync()) {
			topElement = topElement.getParent();
		}

		if (topElement.isSpan()) {
			return getExclusiveDuration() / ((Span) topElement.getDataElement()).getDuration();
		}
		return 0D;
	}

	/**
	 * Gets {@link #parent}.
	 *
	 * @return {@link #parent}
	 */
	public InvocationTreeElement getParent() {
		return this.parent;
	}

	/**
	 * Returns the duration of this trace part relative to it's parent.
	 *
	 * @return Returns the duration of this trace part relative to it's parent.
	 */
	public double getParentRelativeDuration() {
		if (isConsideredAsync() || !isSpan()) {
			return 0D;
		} else {
			return ((Span) dataElement).getDuration();
		}
	}

	/**
	 * Returns the root element of the tree where this element belongs to.
	 *
	 * @return the root {@link InvocationTreeElement} of the current tree
	 */
	public InvocationTreeElement getRootElement() {
		InvocationTreeElement root = this;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		return root;
	}

	/**
	 * Returns the number of elements this tree contains.
	 *
	 * @return the size of the tree
	 */
	public int getSize() {
		if (lookupMap == null) {
			buildLookupMap();
		}

		return lookupMap.size();
	}

	/**
	 * Returns the timestamp of this element. The time stamp will be gathered from the data object.
	 *
	 * @return the {@link Timestamp} of this element
	 */
	private Timestamp getTimeStamp() {
		if (dataElement instanceof Span) {
			return ((Span) dataElement).getTimeStamp();
		} else if (dataElement instanceof InvocationSequenceData) {
			return ((InvocationSequenceData) dataElement).getTimeStamp();
		}
		return null;
	}

	/**
	 * Gets {@link #type}.
	 *
	 * @return {@link #type}
	 */
	public TreeElementType getType() {
		return this.type;
	}

	/**
	 * Returns whether child elements are existing.
	 *
	 * @return <code>true</code> if this element contains child elements
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * Gets {@link #nestedExceptions}.
	 *
	 * @return {@link #nestedExceptions}
	 */
	public boolean hasNestedExceptions() {
		return this.nestedExceptions;
	}

	/**
	 * Gets {@link #nestedSqls}.
	 *
	 * @return {@link #nestedSqls}
	 */
	public boolean hasNestedSqls() {
		return this.nestedSqls;
	}

	/**
	 * Returns whether this element is considered as asynchronous. It will return always
	 * <code>false</code> if the type is not {@link TreeElementType#SPAN}.
	 *
	 * @return <code>true</code> if the element is considered as asynchronous
	 */
	public boolean isConsideredAsync() {
		if (!isSpan()) {
			return false;
		}
		return References.FOLLOWS_FROM.equals(((Span) dataElement).getReferenceType());
	}

	/**
	 * Returns whether this element is the root element of the tree. (NOTE: this is not relating to
	 * the root of a trace but only the current tree)
	 *
	 * @return {@link #root}
	 */
	public boolean isRoot() {
		return this.root;
	}

	/**
	 * Returns whether this element is from type {@link TreeElementType#SPAN} and, thus, containing
	 * a {@link Span} object.
	 *
	 * @return <code>true</code> if the current type is {@link TreeElementType#SPAN}
	 */
	public boolean isSpan() {
		return type == TreeElementType.SPAN;
	}

	/**
	 * Returns the {@link InvocationTreeElement} of the current tree which contains the given data
	 * object. <code>null</code> is returned if no {@link InvocationTreeElement} matches.
	 *
	 * @param element
	 *            the data object
	 * @return {@link InvocationTreeElement} containing the given object otherwise <code>null</code>
	 */
	public InvocationTreeElement lookup(Object element) {
		if (lookupMap == null) {
			buildLookupMap();
		}

		return lookupMap.get(calculateKey(element));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (dataElement == null) {
			return "<null data>";
		}

		StringBuilder builder = new StringBuilder(dataElement.getClass().getSimpleName()).append(" [id=");

		if (type == TreeElementType.INVOCATION_SEQUENCE) {
			InvocationSequenceData sequence = (InvocationSequenceData) dataElement;
			builder.append(sequence.getId());
		} else if (type == TreeElementType.SPAN) {
			Span span = (Span) dataElement;
			builder.append(span.getSpanIdent().getId());
			if (!isRoot()) {
				builder.append(";parent=");
				builder.append(span.getParentSpanId());
			}
		} else if (type == TreeElementType.MISSING_SPAN) {
			SpanIdent spanIdent = (SpanIdent) dataElement;
			builder.append(spanIdent.getId()).append(";missing");
		}

		if (isRoot()) {
			builder.append(";root");
		}

		builder.append(']');
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.children == null) ? 0 : this.children.hashCode());
		result = (prime * result) + ((this.dataElement == null) ? 0 : this.dataElement.hashCode());
		result = (prime * result) + (this.nestedExceptions ? 1231 : 1237);
		result = (prime * result) + (this.nestedSqls ? 1231 : 1237);
		result = (prime * result) + ((this.parent == null) ? 0 : this.parent.hashCode());
		result = (prime * result) + (this.root ? 1231 : 1237);
		result = (prime * result) + ((this.type == null) ? 0 : this.type.hashCode());
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
		InvocationTreeElement other = (InvocationTreeElement) obj;
		if (this.children == null) {
			if (other.children != null) {
				return false;
			}
		} else if (!this.children.equals(other.children)) {
			return false;
		}
		if (this.dataElement == null) {
			if (other.dataElement != null) {
				return false;
			}
		} else if (!this.dataElement.equals(other.dataElement)) {
			return false;
		}
		if (this.nestedExceptions != other.nestedExceptions) {
			return false;
		}
		if (this.nestedSqls != other.nestedSqls) {
			return false;
		}
		if (this.parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!this.parent.equals(other.parent)) {
			return false;
		}
		if (this.root != other.root) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

	/**
	 * #################################
	 *
	 * The following methods are used by the
	 * {@link rocks.inspectit.ui.rcp.editor.tree.util.InvocationTreeBuilder}.
	 *
	 * #################################
	 */

	/**
	 * Adds a new child to this element and sets the child's parent.
	 *
	 * @param child
	 *            {@lin InvocationTreeElement} to add as a child
	 */
	void addChild(InvocationTreeElement child) {
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Sets {@link #nestedExceptions}.
	 *
	 * @param hasNestedExceptions
	 *            New value for {@link #nestedExceptions}
	 */
	void setHasNestedExceptions(boolean hasNestedExceptions) {
		this.nestedExceptions = hasNestedExceptions;
	}

	/**
	 * Sets {@link #nestedSqls}.
	 *
	 * @param hasNestedSqls
	 *            New value for {@link #nestedSqls}
	 */
	void setHasNestedSqls(boolean hasNestedSqls) {
		this.nestedSqls = hasNestedSqls;
	}

	/**
	 * Sets {@link #parent}.
	 *
	 * @param parent
	 *            New value for {@link #parent}
	 */
	void setParent(InvocationTreeElement parent) {
		this.parent = parent;
	}

	/**
	 * Sets {@link #root}.
	 *
	 * @param root
	 *            New value for {@link #root}
	 */
	void setRoot(boolean root) {
		this.root = root;
	}
}
