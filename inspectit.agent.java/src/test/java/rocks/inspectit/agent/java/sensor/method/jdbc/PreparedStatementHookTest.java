package rocks.inspectit.agent.java.sensor.method.jdbc;

import java.util.Map;
import java.util.NoSuchElementException;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.core.impl.PlatformManager;
import rocks.inspectit.agent.java.sensor.method.jdbc.ConnectionMetaDataStorage;
import rocks.inspectit.agent.java.sensor.method.jdbc.PreparedStatementHook;
import rocks.inspectit.agent.java.sensor.method.jdbc.StatementReflectionCache;
import rocks.inspectit.agent.java.sensor.method.jdbc.StatementStorage;
import rocks.inspectit.agent.java.util.Timer;

@SuppressWarnings("PMD")
public class PreparedStatementHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private PlatformManager platformManager;

	@Mock
	private StatementStorage statementStorage;

	@Mock
	private ConnectionMetaDataStorage connectionMetaDataStorage;

	@Mock
	private NoSuchElementException myNoSuchElementException;

	@Mock
	private StatementReflectionCache statementReflectionCache;

	@Mock
	private Map<String, Object> parameter;

	@Mock
	private Logger log;

	@BeforeMethod
	public void initTestClass() {
		Mockito.doThrow(myNoSuchElementException).when(statementStorage).addPreparedStatement(Mockito.anyObject());
	}

	@Test
	public void exceptionLoggingTest() {
		PreparedStatementHook hook = new PreparedStatementHook(timer, platformManager, statementStorage, connectionMetaDataStorage, statementReflectionCache, parameter);
		hook.log = log;

		// Throwing the same exception a few times... (as statement storage always raises the
		// exception)
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);
		hook.afterConstructor(null, 1, 10, "someObject", null, null);

		// ... we still should get only one printing out of the exception
		Mockito.verify(log, Mockito.times(1)).info(Mockito.anyString(), Mockito.eq(myNoSuchElementException));

		// ... if we have a different Statement (meaning different methodId) it should
		// print it out again
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);
		hook.afterConstructor(null, 2, 10, "someObject", null, null);

		Mockito.verify(log, Mockito.times(2)).info(Mockito.anyString(), Mockito.eq(myNoSuchElementException));
	}

}
