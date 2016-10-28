package rocks.inspectit.server.ci.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link ClassInstrumentationChangedEvent} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class ClassInstrumentationChangedEventTest extends TestBase {

	/**
	 * Tests the
	 * {@link ClassInstrumentationChangedEvent#ClassInstrumentationChangedEvent(Object, long, java.util.Collection)}
	 * method.
	 */
	public static class Constructor extends ClassInstrumentationChangedEventTest {

		@Test
		public void successful() {
			List<InstrumentationDefinition> instrumentationDefinitions = Arrays.asList(mock(InstrumentationDefinition.class));

			ClassInstrumentationChangedEvent event = new ClassInstrumentationChangedEvent(this, 10L, instrumentationDefinitions);

			assertThat(event.getAgentId(), is(equalTo(10L)));
			assertThat(instrumentationDefinitions, is(equalTo(event.getInstrumentationDefinitions())));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullDefinitions() {
			new ClassInstrumentationChangedEvent(this, 10L, null);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		@SuppressWarnings("unchecked")
		public void emptyDefinitions() {
			new ClassInstrumentationChangedEvent(this, 10L, Collections.EMPTY_LIST);
		}
	}

}
