package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.cmr.service.IServerStatusService.ServerStatus;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.EnumSerializer;

/**
 * Special type of {@link EnumSerializer} for the {@link ServerStatus} enumeration so that key of
 * server status can also be transmitted.
 * 
 * @author Ivan Senic
 * 
 */
public class ServerStatusSerializer extends EnumSerializer {

	/**
	 * Default constructor.
	 */
	public ServerStatusSerializer() {
		super(ServerStatus.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void write(Kryo kryo, Output output, Enum serverStatus) {
		super.write(kryo, output, serverStatus);
		if (serverStatus instanceof ServerStatus) {
			output.writeString(((ServerStatus) serverStatus).getRegistrationIdsValidationKey());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Enum read(Kryo kryo, Input input, Class<Enum> clazz) {
		Enum serverStatus = super.read(kryo, input, clazz);
		if (serverStatus instanceof ServerStatus) {
			String key = input.readString();
			((ServerStatus) serverStatus).setRegistrationIdsValidationKey(key);
		}
		return serverStatus;
	}
}
