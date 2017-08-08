package rocks.inspectit.shared.cs.data.invocationtree;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

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

	public static class Constructor extends InvocationTreeElementTest {

		@Test
		public void spanDataElement() {
			Object data = mock(Span.class);

			InvocationTreeElement element = new InvocationTreeElement(data);

			assertThat(element.getDataElement(), is(data));
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

}
