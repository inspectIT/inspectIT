package info.novatec.inspectit.storage.serializer.impl;

import java.sql.Timestamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializes instances of {@link java.sql.Timestamp}.
 * 
 * @author Ivan Senic
 */
public class TimestampSerializer extends Serializer<Timestamp> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(Kryo kryo, Output output, Timestamp object) {
		output.writeLong(object.getTime(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp read(Kryo kryo, Input input, Class<Timestamp> type) {
		return new Timestamp(input.readLong(true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp copy(Kryo kryo, Timestamp original) {
		return new Timestamp(original.getTime());
	}

}
