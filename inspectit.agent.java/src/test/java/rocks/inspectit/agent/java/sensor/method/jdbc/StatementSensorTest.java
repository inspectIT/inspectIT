package rocks.inspectit.agent.java.sensor.method.jdbc;

import static org.mockito.Mockito.verifyNoMoreInteractions;


import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sensor.method.jdbc.ConnectionMetaDataStorage;
import rocks.inspectit.agent.java.sensor.method.jdbc.StatementReflectionCache;
import rocks.inspectit.agent.java.sensor.method.jdbc.StatementSensor;
import rocks.inspectit.agent.java.util.Timer;

@SuppressWarnings("PMD")
public class StatementSensorTest extends AbstractLogSupport {

	@InjectMocks
	StatementSensor sqlTimerSensor;

	@Mock
	private ConnectionMetaDataStorage connectionMetaDataStorage;

	@Mock
	Timer timer;

	@Mock
	IPlatformManager platformManager;

	@Mock
	StatementReflectionCache statementReflectionCache;

	@BeforeMethod
	public void initTestClass() {
	}

	@Test
	public void initSensor() {
		Map<String, Object> map = new HashMap<String, Object>();
		sqlTimerSensor.initHook(map);
		verifyNoMoreInteractions(timer, platformManager);
	}

}
