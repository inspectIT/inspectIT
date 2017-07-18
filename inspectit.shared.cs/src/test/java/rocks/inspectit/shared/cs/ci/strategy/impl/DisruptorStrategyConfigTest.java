package rocks.inspectit.shared.cs.ci.strategy.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
public class DisruptorStrategyConfigTest extends TestBase {

	@InjectMocks
	DisruptorStrategyConfig config;

	public static class SetBufferSize extends DisruptorStrategyConfigTest {

		@Test
		public void dataBufferSize() {
			int dataBufferSize = 128;

			config.setBufferSize(dataBufferSize);

			assertThat(config.getBufferSize(), is(dataBufferSize));
		}

		@Test
		public void dataBufferSizeNotPowerOf2() {
			int dataBufferSize = 111;

			config.setBufferSize(dataBufferSize);

			assertThat(config.getBufferSize(), is(128));
		}

		@Test
		public void dataBufferSizeNegative() {
			int dataBufferSize = -44;

			config.setBufferSize(dataBufferSize);

			assertThat(config.getBufferSize(), is(DisruptorStrategyConfig.DEFAULT_BUFFER_SIZE));
		}

	}
}
