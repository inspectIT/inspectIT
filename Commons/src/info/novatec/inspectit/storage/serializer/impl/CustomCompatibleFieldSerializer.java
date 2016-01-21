package info.novatec.inspectit.storage.serializer.impl;

import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;
import info.novatec.inspectit.storage.serializer.schema.ClassSchema;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import java.lang.reflect.Field;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.OutputChunked;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.ObjectMap;

/**
 * This is the custom compatible {@link FieldSerializer} that uses our {@link ClassSchemaManager} to
 * read the integer marker for each class field.
 * <p>
 * This class is similar to the {@link com.esotericsoftware.kryo.serializers.TaggedFieldSerializer}
 * in the way fields are removed if they don't compile (in our case) with schema given for every
 * class. The writing and the reading of the fields is similar to the
 * {@link CompatibleFieldSerializer}. But instead of writing every field name prior to values (as it
 * is done in the mentioned serializer), we only write the marker for each field that is supplied
 * from the schema. So instead of having the following sequence of data: <br>
 * <br>
 * <i>[field1Name, field2Name, field1Value, field2Value]</i> <br>
 * <br>
 * here we do it as:<br>
 * <br>
 * <i>[1, 2, field1Value, field2Value]</i> <br>
 * <br>
 * This way we have the backward/forward compatibility based on the given schema and in addition
 * decrease the amount of binary data. This is because we are writing integers that are less than
 * 128 and occupy only 1 byte and not complete field names.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken/based from
 * <a href="https://github.com/EsotericSoftware/kryo">kryo</a>. Original author is Nathan Sweet.
 * License info can be found
 * <a href="https://github.com/EsotericSoftware/kryo/blob/master/license.txt">here</a>.
 * 
 * @author Nathan Sweet <misc@n4te.com>
 * @author Ivan Senic
 * 
 * @param <T>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CustomCompatibleFieldSerializer<T> extends FieldSerializer<T> {

	/**
	 * Markers that will be assigned to each field that should be serialized.
	 */
	private int[] fieldMarkers;

	/**
	 * Class schema that will be use.
	 */
	private ClassSchema schema;

	/**
	 * Default constructor.
	 * 
	 * @param kryo
	 *            Kryo instance
	 * @param type
	 *            Class to be serialized
	 * @param schemaManager
	 *            {@link ClassSchemaManager} holding information about values.
	 */
	public CustomCompatibleFieldSerializer(Kryo kryo, Class<?> type, ClassSchemaManager schemaManager) {
		this(kryo, type, schemaManager, false);
	}

	/**
	 * Default constructor.
	 * 
	 * @param kryo
	 *            Kryo instance
	 * @param type
	 *            Class to be serialized
	 * @param schemaManager
	 *            {@link ClassSchemaManager} holding information about values.
	 * @param useSuperclassSchema
	 *            If the superclass schema should be used if the one for the class is not available.
	 */
	public CustomCompatibleFieldSerializer(Kryo kryo, Class<?> type, ClassSchemaManager schemaManager, boolean useSuperclassSchema) {
		super(kryo, type);
		schema = schemaManager.getSchema(type.getName());
		if (useSuperclassSchema && null == schema) {
			Class<?> superclass = type.getSuperclass();
			while (null != superclass) {
				schema = schemaManager.getSchema(superclass.getName());
				if (null != schema) {
					break;
				}
				superclass = superclass.getSuperclass();
			}
		}

		if (schema == null) {
			throw new IllegalArgumentException("Schema for the class '" + type.getName() + "' does not exists in provided schema manager.");
		}
		initializeCachedFields();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void initializeCachedFields() {
		if (null != schema) {
			CachedField<?>[] fields = getFields();

			// Remove unwanted fields
			for (int i = 0, n = fields.length; i < n; i++) {
				Field field = fields[i].getField();
				if (null == schema.getFieldMarker(field.getName())) {
					super.removeField(field.getName());
				}
			}

			// Cache markers
			fields = getFields();
			fieldMarkers = new int[fields.length];
			for (int i = 0, n = fields.length; i < n; i++) {
				fieldMarkers[i] = schema.getFieldMarker(fields[i].getField().getName()).intValue();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(Kryo kryo, Output output, T object) {
		CachedField[] fields = getFields();
		ObjectMap context = kryo.getGraphContext();
		if (!context.containsKey(this)) {
			context.put(this, null);
			if (TRACE) {
				trace("kryo", "Write " + fields.length + " field names.");
			}
			output.writeInt(fields.length, true);
			for (int i = 0, n = fields.length; i < n; i++) {
				// Changed by ISE
				output.writeInt(fieldMarkers[i], true);
			}
		}

		OutputChunked outputChunked = new OutputChunked(output, 1024);
		for (int i = 0, n = fields.length; i < n; i++) {
			fields[i].write(outputChunked, object);
			outputChunked.endChunks();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
		T object = kryo.newInstance(type);
		kryo.reference(object);
		ObjectMap context = kryo.getGraphContext();
		CachedField[] fields = (CachedField[]) context.get(this);
		if (fields == null) {
			int length = input.readInt(true);
			if (TRACE) {
				trace("kryo", "Read " + length + " field names.");
			}
			// Changed by ISE
			int[] markers = new int[length];
			for (int i = 0; i < length; i++) {
				markers[i] = input.readInt(true);
			}

			fields = new CachedField[length];
			CachedField[] allFields = getFields();
			outer: for (int i = 0, n = markers.length; i < n; i++) {
				int fieldMarker = markers[i];
				for (int ii = 0, nn = allFields.length; ii < nn; ii++) {
					if (fieldMarkers[ii] == fieldMarker) {
						fields[i] = allFields[ii];
						continue outer;
					}
				}
				if (TRACE) {
					trace("kryo", "Ignoring obsolete field with marker: " + fieldMarker);
				}
			}
			context.put(this, fields);
		}

		InputChunked inputChunked = new InputChunked(input, 1024);
		for (int i = 0, n = fields.length; i < n; i++) {
			CachedField cachedField = fields[i];
			if (cachedField == null) {
				if (TRACE) {
					trace("kryo", "Skip obsolete field.");
				}
				inputChunked.nextChunks();
				continue;
			}
			cachedField.read(inputChunked, object);
			inputChunked.nextChunks();
		}
		return object;
	}

}
