package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.mockito.Mockito.verifyNoMoreInteractions;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.util.Timer;

import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class StatementSensorTest extends AbstractLogSupport {

	@InjectMocks
	StatementSensor sqlTimerSensor;

	@Mock
	private ConnectionMetaDataStorage connectionMetaDataStorage;

	@Mock
	Timer timer;

	@Mock
	IIdManager idManager;

	@Mock
	StatementReflectionCache statementReflectionCache;

	@BeforeMethod
	public void initTestClass() {
	}

	@Test
	public void initSensor() {
		Map<String, Object> map = new HashMap<String, Object>();
		sqlTimerSensor.init(map);
		verifyNoMoreInteractions(timer, idManager);
	}

}
