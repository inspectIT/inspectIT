package info.novatec.inspectit.instrumentation.classcache.util;

import info.novatec.inspectit.instrumentation.classcache.MethodType;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Set for the {@link MethodType}s that inserts element in set based on method name and parameters.
 *
 * @author Ivan Senic
 */
public class MethodTypeSet extends SortedArraySet<MethodType> { // NOPMD

	/**
	 * {@link MethodType} comparator that defines elements as being equal by the method.
	 */
	public static final Comparator<MethodType> METHOD_COMPARATOR = new Comparator<MethodType>() {

		/**
		 * {@inheritDoc}
		 */
		public int compare(MethodType o1, MethodType o2) {
			// first name
			int result = o1.getName().compareTo(o2.getName());

			if (result != 0) {
				return result;
			}

			result = o1.getReturnType().compareTo(o2.getReturnType());
			if (result != 0) {
				return result;
			}

			List<String> parameters1 = o1.getParameters();
			List<String> parameters2 = o2.getParameters();
			// then parameter size
			result = compareInts(parameters1.size(), parameters2.size());

			if (result != 0) {
				return result;
			}

			// then parameter by parameter
			for (Iterator<String> it1 = parameters1.iterator(), it2 = parameters2.iterator(); it1.hasNext() && it2.hasNext();) {
				result = it1.next().compareTo(it2.next());

				if (result != 0) {
					return result;
				}
			}

			return 0;
		}
	};

	/**
	 * Default constructor.
	 */
	public MethodTypeSet() {
		super(METHOD_COMPARATOR);
	}

	/**
	 * Compares two int values.
	 *
	 * @param x
	 *            first
	 * @param y
	 *            second
	 * @return result based on the compare interface
	 */
	public static int compareInts(int x, int y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

}
