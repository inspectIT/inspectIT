package info.novatec.inspectit.rcp.repository.service.cmr;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * This class extends the simple http one by using Kryo for (de-)serializing.
 * 
 * @author Patrice Bouillet
 * 
 */
public class KryoSimpleHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor {

	/**
	 * The serialization manager provider.
	 */
	private SerializationManagerProvider serializationManagerProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException {
		try {
			ISerializer serializer = serializationManagerProvider.createSerializer();
			serializer.serialize(invocation, new Output(os));
		} catch (SerializationException e) {
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RemoteInvocationResult readRemoteInvocationResult(InputStream is, String codebaseUrl) throws IOException, ClassNotFoundException {
		try {
			ISerializer serializer = serializationManagerProvider.createSerializer();
			return (RemoteInvocationResult) serializer.deserialize(new Input(is));
		} catch (SerializationException e) {
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
			throw new IOException(e);
		}
	}

	/**
	 * Gets {@link #serializationManagerProvider}.
	 * 
	 * @return {@link #serializationManagerProvider}
	 */
	public SerializationManagerProvider getSerializationManagerProvider() {
		return serializationManagerProvider;
	}

	/**
	 * Sets {@link #serializationManagerProvider}.
	 * 
	 * @param serializationManagerProvider
	 *            New value for {@link #serializationManagerProvider}
	 */
	public void setSerializationManagerProvider(SerializationManagerProvider serializationManagerProvider) {
		this.serializationManagerProvider = serializationManagerProvider;
	}

}
