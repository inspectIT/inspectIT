package rocks.inspectit.shared.all.util;

import java.util.Comparator;
import java.util.List;

/**
 * Utility class for handling currently a proper equals comparison of objects.
 *
 * @author Patrice Bouillet
 *
 */
public final class ObjectUtils {

	/**
	 * Private constructor.
	 */
	private ObjectUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Compares two objects for equality, where either one or both objects may be <code>null</code>.
	 * This method is actually calling the
	 * {@link org.apache.commons.lang.ObjectUtils#equals(Object, Object)} method.
	 * </p>
	 *
	 * <pre>
	 * ObjectUtils.equals(null, null) = true
	 * ObjectUtils.equals(null, "") = false
	 * ObjectUtils.equals("", null) = false
	 * ObjectUtils.equals("", "") = true
	 * ObjectUtils.equals(Boolean.TRUE, null) = false
	 * ObjectUtils.equals(Boolean.TRUE, "true") = false
	 * ObjectUtils.equals(Boolean.TRUE, Boolean.TRUE) = true
	 * ObjectUtils.equals(Boolean.TRUE, Boolean.FALSE) = false
	 * </pre>
	 *
	 * @param object1
	 *            the first object, may be <code>null</code>
	 * @param object2
	 *            the second object, may be <code>null</code>
	 * @return <code>true</code> if the values of both objects are the same
	 */
	public static boolean equals(Object object1, Object object2) { // NOPMD
		return org.apache.commons.lang.ObjectUtils.equals(object1, object2);
	}

	/**
	 * Null safe compare. Returns following results:
	 *
	 * ObjectUtils.equals(Comparable, Object) = Comparable.compareTo(object)
	 * ObjectUtils.equals(null, Object) = -1 ObjectUtils.equals(Comparable, null) = 1
	 * ObjectUtils.equals(null, null) = 0
	 *
	 * @param <T>
	 *            Type of comparing objects.
	 * @param object1
	 *            Object1
	 * @param object2
	 *            Object2
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal
	 *         to, or greater than the specified object.
	 * @see Comparable#compareTo(Object)
	 */
	public static <T> int compare(Comparable<T> object1, T object2) {
		if ((null != object1) && (null != object2)) {
			return object1.compareTo(object2);
		} else if (null != object1) {
			return 1;
		} else if (null != object2) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * Compares two lists of string comparing one string in the list against string on the same
	 * position in the other list.<br>
	 * <br>
	 * Examples:<br>
	 * <br>
	 * {"ana", "b"} vs {"awa", "b"} - negative<br>
	 * {"ana", "b"} vs {"ana", "c"} - negative<br>
	 * {} vs {"a"} - negative<br>
	 * {"a"} vs {"b", "c"} - negative<br>
	 * {"a"} vs {"a", "b"} - negative<br>
	 * {"a"} vs <code>null</code> - negative<br>
	 * <code>null</code> vs {} - positive<br>
	 * <code>null</code> vs <code>null</code> - 0<br>
	 *
	 * @param list1
	 *            First list of strings.
	 * @param list2
	 *            Second list of strings.
	 * @return a negative integer, zero, or a positive integer as the first argument is less than,
	 *         equal to, or greater than the second.
	 * @see Comparator#compare(Object, Object)
	 */
	public static int compare(List<String> list1, List<String> list2) {
		if ((null != list1) && (null != list2)) {
			int i = 0;
			int listSize1 = list1.size();
			int listSize2 = list2.size();
			while ((i < listSize1) && (i < listSize2)) {
				int result = ObjectUtils.compare(list1.get(i), list2.get(i));
				if (0 != result) {
					return result;
				}
				i++;
			}
			return listSize1 - listSize2;
		} else if (null != list1) {
			return 1;
		} else if (null != list2) {
			return -1;
		} else {
			return 0;
		}

	}

}
