package rocks.inspectit.server.diagnosis.engine.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Utility class to work with {@link Tag}s.
 *
 * @author Claudio Waldvogel
 * @see Tag
 */

public final class Tags {

	/**
	 * The name of the initial Tag. At least one rule implementation has to use this Tag. If not, no
	 * rule will ever fire.
	 */
	public static final String ROOT_TAG = "Root";

	/**
	 * Must not be instantiated.
	 */
	private Tags() {
		throw new UnsupportedOperationException("Must not be instantiated");
	}

	/**
	 * Factory method to create the root {@link Tag} of type {@link #ROOT_TAG}.
	 *
	 * @param input
	 *            The input object to be analyzed.
	 * @return A new Tag
	 */
	public static Tag rootTag(Object input) {
		return tag(ROOT_TAG, input);
	}

	/**
	 * Factory method to create a new Tag.
	 *
	 * @param type
	 *            The type of the {@link Tag}.
	 * @param input
	 *            The input object to be analyzed.
	 * @return A new Tag
	 */
	public static Tag tag(String type, Object input) {
		return tag(type, input, null);
	}

	/**
	 * Factory method to create a new Tag.
	 *
	 * @param type
	 *            The type of the {@link Tag}.
	 * @param input
	 *            The input object to be analyzed.
	 * @param parent
	 *            The parent {@link Tag}.
	 * @return A new Tag
	 */
	public static Tag tag(String type, Object input, Tag parent) {
		return new Tag(type, input, parent);
	}

	/**
	 * Factory method to create a collection of {@link Tag}s.
	 *
	 * @param type
	 *            The type of all {@link Tag}s.
	 * @param parent
	 *            The parent {@link Tag}.
	 * @param values
	 *            The values to be wrapped in {@link Tag}. For each value a new {@link Tag} is
	 *            created.
	 * @return A collection of Tags.
	 */
	public static Collection<Tag> tags(String type, Tag parent, Object... values) {
		return tags(type, parent, Arrays.asList(values));
	}

	/**
	 * Factory method to create a collection of {@link Tag}s.
	 *
	 * @param type
	 *            The type of all {@link Tag}s.
	 * @param parent
	 *            The parent {@link Tag}.
	 * @param values
	 *            The values to be wrapped in {@link Tag}. For each value a new {@link Tag} is
	 *            created.
	 * @return A collection of Tags.
	 */
	public static Collection<Tag> tags(String type, Tag parent, Collection<Object> values) {
		Collection<Tag> result = new ArrayList<>();
		for (Object value : values) {
			result.add(tag(type, value, parent));
		}
		return result;
	}

	/**
	 * Unwraps all requested parents from a {@link Tag}. Since a {@link Tag}s carries it's
	 * predecessor it is possible to extract any subset of all parents from the inheritance
	 * hierarhcy.
	 *
	 * @param tag
	 *            The {@link Tag} to be unwrapped
	 * @param parentTypes
	 *            The requested parentsTypes
	 * @return A collection of {@link Tag}.
	 *         <p>
	 *
	 *         <pre>
	 *    The collection contains:
	 *    <ul>
	 *        <li>The <code>tag</code> itself.</li>
	 *        <li>All <code>Tag</code>s of <code>parentsTypes</code>, which are available in the inheritance hierarchy of the <code>tag</code></li>
	 *    </ul>
	 *         </pre>
	 */
	public static Collection<Tag> unwrap(Tag tag, Collection<String> parentTypes) {
		// Insert Leaf itself to all tags list
		Set<Tag> tags = Sets.newHashSet(tag);
		List<String> requestedTypes = new ArrayList<>(parentTypes);
		// We must not continue, if only 1 tag type is required. This is already available
		if (requestedTypes.size() > 1) {
			Tag parent = tag.getParent();
			// Start stepping up the hierarchy chain to collect all required tags.
			// Our contract is that all inputs to a Rule must be on the same path, starting at the
			// leaf tag
			while (parent != null) {
				if (requestedTypes.contains(parent.getType())) {
					tags.add(parent);
					requestedTypes.remove(parent.getType());
				}
				parent = parent.getParent();
			}
		}
		return tags;
	}

}
