package rocks.inspectit.server.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class CollectionSubtractUtilsTest {

	public class SubtractSafe extends CollectionSubtractUtilsTest {

		@Test
		public void minuendNull() {
			Collection<Long> a = null;
			Collection<Long> b = Collections.singleton(5L);

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, is(empty()));
		}

		@Test
		public void minuendEmpty() {
			Collection<Long> a = Collections.emptyList();
			Collection<Long> b = Collections.singleton(5L);

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, is(empty()));
		}

		@Test
		public void subtrahendNull() {
			Collection<Long> a = Collections.singleton(5L);
			Collection<Long> b = null;

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, contains(a.toArray(new Long[a.size()])));
		}

		@Test
		public void subtrahendEmpty() {
			Collection<Long> a = Collections.singleton(5L);
			Collection<Long> b = Collections.emptyList();

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, contains(a.toArray(new Long[a.size()])));
		}

		@Test
		public void bothNull() {
			Collection<Long> a = null;
			Collection<Long> b = null;

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, is(empty()));
		}

		@Test
		public void subtract() {
			Collection<Long> a = new ArrayList<>();
			a.add(5L);
			a.add(10L);
			Collection<Long> b = Collections.singleton(5L);

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, hasSize(1));
			assertThat(result, hasItem(10L));
		}

		@Test
		public void subtractNothihng() {
			Collection<Long> a = Collections.singleton(10L);
			Collection<Long> b = Collections.singleton(5L);

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, hasSize(1));
			assertThat(result, hasItem(10L));
		}

		@Test
		public void subtractAll() {
			Collection<Long> a = Collections.singleton(5L);
			Collection<Long> b = new ArrayList<>();
			b.add(5L);
			b.add(10L);

			Collection<Long> result = CollectionSubtractUtils.subtractSafe(a, b);

			assertThat(result, is(empty()));
		}

	}
}
