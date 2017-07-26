package rocks.inspectit.agent.java.stats;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
public class AgentStatisticsLoggerTest extends TestBase {

	@InjectMocks
	AgentStatisticsLogger statsLogger;

	@Mock
	Logger log;

	@BeforeMethod
	public void initLogger() {
		when(log.isWarnEnabled()).thenReturn(true);
	}

	public static class DataDropped extends AgentStatisticsLoggerTest {

		@Test
		public void onFirst() {
			statsLogger.dataDropped(1);

			verify(log, times(1)).warn(anyString());
		}

		@Test
		public void onSecondNot() {
			statsLogger.dataDropped(1);
			statsLogger.dataDropped(1);

			verify(log, times(1)).warn(anyString());
		}

		@Test
		public void onFirstAndTenth() {
			statsLogger.dataDropped(1);
			statsLogger.dataDropped(9);

			verify(log, times(2)).warn(anyString());
		}

		@Test
		public void onFirstAndTenthAndHundred() {
			statsLogger.dataDropped(1);
			statsLogger.dataDropped(9);
			statsLogger.dataDropped(90);

			verify(log, times(3)).warn(anyString());
		}

		@Test
		public void onFirstAndTenthAndHundredAndThousand() {
			statsLogger.dataDropped(1);
			statsLogger.dataDropped(9);
			statsLogger.dataDropped(90);
			statsLogger.dataDropped(900);

			verify(log, times(4)).warn(anyString());
		}

		@Test
		public void everyThousand() {
			statsLogger.dataDropped(2999);
			statsLogger.dataDropped(1);
			statsLogger.dataDropped(1);

			verify(log, times(2)).warn(anyString());
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void zeroCount() {
			statsLogger.dataDropped(0);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void negativeCount() {
			statsLogger.dataDropped(-10);
		}
	}

	public static class NoClassCacheAvailable extends AgentStatisticsLoggerTest {

		@Test
		public void onFirst() {
			statsLogger.noClassCacheAvailable();
			verify(log, times(1)).warn(anyString());
		}

		@Test
		public void onSecondUntilInfinity() {
			// log should be executed onces
			statsLogger.noClassCacheAvailable();
			verify(log, times(1)).warn(anyString());

			// log shouldn't be executed twice
			statsLogger.noClassCacheAvailable();
			verify(log, times(1)).warn(anyString());
		}
	}
}
