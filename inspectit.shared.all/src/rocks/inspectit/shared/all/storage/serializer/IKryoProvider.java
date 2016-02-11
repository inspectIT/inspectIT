package info.novatec.inspectit.storage.serializer;

import com.esotericsoftware.kryo.Kryo;

/**
 * Interface for classes that can provide Kryo instance.
 * 
 * @author Ivan Senic
 * 
 */
public interface IKryoProvider {

	/**
	 * Returns {@link Kryo} instance.
	 * 
	 * @return Returns {@link Kryo} instance.
	 */
	Kryo getKryo();
}
