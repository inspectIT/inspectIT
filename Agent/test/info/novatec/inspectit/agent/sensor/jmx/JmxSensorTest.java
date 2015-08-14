package info.novatec.inspectit.agent.sensor.jmx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class JmxSensorTest extends AbstractLogSupport {

	private JmxSensor jmxSensor;

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IIdManager idManager;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private MBeanServer mBeanServer;

	@Mock
	private ICoreService coreService;

	@Mock
	private MBeanInfo mBeanInfo;

	@Mock
	private Map<String, JmxSensorConfig> activeAttributes;

	@Mock
	private List<UnregisteredJmxConfig> unregisteredJmxConfigs;

	@Mock
	private Map<String, JmxSensorConfig> registeredJmxSensorConfigs;

	@Mock
	private Map<String, ObjectName> nameStringToObjectName;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		jmxSensor = new JmxSensor();
		jmxSensor.mBeanServer = mBeanServer;
		jmxSensor.idManager = idManager;
		jmxSensor.configurationStorage = configurationStorage;
		jmxSensor.log = LoggerFactory.getLogger(JmxSensor.class);
	}

	/**
	 * Tests the registration of a mBean.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testRegisterMBean() throws Exception {
		jmxSensor.activeAttributes = activeAttributes;
		jmxSensor.unregisteredJmxConfigs = unregisteredJmxConfigs;
		jmxSensor.registeredJmxSensorConfigs = registeredJmxSensorConfigs;
		jmxSensor.nameStringToObjectName = nameStringToObjectName;

		JmxSensorTypeConfig sensorTypeConfig = new JmxSensorTypeConfig();
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testMBeanName = testObjectName + testAttributeName;
		String testAttrDescription = "test-description";
		String testAttrType = "test-type";
		boolean testAttrIsReadable = true;
		boolean testAttrIsWriteable = false;
		boolean testAttrIsIs = false;

		MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
		MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(sensorTypeConfig, testObjectName, testAttributeName);

		Mockito.doReturn(Collections.singleton(new ObjectName(testObjectName))).when(mBeanServer).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mBeanInfo);
		when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);

		Iterator<UnregisteredJmxConfig> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(unregisteredJmxConfig);
		when(unregisteredJmxConfigs.iterator()).thenReturn(iterator);

		jmxSensor.registerMBeans();

		ArgumentCaptor<JmxSensorConfig> captor = ArgumentCaptor.forClass(JmxSensorConfig.class);
		verify(idManager, times(1)).registerJmxSensorConfig(captor.capture());
		verify(activeAttributes, times(1)).put(testMBeanName, captor.getValue());
		verify(registeredJmxSensorConfigs, times(1)).put(testMBeanName, captor.getValue());
		verify(nameStringToObjectName, times(1)).put(eq(testObjectName), (ObjectName) Mockito.anyObject());

		JmxSensorConfig sensorConfig = captor.getValue();
		assertThat(sensorConfig.getJmxSensorTypeConfig(), is(equalTo(sensorTypeConfig)));
		assertThat(sensorConfig.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(sensorConfig.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(sensorConfig.getmBeanAttributeDescription(), is(equalTo(testAttrDescription)));
		assertThat(sensorConfig.getmBeanAttributeType(), is(equalTo(testAttrType)));
		assertThat(sensorConfig.getmBeanAttributeIsIs(), is(equalTo(testAttrIsIs)));
		assertThat(sensorConfig.getmBeanAttributeIsReadable(), is(equalTo(testAttrIsReadable)));
		assertThat(sensorConfig.getmBeanAttributeIsWritable(), is(equalTo(testAttrIsWriteable)));
	}

	/**
	 * Tests to reactivate a MBean if it was disabled.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testReactivateMBeanIfAvailable() throws Exception {
		jmxSensor.activeAttributes = activeAttributes;
		jmxSensor.unregisteredJmxConfigs = unregisteredJmxConfigs;
		jmxSensor.registeredJmxSensorConfigs = registeredJmxSensorConfigs;

		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testObjectKey = testObjectName + testAttributeName;
		JmxSensorTypeConfig jstc = new JmxSensorTypeConfig();
		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(jstc, testObjectName, testAttributeName);

		Iterator<UnregisteredJmxConfig> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(unregisteredJmxConfig);
		when(unregisteredJmxConfigs.iterator()).thenReturn(iterator);

		when(registeredJmxSensorConfigs.containsKey(testObjectKey)).thenReturn(true);
		when(activeAttributes.containsKey(testObjectKey)).thenReturn(false);

		MBeanAttributeInfo attributeInfo = Mockito.mock(MBeanAttributeInfo.class);
		when(attributeInfo.getName()).thenReturn(testAttributeName);

		when(mBeanServer.getMBeanInfo((ObjectName) Mockito.anyObject()).getAttributes()).thenReturn(new MBeanAttributeInfo[] { attributeInfo });

		Mockito.doReturn(Collections.singleton(new ObjectName(testObjectName))).when(mBeanServer).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));

		jmxSensor.registerMBeans();

		verify(activeAttributes, times(1)).put(eq(testObjectKey), (JmxSensorConfig) Mockito.anyObject());
		verify(registeredJmxSensorConfigs, times(1)).get(testObjectKey);
	}

	/**
	 * Tests the update method while adding new sensor values.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testJmxValueData() throws Exception {
		activeAttributes = Mockito.mock(Map.class, Mockito.RETURNS_DEEP_STUBS);
		jmxSensor.activeAttributes = activeAttributes;
		jmxSensor.nameStringToObjectName = nameStringToObjectName;

		long sensorType = 13L;
		long platformIdent = 11L;
		String value = "value";
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";

		JmxSensorConfig sensorConfig = new JmxSensorConfig();
		sensorConfig.setmBeanObjectName(testObjectName);
		sensorConfig.setAttributeName(testAttributeName);

		Entry<String, JmxSensorConfig> entry = Mockito.mock(Entry.class);
		when(entry.getValue()).thenReturn(sensorConfig);

		Iterator<Entry<String, JmxSensorConfig>> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(entry);

		when(activeAttributes.entrySet().iterator()).thenReturn(iterator);

		when(nameStringToObjectName.get(testObjectName)).thenReturn(new ObjectName(""));

		when(mBeanServer.getAttribute((ObjectName) Mockito.anyObject(), eq(testAttributeName))).thenReturn(value);

		when(idManager.getPlatformId()).thenReturn(platformIdent);
		when(idManager.getRegisteredSensorTypeId(Mockito.anyLong())).thenReturn(sensorType);

		jmxSensor.update(coreService, sensorType);

		ArgumentCaptor<JmxSensorValueData> captor = ArgumentCaptor.forClass(JmxSensorValueData.class);
		verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), captor.capture());

		assertThat(captor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(captor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
		assertThat(captor.getValue().getValue(), is(equalTo(value)));
	}

	/**
	 * Test if an {@link AttributeNotFoundException} occurred in the
	 * {@link JmxSensor#update(ICoreService, long)} method.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testDeactivateMBeanIfAttributeNotFoundOccurs() throws Exception {
		activeAttributes = Mockito.mock(Map.class, Mockito.RETURNS_DEEP_STUBS);
		jmxSensor.activeAttributes = activeAttributes;

		Entry<String, JmxSensorConfig> entry = Mockito.mock(Entry.class);
		when(entry.getValue()).thenReturn(new JmxSensorConfig());
		Iterator<Entry<String, JmxSensorConfig>> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(entry);
		when(activeAttributes.entrySet().iterator()).thenReturn(iterator);

		when(mBeanServer.getAttribute((ObjectName) Mockito.anyObject(), Mockito.anyString())).thenThrow(AttributeNotFoundException.class);

		jmxSensor.update(coreService, 0L);

		verify(iterator, times(1)).remove();
	}

	/**
	 * Test if an {@link InstanceNotFoundException} occurred in the
	 * {@link JmxSensor#update(ICoreService, long)} method.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testDeactivateMBeanIfInstanceNotFoundOccurs() throws Exception {
		activeAttributes = Mockito.mock(Map.class, Mockito.RETURNS_DEEP_STUBS);
		jmxSensor.activeAttributes = activeAttributes;

		Entry<String, JmxSensorConfig> entry = Mockito.mock(Entry.class);
		when(entry.getValue()).thenReturn(new JmxSensorConfig());
		Iterator<Entry<String, JmxSensorConfig>> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(entry);
		when(activeAttributes.entrySet().iterator()).thenReturn(iterator);

		when(mBeanServer.getAttribute((ObjectName) Mockito.anyObject(), Mockito.anyString())).thenThrow(InstanceNotFoundException.class);

		jmxSensor.update(coreService, 0L);

		verify(iterator, times(1)).remove();
	}

	/**
	 * Test if a {@link ReflectionException} occurred in the
	 * {@link JmxSensor#update(ICoreService, long)} method.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testDeactivateMBeanIfReflectionExceptionOccurs() throws Exception {
		activeAttributes = Mockito.mock(Map.class, Mockito.RETURNS_DEEP_STUBS);
		jmxSensor.activeAttributes = activeAttributes;

		Entry<String, JmxSensorConfig> entry = Mockito.mock(Entry.class);
		when(entry.getValue()).thenReturn(new JmxSensorConfig());
		Iterator<Entry<String, JmxSensorConfig>> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(entry);
		when(activeAttributes.entrySet().iterator()).thenReturn(iterator);

		when(mBeanServer.getAttribute((ObjectName) Mockito.anyObject(), Mockito.anyString())).thenThrow(ReflectionException.class);

		jmxSensor.update(coreService, 0L);

		verify(iterator, times(1)).remove();
	}

	/**
	 * Test if a {@link IdNotAvailableException} occurred in the
	 * {@link JmxSensor#update(ICoreService, long)} method.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testIdNotAvailableExceptionOccurs() throws Exception {
		activeAttributes = Mockito.mock(Map.class, Mockito.RETURNS_DEEP_STUBS);
		jmxSensor.activeAttributes = activeAttributes;

		Entry<String, JmxSensorConfig> entry = Mockito.mock(Entry.class);
		when(entry.getValue()).thenReturn(new JmxSensorConfig());
		Iterator<Entry<String, JmxSensorConfig>> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(entry);
		when(activeAttributes.entrySet().iterator()).thenReturn(iterator);

		when(mBeanServer.getAttribute((ObjectName) Mockito.anyObject(), Mockito.anyString())).thenReturn(42L);
		when(idManager.getPlatformId()).thenThrow(IdNotAvailableException.class);

		jmxSensor.update(coreService, 0L);
	}

	/**
	 * Test if a {@link MBeanException} occurred in the {@link JmxSensor#update(ICoreService, long)}
	 * method.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testDeactivateMBeanIfMBeanExceptionOccurs() throws Exception {
		activeAttributes = Mockito.mock(Map.class, Mockito.RETURNS_DEEP_STUBS);
		jmxSensor.activeAttributes = activeAttributes;

		Entry<String, JmxSensorConfig> entry = Mockito.mock(Entry.class);
		when(entry.getValue()).thenReturn(new JmxSensorConfig());
		Iterator<Entry<String, JmxSensorConfig>> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(entry);
		when(activeAttributes.entrySet().iterator()).thenReturn(iterator);

		when(mBeanServer.getAttribute((ObjectName) Mockito.anyObject(), Mockito.anyString())).thenThrow(MBeanException.class);

		jmxSensor.update(coreService, 0L);

		verify(iterator, times(1)).remove();
	}

	/**
	 * Tests that multiple calls of {@link JmxSensor#registerMBeans()} register the same MBean only
	 * once.
	 * 
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test
	public void testMBeanShouldBeRegisteredAndActive() throws Exception {
		jmxSensor.activeAttributes = activeAttributes;
		jmxSensor.unregisteredJmxConfigs = unregisteredJmxConfigs;
		jmxSensor.registeredJmxSensorConfigs = registeredJmxSensorConfigs;
		jmxSensor.nameStringToObjectName = nameStringToObjectName;

		JmxSensorTypeConfig sensorTypeConfig = new JmxSensorTypeConfig();
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testMBeanName = testObjectName + testAttributeName;
		String testAttrDescription = "test-description";
		String testAttrType = "test-type";
		boolean testAttrIsReadable = true;
		boolean testAttrIsWriteable = false;
		boolean testAttrIsIs = false;

		MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
		MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(sensorTypeConfig, testObjectName, testAttributeName);

		Mockito.doReturn(Collections.singleton(new ObjectName(testObjectName))).when(mBeanServer).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mBeanInfo);
		when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);

		Iterator<UnregisteredJmxConfig> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(unregisteredJmxConfig);
		when(unregisteredJmxConfigs.iterator()).thenReturn(iterator);

		jmxSensor.registerMBeans();
		jmxSensor.registerMBeans();

		ArgumentCaptor<JmxSensorConfig> captor = ArgumentCaptor.forClass(JmxSensorConfig.class);
		verify(idManager, times(1)).registerJmxSensorConfig(captor.capture());
		verify(activeAttributes, times(1)).put(testMBeanName, captor.getValue());
		verify(registeredJmxSensorConfigs, times(1)).put(testMBeanName, captor.getValue());
		verify(nameStringToObjectName, times(1)).put(eq(testObjectName), (ObjectName) Mockito.anyObject());

		JmxSensorConfig sensorConfig = captor.getValue();
		assertThat(sensorConfig.getJmxSensorTypeConfig(), is(equalTo(sensorTypeConfig)));
		assertThat(sensorConfig.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(sensorConfig.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(sensorConfig.getmBeanAttributeDescription(), is(equalTo(testAttrDescription)));
		assertThat(sensorConfig.getmBeanAttributeType(), is(equalTo(testAttrType)));
		assertThat(sensorConfig.getmBeanAttributeIsIs(), is(equalTo(testAttrIsIs)));
		assertThat(sensorConfig.getmBeanAttributeIsReadable(), is(equalTo(testAttrIsReadable)));
		assertThat(sensorConfig.getmBeanAttributeIsWritable(), is(equalTo(testAttrIsWriteable)));
	}

	/**
	 * Test if a {@link MalformedObjectNameException} occurred in the
	 * {@link JmxSensor#registerMBeans()} method.
	 */
	@Test
	public void testMalformedObjectNameExceptionIsThrown() {
		jmxSensor.activeAttributes = activeAttributes;
		jmxSensor.unregisteredJmxConfigs = unregisteredJmxConfigs;
		jmxSensor.registeredJmxSensorConfigs = registeredJmxSensorConfigs;

		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(null, "", "");

		Iterator<UnregisteredJmxConfig> iterator = Mockito.mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true, false);
		when(iterator.next()).thenReturn(unregisteredJmxConfig);
		when(unregisteredJmxConfigs.iterator()).thenReturn(iterator);

		when(mBeanServer.queryNames((ObjectName) Mockito.anyObject(), (QueryExp) Mockito.anyObject())).thenThrow(MalformedObjectNameException.class);

		jmxSensor.registerMBeans();

		verify(iterator, times(1)).remove();
	}

}