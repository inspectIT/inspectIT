package info.novatec.inspectit.agent.connection.impl;

import info.novatec.inspectit.agent.connection.FailFastRemoteMethodCall;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.RegistrationException;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.agent.spring.PrototypesProvider;
import info.novatec.inspectit.cmr.service.IAgentService;
import info.novatec.inspectit.cmr.service.IAgentStorageService;
import info.novatec.inspectit.cmr.service.IKeepAliveService;
import info.novatec.inspectit.cmr.service.ServiceInterface;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationDefinition;
import info.novatec.inspectit.kryonet.Client;
import info.novatec.inspectit.kryonet.ExtendedSerializationImpl;
import info.novatec.inspectit.kryonet.IExtendedSerialization;
import info.novatec.inspectit.kryonet.rmi.ObjectSpace;
import info.novatec.inspectit.spring.logger.Log;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.MapUtils;
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
	 * Agent service.
	 */
	private IAgentService agentService;

	/**
	 * THe keep-alive service remote object to send keep-alive messages.
	 */
	private IKeepAliveService keepAliveService;

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

				int agentServiceServiceId = IAgentService.class.getAnnotation(ServiceInterface.class).serviceId();
				agentService = ObjectSpace.getRemoteObject(client, agentServiceServiceId, IAgentService.class);
				((RemoteObject) agentService).setNonBlocking(false);
				((RemoteObject) agentService).setTransmitReturnValue(true);

				int keepAliveServiceId = IKeepAliveService.class.getAnnotation(ServiceInterface.class).serviceId();
				keepAliveService = ObjectSpace.getRemoteObject(client, keepAliveServiceId, IKeepAliveService.class);
				((RemoteObject) keepAliveService).setNonBlocking(true);
				((RemoteObject) keepAliveService).setTransmitReturnValue(false);

				log.info("KryoNet: Connection established!");
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
		agentService = null; // NOPMD
		keepAliveService = null; // NOPMD
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendKeepAlive(final long platformId) throws ServerUnavailableException {
		if (!isConnected()) {
			throw new ServerUnavailableException();
		}

		FailFastRemoteMethodCall<IKeepAliveService, Void> call = new FailFastRemoteMethodCall<IKeepAliveService, Void>(keepAliveService) {
			@Override
			protected Void performRemoteCall(IKeepAliveService service) {
				service.sendKeepAlive(platformId);
				return null;
			}
		};

		try {
			call.makeCall();
		} catch (ExecutionException e) {
			// there should be no execution exception
			log.error("Exception thrown while trying to send keep-alive signal to the server.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AgentConfiguration register(final String agentName, final String version) throws ServerUnavailableException, RegistrationException, BusinessException {
		if (!isConnected()) {
			throw new ServerUnavailableException();
		}

		// ensure network interfaces
		try {
			if (null == networkInterfaces) {
				networkInterfaces = getNetworkInterfaces();
			}
		} catch (SocketException socketException) {
			log.error("Could not obtain network interfaces from this machine!");
			if (log.isTraceEnabled()) {
				log.trace("unregister(List,String)", socketException);
			}
			throw new RegistrationException("Could not un-register the platform", socketException);
		}

		// make call
		FailFastRemoteMethodCall<IAgentService, AgentConfiguration> call = new FailFastRemoteMethodCall<IAgentService, AgentConfiguration>(agentService) {
			@Override
			protected AgentConfiguration performRemoteCall(IAgentService service) throws Exception {
				return agentService.register(networkInterfaces, agentName, version);
			}
		};

		try {
			return call.makeCall();
		} catch (ExecutionException executionException) {
			if (log.isTraceEnabled()) {
				log.trace("register(String, String)", executionException);
			}
			// check for business exception
			if (executionException.getCause() instanceof BusinessException) {
				throw ((BusinessException) executionException.getCause()); // NOPMD
			}

			throw new RegistrationException("Could not register the platform", executionException.getCause()); // NOPMD
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregister(final long platformIdent) throws ServerUnavailableException, RegistrationException, BusinessException {
		if (!isConnected()) {
			throw new ServerUnavailableException();
		}


		// make call
		FailFastRemoteMethodCall<IAgentService, Void> call = new FailFastRemoteMethodCall<IAgentService, Void>(agentService) {
			@Override
			protected Void performRemoteCall(IAgentService service) throws Exception {
				service.unregister(platformIdent);
				return null;
			}
		};

		try {
			call.makeCall();
		} catch (ExecutionException executionException) {
			if (log.isTraceEnabled()) {
				log.trace("unregister(long)", executionException);
			}
			// check for business exception
			if (executionException.getCause() instanceof BusinessException) {
				throw ((BusinessException) executionException.getCause()); // NOPMD
			}

			throw new RegistrationException("Could not un-register the platform", executionException.getCause()); // NOPMD
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendDataObjects(List<? extends DefaultData> measurements) throws ServerUnavailableException {
		if (!isConnected()) {
			throw new ServerUnavailableException();
		}

		if (null != measurements && !measurements.isEmpty()) {
			try {
				AddDataObjects remote = new AddDataObjects(agentStorageService, measurements);
				remote.makeCall();
			} catch (ExecutionException executionException) {
				// there should be no execution exception
				log.error("Could not send data objects", executionException);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public InstrumentationDefinition analyze(final long platformIdent, final String hash, final Type type) throws ServerUnavailableException, BusinessException {
		if (!isConnected()) {
			throw new ServerUnavailableException();
		}

		// make call
		FailFastRemoteMethodCall<IAgentService, InstrumentationDefinition> call = new FailFastRemoteMethodCall<IAgentService, InstrumentationDefinition>(agentService) {
			@Override
			protected InstrumentationDefinition performRemoteCall(IAgentService service) throws Exception {
				return agentService.analyze(platformIdent, hash, type);
			}
		};

		try {
			return call.makeCall();
		} catch (ExecutionException executionException) {
			if (log.isTraceEnabled()) {
				log.trace("analyzeAndInstrument(long,String,Type)", executionException);
			}

			// check for business exception
			if (executionException.getCause() instanceof BusinessException) {
				throw ((BusinessException) executionException.getCause()); // NOPMD
			}

			// otherwise we log and return null as it's unexpected exception for us
			log.error("Could not get instrumentation result", executionException);
			return null;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void instrumentationApplied(Map<Long, long[]> methodToSensorMap) throws ServerUnavailableException {
		if (!isConnected()) {
			throw new ServerUnavailableException();
		}

		if (MapUtils.isNotEmpty(methodToSensorMap)) {
			try {
				InstrumentationAppliedCall call = new InstrumentationAppliedCall(agentService, methodToSensorMap);
				call.makeCall();
			} catch (ExecutionException executionException) {
				// there should be no execution exception
				log.error("Could not sent instrumented method ids", executionException);
			}
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
			NetworkInterface networkInterface = interfaces.nextElement();
			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				networkInterfaces.add(address.getHostAddress());
			}
		}

		return networkInterfaces;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConnected() {
		return null != client && client.isConnected();
	}

}
