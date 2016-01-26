package rocks.inspectit.shared.all.instrumentation.classcache.util;

import java.util.Comparator;

import rocks.inspectit.shared.all.instrumentation.classcache.Type;

/**
 * Set for the different {@link Type}s that inserts element in set based on their FQN.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type
 */
public class TypeSet<E extends Type> extends SortedArraySet<E> {

	/**
	 * FQN comparator that defines elements as being equal by the FQN.
	 */
	public static final Comparator<Type> FQN_COMPARATOR = new Comparator<Type>() {

		/**
		 * {@inheritDoc}
		 */
		public int compare(Type o1, Type o2) {
			return o1.getFQN().compareTo(o2.getFQN());
		}
	};

	/**
	 * Default constructor. Uses {@link #FQN_COMPARATOR} for comparing elements in the set.
	 */
	@SuppressWarnings("unchecked")
	public TypeSet() {
		super((Comparator<E>) FQN_COMPARATOR);
	}

}
