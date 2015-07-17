package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.connection.AbstractRemoteMethodCall;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.RegistrationException;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.agent.spring.PrototypesProvider;
import info.novatec.inspectit.cmr.service.IAgentStorageService;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.cmr.service.ServiceInterface;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.kryonet.Client;
import info.novatec.inspectit.kryonet.ExtendedSerializationImpl;
import info.novatec.inspectit.kryonet.IExtendedSerialization;
import info.novatec.inspectit.kryonet.rmi.ObjectSpace;
import info.novatec.inspectit.spring.logger.Log;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryonet.rmi.RemoteObject;

/**
 * Implements the {@link IConnection} interface using the kryo-net.
 * 
 * @author Patrice Bouillet
 * 
 */
@Component
public class KryoNetConnection implements IConnection {

	/**
	 * The logger of the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link PrototypesProvider}.
	 */
	@Autowired
	private PrototypesProvider prototypesProvider;

	/**
	 * The kryonet client to connect to the CMR.
	 */
	private Client client;

	/**
	 * The agent storage remote object which will be used to send the measurements to.
	 */
	private IAgentStorageService agentStorageService;

	/**
	 * The registration remote object which will be used for the registration of the sensors.
	 */
	private IRegistrationService registrationService;

	/**
	 * Attribute to check if we are connected.
	 */
	private boolean connected = false;

	/**
	 * Defines if there was a connection exception before. Used for throttling the info log
	 * messages.
	 */
	private boolean connectionException = false;

	/**
	 * The list of all network interfaces.
	 */
	private List<String> networkInterfaces;

	/**
	 * {@inheritDoc}
	 */
	public void connect(String host, int port) throws ConnectException {
		if (null == client) {
			try {
				if (!connectionException) {
					log.info("KryoNet: Connecting to " + host + ":" + port);
				}
				initClient(host, port);

				int agentStorageServiceId = IAgentStorageService.class.getAnnotation(ServiceInterface.class).serviceId();
				agentStorageService = ObjectSpace.getRemoteObject(client, agentStorageServiceId, IAgentStorageService.class);
				((RemoteObject) agentStorageService).setNonBlocking(true);
				((RemoteObject) agentStorageService).setTransmitReturnValue(false);

				int registrationServiceServiceId = IRegistrationService.class.getAnnotation(ServiceInterface.class).serviceId();
				registrationService = ObjectSpace.getRemoteObject(client, registrationServiceServiceId, IRegistrationService.class);
				((RemoteObject) registrationService).setNonBlocking(false);
				((RemoteObject) registrationService).setTransmitReturnValue(true);

				log.info("KryoNet: Connection established!");
				connected = true;
				connectionException = false;
			} catch (Exception exception) {
				if (!connectionException) {
					log.info("KryoNet: Connection to the server failed.");
				}
				connectionException = true;
				disconnect();
				if (log.isTraceEnabled()) {
					log.trace("connect()", exception);
				}
				ConnectException e = new ConnectException(exception.getMessage());
				e.initCause(exception);
				throw e; // NOPMD root cause exception is set
			}
		}
	}

	/**
	 * Creates new client and tries to connect to host.
	 * 
	 * @param host
	 *            Host IP address.
	 * @param port
	 *            Port to connect to.
	 * @throws Exception
	 *             If {@link Exception} occurs during communication.
	 */
	private void initClient(String host, int port) throws Exception {
		IExtendedSerialization serialization = new ExtendedSerializationImpl(prototypesProvider);

		client = new Client(serialization, prototypesProvider);
		client.start();
		client.connect(5000, host, port);
	}

	/**
	 * {@inheritDoc}
	 */
	public void disconnect() {
		if (null != client) {
			client.stop();
			client = null; // NOPMD
		}
		agentStorageService = null; // NOPMD
		registrationService = null; // NOPMD
		connected = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatform(String agentName, String version) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		try {
			if (null == networkInterfaces) {
				networkInterfaces = getNetworkInterfaces();
			}
			return registrationService.registerPlatformIdent(networkInterfaces, agentName, version);
		} catch (RemoteException remoteException) {
			if (log.isTraceEnabled()) {
				log.trace("registerPlatform(String)", remoteException);
			}
			throw new RegistrationException("Could not register the platform", remoteException);
		} catch (SocketException socketException) {
			log.error("Could not obtain network interfaces from this machine!");
			if (log.isTraceEnabled()) {
				log.trace("Constructor", socketException);
			}
			throw new RegistrationException("Could not register the platform", socketException);
		} catch (ServiceException serviceException) {
			if (log.isTraceEnabled()) {
				log.trace("registerPlatform(String)", serviceException);
			}
			throw new RegistrationException("Could not register the platform", serviceException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterPlatform(String agentName) throws RegistrationException {
		if (!connected) {
			return;
		}

		try {
			if (null == networkInterfaces) {
				networkInterfaces = getNetworkInterfaces();
			}

			registrationService.unregisterPlatformIdent(networkInterfaces, agentName);
		} catch (SocketException socketException) {
			log.error("Could not obtain network interfaces from this machine!");
			if (log.isTraceEnabled()) {
				log.trace("unregisterPlatform(List,String)", socketException);
			}
			throw new RegistrationException("Could not un-register the platform", socketException);
		} catch (ServiceException serviceException) {
			if (log.isTraceEnabled()) {
				log.trace("unregisterPlatform(List,String)", serviceException);
			}
			throw new RegistrationException("Could not un-register the platform", serviceException);
		} catch (RemoteException remoteException) {
			if (log.isTraceEnabled()) {
				log.trace("unregisterPlatform(List,String)", remoteException);
			}
			throw new RegistrationException("Could not un-register the platform", remoteException);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void sendDataObjects(List<? extends DefaultData> measurements) throws ServerUnavailableException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		if (null != measurements && !measurements.isEmpty()) {
			try {
				AbstractRemoteMethodCall remote = new AddDataObjects(agentStorageService, measurements);
				remote.makeCall();
			} catch (ServerUnavailableException serverUnavailableException) {
				if (log.isTraceEnabled()) {
					log.trace("sendDataObjects(List)", serverUnavailableException);
				}
				throw serverUnavailableException;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethod(long platformId, RegisteredSensorConfig sensorConfig) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		RegisterMethodIdent register = new RegisterMethodIdent(registrationService, sensorConfig, platformId);
		try {
			Long id = (Long) register.makeCall();
			return id.longValue();
		} catch (ServerUnavailableException serverUnavailableException) {
			if (log.isTraceEnabled()) {
				log.trace("registerMethod(RegisteredSensorConfig)", serverUnavailableException);
			}
			throw new RegistrationException("Could not register the method", serverUnavailableException);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public long registerMethodSensorType(long platformId, MethodSensorTypeConfig methodSensorTypeConfig) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		RegisterMethodSensorType register = new RegisterMethodSensorType(registrationService, methodSensorTypeConfig, platformId);
		try {
			Long id = (Long) register.makeCall();
			return id.longValue();
		} catch (ServerUnavailableException serverUnavailableException) {
			if (log.isTraceEnabled()) {
				log.trace("registerMethod(RegisteredSensorConfig)", serverUnavailableException);
			}
			throw new RegistrationException("Could not register the method sensor type", serverUnavailableException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long registerPlatformSensorType(long platformId, PlatformSensorTypeConfig platformSensorTypeConfig) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		RegisterPlatformSensorType register = new RegisterPlatformSensorType(registrationService, platformSensorTypeConfig, platformId);
		try {
			Long id = (Long) register.makeCall();
			return id.longValue();
		} catch (ServerUnavailableException serverUnavailableException) {
			if (log.isTraceEnabled()) {
				log.trace("registerPlatformSensorType(PlatformSensorTypeConfig)", serverUnavailableException);
			}
			throw new RegistrationException("Could not register the platform sensor type", serverUnavailableException);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSensorTypeToMethod(long sensorTypeId, long methodId) throws ServerUnavailableException, RegistrationException {
		if (!connected) {
			throw new ServerUnavailableException();
		}

		AddSensorTypeToMethod addTypeToSensor = new AddSensorTypeToMethod(registrationService, sensorTypeId, methodId);
		try {
			addTypeToSensor.makeCall();
		} catch (ServerUnavailableException serverUnavailableException) {
			if (log.isTraceEnabled()) {
				log.trace("addSensorTypeToMethod(long, long)", serverUnavailableException);
			}
			throw new RegistrationException("Could not add the sensor type to a method", serverUnavailableException);
		}
	}

	/**
	 * Loads all the network interfaces and transforms the enumeration to the list of strings
	 * containing all addresses.
	 * 
	 * @return List of all network interfaces.
	 * @throws SocketException
	 *             If {@link SocketException} occurs.
	 */
	private List<String> getNetworkInterfaces() throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		List<String> networkInterfaces = new ArrayList<String>();

		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = (InetAddress) addresses.nextElement();
				networkInterfaces.add(address.getHostAddress());
			}
		}

		return networkInterfaces;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConnected() {
		return connected;
	}
}
