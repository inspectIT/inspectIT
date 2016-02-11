package info.novatec.inspectit.storage.serializer;

/**
 * Interface for all classes that can create new {@link ISerializer} instances.
 * 
 * @param <T>
 *            Type that can be provided.
 * @author Ivan Senic
 * 
 */
public interface ISerializerProvider<T extends ISerializer & IKryoProvider> {

	/**
	 * Returns the new instance of the {@link ISerializer}.
	 * 
	 * @return Returns the new instance of the {@link ISerializer}.
	 */
	T createSerializer();
}
