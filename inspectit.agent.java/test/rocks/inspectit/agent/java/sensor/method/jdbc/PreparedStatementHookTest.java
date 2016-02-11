package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.util.Timer;

import java.util.Map;
import java.util.NoSuchElementException;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class PreparedStatementHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private IdManager idManager;

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
		PreparedStatementHook hook = new PreparedStatementHook(timer, idManager, statementStorage, connectionMetaDataStorage, statementReflectionCache, parameter);
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
