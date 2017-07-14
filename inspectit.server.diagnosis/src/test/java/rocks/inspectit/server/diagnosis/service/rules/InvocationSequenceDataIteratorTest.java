package rocks.inspectit.server.diagnosis.service.rules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class of {@link InvocationSequenceDataIterator}.
 *
 * @author Tobias Angerstein
 *
 */
public class InvocationSequenceDataIteratorTest extends TestBase {

	private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());
	private static final Random RANDOM = new Random();
	private static final long PLATFORM_IDENT = RANDOM.nextLong();
	private static final long SENSOR_TYPE_IDENT = RANDOM.nextLong();
	private static InvocationSequenceData rootInvocationSequence;
	private static InvocationSequenceData firstChildSequence;
	private static InvocationSequenceData secondChildSequence;
	private static InvocationSequenceData thirdChildSequence;
	private static InvocationSequenceData fourthChildSequence;

	/**
	 * Builds an invocation sequence data tree.
	 *
	 */
	@BeforeMethod
	public void init() {
		List<InvocationSequenceData> nestedSequences1 = new ArrayList<>();
		List<InvocationSequenceData> nestedSequences2 = new ArrayList<>();
		rootInvocationSequence = new InvocationSequenceData();
		rootInvocationSequence.setNestedSequences(nestedSequences1);
		firstChildSequence = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, 1L);
		firstChildSequence.setDuration(200d);
		firstChildSequence.setParentSequence(rootInvocationSequence);
		secondChildSequence = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, 2L);
		secondChildSequence.setDuration(4000d);
		secondChildSequence.setParentSequence(rootInvocationSequence);
		thirdChildSequence = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, 3L);
		thirdChildSequence.setDuration(500d);
		thirdChildSequence.setParentSequence(secondChildSequence);
		fourthChildSequence = new InvocationSequenceData(DEF_DATE, PLATFORM_IDENT, SENSOR_TYPE_IDENT, 4L);
		fourthChildSequence.setDuration(500d);
		fourthChildSequence.setParentSequence(rootInvocationSequence);
		nestedSequences1.add(firstChildSequence);
		nestedSequences1.add(secondChildSequence);
		nestedSequences1.add(fourthChildSequence);
		nestedSequences2.add(thirdChildSequence);
		rootInvocationSequence = new InvocationSequenceData();
		rootInvocationSequence.setNestedSequences(nestedSequences1);
		secondChildSequence.setNestedSequences(nestedSequences2);
	}

	/**
	 * Tests the constructor.
	 *
	 * @author Tobias Angerstein
	 *
	 */
	public static class Constructor extends InvocationSequenceDataIteratorTest {

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void constructorMustThrowAnException() {
			@SuppressWarnings("unused")
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(null);
		}

		@Test
		public void iteratorShouldStartWithTheGivenInvocationSequence() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(firstChildSequence, true);
			InvocationSequenceData nextInvocationSequence = iterator.next();

			assertThat(nextInvocationSequence, is(firstChildSequence));
		}
	}

	/**
	 * Tests the {@link InvocationSequenceDataIterator#hasNext()} method.
	 *
	 * @author Tobias Angerstein
	 *
	 */
	public static class HasNext extends InvocationSequenceDataIteratorTest {

		@Test
		public void nextInvocationSequenceDataMustExist() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(rootInvocationSequence);
			boolean hasNext = iterator.hasNext();

			assertThat(hasNext, is(true));
		}

		@Test
		public void nextInvocationSequenceDataMustNotExist() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(rootInvocationSequence);
			iterator.next();
			iterator.next();
			iterator.next();
			iterator.next();
			iterator.next();
			boolean hasNext = iterator.hasNext();

			assertThat(hasNext, is(false));
		}
	}

	/**
	 * Tests the {@link InvocationSequenceDataIterator#next()} method.
	 *
	 * @author Tobias Angerstein
	 *
	 */
	public static class Next extends InvocationSequenceDataIteratorTest {

		@Test
		public static void nextMustProvideTheRightOrderOfInvocationsOnlySubtree() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(secondChildSequence, true);
			InvocationSequenceData nextInvocationSequence;

			nextInvocationSequence = iterator.next();

			assertThat(nextInvocationSequence, is(secondChildSequence));

			nextInvocationSequence = iterator.next();

			assertThat(nextInvocationSequence, is(thirdChildSequence));
			assertThat(iterator.hasNext(), is(false));
		}

		@Test
		public static void nextMustProvideTheRightOrderOfInvocationsWithParentSequences() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(secondChildSequence, false);
			InvocationSequenceData nextInvocationSequence;

			nextInvocationSequence = iterator.next();

			assertThat(nextInvocationSequence, is(secondChildSequence));

			nextInvocationSequence = iterator.next();

			assertThat(nextInvocationSequence, is(thirdChildSequence));

			nextInvocationSequence = iterator.next();

			assertThat(nextInvocationSequence, is(fourthChildSequence));

			assertThat(iterator.hasNext(), is(false));
		}

		@Test
		public static void nextMustProvideTheRightOrderOfInvocationsStartingFromRoot() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(rootInvocationSequence, true);
			InvocationSequenceData nextInvocationSequence;

			nextInvocationSequence = iterator.next();
			assertThat(nextInvocationSequence, is(rootInvocationSequence));

			nextInvocationSequence = iterator.next();
			assertThat(nextInvocationSequence, is(firstChildSequence));

			nextInvocationSequence = iterator.next();
			assertThat(nextInvocationSequence, is(secondChildSequence));

			nextInvocationSequence = iterator.next();
			assertThat(nextInvocationSequence, is(thirdChildSequence));

			nextInvocationSequence = iterator.next();
			assertThat(nextInvocationSequence, is(fourthChildSequence));

			assertThat(iterator.hasNext(), is(false));
		}
	}

	/**
	 * Tests the {@link InvocationSequenceDataIterator#currentDepth()} method.
	 *
	 * @author Tobias Angerstein
	 *
	 */
	public static class CurrentDepth extends InvocationSequenceDataIteratorTest {
		@Test
		public static void currentDepthMustProvideTheCorrectCurrentDepthStartingFromRoot() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(rootInvocationSequence);

			iterator.next();
			assertThat(iterator.currentDepth(), is(0));

			iterator.next();
			assertThat(iterator.currentDepth(), is(1));

			iterator.next();
			assertThat(iterator.currentDepth(), is(1));

			iterator.next();
			assertThat(iterator.currentDepth(), is(2));

			iterator.next();
			assertThat(iterator.currentDepth(), is(1));

			assertThat(iterator.hasNext(), is(false));
		}

		@Test
		public static void currentDepthMustProvideTheCorrectCurrentDepthStartingFromChild() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(secondChildSequence, false);

			iterator.next();
			assertThat(iterator.currentDepth(), is(1));

			iterator.next();
			assertThat(iterator.currentDepth(), is(2));

			iterator.next();
			assertThat(iterator.currentDepth(), is(1));

			assertThat(iterator.hasNext(), is(false));
		}

		@Test
		public static void currentDepthMustProvideTheCorrectCurrentDepthStartingFromChildOnlySubtree() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(secondChildSequence, true);

			iterator.next();
			assertThat(iterator.currentDepth(), is(1));

			iterator.next();
			assertThat(iterator.currentDepth(), is(2));

			assertThat(iterator.hasNext(), is(false));
		}
	}

	/**
	 * Tests the {@link InvocationSequenceDataIterator#remove()} method.
	 *
	 * @author Tobias Angerstein
	 *
	 */
	public static class Remove extends InvocationSequenceDataIteratorTest {

		@Test(expectedExceptions = UnsupportedOperationException.class)
		public void removeMustThrowAnException() {
			InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(rootInvocationSequence);
			iterator.remove();
		}
	}
}
