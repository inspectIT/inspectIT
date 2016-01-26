package rocks.inspectit.shared.all.instrumentation.classcache.util;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.collections.iterators.ObjectArrayIterator;

/**
 * Non-thread safe set that is backed with the sorted array.
 * <p>
 * The set must be initialized with the {@link Comparator} that will define sorting order of the
 * elements in the array. In addition, this comparator must only return <code>0</code> as a result
 * from a compare operation if elements compared are equal in set terms. Meaning that if comparator
 * returns zero <code>0</code> as a result from a compare for two different elements, only one can
 * be placed in the set.
 * <p>
 * This set is performing better in both terms of memory and speed over the similar implementations
 * (especially for small amount of elements). The adding/removing operations do require an array
 * copy operation. Note that comparator should be as fast as possible, since it's directly
 * influencing the speed of adding/removing the elements.
 * <p>
 * Regarding a memory, this class has a fixed cost of 16 bytes + the array holding the references to
 * the elements (that's 12 bytes + 4 bytes for each reference). Thus, there is a very small cost for
 * the maintaining the set.
 *
 * @author Ivan Senic
 *
 * @param <E>
 *            Type of the elements in set.
 */
public class SortedArraySet<E> extends AbstractCollection<E> implements UpdateableSet<E> {

	/**
	 * Mask for element already exists.
	 */
	private static final int ELEMENT_EXISTS_MASK = 1;

	/**
	 * Empty array to denote we have nothing in the set.
	 */
	private static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * The array holding the elements in the set.
	 */
	@SuppressWarnings("unchecked")
	private E[] array = (E[]) EMPTY_ARRAY;

	/**
	 * Comparator used in sorting the elements in the backing array. This comparator must only
	 * return <code>0</code> as a result from a compare operation if elements compared are equal in
	 * set terms. Meaning that if comparator returns zero <code>0</code> as a result from a compare
	 * for two different elements, only one can be placed in the set.
	 */
	private final Comparator<E> comparator;

	/**
	 * Default constructor.
	 *
	 * @param comparator
	 *            Comparator used in sorting the elements in the backing array. This comparator must
	 *            only return <code>0</code> as a result from a compare operation if elements
	 *            compared are equal in set terms. Meaning that if comparator returns zero
	 *            <code>0</code> as a result from a compare for two different elements, only one can
	 *            be placed in the set.
	 */
	public SortedArraySet(Comparator<E> comparator) {
		if (null == comparator) {
			throw new IllegalArgumentException("Comparator for the SortedArraySet must not be null.");
		}

		this.comparator = comparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean add(E e) {
		if (null == e) {
			throw new NullPointerException("Null element"); // NOPMD
		}

		E[] update;

		long l = findInsertDataFor(array, e);

		if (ELEMENT_EXISTS_MASK == getUpperInt(l)) {
			return false;
		}

		int i = getLowerInt(l);

		int length = array.length;
		update = (E[]) new Object[length + 1];

		System.arraycopy(array, 0, update, 0, i);
		System.arraycopy(array, i, update, i + 1, length - i);
		update[i] = e;

		array = update;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void addOrUpdate(E e) {
		if (null == e) {
			throw new NullPointerException("Null element"); // NOPMD
		}

		E[] update;

		long l = findInsertDataFor(array, e);
		int i = getLowerInt(l);

		int length = array.length;

		if (ELEMENT_EXISTS_MASK == getUpperInt(l)) {
			if (e == array[i]) {
				// this is the same object no need to update
				return;
			}

			// replacing the element just
			array[i] = e;
		} else {
			// we add new element
			update = (E[]) new Object[length + 1];

			System.arraycopy(array, 0, update, 0, i);
			System.arraycopy(array, i, update, i + 1, length - i);

			update[i] = e;
			array = update;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (null == o) {
			throw new NullPointerException("Null element"); // NOPMD
		}

		E[] update;

		int i = find(array, (E) o);

		if (i < 0) {
			return false;
		}

		int length = array.length;
		update = (E[]) new Object[length - 1];

		System.arraycopy(array, 0, update, 0, i);
		System.arraycopy(array, i + 1, update, i, length - i - 1);

		array = update;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return array.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return array.length == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return find(array, (E) o) > -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		return new ArraySetIterator(array);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return arrayCopyOf(array, array.length, array.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		array = (E[]) EMPTY_ARRAY;
	}

	/**
	 * Finds index for an element in the array. If the element does not exists by comparator it will
	 * return negative value.
	 *
	 * @param array
	 *            Array to find element in.
	 * @param element
	 *            Element to find.
	 * @return Index where element is located.
	 */
	private int find(E[] array, E element) {
		int min = 0;
		int max = array.length - 1;

		while (max >= min) {

			int mid = midpoint(min, max);
			int compare = comparator.compare(array[mid], element);

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
	 * Finds index for a element to insert and informs if element already exists. If the element
	 * already exists by comparator the function the upper int in the returned long will have the
	 * value of {@value #ELEMENT_EXISTS_MASK}. Lower int always holds the index value.
	 *
	 * @param array
	 *            Array to find element in.
	 * @param element
	 *            Element to find.
	 * @return Data where element should be inserted. The upper int in the returned long will have
	 *         the value of {@value #ELEMENT_EXISTS_MASK} if same element already exists. Lower int
	 *         always holds the index value.
	 */
	private long findInsertDataFor(E[] array, E element) {
		int min = 0;
		int max = array.length;

		while (max > min) {

			int mid = midpoint(min, max);
			int compare = comparator.compare(array[mid], element);

			// if no difference then we return mask and index
			if (0 == compare) {
				return pack(ELEMENT_EXISTS_MASK, mid);
			}

			// otherwise adapt min and max
			min = (compare < 0) ? mid + 1 : min;
			max = (compare > 0) ? mid : max;

			if (max == min) {
				return pack(0, max);
			}
		}

		return 0;
	}

	/**
	 * Finds midpoint.
	 *
	 * @param min
	 *            Minimum.
	 * @param max
	 *            Maximum.
	 * @return Midpoint between two.
	 */
	protected int midpoint(int min, int max) {
		return min + ((max - min) >> 1);
	}

	/**
	 * Packs two integers into a long.
	 *
	 * @param upper
	 *            Upper int.
	 * @param lower
	 *            Lower int.
	 * @return long as a result.
	 */
	protected long pack(int upper, int lower) {
		return ((long) upper) << 32 | lower;
	}

	/**
	 * Returns int value packed in the lower 4 bytes of the long.
	 *
	 * @param l
	 *            long
	 * @return Value that is packed to the lower int value.
	 */
	protected int getLowerInt(long l) {
		// masking all upper bits first and then returning the int
		return (int) (l & 0x00000000FFFFFFFF);
	}

	/**
	 * Returns int value packed in the lower 4 bytes of the long.
	 *
	 * @param l
	 *            long
	 * @return Value that is packed to the upper int value.
	 */
	protected int getUpperInt(long l) {
		// shifting 32 bits (4 bytes) and returning as int
		return (int) (l >> 32);
	}

	/**
	 * Returns element on specific index in array. Sub-classes can use this to retrieve elements
	 * from specific indexes.
	 *
	 * @param index
	 *            Index to retrieve.
	 * @return Element or <code>null</code> if index is out of bound.
	 */
	protected E getAt(int index) {
		if (index >= 0 && index < array.length) {
			return array[index];
		}
		return null;
	}

	/**
	 * JDK7 version of the Arrays.copyOf.
	 * <p>
	 * <b>IMPORTANT:</b> The class code is copied/taken from
	 * <a href="http://hg.openjdk.java.net/jdk7">OpenJDK JDK7</a>. Original authors are Josh Bloch,
	 * Neal Gafter and John Rose. License info can be found
	 * <a href="http://openjdk.java.net/faq/">here</a>.
	 *
	 * @param <T>
	 *            type of array
	 * @param <U>
	 *            original
	 * @param original
	 *            the array to be copied
	 * @param newLength
	 *            the length of the copy to be returned
	 * @param newType
	 *            the class of the copy to be returned
	 * @return a copy of the original array, truncated or padded with nulls to obtain the specified
	 *         length
	 */
	@SuppressWarnings("unchecked")
	private static <T, U> T[] arrayCopyOf(U[] original, int newLength, Class<? extends T[]> newType) {
		T[] copy = ((Object) newType == (Object) Object[].class) ? (T[]) new Object[newLength] : (T[]) Array.newInstance(newType.getComponentType(), newLength);
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	/**
	 * Iterator to use in the set.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class ArraySetIterator extends ObjectArrayIterator {

		/**
		 * @param array
		 *            the array to iterate over
		 */
		public ArraySetIterator(Object[] array) {
			super(array);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			SortedArraySet.this.remove(this.array[index]);
		}
	}

}
