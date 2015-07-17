package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.impl.IdManager;
import info.novatec.inspectit.util.Timer;

import java.sql.Connection;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConnectionMetaDataHookTest extends AbstractLogSupport {

	ConnectionMetaDataHook hook;

	@Mock
	ConnectionMetaDataStorage storage;

	@Mock
	Connection myConnection;

	@Mock
	private Timer timer;

	@Mock
	private IdManager idManager;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@BeforeMethod
	public void init() {
		hook = new ConnectionMetaDataHook(storage);
	}

	@Test
	public void normalRunThrough() {
		// set up data
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object[] parameters = new Object[0];

		// dispatch the beforeMethod
		hook.beforeConstructor(methodId, sensorTypeId, parameters, registeredSensorConfig);
		Mockito.verifyZeroInteractions(storage);

		hook.afterConstructor(coreService, registeredMethodId, registeredSensorTypeId, myConnection, parameters, registeredSensorConfig);
		Mockito.verify(storage, Mockito.timeout(1)).add(myConnection);
		Mockito.verifyNoMoreInteractions(storage);
	}
}
