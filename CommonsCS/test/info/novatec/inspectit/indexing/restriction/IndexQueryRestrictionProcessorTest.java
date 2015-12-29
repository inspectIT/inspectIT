package info.novatec.inspectit.indexing.restriction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.restriction.impl.CachingIndexQueryRestrictionProcessor;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

/**
 * Tests the indexing restriction used with index queries.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class IndexQueryRestrictionProcessorTest extends TestBase {

	/**
	 * The processor under test.
	 */
	@InjectMocks
	protected CachingIndexQueryRestrictionProcessor processor;

	public static class DirectRestriction extends IndexQueryRestrictionProcessorTest {

		/**
		 * Test equal restriction.
		 */
		@Test
		public void equalsTrueRestriction() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.equal("id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test equal restriction.
		 */
		@Test
		public void equalsFalseRestriction() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.equal("id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test not equal restriction.
		 */
		@Test
		public void notEqualsFalseRestriction() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.notEqual("id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test not equal restriction.
		 */
		@Test
		public void notEqualsTrueRestriction() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.notEqual("id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test is null restriction.
		 */
		@Test
		public void isNull() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isNull("id"));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test is not null restriction.
		 */
		@Test
		public void isNotNull() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isNotNull("id"));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater than restriction.
		 */
		@Test
		public void greaterThanOne() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test greater than restriction.
		 */
		@Test
		public void greaterThanZero() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater than restriction.
		 */
		@Test
		public void greaterThanTwo() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test greater or equal restriction.
		 */
		@Test
		public void greaterEqualOne() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater or equal restriction.
		 */
		@Test
		public void greaterEqualZero() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater or equal restriction.
		 */
		@Test
		public void greaterEqualTwo() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less than restriction.
		 */
		@Test
		public void lessThanOne() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessThan("id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less than restriction.
		 */
		@Test
		public void lessThanZero() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessThan("id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less than restriction.
		 */
		@Test
		public void lessThanTwo() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessThan("id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test less or equal restriction.
		 */
		@Test
		public void lessEqualOne() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test less or equal restriction.
		 */
		@Test
		public void lessEqualZero() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less or equal restriction.
		 */
		@Test
		public void lessEqualTwo() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Tests is in collection restrictions.
		 */
		@Test
		public void isInCollectionTrue() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("id", Collections.singletonList(1L)));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Tests is in collection restrictions.
		 */
		@Test
		public void isInCollectionFalse() {
			TimerData timerData = new TimerData();
			timerData.setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("id", Collections.singletonList(2L)));

			boolean result = processor.areAllRestrictionsFulfilled(timerData, restrictions);

			assertThat(result, is(false));
		}
	}

	public static class NavigationRestriction extends IndexQueryRestrictionProcessorTest {

		/**
		 * Test equal restriction.
		 */
		@Test
		public void equalsTrueRestriction() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.equal("httpInfo.id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test equal restriction.
		 */
		@Test
		public void equalsFalseRestriction() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.equal("httpInfo.id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test not equal restriction.
		 */
		@Test
		public void notEqualsFalseRestriction() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.notEqual("httpInfo.id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test not equal restriction.
		 */
		@Test
		public void notEqualsTrueRestriction() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.notEqual("httpInfo.id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test is null restriction.
		 */
		@Test
		public void isNull() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isNull("httpInfo.id"));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test is not null restriction.
		 */
		@Test
		public void isNotNull() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isNotNull("httpInfo.id"));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater than restriction.
		 */
		@Test
		public void greaterThanOne() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("httpInfo.id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test greater than restriction.
		 */
		@Test
		public void greaterThanZero() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("httpInfo.id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater than restriction.
		 */
		@Test
		public void greaterThanTwo() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterThan("httpInfo.id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test greater or equal restriction.
		 */
		@Test
		public void greaterEqualOne() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("httpInfo.id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater or equal restriction.
		 */
		@Test
		public void greaterEqualZero() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("httpInfo.id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test greater or equal restriction.
		 */
		@Test
		public void greaterEqualTwo() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.greaterEqual("httpInfo.id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less than restriction.
		 */
		@Test
		public void lessThanOne() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessThan("httpInfo.id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less than restriction.
		 */
		@Test
		public void lessThanZero() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessThan("httpInfo.id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less than restriction.
		 */
		@Test
		public void lessThanTwo() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessThan("httpInfo.id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test less or equal restriction.
		 */
		@Test
		public void lessEqualOne() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("httpInfo.id", 1L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Test less or equal restriction.
		 */
		@Test
		public void lessEqualZero() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("httpInfo.id", 0L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}

		/**
		 * Test less or equal restriction.
		 */
		@Test
		public void lessEqualTwo() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.lessEqual("httpInfo.id", 2L));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Tests is in collection restrictions.
		 */
		@Test
		public void isInCollectionTrue() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("httpInfo.id", Collections.singletonList(1L)));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(true));
		}

		/**
		 * Tests is in collection restrictions.
		 */
		@Test
		public void isInCollectionFalse() {
			HttpTimerData httpData = new HttpTimerData();
			httpData.getHttpInfo().setId(1L);
			List<IIndexQueryRestriction> restrictions = Collections.singletonList(IndexQueryRestrictionFactory.isInCollection("httpInfo.id", Collections.singletonList(2L)));

			boolean result = processor.areAllRestrictionsFulfilled(httpData, restrictions);

			assertThat(result, is(false));
		}
	}

}
