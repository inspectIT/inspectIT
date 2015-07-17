package info.novatec.inspectit.kryonet;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Extended {@link Serialization} to support additional methods for object (de-)serialization.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("all")
public interface IExtendedSerialization extends Serialization {

	/**
	 * Writes object to the given {@link OutputStream}.
	 * 
	 * @param connection
	 *            Connection to be added to kryo context.
	 * @param outputStream
	 *            Stream to serialize object to.
	 * @param object
	 *            Object
	 */
	void write(Connection connection, OutputStream outputStream, Object object);

	/**
	 * Reads object from the given {@link InputStream}.
	 * 
	 * @param connection
	 *            Connection to be added to kryo context.
	 * @param inputStream
	 *            Stream to de-serialize object from.
	 * @return De-serialized object.
	 */
	Object read(Connection connection, InputStream inputStream);
}
