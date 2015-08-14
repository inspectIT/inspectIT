package info.novatec.inspectit.agent.sensor.jmx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.impl.JmxSensorConfig;
import info.novatec.inspectit.agent.config.impl.JmxSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.UnregisteredJmxConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.communication.data.JmxSensorValueData;

import java.util.Collections;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JmxSensorTest extends AbstractLogSupport {
	private JmxSensor jmxSensor;

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IIdManager idManager;

	@Mock
	private MBeanServer mBeanServer;

	@Mock
	private ICoreService coreService;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		jmxSensor = new JmxSensor();
		jmxSensor.mBeanServer = mBeanServer;
		jmxSensor.idManager = idManager;
		jmxSensor.configurationStorage = configurationStorage;
		jmxSensor.log = LoggerFactory.getLogger(JmxSensor.class);
	}

	@Test
	public void testRegisterMBean() throws IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException {

		JmxSensorTypeConfig jstc = new JmxSensorTypeConfig();
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testMBeanName = testObjectName + testAttributeName;
		String testAttributeDescription = "test-description";
		String testAttributeType = "test-type";
		boolean testAttributeIsReadable = true;
		boolean testAttributeIsWriteable = false;
		boolean testAttributeisIs = false;

		MBeanAttributeInfo mba = new MBeanAttributeInfo(testAttributeName, testAttributeType, testAttributeDescription, testAttributeIsReadable, testAttributeIsWriteable, testAttributeisIs);
		MBeanAttributeInfo[] mbas = { mba };

		MBeanInfo mbi = Mockito.mock(MBeanInfo.class);

		UnregisteredJmxConfig ujc = new UnregisteredJmxConfig(jstc, testObjectName, testAttributeName);

		Mockito.doReturn(Collections.singleton(new ObjectName(testObjectName))).when(mBeanServer).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mbi);
		when(mbi.getAttributes()).thenReturn(mbas);

		assertThat(jmxSensor.unregisteredJmxConfigs, is(empty()));
		assertThat(jmxSensor.registeredJmxSensorConfigs.entrySet(), is(empty()));
		assertThat(jmxSensor.activeAttributes.entrySet(), is(empty()));

		jmxSensor.unregisteredJmxConfigs.add(ujc);

		jmxSensor.registerMBeans();

		JmxSensorConfig jsc = jmxSensor.registeredJmxSensorConfigs.get(testMBeanName);

		assertThat(jmxSensor.activeAttributes.entrySet(), hasSize(1));
		assertThat(jmxSensor.registeredJmxSensorConfigs.entrySet(), hasSize(1));
		assertThat(jsc.getJmxSensorTypeConfig(), is(equalTo(jstc)));
		assertThat(jsc.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(jsc.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(jsc.getmBeanAttributeDescription(), is(equalTo(testAttributeDescription)));
		assertThat(jsc.getmBeanAttributeType(), is(equalTo(testAttributeType)));
		assertThat(jsc.getmBeanAttributeIsIs(), is(equalTo(testAttributeisIs)));
		assertThat(jsc.getmBeanAttributeIsReadable(), is(equalTo(testAttributeIsReadable)));
		assertThat(jsc.getmBeanAttributeIsWritable(), is(equalTo(testAttributeIsWriteable)));
	}

	@Test
	public void testJmxValueData() throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException, MBeanException,
			IdNotAvailableException {

		long sensorType = 13L;
		long platformIdent = 11L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";

		MBeanAttributeInfo mba = new MBeanAttributeInfo(testAttributeName, "test-type", "test-description", true, false, false);
		MBeanAttributeInfo[] mbas = { mba };

		MBeanInfo mbi = Mockito.mock(MBeanInfo.class);

		UnregisteredJmxConfig ujc = new UnregisteredJmxConfig(new JmxSensorTypeConfig(), testObjectName, testAttributeName);
		Mockito.doReturn(Collections.singleton(new ObjectName(testObjectName))).when(mBeanServer).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mbi);
		when(mbi.getAttributes()).thenReturn(mbas);
		when(mBeanServer.getAttribute(Mockito.<ObjectName> any(), Mockito.<String> any())).thenReturn(42);
		when(idManager.getPlatformId()).thenReturn(11L);
		when(idManager.getRegisteredmBeanId(Mockito.anyLong())).thenReturn(13L);
		when(idManager.getRegisteredSensorTypeId(Mockito.anyLong())).thenReturn(sensorType);

		jmxSensor.unregisteredJmxConfigs.add(ujc);

		jmxSensor.update(coreService, sensorType);

		ArgumentCaptor<JmxSensorValueData> sensorDataCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
		verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), sensorDataCaptor.capture());
		JmxSensorValueData parameter = sensorDataCaptor.getValue();

		assertThat(parameter, is(instanceOf(JmxSensorValueData.class)));
		assertThat(parameter.getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(parameter.getSensorTypeIdent(), is(equalTo(sensorType)));
		assertThat(parameter.getValue(), is(equalTo("42")));
	}

	@Test
	public void testDeactivateMBeanIfAttributeNotFound() throws IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
			MBeanException {

		long sensorType = 13L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		JmxSensorConfig jsc = new JmxSensorConfig();

		jmxSensor.activeAttributes.put(testObjectName + testAttributeName, jsc);

		when(mBeanServer.getAttribute(Mockito.<ObjectName> any(), Mockito.<String> any())).thenThrow(AttributeNotFoundException.class);

		jmxSensor.update(coreService, sensorType);

		assertThat(jmxSensor.activeAttributes.entrySet(), is(empty()));
	}

	@Test
	public void testDeactivateMBeanIfInstanceNotFound() throws IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
			MBeanException {

		long sensorType = 13L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		JmxSensorConfig jsc = new JmxSensorConfig();

		jmxSensor.activeAttributes.put(testObjectName + testAttributeName, jsc);

		when(mBeanServer.getAttribute(Mockito.<ObjectName> any(), Mockito.<String> any())).thenThrow(InstanceNotFoundException.class);

		jmxSensor.update(coreService, sensorType);

		assertThat(jmxSensor.activeAttributes.entrySet(), is(empty()));
	}

	@Test
	public void testRegisteredJmxConfigNotAvailable() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IdNotAvailableException {
		long sensorType = 13L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		JmxSensorConfig jsc = new JmxSensorConfig();

		jmxSensor.activeAttributes.put(testObjectName + testAttributeName, jsc);

		when(mBeanServer.getAttribute(Mockito.<ObjectName> any(), Mockito.<String> any())).thenReturn("42");
		when(idManager.getRegisteredmBeanId(Mockito.anyLong())).thenThrow(IdNotAvailableException.class);

		jmxSensor.update(coreService, sensorType);

		assertThat(jmxSensor.activeAttributes.entrySet(), hasSize(1));
	}

	@Test
	public void testDeactivateMBeanIfMBeanExceptionOccurs() throws IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, AttributeNotFoundException,
			MBeanException {

		long sensorType = 13L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		JmxSensorConfig jsc = new JmxSensorConfig();

		jmxSensor.activeAttributes.put(testObjectName + testAttributeName, jsc);

		when(mBeanServer.getAttribute(Mockito.<ObjectName> any(), Mockito.<String> any())).thenThrow(MBeanException.class);

		jmxSensor.update(coreService, sensorType);

		assertThat(jmxSensor.activeAttributes.entrySet(), is(empty()));
	}

	@Test
	public void testDeactivateMBeanIfReflectionExceptionOccurs() throws IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException,
			AttributeNotFoundException, MBeanException {

		long sensorType = 13L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		JmxSensorConfig jsc = new JmxSensorConfig();

		jmxSensor.activeAttributes.put(testObjectName + testAttributeName, jsc);

		when(mBeanServer.getAttribute(Mockito.<ObjectName> any(), Mockito.<String> any())).thenThrow(ReflectionException.class);

		jmxSensor.update(coreService, sensorType);

		assertThat(jmxSensor.activeAttributes.entrySet(), is(empty()));
	}

	@Test
	public void testReactivateMBeanIfAvailable() throws MalformedObjectNameException {
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		JmxSensorTypeConfig jstc = new JmxSensorTypeConfig();
		JmxSensorConfig jsc = new JmxSensorConfig();
		UnregisteredJmxConfig ujc = new UnregisteredJmxConfig(jstc, testObjectName, testAttributeName);

		jmxSensor.unregisteredJmxConfigs.add(ujc);
		jmxSensor.registeredJmxSensorConfigs.put(testObjectName + testAttributeName, jsc);

		Mockito.doReturn(Collections.singleton(new ObjectName(testObjectName))).when(mBeanServer).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));

		jmxSensor.registerMBeans();

		assertThat(jmxSensor.activeAttributes.entrySet(), hasSize(1));
	}

	@Test(dependsOnMethods = { "testRegisterMBean" })
	public void testMBeanShouldBeRegisteredAndActive() throws IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException {
		JmxSensorTypeConfig jstc = new JmxSensorTypeConfig();
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testMBeanName = testObjectName + testAttributeName;
		String testAttributeDescription = "test-description";
		String testAttributeType = "test-type";
		boolean testAttributeIsReadable = true;
		boolean testAttributeIsWriteable = false;
		boolean testAttributeisIs = false;

		MBeanAttributeInfo mba = new MBeanAttributeInfo(testAttributeName, testAttributeType, testAttributeDescription, testAttributeIsReadable, testAttributeIsWriteable, testAttributeisIs);
		MBeanAttributeInfo[] mbas = { mba };

		MBeanInfo mbi = Mockito.mock(MBeanInfo.class);

		UnregisteredJmxConfig ujc = new UnregisteredJmxConfig(jstc, testObjectName, testAttributeName);
		
		Mockito.doReturn(Collections.singleton(new ObjectName(testObjectName))).when(mBeanServer).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mbi);
		when(mbi.getAttributes()).thenReturn(mbas);

		assertThat(jmxSensor.unregisteredJmxConfigs, is(empty()));
		assertThat(jmxSensor.registeredJmxSensorConfigs.entrySet(), is(empty()));
		assertThat(jmxSensor.activeAttributes.entrySet(), is(empty()));

		jmxSensor.unregisteredJmxConfigs.add(ujc);

		jmxSensor.registerMBeans();

		JmxSensorConfig jsc = jmxSensor.registeredJmxSensorConfigs.get(testMBeanName);

		assertThat(jmxSensor.activeAttributes.entrySet(), hasSize(1));
		assertThat(jmxSensor.registeredJmxSensorConfigs.entrySet(), hasSize(1));
		assertThat(jsc.getJmxSensorTypeConfig(), is(equalTo(jstc)));
		assertThat(jsc.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(jsc.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(jsc.getmBeanAttributeDescription(), is(equalTo(testAttributeDescription)));
		assertThat(jsc.getmBeanAttributeType(), is(equalTo(testAttributeType)));
		assertThat(jsc.getmBeanAttributeIsIs(), is(equalTo(testAttributeisIs)));
		assertThat(jsc.getmBeanAttributeIsReadable(), is(equalTo(testAttributeIsReadable)));
		assertThat(jsc.getmBeanAttributeIsWritable(), is(equalTo(testAttributeIsWriteable)));

		// re-run registerMBeans() to verify that nothing has changed
		jmxSensor.registerMBeans();
		
		assertThat(jmxSensor.activeAttributes.entrySet(), hasSize(1));
		assertThat(jmxSensor.registeredJmxSensorConfigs.entrySet(), hasSize(1));
		assertThat(jsc.getJmxSensorTypeConfig(), is(equalTo(jstc)));
		assertThat(jsc.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(jsc.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(jsc.getmBeanAttributeDescription(), is(equalTo(testAttributeDescription)));
		assertThat(jsc.getmBeanAttributeType(), is(equalTo(testAttributeType)));
		assertThat(jsc.getmBeanAttributeIsIs(), is(equalTo(testAttributeisIs)));
		assertThat(jsc.getmBeanAttributeIsReadable(), is(equalTo(testAttributeIsReadable)));
		assertThat(jsc.getmBeanAttributeIsWritable(), is(equalTo(testAttributeIsWriteable)));
	}

	@Test
	public void testMalformedObjectNameExceptionIsThrown() {

		String unresolvableTestObjectName = "unresolvableTestObjectName";
		String testAttributeName = "TestAttributename";
		JmxSensorTypeConfig jstc = new JmxSensorTypeConfig();

		assertThat(jmxSensor.unregisteredJmxConfigs, is(empty()));

		UnregisteredJmxConfig ujc2 = new UnregisteredJmxConfig(jstc, unresolvableTestObjectName, testAttributeName);
		jmxSensor.unregisteredJmxConfigs.add(ujc2);

		jmxSensor.registerMBeans();

		assertThat(jmxSensor.registeredJmxSensorConfigs.entrySet(), is(empty()));
		assertThat(jmxSensor.unregisteredJmxConfigs, is(empty()));
	}

	@AfterMethod
	private void resetLists() {
		jmxSensor.unregisteredJmxConfigs.clear();
		jmxSensor.registeredJmxSensorConfigs.clear();
		jmxSensor.activeAttributes.clear();
	}

}