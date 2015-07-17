package info.novatec.inspectit.storage.serializer;

import java.util.Map;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Generic interface for serializer to be used in inspectIT. The interface defines only two methods,
 * one for serialization and one from the serialization.
 * 
 * @author Ivan Senic
 * 
 */
public interface ISerializer {

	/**
	 * Serialize the object into bytes and puts the bytes in the supplied {@link Output}. All
	 * operations for preparing the output have to be performed before calling this method. The
	 * {@link Output#flush()} will be called after the serialization.
	 * 
	 * @param object
	 *            Object to serialize.
	 * @param output
	 *            {@link Output} to hold the serialized bytes.
	 * @throws SerializationException
	 *             Serialization exception is thrown when serialization could not be performed.
	 */
	void serialize(Object object, Output output) throws SerializationException;

	/**
	 * Serialize the object into bytes and puts the bytes in the supplied {@link Output}. All
	 * operations for preparing the output have to be performed before calling this method. The
	 * {@link Output#flush()} will be called after the serialization.
	 * <p>
	 * This method allows the caller to pass the map which will be added to the kryo graph context,
	 * so that any preference can be passed to the serializer expecting them.
	 * 
	 * @param object
	 *            Object to serialize.
	 * @param output
	 *            {@link Output} to hold the serialized bytes.
	 * @param kryoPreferences
	 *            Map of preferences to be put into the context before serialization.
	 * @throws SerializationException
	 *             Serialization exception is thrown when serialization could not be performed.
	 */
	void serialize(Object object, Output output, Map<?, ?> kryoPreferences) throws SerializationException;

	/**
	 * De-serialize the bytes provided by the {@link Input}. It is responsibility of the caller to
	 * set up the input correctly. The way bytes are read, is defined in the implementing classes.
	 * 
	 * @param input
	 *            {@link Input} that provides the bytes.
	 * @return Returns the de-serialized object.
	 * @throws SerializationException
	 *             If de-serialization fails.
	 */
	Object deserialize(Input input) throws SerializationException;

}
