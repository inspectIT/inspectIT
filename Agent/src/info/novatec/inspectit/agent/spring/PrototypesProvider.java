package info.novatec.inspectit.agent.spring;

import info.novatec.inspectit.storage.nio.stream.ExtendedByteBufferOutputStream;
import info.novatec.inspectit.storage.nio.stream.SocketExtendedByteBufferInputStream;
import info.novatec.inspectit.storage.nio.stream.StreamProvider;
import info.novatec.inspectit.storage.serializer.ISerializerProvider;
import info.novatec.inspectit.storage.serializer.impl.SerializationManager;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provider for all needed prototypes since we don't have spring config files anymore.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class PrototypesProvider extends StreamProvider implements ISerializerProvider<SerializationManager> {

	/**
	 * Factory for {@link SerializationManager}.
	 */
	@Autowired
	private ObjectFactory<SerializationManager> serializationManagerFactory;

	/**
	 * Factory for {@link ExtendedByteBufferOutputStream}.
	 */
	@Autowired
	private ObjectFactory<ExtendedByteBufferOutputStream> extendedByteBufferOutputStreamFactory;

	/**
	 * Factory for {@link SocketExtendedByteBufferInputStream}.
	 */
	@Autowired
	private ObjectFactory<SocketExtendedByteBufferInputStream> socketExtendedByteBufferInputStreamFactory;

	/**
	 * Returns the new {@link SerializationManager} enhanced by Spring.
	 * 
	 * @return Returns the new {@link SerializationManager} enhanced by Spring.
	 */
	public SerializationManager createSerializer() {
		return serializationManagerFactory.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ExtendedByteBufferOutputStream createExtendedByteBufferOutputStream() {
		return extendedByteBufferOutputStreamFactory.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SocketExtendedByteBufferInputStream createSocketExtendedByteBufferInputStream() {
		return socketExtendedByteBufferInputStreamFactory.getObject();
	}
}
