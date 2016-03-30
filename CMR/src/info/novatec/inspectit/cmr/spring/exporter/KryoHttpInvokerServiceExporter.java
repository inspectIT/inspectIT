package info.novatec.inspectit.cmr.spring.exporter;

import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * This service exporter using kryo for (de-)serialization is nearly the same as the one with plain
 * java serialization.
 * 
 * @author Patrice Bouillet
 * 
 */
public class KryoHttpInvokerServiceExporter extends HttpInvokerServiceExporter {

	/**
	 * The serialization manager.
	 */
	@Autowired
	private SerializationManagerProvider serializationManagerProvider;
	
	/**
	 * The secure remote invocation executor.
	 */
	@Autowired
	private SessionAwareSecureRemoteInvocationExecutor sessionAwareSecureExecutor;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is) throws IOException, ClassNotFoundException {
		try (Input input = new Input(is)) {
			ISerializer serializer = serializationManagerProvider.createSerializer();
			return (RemoteInvocation) serializer.deserialize(input);
		} catch (SerializationException e) {
			throw new IOException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeRemoteInvocationResult(HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result, OutputStream os) throws IOException {
		try (Output output = new Output(os)) {
			if (!result.hasException()) {
				Object value = result.getValue();
				result = new RemoteInvocationResult(value);
			}

			ISerializer serializer = serializationManagerProvider.createSerializer();
						
			// if there is a session id write it first, or null if there is not
			Object sessionId = sessionAwareSecureExecutor.getSessionId();
			serializer.serialize(sessionId, output);
			
			serializer.serialize(result, output);
		} catch (SerializationException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Post constructor.
	 */
	@PostConstruct
	protected void init() {
		setRemoteInvocationExecutor(sessionAwareSecureExecutor);
	}
	
}
