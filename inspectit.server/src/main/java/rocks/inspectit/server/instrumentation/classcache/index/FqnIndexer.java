package rocks.inspectit.server.instrumentation.classcache.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.instrumentation.classcache.events.INodeChangeListener;
import rocks.inspectit.server.instrumentation.classcache.events.NodeEvent;
import rocks.inspectit.server.instrumentation.classcache.events.NodeEvent.NodeEventType;
import rocks.inspectit.server.instrumentation.classcache.events.ReferenceEvent;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeSet;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.pattern.WildcardMatchPattern;

/**
 * Fast type indexer by FQN name. Indexer can locate types by exact name or by startsWith approach.
 * <p>
 * Note that this indexer should not be used with multiple threads reading and writing. Multiple
 * threads reading is OK.
 *
 * @author Ivan Senic
 *
 * @param <E>
 *            Type being indexed.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class FqnIndexer<E extends Type> extends TypeSet<E> implements INodeChangeListener {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void informNodeChange(NodeEvent event) {
		if (NodeEventType.NEW.equals(event.getEventType())) {
			index((E) event.getType());
		} else if (NodeEventType.REMOVED.equals(event.getEventType())) {
			remove(event.getType());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void informReferenceChange(ReferenceEvent event) {
		// not interesting
	}

	/**
	 * Index one type.
	 *
	 * @param type
	 *            Type to index.
	 */
	void index(E type) {
		addOrUpdate(type);
	}

	/**
	 * Finds type by exact FQN name.
	 *
	 * @param fqn
	 *            Fully qualified class name.
	 * @return Returns type or <code>null</code> if it can not be found.
	 */
	public E lookup(String fqn) {
		int index = findRetrieveIndexFor(fqn);
		return getAt(index);
	}

	/**
	 * Finds all {@link NonPrimitiveType}s that start with given string.
	 *
	 * @param fqnWildCard
	 *            String that class should start with.
	 * @return All types starting with the given string.
	 */
	public Collection<E> findStartsWith(String fqnWildCard) {
		long minMaxIndex = findStartsWithMinMaxIndexes(fqnWildCard);

		int min = getLowerInt(minMaxIndex);
		int max = getUpperInt(minMaxIndex);

		if (min < 0) {
			return Collections.emptyList();
		}

		int size = size();
		List<E> results = new ArrayList<>(max - min + 1);
		for (int i = min; i <= max && max < size; i++) {
			results.add(getAt(i));
		}
		return results;
	}

	/**
	 * Finds all indexed types.
	 *
	 * @return All currently indexed types.
	 */
	public Collection<E> findAll() {
		return new ArrayList<>(this);
	}

	/**
	 * Finds types by {@link IMatchPattern}. FQN of each returned type will match the given pattern.
	 *
	 * @param matchPattern
	 *            {@link IMatchPattern}
	 * @return Collection of types which FQN matches the pattern.
	 */
	public Collection<E> findByPattern(IMatchPattern matchPattern) {
		String template = matchPattern.getPattern();
		if (null == template) {
			return Collections.emptyList();
		}

		Collection<E> results;
		if (WildcardMatchPattern.isPattern(template)) {
			// for wild card use startsWith method
			String startsWithCriteria = template.substring(0, template.indexOf('*'));
			results = findStartsWith(startsWithCriteria);
			// make sure each type it's fitting to the pattern
			for (Iterator<E> it = results.iterator(); it.hasNext();) {
				E element = it.next();
				if (!matchPattern.match(element.getFQN())) {
					it.remove();
				}
			}
		} else {
			E type = lookup(template);
			if (null != type) {
				results = new ArrayList<>(1);
				results.add(type);
			} else {
				results = Collections.emptyList();
			}
		}
		return results;
	}

	/**
	 * Finds index for a FQN to retrieve.
	 *
	 * @param fqn
	 *            String representing the FQN.
	 * @return Index where element should be retrieved from or negative value if one can not be
	 *         located.
	 */
	private int findRetrieveIndexFor(String fqn) {
		int size = size();

		int min = 0;
		int max = size - 1;

		while (max >= min) {

			int mid = midpoint(min, max);
			int compare = getAt(mid).getFQN().compareTo(fqn);

			// if no difference then we have it
			if (0 == compare) {
				return mid;
			}

			// otherwise adapt min and max
			min = (compare < 0) ? mid + 1 : min;
			max = (compare > 0) ? mid - 1 : max;

		}

		return -1;
	}

	/**
	 * Find elements that starts with given {@link String}.
	 *
	 * @param fqnWildCard
	 *            String that elements should start with.
	 * @return Min and max index packed in a long. Min index is in lower int, while max index is in
	 *         upper int. If no element is found, min index will be packed as -1.
	 */
	private long findStartsWithMinMaxIndexes(String fqnWildCard) {
		int size = size();
		int minIndex = -1;
		int maxIndex = -1;

		// first search for any element that starts with and remember the index
		int anyElementIndex = -1;
		int min = 0;
		int max = size - 1;

		while (max >= min) {

			int mid = midpoint(min, max);

			String fqn = getAt(mid).getFQN();
			if (fqn.startsWith(fqnWildCard)) {
				// setting index for the found element in both
				anyElementIndex = mid;
				break;
			}

			int compare = fqn.compareTo(fqnWildCard);

			// otherwise adapt min and max
			min = (compare < 0) ? mid + 1 : min;
			max = (compare > 0) ? mid - 1 : max;

		}

		// if we did not find any element then return nothing
		if (anyElementIndex == -1) {
			return pack(0, -1);
		}

		// else first go for minimum index then
		// somewhere between 0 and anyElementIndex
		min = 0;
		max = anyElementIndex;

		while (max >= min) {

			int mid = midpoint(min, max);

			int compare = getAt(mid).getFQN().startsWith(fqnWildCard) ? 1 : -1;

			// adapt min and max
			min = (compare < 0) ? mid + 1 : min;
			max = (compare > 0) ? mid - 1 : max;

			if (compare > 0) {
				minIndex = mid;
			}

			if (mid == min) {
				break;
			}

		}

		// and then for maximum index
		// somewhere between anyElementIndex and last
		min = anyElementIndex;
		max = size - 1;

		while (max >= min) {

			int mid = midpoint(min, max);

			int compare = getAt(mid).getFQN().startsWith(fqnWildCard) ? -1 : 1;

			// adapt min and max
			min = (compare < 0) ? mid + 1 : min;
			max = (compare > 0) ? mid - 1 : max;

			if (compare < 0) {
				maxIndex = mid;
			}

			if (mid == max) {
				break;
			}

		}

		// pack result and return
		return pack(maxIndex, minIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int size = size();
		for (int i = 0; i < size; i++) {
			sb.append("[" + i + "] = " + getAt(i).getFQN() + "\n");
		}
		return sb.toString();
	}

}
