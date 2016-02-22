package rocks.inspectit.agent.java.sending.impl;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.MockInit;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.sending.ISendingStrategy;
import rocks.inspectit.agent.java.sending.impl.TimeStrategy;

@SuppressWarnings("PMD")
public class TimeStrategyTest extends MockInit {

	@Mock
	private ICoreService coreService;

	private ISendingStrategy sendingStrategy;

	@BeforeMethod
	public void initTestClass() {
		sendingStrategy = new TimeStrategy();
	}

	@Test
	public void startStop() {
		sendingStrategy.start(coreService);

		sendingStrategy.stop();

		verifyZeroInteractions(coreService);
	}

	/**
	 * This test could fail, thus it invocation count is increased to 5, which means that this test
	 * will be executed 5 times and only 60% of the tests need to be completed successfully.
	 */
	@Test(invocationCount = 5, successPercentage = 60)
	public void sendAfterOneSecond() throws InterruptedException {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("time", "1000");
		sendingStrategy.init(settings);
		sendingStrategy.start(coreService);

		synchronized (this) {
			wait(1500L);
		}

		// should be called at least once, but sometimes it could be even two
		// times.
		verify(coreService, atLeastOnce()).sendData();
	}

}
