package rocks.inspectit.server.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.dao.JmxDefinitionDataIdentDao;
import rocks.inspectit.server.dao.MethodIdentDao;
import rocks.inspectit.server.dao.MethodIdentToSensorTypeDao;
import rocks.inspectit.server.dao.MethodSensorTypeIdentDao;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.dao.PlatformSensorTypeIdentDao;
import rocks.inspectit.server.dao.impl.JmxSensorTypeIdentDaoImpl;
import rocks.inspectit.server.dao.impl.MethodIdentDaoImpl;
import rocks.inspectit.server.dao.impl.MethodSensorTypeIdentDaoImpl;
import rocks.inspectit.server.dao.impl.PlatformIdentDaoImpl;
import rocks.inspectit.server.dao.impl.PlatformSensorTypeIdentDaoImpl;
import rocks.inspectit.server.messaging.AgentInstrumentationMessageGate;
import rocks.inspectit.server.messaging.AgentMessageProvider;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.server.util.PlatformIdentCache;
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.JmxSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdentToSensorType;
import rocks.inspectit.shared.all.cmr.model.MethodSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformSensorTypeIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Thesting the {@link RegistrationService} of CMR.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class RegistrationServiceTest extends TestBase {

	/**
	 * Service to test.
	 */
	@InjectMocks
	RegistrationService registrationService;

	/**
	 * Mocked {@link PlatformIdentDaoImpl}.
	 */
	@Mock
	PlatformIdentDao platformIdentDao;

	/**
	 * Mocked {@link MethodIdentDaoImpl}.
	 */
	@Mock
	MethodIdentDao methodIdentDao;

	/**
	 * Mocked {@link MethodSensorTypeIdentDaoImpl}.
	 */
	@Mock
	MethodSensorTypeIdentDao methodSensorTypeIdentDao;

	/**
	 * Mocked {@link PlatformSensorTypeIdentDaoImpl}.
	 */
	@Mock
	PlatformSensorTypeIdentDao platformSensorTypeIdentDao;

	@Mock
	AgentStatusDataProvider agentStatusDataProvider;

	@Mock
	MethodIdentToSensorTypeDao methodIdentToSensorTypeDao;

	@Mock
	JmxSensorTypeIdentDaoImpl jmxSensorTypeIdentDao;

	@Mock
	JmxDefinitionDataIdentDao jmxDefinitionDataIdentDao;

	@Mock
	PlatformIdentCache platformIdentCache;

	@Mock
	AgentInstrumentationMessageGate messageGate;

	@Mock
	AgentMessageProvider messageProvider;

	@Mock
	Logger log;

	public class RegisterPlatformIdent extends RegistrationServiceTest {
		/**
		 * Tests that an exception will be thrown if the database returns two or more platform
		 * idents after findByExample search.
		 *
		 * @throws BusinessException
		 */
		@Test(expectedExceptions = { BusinessException.class })
		public void noRegistrationTwoAgents() throws BusinessException {
			List<String> definedIps = new ArrayList<>();
			definedIps.add("ip");
			String agentName = "agentName";
			String version = "version";

			List<PlatformIdent> dbResponseList = new ArrayList<>();
			dbResponseList.add(new PlatformIdent());
			dbResponseList.add(new PlatformIdent());
			when(platformIdentDao.findByNameAndIps(agentName, definedIps)).thenReturn(dbResponseList);

			registrationService.registerPlatformIdent(definedIps, agentName, version);
		}

		/**
		 * Test that registration will be done properly if the {@link LicenseUtil} validates
		 * license.
		 *
		 * @throws BusinessException
		 *             If {@link BusinessException} occurs.
		 */
		@Test
		public void registerNewPlatformIdent() throws BusinessException {
			final long platformId = 10;
			List<String> definedIps = new ArrayList<>();
			definedIps.add("ip");
			final String agentName = "agentName";
			String version = "version";

			when(platformIdentDao.findByNameAndIps(agentName, definedIps)).thenReturn(Collections.<PlatformIdent> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					PlatformIdent platformIdent = (PlatformIdent) invocation.getArguments()[0];
					platformIdent.setId(Long.valueOf(platformId));
					platformIdent.setAgentName(agentName);
					return null;
				}
			}).when(platformIdentDao).saveOrUpdate((PlatformIdent) anyObject());

			long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
			assertThat(registeredId, is(equalTo(platformId)));

			ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
			verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

			assertThat(argument.getValue().getDefinedIPs(), is(equalTo(definedIps)));
			assertThat(argument.getValue().getAgentName(), is(equalTo(agentName)));
			assertThat(argument.getValue().getVersion(), is(equalTo(version)));
			assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));

			verify(agentStatusDataProvider, times(1)).registerConnected(platformId);

			verify(messageGate).clear(platformId);
			verify(messageProvider).clear(platformId);
		}

		/**
		 * Tests that the version and timestamp will be updated if the agent is already registered.
		 *
		 * @throws BusinessException
		 *             If {@link BusinessException} occurs.
		 */
		@Test
		public void registerExistingPlatformIdent() throws BusinessException {
			long platformId = 10;
			List<String> definedIps = new ArrayList<>();
			definedIps.add("ip");
			String agentName = "agentName";
			String version = "version";
			Timestamp timestamp = new Timestamp(1);

			PlatformIdent platformIdent = new PlatformIdent();
			platformIdent.setId(Long.valueOf(platformId));
			platformIdent.setAgentName(agentName);
			platformIdent.setDefinedIPs(definedIps);
			platformIdent.setVersion("versionOld");
			platformIdent.setTimeStamp(timestamp);
			List<PlatformIdent> findByExampleList = new ArrayList<>();
			findByExampleList.add(platformIdent);

			when(platformIdentDao.findByNameAndIps(agentName, definedIps)).thenReturn(findByExampleList);

			long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
			assertThat(registeredId, is(equalTo(platformId)));

			ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
			verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

			assertThat(argument.getValue().getDefinedIPs(), is(equalTo(definedIps)));
			assertThat(argument.getValue().getAgentName(), is(equalTo(agentName)));
			assertThat(argument.getValue().getVersion(), is(equalTo(version)));
			assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));
			assertThat(argument.getValue().getTimeStamp(), is(not(timestamp)));

			verify(agentStatusDataProvider, times(1)).registerConnected(platformId);

			verify(messageGate).clear(platformId);
			verify(messageProvider).clear(platformId);
		}

		/**
		 * Test that registration will be done properly if the {@link LicenseUtil} validates license
		 * and IP based registration is off.
		 *
		 * @throws BusinessException
		 *             If {@link BusinessException} occurs.
		 */
		@Test
		public void registerNewPlatformIdentNoIpBased() throws BusinessException {
			final long platformId = 10;
			List<String> definedIps = new ArrayList<>();
			definedIps.add("ip");
			final String agentName = "agentName";
			String version = "version";

			registrationService.ipBasedAgentRegistration = false;
			when(platformIdentDao.findByName(agentName)).thenReturn(Collections.<PlatformIdent> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					PlatformIdent platformIdent = (PlatformIdent) invocation.getArguments()[0];
					platformIdent.setId(Long.valueOf(platformId));
					platformIdent.setAgentName(agentName);
					return null;
				}
			}).when(platformIdentDao).saveOrUpdate((PlatformIdent) anyObject());

			long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
			assertThat(registeredId, equalTo(platformId));

			ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
			verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

			assertThat(argument.getValue().getDefinedIPs(), equalTo(definedIps));
			assertThat(argument.getValue().getAgentName(), equalTo(agentName));
			assertThat(argument.getValue().getVersion(), equalTo(version));
			assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));

			verify(agentStatusDataProvider, times(1)).registerConnected(platformId);

			verify(messageGate).clear(platformId);
			verify(messageProvider).clear(platformId);
		}

		/**
		 * Tests that the version and timestamp will be updated if the agent is already registered
		 * and IP registration is off.
		 *
		 * @throws BusinessException
		 *             If {@link BusinessException} occurs.
		 */
		@Test
		public void registerExistingPlatformIdentNoIpBased() throws BusinessException {
			long platformId = 10;
			List<String> definedIps = new ArrayList<>();
			definedIps.add("ip");
			String agentName = "agentName";
			String version = "version";
			Timestamp timestamp = new Timestamp(1);

			PlatformIdent platformIdent = new PlatformIdent();
			platformIdent.setId(Long.valueOf(platformId));
			platformIdent.setAgentName(agentName);
			platformIdent.setDefinedIPs(Collections.<String> emptyList());
			platformIdent.setVersion("versionOld");
			platformIdent.setTimeStamp(timestamp);
			List<PlatformIdent> findByExampleList = new ArrayList<>();
			findByExampleList.add(platformIdent);

			registrationService.ipBasedAgentRegistration = false;
			when(platformIdentDao.findByName(agentName)).thenReturn(findByExampleList);

			long registeredId = registrationService.registerPlatformIdent(definedIps, agentName, version);
			assertThat(registeredId, equalTo(platformId));

			ArgumentCaptor<PlatformIdent> argument = ArgumentCaptor.forClass(PlatformIdent.class);
			verify(platformIdentDao, times(1)).saveOrUpdate(argument.capture());

			assertThat(argument.getValue().getDefinedIPs(), equalTo(definedIps));
			assertThat(argument.getValue().getAgentName(), equalTo(agentName));
			assertThat(argument.getValue().getVersion(), equalTo(version));
			assertThat(argument.getValue().getTimeStamp(), is(notNullValue()));
			assertThat(argument.getValue().getTimeStamp(), not(equalTo(timestamp)));

			verify(agentStatusDataProvider, times(1)).registerConnected(platformId);

			verify(messageGate).clear(platformId);
			verify(messageProvider).clear(platformId);
		}
	}

	public class UnregisterPlatformIdent extends RegistrationServiceTest {

		/**
		 * Test unregistration of platform ident.
		 */
		@Test
		public void unregisterPlatformIdent() throws BusinessException {
			long platformId = 10;
			when(agentStatusDataProvider.registerDisconnected(platformId)).thenReturn(true);

			registrationService.unregisterPlatformIdent(platformId);

			verify(agentStatusDataProvider, times(1)).registerDisconnected(platformId);
		}

		/**
		 * Confirm that {@link BusinessException} is thrown if platform ident can not be located.
		 */
		@Test(expectedExceptions = { BusinessException.class })
		public void unregisterNotExistingPlatformIdent() throws BusinessException {
			long platformId = 10;
			when(agentStatusDataProvider.registerDisconnected(platformId)).thenReturn(false);

			registrationService.unregisterPlatformIdent(platformId);
		}
	}

	public class RegisterMethodIdent extends RegistrationServiceTest {

		/**
		 * Tests registration of the new {@link MethodIdent}.
		 */
		@Test
		public void registerNewMethodIdent() {
			final long methodId = 20;
			long platformId = 1;
			String packageName = "package";
			String className = "class";
			String methodName = "method";
			List<String> parameterTypes = new ArrayList<>();
			parameterTypes.add("parameter");
			String returnType = "returnType";
			int modifiers = 2;
			when(methodIdentDao.findIdForPlatformIdAndExample(eq(platformId), (MethodIdent) anyObject(), anyBoolean())).thenReturn(Collections.<Long> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					MethodIdent methodIdent = (MethodIdent) invocation.getArguments()[0];
					methodIdent.setId(Long.valueOf(methodId));
					return null;
				}
			}).when(methodIdentDao).saveOrUpdate((MethodIdent) anyObject());

			long registeredId = registrationService.registerMethodIdent(platformId, packageName, className, methodName, parameterTypes, returnType, modifiers);

			assertThat(registeredId, equalTo(methodId));

			verify(platformIdentCache, times(1)).markDirty(platformId);
			ArgumentCaptor<MethodIdent> argument = ArgumentCaptor.forClass(MethodIdent.class);
			verify(methodIdentDao, times(1)).saveOrUpdate(argument.capture());
			verify(methodIdentDao, times(1)).findIdForPlatformIdAndExample(eq(platformId), (MethodIdent) anyObject(), eq(true));
			verify(platformIdentCache, times(1)).markDirty(platformId);

			assertThat(argument.getValue().getPlatformIdent().getId(), is(equalTo(platformId)));
			assertThat(argument.getValue().getPackageName(), is(equalTo(packageName)));
			assertThat(argument.getValue().getClassName(), is(equalTo(className)));
			assertThat(argument.getValue().getMethodName(), is(equalTo(methodName)));
			assertThat(argument.getValue().getParameters(), is(equalTo(parameterTypes)));
			assertThat(argument.getValue().getReturnType(), is(equalTo(returnType)));
			assertThat(argument.getValue().getModifiers(), is(equalTo(modifiers)));
			assertThat(argument.getValue(), is(notNullValue()));
		}

		/**
		 * Tests registration of the existing {@link MethodIdent}.
		 */
		@Test
		public void registerExistnigMethodIdent() {
			final long methodId = 20;
			long platformId = 1;
			String packageName = "package";
			String className = "class";
			String methodName = "method";
			List<String> parameterTypes = new ArrayList<>();
			parameterTypes.add("parameter");
			String returnType = "returnType";
			int modifiers = 2;

			when(methodIdentDao.findIdForPlatformIdAndExample(eq(platformId), (MethodIdent) anyObject(), anyBoolean())).thenReturn(Collections.singletonList(methodId));

			long registeredId = registrationService.registerMethodIdent(platformId, packageName, className, methodName, parameterTypes, returnType, modifiers);
			assertThat(registeredId, equalTo(methodId));

			verify(platformIdentCache, times(1)).markDirty(platformId);
			ArgumentCaptor<MethodIdent> argument = ArgumentCaptor.forClass(MethodIdent.class);
			verify(methodIdentDao, times(1)).findIdForPlatformIdAndExample(eq(platformId), argument.capture(), eq(true));
			verify(platformIdentCache, times(1)).markDirty(platformId);

			assertThat(argument.getValue().getPackageName(), is(equalTo(packageName)));
			assertThat(argument.getValue().getClassName(), is(equalTo(className)));
			assertThat(argument.getValue().getMethodName(), is(equalTo(methodName)));
			assertThat(argument.getValue().getParameters(), is(equalTo(parameterTypes)));
			assertThat(argument.getValue().getReturnType(), is(equalTo(returnType)));
			assertThat(argument.getValue().getModifiers(), is(equalTo(modifiers)));
		}
	}

	public class RegisterMethodSensorTypeIdent extends RegistrationServiceTest {
		/**
		 * Test the registration of the method sensor type.
		 */
		@Test
		public void registerMethodSensorType() {
			final long methodSensorId = 30;
			long platformId = 1;
			final String fqcName = "class";

			when(methodSensorTypeIdentDao.findIdByClassNameAndPlatformId(fqcName, platformId)).thenReturn(Collections.<Long> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					MethodSensorTypeIdent methodSensorIdent = (MethodSensorTypeIdent) invocation.getArguments()[0];
					methodSensorIdent.setId(Long.valueOf(methodSensorId));
					methodSensorIdent.setFullyQualifiedClassName(fqcName);
					return null;
				}
			}).when(methodSensorTypeIdentDao).saveOrUpdate((MethodSensorTypeIdent) anyObject());

			long registeredId = registrationService.registerMethodSensorTypeIdent(platformId, fqcName, Collections.<String, Object> emptyMap());
			assertThat(registeredId, is(equalTo(methodSensorId)));

			verify(platformIdentCache, times(1)).markDirty(platformId);
			ArgumentCaptor<MethodSensorTypeIdent> methodSensorArgument = ArgumentCaptor.forClass(MethodSensorTypeIdent.class);
			verify(methodSensorTypeIdentDao, times(1)).saveOrUpdate(methodSensorArgument.capture());
			assertThat(methodSensorArgument.getValue().getFullyQualifiedClassName(), is(equalTo(fqcName)));
			assertThat(methodSensorArgument.getValue().getPlatformIdent().getId(), is(equalTo(platformId)));

			verify(platformIdentCache, times(1)).markDirty(platformId);
		}

		/**
		 * Test that the registration of the {@link MethodSensorTypeIdent} will be correct if
		 * properties are provided.
		 */
		@SuppressWarnings("unchecked")
		@Test
		public void registerMethodSensorTypeWithSettings() {
			final long methodSensorId = 30;
			long platformId = 1;
			final String fqcName = "class";
			String regEx = "myRegEx";
			String regExTemplate = "myRegExTemplate";

			Map<String, Object> settings = MapUtils.putAll(new HashMap<String, Object>(), new String[][] { { "regEx", regEx }, { "regExTemplate", regExTemplate } });

			when(methodSensorTypeIdentDao.findIdByClassNameAndPlatformId(fqcName, platformId)).thenReturn(Collections.<Long> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					MethodSensorTypeIdent methodSensorIdent = (MethodSensorTypeIdent) invocation.getArguments()[0];
					methodSensorIdent.setId(Long.valueOf(methodSensorId));
					methodSensorIdent.setFullyQualifiedClassName(fqcName);
					return null;
				}
			}).when(methodSensorTypeIdentDao).saveOrUpdate((MethodSensorTypeIdent) anyObject());

			long registeredId = registrationService.registerMethodSensorTypeIdent(platformId, fqcName, settings);
			assertThat(registeredId, is(equalTo(methodSensorId)));

			verify(platformIdentCache, times(1)).markDirty(platformId);
			ArgumentCaptor<MethodSensorTypeIdent> methodSensorArgument = ArgumentCaptor.forClass(MethodSensorTypeIdent.class);
			verify(methodSensorTypeIdentDao, times(1)).saveOrUpdate(methodSensorArgument.capture());
			assertThat(methodSensorArgument.getValue().getSettings(), is(settings));
		}

		/**
		 * Test that the registration of the {@link MethodSensorTypeIdent} will be correct if
		 * properties are provided.
		 */
		@SuppressWarnings("unchecked")
		@Test
		public void registerExistingMethodSensorTypeWithSettings() {
			final long methodSensorId = 30;
			long platformId = 1;
			final String fqcName = "class";
			String regEx = "myRegEx";
			String regExTemplate = "myRegExTemplate";

			Map<String, Object> settings = MapUtils.putAll(new HashMap<String, Object>(), new String[][] { { "regEx", regEx }, { "regExTemplate", regExTemplate } });

			when(methodSensorTypeIdentDao.findIdByClassNameAndPlatformId(fqcName, platformId)).thenReturn(Collections.singletonList(methodSensorId));

			long registeredId = registrationService.registerMethodSensorTypeIdent(platformId, fqcName, settings);
			assertThat(registeredId, is(equalTo(methodSensorId)));

			verify(methodSensorTypeIdentDao, times(1)).updateParameters(methodSensorId, settings);
		}
	}

	public class RegisterPlatformSensorTypeIdent extends RegistrationServiceTest {

		/**
		 * Test the registration of the platform sensor type.
		 */
		@Test
		public void registerPlatformSensorType() {
			final long platformSensorId = 20;
			long platformId = 1;
			final String fqcName = "class";

			when(platformSensorTypeIdentDao.findIdByClassNameAndPlatformId(fqcName, platformId)).thenReturn(Collections.<Long> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					PlatformSensorTypeIdent platformSensorTypeIdent = (PlatformSensorTypeIdent) invocation.getArguments()[0];
					platformSensorTypeIdent.setId(Long.valueOf(platformSensorId));
					platformSensorTypeIdent.setFullyQualifiedClassName(fqcName);
					return null;
				}
			}).when(platformSensorTypeIdentDao).saveOrUpdate((PlatformSensorTypeIdent) anyObject());

			long registeredId = registrationService.registerPlatformSensorTypeIdent(platformId, fqcName);
			assertThat(registeredId, is(equalTo(platformSensorId)));

			verify(platformIdentCache, times(1)).markDirty(platformId);
			ArgumentCaptor<PlatformSensorTypeIdent> platformSensorArgument = ArgumentCaptor.forClass(PlatformSensorTypeIdent.class);
			verify(platformSensorTypeIdentDao, times(1)).saveOrUpdate(platformSensorArgument.capture());
			assertThat(platformSensorArgument.getValue().getFullyQualifiedClassName(), is(equalTo(fqcName)));
			assertThat(platformSensorArgument.getValue().getPlatformIdent().getId(), is(equalTo(platformId)));

			verify(platformIdentCache, times(1)).markDirty(platformId);
		}
	}

	public class RegisterJmxSensorTypeIdent extends RegistrationServiceTest {

		/**
		 * Test the registration of the JMX sensor type ident.
		 */
		@Test
		public void registerJmxSensorTypeIdent() {
			final long jmxSensorId = 50;
			long platformId = 1;
			String fqcName = "class";

			when(jmxSensorTypeIdentDao.findIdByExample(eq(platformId), (JmxSensorTypeIdent) anyObject())).thenReturn(Collections.<Long> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					JmxSensorTypeIdent jmxSensorTypeIdent = (JmxSensorTypeIdent) invocation.getArguments()[0];
					jmxSensorTypeIdent.setId(Long.valueOf(jmxSensorId));
					return null;
				}
			}).when(jmxSensorTypeIdentDao).saveOrUpdate((JmxSensorTypeIdent) anyObject());

			long registeredId = registrationService.registerJmxSensorTypeIdent(platformId, fqcName);
			assertThat(registeredId, is(equalTo(jmxSensorId)));

			verify(platformIdentCache, times(1)).markDirty(platformId);
			ArgumentCaptor<JmxSensorTypeIdent> jmxSensorArgument = ArgumentCaptor.forClass(JmxSensorTypeIdent.class);
			verify(jmxSensorTypeIdentDao, times(1)).saveOrUpdate(jmxSensorArgument.capture());
			assertThat(jmxSensorArgument.getValue().getFullyQualifiedClassName(), is(equalTo(fqcName)));
			assertThat(jmxSensorArgument.getValue().getPlatformIdent().getId(), is(equalTo(platformId)));

			verify(platformIdentCache, times(1)).markDirty(platformId);

		}
	}

	public class RegisterJmxSensorDefinitionDataIdent extends RegistrationServiceTest {

		/**
		 * Tests the registration of a {@link JmxSensorTypeIdent}.
		 */
		@Test
		public void registerJmxSensorDefinitionDataIdent() {
			final long jmxSensorId = 50;
			long platformId = 1;
			String mBeanObjectName = "mBeanObjectName";
			String mBeanAttributeName = "mBeanAttributeName";
			String mBeanAttributeDescription = "mBeanAttributeDescription";
			String mBeanAttributeType = "mBeanAttributeType";
			boolean isIs = true;
			boolean isReadable = true;
			boolean isWritable = true;

			when(jmxDefinitionDataIdentDao.findIdForPlatformIdent(eq(platformId), (JmxDefinitionDataIdent) anyObject(), anyBoolean())).thenReturn(Collections.<Long> emptyList());
			Mockito.doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					JmxDefinitionDataIdent jmxSensorTypeIdent = (JmxDefinitionDataIdent) invocation.getArguments()[0];
					jmxSensorTypeIdent.setId(Long.valueOf(jmxSensorId));
					return null;
				}
			}).when(jmxDefinitionDataIdentDao).saveOrUpdate((JmxDefinitionDataIdent) anyObject());

			long registeredId = registrationService.registerJmxSensorDefinitionDataIdent(platformId, mBeanObjectName, mBeanAttributeName, mBeanAttributeDescription, mBeanAttributeType, isIs,
					isReadable, isWritable);
			assertThat(registeredId, is(equalTo(jmxSensorId)));

			ArgumentCaptor<JmxDefinitionDataIdent> jmxSensorArgument = ArgumentCaptor.forClass(JmxDefinitionDataIdent.class);
			verify(jmxDefinitionDataIdentDao, times(1)).saveOrUpdate(jmxSensorArgument.capture());
			verify(jmxDefinitionDataIdentDao, times(1)).findIdForPlatformIdent(eq(platformId), (JmxDefinitionDataIdent) anyObject(), eq(true));
			verify(platformIdentCache, times(1)).markDirty(platformId);

			JmxDefinitionDataIdent dataIdent = jmxSensorArgument.getValue();
			assertThat(dataIdent.getmBeanObjectName(), is(equalTo(mBeanObjectName)));
			assertThat(dataIdent.getmBeanAttributeName(), is(equalTo(mBeanAttributeName)));
			assertThat(dataIdent.getmBeanAttributeDescription(), is(equalTo(mBeanAttributeDescription)));
			assertThat(dataIdent.getmBeanAttributeType(), is(equalTo(mBeanAttributeType)));
			assertThat(dataIdent.getmBeanAttributeIsIs(), is(equalTo(isIs)));
			assertThat(dataIdent.getmBeanAttributeIsReadable(), is(equalTo(isReadable)));
			assertThat(dataIdent.getmBeanAttributeIsWritable(), is(equalTo(isWritable)));
		}
	}

	public class AddSensorTypeToMethod extends RegistrationServiceTest {

		/**
		 * Test the registering of the method sensor type to method occurring for the first time.
		 */
		@Test
		public void registerSensorTypeWithMethodFirstTime() {
			long platformId = 1;
			long methodId = 20;
			long methodSensorId = 50;
			when(methodIdentToSensorTypeDao.findId(eq(methodId), eq(methodSensorId), anyBoolean())).thenReturn(null);

			registrationService.addSensorTypeToMethod(platformId, methodSensorId, methodId);

			ArgumentCaptor<MethodIdentToSensorType> argument = ArgumentCaptor.forClass(MethodIdentToSensorType.class);
			verify(methodIdentToSensorTypeDao, times(1)).saveOrUpdate(argument.capture());
			verify(methodIdentToSensorTypeDao, times(1)).findId(methodId, methodSensorId, true);
			verify(platformIdentCache, times(1)).markDirty(platformId);
			verifyZeroInteractions(methodIdentDao);
			verifyZeroInteractions(methodSensorTypeIdentDao);

			assertThat(argument.getValue().getMethodIdent().getId(), is(equalTo(methodId)));
			assertThat(argument.getValue().getMethodSensorTypeIdent().getId(), is(equalTo(methodSensorId)));
		}

		/**
		 * Test the registering of the method sensor type to method occurring not for the first
		 * time.
		 */
		@Test
		public void registerSensorTypeWithMethodSecondTime() {
			long platformId = 1;
			long methodId = 20;
			long methodSensorId = 50;
			long methodIdentToSensorTypeId = 100L;
			when(methodIdentToSensorTypeDao.findId(eq(methodId), eq(methodSensorId), anyBoolean())).thenReturn(methodIdentToSensorTypeId);

			registrationService.addSensorTypeToMethod(platformId, methodSensorId, methodId);

			verify(methodIdentToSensorTypeDao, times(1)).findId(methodId, methodSensorId, true);
			verify(platformIdentCache, times(1)).markDirty(platformId);
			verifyZeroInteractions(methodIdentDao);
			verifyZeroInteractions(methodSensorTypeIdentDao);
		}
	}

}
