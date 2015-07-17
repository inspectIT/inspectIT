package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.MethodIdentDao;
import info.novatec.inspectit.cmr.dao.MethodIdentToSensorTypeDao;
import info.novatec.inspectit.cmr.dao.MethodSensorTypeIdentDao;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.dao.PlatformSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.cmr.util.AgentStatusDataProvider;
import info.novatec.inspectit.spring.logger.Log;

import java.rmi.RemoteException;
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

/**
 * This class is used as a delegator to the real registration service. It is needed because Spring
 * weaves a proxy around the real registration service which cannot be used in an RMI context with
 * Java 1.4 (as no pre-generated stub is available).
 * 
 * @author Patrice Bouillet
 * 
 */
@Service
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
	@Transactional
	@MethodLog
	public synchronized long registerPlatformIdent(List<String> definedIPs, String agentName, String version) throws RemoteException, ServiceException {
		if (log.isInfoEnabled()) {
			log.info("Trying to register Agent '" + agentName + "'");
		}

		PlatformIdent platformIdent = new PlatformIdent();
		if (ipBasedAgentRegistration) {
			platformIdent.setDefinedIPs(definedIPs);
		}
		platformIdent.setAgentName(agentName);

		// need to reset the version number, otherwise it will be used for the query
		platformIdent.setVersion(null);

		// we will not set the version for the platformIdent object here as we use this object
		// for a QBE (Query by example) and this query should not be performed based on the
		// version information.

		List<PlatformIdent> platformIdentResults = platformIdentDao.findByExample(platformIdent);
		if (1 == platformIdentResults.size()) {
			platformIdent = platformIdentResults.get(0);
		} else if (platformIdentResults.size() > 1) {
			// this cannot occur anymore, if it occurs, then there is something totally wrong!
			log.error("More than one platform ident has been retrieved! Please send your Database to the NovaTec inspectIT support!");
			throw new ServiceException("Platform ident can not be registered because the inspectIT Database has more than one corresponding platform ident already.");
		}

		// always update the time stamp and ips, no matter if this is an old or new record.
		platformIdent.setTimeStamp(new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()));
		platformIdent.setDefinedIPs(definedIPs);

		// also always update the version information of the agent
		platformIdent.setVersion(version);

		platformIdentDao.saveOrUpdate(platformIdent);

		agentStatusDataProvider.registerConnected(platformIdent.getId());

		if (log.isInfoEnabled()) {
			log.info("Successfully registered Agent '" + agentName + "' with id " + platformIdent.getId() + ", version " + version + " and following network interfaces:");
			printOutDefinedIPs(definedIPs);
		}
		return platformIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ServiceException
	 */
	@Transactional
	@MethodLog
	public void unregisterPlatformIdent(List<String> definedIPs, String agentName) throws ServiceException {
		log.info("Trying to unregister the Agent with following network interfaces:");
		printOutDefinedIPs(definedIPs);

		PlatformIdent platformIdent = new PlatformIdent();
		platformIdent.setDefinedIPs(definedIPs);
		platformIdent.setAgentName(agentName);

		// need to reset the version number, otherwise it will be used for the query
		platformIdent.setVersion(null);

		List<PlatformIdent> platformIdentResults = platformIdentDao.findByExample(platformIdent);
		if (1 == platformIdentResults.size()) {
			platformIdent = platformIdentResults.get(0);
			agentStatusDataProvider.registerDisconnected(platformIdent.getId());
			log.info("The Agent '" + platformIdent.getAgentName() + "' has been successfully unregistered.");
		} else if (platformIdentResults.size() > 1) {
			// this cannot occur anymore, if it occurs, then there is something totally wrong!
			log.error("More than one platform ident has been retrieved! Please send your Database to the NovaTec inspectIT support!");
			throw new ServiceException("Platform ident can not be unregistered because the inspectIT Database has more than one corresponding platform idents.");
		} else {
			log.warn("No registered agent with given network interfaces exists. Unregistration is aborted.");
			throw new ServiceException("Platform can not be unregistered because there is no platform registered with given network interfaces.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public long registerMethodIdent(long platformId, String packageName, String className, String methodName, List<String> parameterTypes, String returnType, int modifiers) throws RemoteException {
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

		List<MethodIdent> methodIdents = methodIdentDao.findForPlatformIdent(platformId, methodIdent);
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
	@Transactional
	@MethodLog
	public long registerMethodSensorTypeIdent(long platformId, String fullyQualifiedClassName, Map<String, Object> parameters) throws RemoteException {
		MethodSensorTypeIdent methodSensorTypeIdent = new MethodSensorTypeIdent();
		methodSensorTypeIdent.setFullyQualifiedClassName(fullyQualifiedClassName);

		List<MethodSensorTypeIdent> methodSensorTypeIdents = methodSensorTypeIdentDao.findByExample(platformId, methodSensorTypeIdent);
		if (1 == methodSensorTypeIdents.size()) {
			methodSensorTypeIdent = methodSensorTypeIdents.get(0);
		} else {
			// only if the new sensor is register we need to update the platform ident
			PlatformIdent platformIdent = platformIdentDao.load(platformId);
			methodSensorTypeIdent.setPlatformIdent(platformIdent);

			Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
			sensorTypeIdents.add(methodSensorTypeIdent);

			platformIdentDao.saveOrUpdate(platformIdent);
		}

		methodSensorTypeIdent.setSettings(parameters);

		methodSensorTypeIdentDao.saveOrUpdate(methodSensorTypeIdent);
		return methodSensorTypeIdent.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional
	@MethodLog
	public void addSensorTypeToMethod(long methodSensorTypeId, long methodId) throws RemoteException {
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
	@Transactional
	@MethodLog
	public long registerPlatformSensorTypeIdent(long platformId, String fullyQualifiedClassName) throws RemoteException {
		PlatformSensorTypeIdent platformSensorTypeIdent = new PlatformSensorTypeIdent();
		platformSensorTypeIdent.setFullyQualifiedClassName(fullyQualifiedClassName);

		List<PlatformSensorTypeIdent> platformSensorTypeIdents = platformSensorTypeIdentDao.findByExample(platformId, platformSensorTypeIdent);
		PlatformIdent platformIdent;
		if (1 == platformSensorTypeIdents.size()) {
			platformSensorTypeIdent = platformSensorTypeIdents.get(0);
		} else {
			// only if it s not registered we need updating
			platformIdent = platformIdentDao.load(platformId);
			platformSensorTypeIdent.setPlatformIdent(platformIdent);

			Set<SensorTypeIdent> sensorTypeIdents = platformIdent.getSensorTypeIdents();
			sensorTypeIdents.add(platformSensorTypeIdent);

			platformSensorTypeIdentDao.saveOrUpdate(platformSensorTypeIdent);
			platformIdentDao.saveOrUpdate(platformIdent);
		}

		return platformSensorTypeIdent.getId();
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
