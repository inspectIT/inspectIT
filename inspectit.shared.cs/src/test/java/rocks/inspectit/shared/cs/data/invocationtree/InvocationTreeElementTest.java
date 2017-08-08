package rocks.inspectit.shared.cs.data.invocationtree;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Tests the {@link InvocationTreeElement} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class InvocationTreeElementTest extends TestBase {

	/**
	 * Tests the {@link InvocationTreeElement#InvocationTreeElement(Object)} constructor.
	 *
	 */
	public static class Constructor extends InvocationTreeElementTest {

		@Test
		public void spanDataElement() {
			Object data = mock(Span.class);

			InvocationTreeElement element = new InvocationTreeElement(data);

			assertThat(element.getDataElement(), is(data));
			assertThat(element.getDataElement(), is(instanceOf(Span.class)));
		}

		@Test
		public void invocationSequenceDataElement() {
			Object data = mock(InvocationSequenceData.class);

			InvocationTreeElement element = new InvocationTreeElement(data);

			assertThat(element.getDataElement(), is(data));
		}

		@Test
		public void spanIdentDataElement() {
			Object data = mock(SpanIdent.class);

			InvocationTreeElement element = new InvocationTreeElement(data);

			assertThat(element.getDataElement(), is(data));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void invalidDataElement() {
			Object data = new Object();

			new InvocationTreeElement(data);
		}
	}

	/**
	 * Tests the {@link InvocationTreeElement#compareTo(InvocationTreeElement)} method.
	 */
	public static class CompareTo extends InvocationTreeElementTest {

		@Test
		public void ascendingTime() {
			InvocationSequenceData sequence1 = mock(InvocationSequenceData.class);
			InvocationSequenceData sequence2 = mock(InvocationSequenceData.class);
			InvocationTreeElement e1 = new InvocationTreeElement(sequence1);
			InvocationTreeElement e2 = new InvocationTreeElement(sequence2);
			when(sequence1.getTimeStamp()).thenReturn(new Timestamp(10));
			when(sequence2.getTimeStamp()).thenReturn(new Timestamp(20));

			int result = e1.compareTo(e2);

			assertThat(result, is(-1));
		}

		@Test
		public void descendingTime() {
			InvocationSequenceData sequence1 = mock(InvocationSequenceData.class);
			InvocationSequenceData sequence2 = mock(InvocationSequenceData.class);
			InvocationTreeElement e1 = new InvocationTreeElement(sequence1);
			InvocationTreeElement e2 = new InvocationTreeElement(sequence2);
			when(sequence1.getTimeStamp()).thenReturn(new Timestamp(20));
			when(sequence2.getTimeStamp()).thenReturn(new Timestamp(10));

			int result = e1.compareTo(e2);

			assertThat(result, is(1));
		}

		@Test
		public void equalTime() {
			InvocationSequenceData sequence1 = mock(InvocationSequenceData.class);
			InvocationSequenceData sequence2 = mock(InvocationSequenceData.class);
			InvocationTreeElement e1 = new InvocationTreeElement(sequence1);
			InvocationTreeElement e2 = new InvocationTreeElement(sequence2);
			when(sequence1.getTimeStamp()).thenReturn(new Timestamp(10));
			when(sequence2.getTimeStamp()).thenReturn(new Timestamp(10));

			int result = e1.compareTo(e2);

			assertThat(result, is(0));
		}

		@Test
		public void bothTimestampsAreNull() {
			InvocationTreeElement e1 = new InvocationTreeElement(mock(InvocationSequenceData.class));
			InvocationTreeElement e2 = new InvocationTreeElement(mock(InvocationSequenceData.class));

			int result = e1.compareTo(e2);

			assertThat(result, is(0));
		}

		@Test
		public void secondTimeIsNull() {
			InvocationSequenceData sequence1 = mock(InvocationSequenceData.class);
			InvocationTreeElement e1 = new InvocationTreeElement(sequence1);
			InvocationTreeElement e2 = new InvocationTreeElement(mock(InvocationSequenceData.class));
			when(sequence1.getTimeStamp()).thenReturn(new Timestamp(10));

			int result = e1.compareTo(e2);

			assertThat(result, is(-1));
		}

		@Test
		public void firstTimeIsNull() {
			InvocationSequenceData sequence2 = mock(InvocationSequenceData.class);
			InvocationTreeElement e1 = new InvocationTreeElement(mock(InvocationSequenceData.class));
			InvocationTreeElement e2 = new InvocationTreeElement(sequence2);
			when(sequence2.getTimeStamp()).thenReturn(new Timestamp(10));

			int result = e1.compareTo(e2);

			assertThat(result, is(1));
		}
	}
}
