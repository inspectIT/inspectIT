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
		try (Output output = new Output(os)) {
			ISerializer serializer = serializationManagerProvider.createSerializer();
			serializer.serialize(invocation, output);
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
		try (Input input = new Input(is)) {
			ISerializer serializer = serializationManagerProvider.createSerializer();

			Object sessionId = serializer.deserialize(input);
			if (null != sessionId) {
				System.setProperty("shiro.session.id", sessionId.toString());
			} else {
				System.clearProperty("shiro.session.id");
			}

			return (RemoteInvocationResult) serializer.deserialize(input);
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
