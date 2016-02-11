package info.novatec.inspectit.storage.serializer.schema;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * {@link ClassSchema} is simple class that defines the field of the class that will be serialized.
 * Each field has a field marker integer value that goes to serialization to define the field.
 * 
 * @author Ivan Senic
 * 
 */
public class ClassSchema {

	/**
	 * Extension of schema files.
	 */
	public static final String SCHEMA_EXT = ".sch";

	/**
	 * Name that defines the class name in the schema files.
	 */
	private static final String CLAZZ = "class";

	/**
	 * Class name that schema is defined for.
	 */
	private String className;

	/**
	 * Map of field names, and their field marker int value.
	 */
	private Map<Integer, String> fieldMap;

	/**
	 * Default constructor.
	 */
	public ClassSchema() {
	}

	/**
	 * Creates the schema out of the key value string pairs. The map should hold the key
	 * {@value #CLAZZ} with class name and the pairs field names as values and its markers as keys.
	 * 
	 * @param initMap
	 *            Initialization map.
	 */
	public ClassSchema(Map<String, String> initMap) {
		String className = initMap.get(CLAZZ);
		if (null == className) {
			throw new IllegalArgumentException("Schema initial map does not define the class.");
		}
		this.className = className;
		for (Map.Entry<String, String> entry : initMap.entrySet()) {
			if (entry.getKey().equals(CLAZZ)) {
				continue;
			}
			int fieldMarker = 0;
			try {
				fieldMarker = Integer.parseInt(entry.getKey());
			} catch (NumberFormatException exception) {
				throw new IllegalArgumentException("Schema initial map contains wrong data.", exception);
			}
			if (fieldMarker > 0 && null != entry.getValue()) {
				if (null == fieldMap) {
					fieldMap = new HashMap<Integer, String>();
				}
				fieldMap.put(fieldMarker, entry.getValue());
			} else {
				throw new IllegalArgumentException("Schema initial map contains wrong data.");
			}
		}
	}

	/**
	 * @return The FQN of the class that schema is defined for.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            The FQN of the class that schema is defined for.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Returns the name of the class field that marker is defined for.
	 * 
	 * @param fieldMarker
	 *            Integer marker.
	 * @return The name of the class field that marker is defined for. <code>Null</code> if no
	 *         marker is defined for a field.
	 */
	public String getFieldName(int fieldMarker) {
		return fieldMap.get(fieldMarker);
	}

	/**
	 * Returns if the field is defined for supplied marker.
	 * 
	 * @param fieldMarker
	 *            Integer marker.
	 * @return If the field is defined for supplied marker.
	 */
	public boolean isFieldExisting(int fieldMarker) {
		return fieldMap.containsKey(fieldMarker);
	}

	/**
	 * Returns the {@link Integer} object that holds the int value for the marker of the supplied
	 * field name. If the marker for this field name is not defined, <code>null</code> will be
	 * returned.
	 * 
	 * @param fieldName
	 *            Field name of a class.
	 * @return The {@link Integer} object that holds the int value for the marker of the supplied
	 *         field name. If the marker for this field name is not defined, <code>null</code> will
	 *         be returned.
	 */
	public Integer getFieldMarker(String fieldName) {
		if (fieldMap.containsValue(fieldName)) {
			for (Map.Entry<Integer, String> entry : fieldMap.entrySet()) {
				if (entry.getValue().equals(fieldName)) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("className", className);
		toStringBuilder.append("fieldMap", fieldMap);
		return toStringBuilder.toString();
	}
}
