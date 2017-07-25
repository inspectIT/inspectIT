package rocks.inspectit.agent.java.config.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests for the {@link AbstractSensorConfig} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AbstractSensorConfigTest extends TestBase {

	@InjectMocks
	AbstractSensorConfig asc;

	/**
	 * Tests the {@link AbstractSensorConfig#getFullTargetMethodName()} method.
	 *
	 */
	public static class GetFullTargetMethodName extends AbstractSensorConfigTest {

		@Test
		public void noParameters() {
			asc.setTargetMethodName("method");

			String result = asc.getFullTargetMethodName();

			assertThat(result, is(equalTo("method()")));
		}

		@Test
		public void oneParameter() {
			asc.setTargetMethodName("method");
			asc.setParameterTypes(Arrays.asList(new String[] { "java.lang.Runnable" }));

			String result = asc.getFullTargetMethodName();

			assertThat(result, is(equalTo("method(Runnable)")));
		}

		@Test
		public void twoParameter() {
			asc.setTargetMethodName("method");
			asc.setParameterTypes(Arrays.asList(new String[] { "java.lang.Runnable", "boolean" }));

			String result = asc.getFullTargetMethodName();

			assertThat(result, is(equalTo("method(Runnable, boolean)")));
		}

		@Test
		public void nullMethodName() {
			String result = asc.getFullTargetMethodName();

			assertThat(result, is(nullValue()));
		}
	}
}
