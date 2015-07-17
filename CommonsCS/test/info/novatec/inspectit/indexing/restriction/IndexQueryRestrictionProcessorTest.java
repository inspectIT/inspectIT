package info.novatec.inspectit.indexing.restriction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.restriction.impl.CachingIndexQueryRestrictionProcessor;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	 * Initialize. Set up the timer data.
	 */
	@BeforeClass
	public void init() {
		timerData = new TimerData();
		timerData.setId(1L);
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
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.equal("id", 1L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test equal restriction.
	 */
	@Test
	public void equalsFalseRestriction() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.equal("id", 0L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test not equal restriction.
	 */
	@Test
	public void notEqualsFalseRestriction() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.notEqual("id", 1L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test not equal restriction.
	 */
	@Test
	public void notEqualsTrueRestriction() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.notEqual("id", 0L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test is null restriction.
	 */
	@Test
	public void isNull() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.isNull("id"));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test is not null restriction.
	 */
	@Test
	public void isNotNull() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.isNotNull("id"));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterThan("id", 1L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterThan("id", 0L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test greater than restriction.
	 */
	@Test
	public void greaterThanTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterThan("id", 2L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterEqual("id", 1L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterEqual("id", 0L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test greater or equal restriction.
	 */
	@Test
	public void greaterEqualTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.greaterEqual("id", 2L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessThan("id", 1L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessThan("id", 0L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test less than restriction.
	 */
	@Test
	public void lessThanTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessThan("id", 2L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualOne() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessEqual("id", 1L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualZero() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessEqual("id", 0L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}

	/**
	 * Test less or equal restriction.
	 */
	@Test
	public void lessEqualTwo() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.lessEqual("id", 2L));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));
	}

	/**
	 * Tests is in collection restrictions.
	 */
	@Test
	public void isInCollection() {
		List<IIndexQueryRestriction> restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.isInCollection("id", Collections.singletonList(1L)));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(true));

		restrictions = new ArrayList<IIndexQueryRestriction>(1);
		restrictions.add(IndexQueryRestrictionFactory.isInCollection("id", Collections.singletonList(2L)));
		assertThat(processor.areAllRestrictionsFulfilled(timerData, restrictions), is(false));
	}
}
