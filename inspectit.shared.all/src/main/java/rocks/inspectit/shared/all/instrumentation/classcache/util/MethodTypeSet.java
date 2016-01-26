package rocks.inspectit.shared.all.instrumentation.classcache.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;

/**
 * Set for the {@link MethodType}s that inserts element in set based on method name, return type and
 * parameters.
 *
 * @author Ivan Senic
 */
public class MethodTypeSet extends SortedArraySet<MethodType> {

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

		/**
		 * Compares two int values.
		 *
		 * @param x
		 *            first
		 * @param y
		 *            second
		 * @return result based on the compare interface
		 */
		public int compareInts(int x, int y) {
			return (x < y) ? -1 : ((x == y) ? 0 : 1);
		}
	};

	/**
	 * Default constructor.
	 */
	public MethodTypeSet() {
		super(METHOD_COMPARATOR);
	}


}
