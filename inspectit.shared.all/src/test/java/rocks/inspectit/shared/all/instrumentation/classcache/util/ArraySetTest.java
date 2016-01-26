package rocks.inspectit.shared.all.instrumentation.classcache.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.classcache.util.ArraySet;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
public class ArraySetTest extends TestBase {

	@InjectMocks
	ArraySet<Long> set;

	public class Add extends ArraySetTest {

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
	}

	public class AddOrUpdate extends ArraySetTest {

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
	}
}
