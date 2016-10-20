package rocks.inspectit.shared.all.serializer.impl;

import java.lang.reflect.Constructor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Special serialzier that eliminates the difficultes of de-serializing the
 * {@link StackTraceElement}.
 *
 * @author Ivan Senic
 *
 */
public class StackTraceElementSerializer extends Serializer<StackTraceElement> {

	/**
	 * Constructor for new instances.
	 */
	private final Constructor<StackTraceElement> constructor;

	/**
	 * Default constructor.
	 */
	public StackTraceElementSerializer() {
		try {
			constructor = StackTraceElement.class.getConstructor(String.class, String.class, String.class, int.class);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StackTraceElement read(Kryo kryo, Input input, Class<StackTraceElement> type) {
		String className = input.readString();
		String methodName = input.readString();
		String fileName = input.readString();
		int lineNumber = input.readInt(true);
		try {
			return constructor.newInstance(className, methodName, fileName, lineNumber);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(Kryo kryo, Output output, StackTraceElement stackTrace) {
		output.writeString(stackTrace.getClassName());
		output.writeString(stackTrace.getMethodName());
		output.writeString(stackTrace.getFileName());
		output.writeInt(stackTrace.getLineNumber(), true);
	}
}
