package rocks.inspectit.server.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rocks.inspectit.server.dao.JmxDefinitionDataIdentDao;
import rocks.inspectit.server.dao.JmxSensorTypeIdentDao;
import rocks.inspectit.server.dao.MethodIdentDao;
import rocks.inspectit.server.dao.MethodIdentToSensorTypeDao;
import rocks.inspectit.server.dao.MethodSensorTypeIdentDao;
import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.dao.PlatformSensorTypeIdentDao;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.JmxSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdentToSensorType;
import rocks.inspectit.shared.all.cmr.model.MethodSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.AgentManagementErrorCodeEnum;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * This class is used as a delegator to the real registration service. It is needed because Spring
 * weaves a proxy around the real registration service which cannot be used in an RMI context with
 * Java 1.4 (as no pre-generated stub is available).
 *
 * @author Patrice Bouillet
 *
 */
@Service
@Transactional
public class RegistrationService implements IRegistrationService {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Should IP addresses be used for agent distinction.
	 */
	@Value(value = "${cmr.ipBasedAgentRegistration}")
	boolean ipBasedAgentRegistration = true;

	/**
	 * The platform ident DAO.
	 */
	@Autowired
	PlatformIdentDao platformIdentDao;

	/**
	 * The jmx definition data ident DAO.
	 */
	@Autowired
	JmxDefinitionDataIdentDao jmxDefinitionDataIdentDao;

	/**
	 * The method ident DAO.
	 */
	@Autowired
	MethodIdentDao methodIdentDao;

	/**
	 * The method sensor type ident DAO.
	 */
	@Autowired
	MethodSensorTypeIdentDao methodSensorTypeIdentDao;

	/**
	 * The platform sensor type ident DAO.
	 */
	@Autowired
	PlatformSensorTypeIdentDao platformSensorTypeIdentDao;

	/**
	 * The jmx sensor type ident DAO.
	 */
	@Autowired
	JmxSensorTypeIdentDao jmxSensorTypeIdentDao;

	/**
	 * The method ident to sensor type DAO.
	 */
	@Autowired
	MethodIdentToSensorTypeDao methodIdentToSensorTypeDao;

	/**
	 * {@link AgentStatusDataProvider}.
	 */
	@Autowired
	AgentStatusDataProvider agentStatusDataProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public synchronized long registerPlatformIdent(List<String> definedIPs, String agentName, String version) throws BusinessException {
		if (log.isInfoEnabled()) {
			log.info("Trying to register Agent '" + agentName + "'");
		}

		// find existing registered
		List<PlatformIdent> platformIdentResults;
		if (ipBasedAgentRegistration) {
			platformIdentResults = platformIdentDao.findByNameAndIps(agentName, definedIPs);
		} else {
			platformIdentResults = platformIdentDao.findByName(agentName);
		}

		PlatformIdent platformIdent = new PlatformIdent();
		platformIdent.setAgentName(agentName);
		if (1 == platformIdentResults.size()) {
			platformIdent = platformIdentResults.get(0);
		} else if (platformIdentResults.size() > 1) {
			// this cannot occur anymore, if it occurs, then there is something totally wrong!
			log.error("More than one platform ident has been retrieved! Please send your Database to the NovaTec inspectIT support!");
			throw new BusinessException("Register the agent with name " + agentName + " and following network interfaces " + definedIPs + ".",
					AgentManagementErrorCodeEnum.MORE_THAN_ONE_AGENT_REGISTERED);
		}

		// always update the time stamp and ips, no matter if this is an old or new record.
		platformIdent.setTimeStamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));
		platformIdent.setDefinedIPs(definedIPs);

		// also always update the version information of the agent
		platformIdent.setVersion(version);

		platformIdentDao.saveOrUpdate(platformIdent);

		agentStatusDataProvider.registerConnected(platformIdent.getId());

		if (log.isInfoEnabled()) {
			log.info("Successfully registered the Agent '" + agentName + "' with id " + platformIdent.getId() + ", version " + version + " and following network interfaces:");
			printOutDefinedIPs(definedIPs);
		}
		return platformIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws BusinessException
	 */
	@Override
	@MethodLog
	public void unregisterPlatformIdent(long platformId) throws BusinessException {
		log.info("Trying to unregister the Agent with the ID " + platformId);

		PlatformIdent platformIdent = platformIdentDao.load(platformId);

		if (null != platformIdent) {
			agentStatusDataProvider.registerDisconnected(platformIdent.getId());
			log.info("The Agent '" + platformIdent.getAgentName() + "' has been successfully unregistered.");
		} else {
			log.warn("No registered agent with given ID exists. Unregistration is aborted.");
			throw new BusinessException("Unregister the agent with ID " + platformId + ".", AgentManagementErrorCodeEnum.AGENT_DOES_NOT_EXIST);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public long registerMethodIdent(long platformId, String packageName, String className, String methodName, List<String> parameterTypes, String returnType, int modifiers) {
		MethodIdent methodIdent = new MethodIdent();
		methodIdent.setPackageName(packageName);
		methodIdent.setClassName(className);
		methodIdent.setMethodName(methodName);
		if (null == parameterTypes) {
			parameterTypes = Collections.emptyList();
		}
		methodIdent.setParameters(parameterTypes);
		methodIdent.setReturnType(returnType);
		methodIdent.setModifiers(modifiers);

		List<MethodIdent> methodIdents = methodIdentDao.findForPlatformIdAndExample(platformId, methodIdent);
		if (1 == methodIdents.size()) {
			methodIdent = methodIdents.get(0);
		} else {
			PlatformIdent platformIdent = platformIdentDao.load(platformId);
			methodIdent.setPlatformIdent(platformIdent);
		}

		// always update the time stamp, no matter if this is an old or new
		// record.
		methodIdent.setTimeStamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));

		methodIdentDao.saveOrUpdate(methodIdent);

		return methodIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public long registerMethodSensorTypeIdent(long platformId, String fullyQualifiedClassName, Map<String, Object> parameters) {
		MethodSensorTypeIdent methodSensorTypeIdent;

		List<MethodSensorTypeIdent> methodSensorTypeIdents = methodSensorTypeIdentDao.findByClassNameAndPlatformId(fullyQualifiedClassName, platformId);
		if (1 == methodSensorTypeIdents.size()) {
			methodSensorTypeIdent = methodSensorTypeIdents.get(0);

			// update preferences
			methodSensorTypeIdent.setSettings(parameters);
			methodSensorTypeIdentDao.saveOrUpdate(methodSensorTypeIdent);
		} else {
			// only if the new sensor is registered we need to update the platform ident
			PlatformIdent platformIdent = platformIdentDao.load(platformId);
			methodSensorTypeIdent = new MethodSensorTypeIdent();
			methodSensorTypeIdent.setPlatformIdent(platformIdent);
			methodSensorTypeIdent.setFullyQualifiedClassName(fullyQualifiedClassName);
			methodSensorTypeIdent.setSettings(parameters);

			Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
			sensorTypeIdents.add(methodSensorTypeIdent);

			methodSensorTypeIdentDao.saveOrUpdate(methodSensorTypeIdent);
			platformIdentDao.saveOrUpdate(platformIdent);
		}

		return methodSensorTypeIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public void addSensorTypeToMethod(long methodSensorTypeId, long methodId) {
		MethodIdentToSensorType methodIdentToSensorType = methodIdentToSensorTypeDao.find(methodId, methodSensorTypeId);
		if (null == methodIdentToSensorType) {
			MethodIdent methodIdent = methodIdentDao.load(methodId);
			MethodSensorTypeIdent methodSensorTypeIdent = methodSensorTypeIdentDao.load(methodSensorTypeId);
			methodIdentToSensorType = new MethodIdentToSensorType(methodIdent, methodSensorTypeIdent, null);
		}

		// always update the timestamp
		methodIdentToSensorType.setTimestamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));

		methodIdentToSensorTypeDao.saveOrUpdate(methodIdentToSensorType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public long registerPlatformSensorTypeIdent(long platformId, String fullyQualifiedClassName) {
		PlatformSensorTypeIdent platformSensorTypeIdent;
		List<PlatformSensorTypeIdent> platformSensorTypeIdents = platformSensorTypeIdentDao.findByClassNameAndPlatformId(fullyQualifiedClassName, platformId);
		if (1 == platformSensorTypeIdents.size()) {
			platformSensorTypeIdent = platformSensorTypeIdents.get(0);
		} else {
			// only if it s not registered we need updating
			PlatformIdent platformIdent = platformIdentDao.load(platformId);
			platformSensorTypeIdent = new PlatformSensorTypeIdent();
			platformSensorTypeIdent.setPlatformIdent(platformIdent);
			platformSensorTypeIdent.setFullyQualifiedClassName(fullyQualifiedClassName);

			Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
			sensorTypeIdents.add(platformSensorTypeIdent);

			platformSensorTypeIdentDao.saveOrUpdate(platformSensorTypeIdent);
			platformIdentDao.saveOrUpdate(platformIdent);
		}

		return platformSensorTypeIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	@MethodLog
	public long registerJmxSensorTypeIdent(long platformId, String fullyQualifiedClassName) {
		JmxSensorTypeIdent jmxSensorTypeIdent = new JmxSensorTypeIdent();
		jmxSensorTypeIdent.setFullyQualifiedClassName(fullyQualifiedClassName);

		List<JmxSensorTypeIdent> jmxSensorTypeIdents = jmxSensorTypeIdentDao.findByExample(platformId, jmxSensorTypeIdent);
		if (1 == jmxSensorTypeIdents.size()) {
			jmxSensorTypeIdent = jmxSensorTypeIdents.get(0);
		} else {
			PlatformIdent platformIdent = platformIdentDao.load(platformId);
			jmxSensorTypeIdent.setPlatformIdent(platformIdent);

			Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
			sensorTypeIdents.add(jmxSensorTypeIdent);

			jmxSensorTypeIdentDao.saveOrUpdate(jmxSensorTypeIdent);
			platformIdentDao.saveOrUpdate(platformIdent);
		}

		return jmxSensorTypeIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	@MethodLog
	public long registerJmxSensorDefinitionDataIdent(long platformId, String mBeanObjectName, String mBeanAttributeName, String mBeanAttributeDescription, String mBeanAttributeType, boolean isIs, // NOCHK
			boolean isReadable, boolean isWritable) {
		JmxDefinitionDataIdent jmxDefinitionDataIdent = new JmxDefinitionDataIdent();
		jmxDefinitionDataIdent.setmBeanObjectName(mBeanObjectName);
		jmxDefinitionDataIdent.setmBeanAttributeName(mBeanAttributeName);
		jmxDefinitionDataIdent.setmBeanAttributeDescription(mBeanAttributeDescription);
		jmxDefinitionDataIdent.setmBeanAttributeType(mBeanAttributeType);
		jmxDefinitionDataIdent.setmBeanAttributeIsIs(isIs);
		jmxDefinitionDataIdent.setmBeanAttributeIsReadable(isReadable);
		jmxDefinitionDataIdent.setmBeanAttributeIsWritable(isWritable);

		List<JmxDefinitionDataIdent> jmxDefinitionDataIdents = jmxDefinitionDataIdentDao.findForPlatformIdent(platformId, jmxDefinitionDataIdent);
		if (1 == jmxDefinitionDataIdents.size()) {
			jmxDefinitionDataIdent = jmxDefinitionDataIdents.get(0);
		} else {
			PlatformIdent platformIdent = platformIdentDao.load(Long.valueOf(platformId));
			jmxDefinitionDataIdent.setPlatformIdent(platformIdent);
		}

		jmxDefinitionDataIdent.setTimeStamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));

		jmxDefinitionDataIdentDao.saveOrUpdate(jmxDefinitionDataIdent);
		return jmxDefinitionDataIdent.getId();
	}

	/**
	 * Prints out the given list of defined IP addresses. The example is:
	 * <p>
	 * |- IPv4: 192.168.1.6<br>
	 * |- IPv4: 127.0.0.1<br>
	 * |- IPv6: fe80:0:0:0:221:5cff:fe1d:ffdf%3<br>
	 * |- IPv6: 0:0:0:0:0:0:0:1%1
	 *
	 * @param definedIPs
	 *            List of IPv4 and IPv6 IPs.
	 */
	private void printOutDefinedIPs(List<String> definedIPs) {
		List<String> ipList = new ArrayList<String>();
		for (String ip : definedIPs) {
			if (ip.indexOf(':') != -1) {
				ipList.add("|- IPv6: " + ip);
			} else {
				ipList.add("|- IPv4: " + ip);
			}
		}
		Collections.sort(ipList);
		for (String ip : ipList) {
			log.info(ip);
		}

	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 */
	@PostConstruct
	public void postConstruct() {
		if (log.isInfoEnabled()) {
			log.info("|-Registration Service active...");
		}
	}
}
