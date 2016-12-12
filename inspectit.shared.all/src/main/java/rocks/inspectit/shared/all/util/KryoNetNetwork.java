package rocks.inspectit.shared.all.util;

import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.FrameworkMessage.DiscoverHost;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.FrameworkMessage.Ping;
import com.esotericsoftware.kryonet.FrameworkMessage.RegisterTCP;
import com.esotericsoftware.kryonet.FrameworkMessage.RegisterUDP;

import rocks.inspectit.shared.all.cmr.service.IAgentService;
import rocks.inspectit.shared.all.cmr.service.IAgentStorageService;
import rocks.inspectit.shared.all.cmr.service.IKeepAliveService;
import rocks.inspectit.shared.all.kryonet.rmi.ObjectSpace;

/**
 * Utility class for Network registrations when using kryonet.
 *
 * @author Ivan Senic
 *
 */
@Component
public final class KryoNetNetwork {

	/**
	 * Registers needed classes for network communication.
	 *
	 * @param kryo
	 *            Kryo instance.
	 * @param nextRegistrationId
	 *            Registration id to start registering at.
	 *
	 * @return updated value of the next registration id
	 */
	public int register(Kryo kryo, int nextRegistrationId) {
		// services to export must be registered due to the bug in KryoNet
		kryo.register(IAgentStorageService.class, nextRegistrationId++);
		kryo.register(IAgentService.class, nextRegistrationId++);
		kryo.register(IKeepAliveService.class, nextRegistrationId++);

		// below classes must match the registration performed in the KryoSerialization class
		// constructor, we pre-register them so that IDs keep same in all components
		kryo.register(RegisterTCP.class, nextRegistrationId++);
		kryo.register(RegisterUDP.class, nextRegistrationId++);
		kryo.register(KeepAlive.class, nextRegistrationId++);
		kryo.register(DiscoverHost.class, nextRegistrationId++);
		kryo.register(Ping.class, nextRegistrationId++);

		// this also must be called in this position cause we don't want to call it after the
		// post processor from the CommonsCS registers new classes
		return ObjectSpace.registerClasses(kryo, nextRegistrationId);
	}

}
