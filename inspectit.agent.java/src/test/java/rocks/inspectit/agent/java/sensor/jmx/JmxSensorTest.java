package rocks.inspectit.agent.java.sensor.jmx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.impl.JmxSensorConfig;
import rocks.inspectit.agent.java.config.impl.UnregisteredJmxConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * All JMX sensor tests disabled until we support JXM via the Configuration interface.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings({ "unchecked", "PMD" })
public class JmxSensorTest extends TestBase {

	@InjectMocks
	private JmxSensor jmxSensor;

	@Mock
	private JmxSensorTypeConfig sensorTypeConfig;

	@Mock
	private IConfigurationStorage configurationStorage;

	@Mock
	private IPlatformManager platformManager;

	@Mock
	private MBeanServer mBeanServer;

	@Mock
	private ICoreService coreService;

	@Mock
	private MBeanInfo mBeanInfo;

	@Mock
	private Logger logger;

	/**
	 * Tests the registration of a mBean.
	 *
	 * @throws Exception
	 *             any occurring exception
	 */
	@Test(enabled = false)
	public void registerAndCollect() throws Exception {
		long sensorType = 13L;
		long platformIdent = 11L;
		String value = "value";
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testAttrDescription = "test-description";
		String testAttrType = "test-type";
		boolean testAttrIsReadable = true;
		boolean testAttrIsWriteable = false;
		boolean testAttrIsIs = false;

		when(sensorTypeConfig.getId()).thenReturn(sensorType);

		MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
		MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(sensorTypeConfig, testObjectName, testAttributeName);
		jmxSensor.setUnregisteredJmxConfigs(Collections.singletonList(unregisteredJmxConfig));

		ObjectName objectName = new ObjectName(testObjectName);
		when(mBeanServer.queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mBeanInfo);
		when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);

		when(platformManager.getPlatformId()).thenReturn(platformIdent);
		// when(platformManager.getRegisteredSensorTypeId(anyLong())).thenReturn(sensorType);
		when(mBeanServer.getAttribute(objectName, testAttributeName)).thenReturn(value);

		jmxSensor.update(coreService);

		verify(mBeanServer, times(1)).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));

		ArgumentCaptor<JmxSensorConfig> captor = ArgumentCaptor.forClass(JmxSensorConfig.class);
		// verify(platformManager, times(1)).registerJmxSensorConfig(captor.capture());
		JmxSensorConfig sensorConfig = captor.getValue();
		assertThat(sensorConfig.getJmxSensorTypeConfig(), is(equalTo(sensorTypeConfig)));
		assertThat(sensorConfig.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(sensorConfig.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(sensorConfig.getmBeanAttributeDescription(), is(equalTo(testAttrDescription)));
		assertThat(sensorConfig.getmBeanAttributeType(), is(equalTo(testAttrType)));
		assertThat(sensorConfig.getmBeanAttributeIsIs(), is(equalTo(testAttrIsIs)));
		assertThat(sensorConfig.getmBeanAttributeIsReadable(), is(equalTo(testAttrIsReadable)));
		assertThat(sensorConfig.getmBeanAttributeIsWritable(), is(equalTo(testAttrIsWriteable)));

		verify(mBeanServer, times(1)).getAttribute(objectName, testAttributeName);

		ArgumentCaptor<JmxSensorValueData> valueCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
		verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), valueCaptor.capture());

		assertThat(valueCaptor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(valueCaptor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
		assertThat(valueCaptor.getValue().getValue(), is(equalTo(value)));
	}

	@Test(dataProvider = "throwableProvider", enabled = false)
	public void beanReactivated(Class<? extends Throwable> throwable) throws Exception {
		long sensorType = 13L;
		long platformIdent = 11L;
		String value = "value";
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testAttrDescription = "test-description";
		String testAttrType = "test-type";
		boolean testAttrIsReadable = true;
		boolean testAttrIsWriteable = false;
		boolean testAttrIsIs = false;

		when(sensorTypeConfig.getId()).thenReturn(sensorType);

		MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
		MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(sensorTypeConfig, testObjectName, testAttributeName);
		jmxSensor.setUnregisteredJmxConfigs(Collections.singletonList(unregisteredJmxConfig));

		ObjectName objectName = new ObjectName(testObjectName);
		when(mBeanServer.queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName));
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mBeanInfo);
		when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);

		when(platformManager.getPlatformId()).thenReturn(platformIdent);
		// when(platformManager.getRegisteredSensorTypeId(anyLong())).thenReturn(sensorType);
		when(mBeanServer.getAttribute(objectName, testAttributeName)).thenThrow(throwable).thenReturn(value);

		// two updates but reset time stamps
		jmxSensor.update(coreService);
		jmxSensor.lastDataCollectionTimestamp = 0;
		jmxSensor.lastRegisterBeanTimestamp = 0;
		jmxSensor.update(coreService);

		verify(mBeanServer, times(2)).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));

		ArgumentCaptor<JmxSensorConfig> captor = ArgumentCaptor.forClass(JmxSensorConfig.class);
		// verify(platformManager, times(1)).registerJmxSensorConfig(captor.capture());

		JmxSensorConfig sensorConfig = captor.getValue();
		assertThat(sensorConfig.getJmxSensorTypeConfig(), is(equalTo(sensorTypeConfig)));
		assertThat(sensorConfig.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(sensorConfig.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(sensorConfig.getmBeanAttributeDescription(), is(equalTo(testAttrDescription)));
		assertThat(sensorConfig.getmBeanAttributeType(), is(equalTo(testAttrType)));
		assertThat(sensorConfig.getmBeanAttributeIsIs(), is(equalTo(testAttrIsIs)));
		assertThat(sensorConfig.getmBeanAttributeIsReadable(), is(equalTo(testAttrIsReadable)));
		assertThat(sensorConfig.getmBeanAttributeIsWritable(), is(equalTo(testAttrIsWriteable)));

		verify(mBeanServer, times(2)).getAttribute(objectName, testAttributeName);

		ArgumentCaptor<JmxSensorValueData> valueCaptor = ArgumentCaptor.forClass(JmxSensorValueData.class);
		verify(coreService).addJmxSensorValueData(eq(sensorType), eq(testObjectName), eq(testAttributeName), valueCaptor.capture());

		assertThat(valueCaptor.getValue().getPlatformIdent(), is(equalTo(platformIdent)));
		assertThat(valueCaptor.getValue().getSensorTypeIdent(), is(equalTo(sensorType)));
		assertThat(valueCaptor.getValue().getValue(), is(equalTo(value)));
	}

	@Test(dataProvider = "throwableProvider", enabled = false)
	public void beanDeactivated(Class<? extends Throwable> throwable) throws Exception {
		long sensorType = 13L;
		long platformIdent = 11L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";
		String testAttrDescription = "test-description";
		String testAttrType = "test-type";
		boolean testAttrIsReadable = true;
		boolean testAttrIsWriteable = false;
		boolean testAttrIsIs = false;

		when(sensorTypeConfig.getId()).thenReturn(sensorType);

		MBeanAttributeInfo mBeanAttributeInfo = new MBeanAttributeInfo(testAttributeName, testAttrType, testAttrDescription, testAttrIsReadable, testAttrIsWriteable, testAttrIsIs);
		MBeanAttributeInfo[] mBeanAttributeInfos = { mBeanAttributeInfo };
		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(sensorTypeConfig, testObjectName, testAttributeName);
		jmxSensor.setUnregisteredJmxConfigs(Collections.singletonList(unregisteredJmxConfig));

		ObjectName objectName = new ObjectName(testObjectName);
		when(mBeanServer.queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null))).thenReturn(Collections.singleton(objectName)).thenReturn(Collections.<ObjectName> emptySet());
		when(mBeanServer.getMBeanInfo(Mockito.<ObjectName> any())).thenReturn(mBeanInfo);
		when(mBeanInfo.getAttributes()).thenReturn(mBeanAttributeInfos);

		when(platformManager.getPlatformId()).thenReturn(platformIdent);
		//when(platformManager.getRegisteredSensorTypeId(anyLong())).thenReturn(sensorType);
		when(mBeanServer.getAttribute(objectName, testAttributeName)).thenThrow(throwable);

		// two updates but reset time stamps
		jmxSensor.update(coreService);
		jmxSensor.lastDataCollectionTimestamp = 0;
		jmxSensor.lastRegisterBeanTimestamp = 0;
		jmxSensor.update(coreService);

		verify(mBeanServer, times(2)).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));

		ArgumentCaptor<JmxSensorConfig> captor = ArgumentCaptor.forClass(JmxSensorConfig.class);
		//verify(platformManager, times(1)).registerJmxSensorConfig(captor.capture());
		JmxSensorConfig sensorConfig = captor.getValue();
		assertThat(sensorConfig.getJmxSensorTypeConfig(), is(equalTo(sensorTypeConfig)));
		assertThat(sensorConfig.getmBeanObjectName(), is(equalTo(testObjectName)));
		assertThat(sensorConfig.getAttributeName(), is(equalTo(testAttributeName)));
		assertThat(sensorConfig.getmBeanAttributeDescription(), is(equalTo(testAttrDescription)));
		assertThat(sensorConfig.getmBeanAttributeType(), is(equalTo(testAttrType)));
		assertThat(sensorConfig.getmBeanAttributeIsIs(), is(equalTo(testAttrIsIs)));
		assertThat(sensorConfig.getmBeanAttributeIsReadable(), is(equalTo(testAttrIsReadable)));
		assertThat(sensorConfig.getmBeanAttributeIsWritable(), is(equalTo(testAttrIsWriteable)));

		verify(mBeanServer, times(1)).getAttribute(objectName, testAttributeName);
		verifyZeroInteractions(coreService);
	}

	@Test(enabled = false)
	public void malformedObjectName() {
		long sensorType = 13L;
		String testObjectName = "Testdomain:Test=TestObjectName,name=test";
		String testAttributeName = "TestAttributename";

		when(sensorTypeConfig.getId()).thenReturn(sensorType);

		UnregisteredJmxConfig unregisteredJmxConfig = new UnregisteredJmxConfig(sensorTypeConfig, testObjectName, testAttributeName);
		List<UnregisteredJmxConfig> list = new ArrayList<UnregisteredJmxConfig>();
		list.add(unregisteredJmxConfig);
		jmxSensor.setUnregisteredJmxConfigs(list);

		when(mBeanServer.queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null))).thenThrow(MalformedObjectNameException.class);

		// two updates to confirm the removal
		jmxSensor.update(coreService);
		jmxSensor.lastDataCollectionTimestamp = 0;
		jmxSensor.lastRegisterBeanTimestamp = 0;
		jmxSensor.update(coreService);

		verify(mBeanServer, times(1)).queryNames(Mockito.<ObjectName> any(), (QueryExp) eq(null));
		verifyZeroInteractions(coreService, platformManager);
	}

	@DataProvider(name = "throwableProvider")
	public Object[][] getThrowables() {
		return new Object[][] { { AttributeNotFoundException.class }, { InstanceNotFoundException.class }, { MBeanException.class }, { ReflectionException.class } };
	}

}