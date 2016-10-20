package rocks.inspectit.shared.all.serializer.provider;

import rocks.inspectit.shared.all.serializer.ISerializer;
import rocks.inspectit.shared.all.serializer.ISerializerProvider;
import rocks.inspectit.shared.all.serializer.impl.SerializationManager;

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
	@Override
	public abstract SerializationManager createSerializer();
}
