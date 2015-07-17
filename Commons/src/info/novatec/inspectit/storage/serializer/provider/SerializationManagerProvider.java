package info.novatec.inspectit.storage.serializer.provider;

import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.ISerializerProvider;
import info.novatec.inspectit.storage.serializer.impl.SerializationManager;

/**
 * This is a typical provider of the new instances enhanced by Spring. Returns the
 * {@link ISerializer} instance.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class SerializationManagerProvider implements ISerializerProvider<SerializationManager> {

	/**
	 * Returns the new {@link SerializationManager} enhanced by Spring.
	 * 
	 * @return Returns the new {@link SerializationManager} enhanced by Spring.
	 */
	public abstract SerializationManager createSerializer();
}
