package info.novatec.inspectit.indexing.restriction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.restriction.impl.CachingIndexQueryRestrictionProcessor;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;

import java.util.Collections;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the indexing restriction used with index queries.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class IndexQueryRestrictionProcessorTest {

	/**
	 * The processor under test.
	 */
	private IIndexQueryRestrictionProcessor processor;

	/**
	 * Timer data.
	 */
	private TimerData timerData;

	/**
	 * HTTP timer data.
	 */
	private HttpTimerData httpTimerData;

	/**
	 * Initialize. Set up the timer data.
	 */
	@BeforeClass
	public void init() {
		timerData = new TimerData();
		timerData.setId(1L);
		// http for navigation testing
		httpTimerData = new HttpTimerData();
		httpTimerData.getHttpInfo().setId(1L);
	}

	/**
	 * Initialize processor before executing each test.
	 */
	@BeforeMethod
	public void initTestMethod() {
		processor = new CachingIndexQueryRestrictionProcessor();
	}

	/**
	 * Test equal restriction.
	 */
	@Test
	public void equalsTrueRestriction() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.equal("id", 1L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.equal("httpInfo.id", 1L))), is(true));
	}

	/**
	 * Test equal restriction.
	 */
	@Test
	public void equalsFalseRestriction() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.equal("id", 0L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.equal("httpInfo.id", 0L))), is(false));
	}

	/**
	 * Test not equal restriction.
	 */
	@Test
	public void notEqualsFalseRestriction() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.notEqual("id", 1L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.notEqual("httpInfo.id", 1L))), is(false));
	}

	/**
	 * Test not equal restriction.
	 */
	@Test
	public void notEqualsTrueRestriction() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.notEqual("id", 0L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.notEqual("httpInfo.id", 0L))), is(true));
	}

	/**
	 * Test is null restriction.
	 */
	@Test
	public void isNull() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.isNull("id"))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.isNull("httpInfo.id"))), is(false));
	}

	/**
	 * Test is not null restriction.
	 */
	@Test
	public void isNotNull() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.isNotNull("id"))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.isNotNull("httpInfo.id"))), is(true));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanOne() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("id", 1L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("httpInfo.id", 1L))), is(false));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanZero() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("id", 0L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("httpInfo.id", 0L))), is(true));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanTwo() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("id", 2L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("httpInfo.id", 2L))), is(false));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualOne() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("id", 1L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("httpInfo.id", 1L))), is(true));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualZero() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("id", 0L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("httpInfo.id", 0L))), is(true));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualTwo() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("id", 2L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("httpInfo.id", 2L))), is(false));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanOne() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.lessThan("id", 1L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.lessThan("httpInfo.id", 1L))), is(false));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanZero() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.lessThan("id", 0L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.lessThan("httpInfo.id", 0L))), is(false));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanTwo() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.lessThan("id", 2L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.lessThan("httpInfo.id", 2L))), is(true));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualOne() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("id", 1L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("httpInfo.id", 1L))), is(true));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualZero() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("id", 0L))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("httpInfo.id", 0L))), is(false));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualTwo() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("id", 2L))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("httpInfo.id", 2L))), is(true));
	}

	/**
	 * Tests is in collection restrictions.
	 */
	@Test
	public void isInCollection() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("id", Collections.singletonList(1L)))), is(true));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("httpInfo.id", Collections.singletonList(1L)))), is(true));

		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("id", Collections.singletonList(2L)))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("httpInfo.id", Collections.singletonList(2L)))),
				is(false));
	}

	/**
	 * If field does not exists it should be <code>false</code>.
	 */
	@Test
	public void fieldDoesNotExists() {
		assertThat(processor.areAllRestrictionsFulfilled(timerData, Collections.singletonList(IndexQueryRestrictionFactory.isNotNull("myId"))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.isNotNull("httpInfo.myid"))), is(false));
		assertThat(processor.areAllRestrictionsFulfilled(httpTimerData, Collections.singletonList(IndexQueryRestrictionFactory.isNotNull("myHttpInfo.myid"))), is(false));
	}
}
