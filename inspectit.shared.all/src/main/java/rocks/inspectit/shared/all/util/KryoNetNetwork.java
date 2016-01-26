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
	 */
	public void register(Kryo kryo) {
		// services to export must be registered due to the bug in KryoNet
		kryo.register(IAgentStorageService.class);
		kryo.register(IAgentService.class);
		kryo.register(IKeepAliveService.class);

		// below classes must match the registration performed in the KryoSerialization class
		// constructor, we pre-register them so that IDs keep same in all components
		kryo.register(RegisterTCP.class);
		kryo.register(RegisterUDP.class);
		kryo.register(KeepAlive.class);
		kryo.register(DiscoverHost.class);
		kryo.register(Ping.class);

		// this also must be called in this position cause we don't want to call it after the
		// post processor from the CommonsCS registers new classes
		ObjectSpace.registerClasses(kryo);
	}

}
