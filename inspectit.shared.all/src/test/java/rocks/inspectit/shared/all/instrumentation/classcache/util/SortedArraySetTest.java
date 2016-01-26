package rocks.inspectit.shared.all.instrumentation.classcache.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Comparator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.classcache.util.SortedArraySet;

/**
 * @author Ivan Senic
 *
 */
public class SortedArraySetTest {

	SortedArraySet<Long> set;

	@BeforeMethod
	public void setup() {
		set = new SortedArraySet<Long>(new Comparator<Long>() {
			public int compare(Long o1, Long o2) {
				return o1.compareTo(o2);
			}
		});
	}

	public class Add extends SortedArraySetTest {

		@Test
		public void add() {
			Long l = new Long(1);

			set.add(l);

			assertThat(set, hasSize(1));
			assertThat(set, contains(l));
		}

		@Test
		public void addTwice() {
			Long l = new Long(1);

			set.add(l);
			set.add(l);

			assertThat(set, hasSize(1));
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void addNull() {
			set.add(null);
		}
	}

	public class AddOrUpdate extends SortedArraySetTest {

		@Test
		public void add() {
			Long l = new Long(1);

			set.addOrUpdate(l);

			assertThat(set, hasSize(1));
			assertThat(set, contains(l));
		}

		@Test
		public void update() {
			Long l = new Long(1);
			Long l2 = new Long(1);

			set.addOrUpdate(l);
			set.addOrUpdate(l2);

			assertThat(set, hasSize(1));
			assertThat(set.iterator().next() == l2, is(true));
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void addNull() {
			set.addOrUpdate(null);
		}
	}

	public class Remove extends SortedArraySetTest {

		@Test
		public void remove() {
			Long l = new Long(1);

			set.add(l);
			set.remove(l);

			assertThat(set, is(empty()));
		}

		@Test
		public void removeNonExisting() {
			Long l = new Long(1);

			set.add(l);
			set.remove(Long.valueOf(2));

			assertThat(set, hasSize(1));
		}
	}

	public class Size extends SortedArraySetTest {

		@Test
		public void empty() {
			int size = set.size();

			assertThat(size, is(0));
		}

		@Test
		public void notEmpty() {
			set.add(1L);
			set.add(2L);

			int size = set.size();

			assertThat(size, is(2));
		}
	}

	public class IsEmpty extends SortedArraySetTest {

		@Test
		public void empty() {
			boolean isEmpty = set.isEmpty();

			assertThat(isEmpty, is(true));
		}

		@Test
		public void notEmpty() {
			set.add(1L);

			boolean isEmpty = set.isEmpty();

			assertThat(isEmpty, is(false));
		}
	}

	public class Contains extends SortedArraySetTest {

		@Test
		public void doesContain() {
			set.add(1L);
			set.add(2L);
			set.add(3L);

			boolean contains1 = set.contains(1L);
			boolean contains2 = set.contains(2L);
			boolean contains3 = set.contains(3L);

			assertThat(contains1, is(true));
			assertThat(contains2, is(true));
			assertThat(contains3, is(true));
		}

		@Test
		public void doesNotContain() {
			set.add(1L);
			set.add(2L);
			set.add(3L);

			boolean contains4 = set.contains(4L);

			assertThat(contains4, is(false));
		}

	}

	public class Iterator extends SortedArraySetTest {

		@Test
		public void hasNext() {
			set.add(1L);

			java.util.Iterator<Long> it = set.iterator();
			boolean hasNext = it.hasNext();

			assertThat(hasNext, is(true));
		}

		@Test
		public void hasNextNot() {
			java.util.Iterator<Long> it = set.iterator();
			boolean hasNext = it.hasNext();

			assertThat(hasNext, is(false));
		}

		@Test
		public void next() {
			set.add(1L);

			java.util.Iterator<Long> it = set.iterator();
			Long next = it.next();

			assertThat(next, is(1L));
		}

		@Test
		public void nextTwice() {
			set.add(1L);
			set.add(2L);

			java.util.Iterator<Long> it = set.iterator();
			it.next();
			Long next = it.next();

			assertThat(next, is(2L));
		}

		@Test
		public void remove() {
			set.add(1L);
			set.add(2L);

			java.util.Iterator<Long> it = set.iterator();
			it.next();
			it.remove();

			assertThat(set, hasSize(1));
		}
	}

	public class ToArray extends SortedArraySetTest {

		@Test
		public void toArray() {
			set.add(1L);
			set.add(2L);

			Object[] array = set.toArray();

			assertThat(array, is(arrayWithSize(2)));
			assertThat(array, is(arrayContaining((Object) 1L, 2L)));
		}
	}

	public class Clear extends SortedArraySetTest {

		@Test
		public void clear() {
			set.add(1L);
			set.add(2L);

			set.clear();

			assertThat(set, is(empty()));
		}

		@Test
		public void clearEmpty() {
			set.clear();

			assertThat(set, is(empty()));
		}

	}
}
