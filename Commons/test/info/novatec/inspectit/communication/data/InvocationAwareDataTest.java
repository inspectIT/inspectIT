package info.novatec.inspectit.communication.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * Tests that functionality in the {@link InvocationAwareData} is correct.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class InvocationAwareDataTest {

	/**
	 * Tests the simple aggregation when objects are in different invocations.
	 */
	@Test
	public void simpleDifferentParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(2));

		i1.aggregateInvocationAwareData(i2);

		assertThat(i1.getObjectsInInvocationsCount(), is(equalTo(2)));
		assertThat(i1.getInvocationParentsIdSet().size(), is(equalTo(2)));
		assertThat(i1.getInvocationParentsIdSet(), contains(1L, 2L));
	}

	/**
	 * Tests the simple aggregation when objects are in same invocations.
	 */
	@Test
	public void simpleSameParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(1));

		i1.aggregateInvocationAwareData(i2);

		assertThat(i1.getObjectsInInvocationsCount(), is(equalTo(2)));
		assertThat(i1.getInvocationParentsIdSet().size(), is(equalTo(1)));
		assertThat(i1.getInvocationParentsIdSet(), contains(1L));
	}

	/**
	 * Tests the complicated aggregation when objects are in different invocations.
	 */
	@Test
	public void complicatedDifferentParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(2));

		i1.aggregateInvocationAwareData(i2);

		InvocationAwareData i3 = getInvocationAwareData();
		i3.addInvocationParentId(Long.valueOf(3));

		InvocationAwareData i4 = getInvocationAwareData();
		i4.addInvocationParentId(Long.valueOf(4));

		i3.aggregateInvocationAwareData(i4);

		i1.aggregateInvocationAwareData(i3);

		assertThat(i1.getObjectsInInvocationsCount(), is(equalTo(4)));
		assertThat(i1.getInvocationParentsIdSet().size(), is(equalTo(4)));
		assertThat(i1.getInvocationParentsIdSet(), contains(1L, 2L, 3L, 4L));
	}

	/**
	 * Tests the complicated aggregation when objects are in same invocations.
	 */
	@Test
	public void complicatedSameParentsAggregation() {
		InvocationAwareData i1 = getInvocationAwareData();
		i1.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i2 = getInvocationAwareData();
		i2.addInvocationParentId(Long.valueOf(2));

		i1.aggregateInvocationAwareData(i2);

		InvocationAwareData i3 = getInvocationAwareData();
		i3.addInvocationParentId(Long.valueOf(1));

		InvocationAwareData i4 = getInvocationAwareData();
		i4.addInvocationParentId(Long.valueOf(2));

		i3.aggregateInvocationAwareData(i4);

		i1.aggregateInvocationAwareData(i3);

		assertThat(i1.getObjectsInInvocationsCount(), is(equalTo(4)));
		assertThat(i1.getInvocationParentsIdSet().size(), is(equalTo(2)));
		assertThat(i1.getInvocationParentsIdSet(), contains(1L, 2L));
	}

	/**
	 * Gets the instance of the abstract class {@link InvocationAwareData}.
	 * 
	 * @return Gets the instance of the abstract class {@link InvocationAwareData}.
	 */
	private InvocationAwareData getInvocationAwareData() {
		return new InvocationAwareData() {

			/**
			 * Generated UID.
			 */
			private static final long serialVersionUID = 8228055838391889943L;

			@Override
			public double getInvocationAffiliationPercentage() {
				return 0;
			}
		};
	}

}
