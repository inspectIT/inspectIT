package info.novatec.inspectit.jpa;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * {@link AttributeConverter} that can convert Map to a String value so it can be saved in a single
 * column in DB.
 * 
 * @author Ivan Senic
 * 
 */
public class MapStringConverter implements AttributeConverter<Map<?, ?>, String> {

	/**
	 * Delimiter for key and value pairs.
	 */
	private static final String DELIMITER = "~:~";

	/**
	 * {@inheritDoc}
	 * <p>
	 * Transforms map to a string.
	 */
	public String convertToDatabaseColumn(Map<?, ?> map) {
		StringBuilder stringBuilder = new StringBuilder();
		if (MapUtils.isNotEmpty(map)) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				stringBuilder.append(entry.getKey().toString());
				stringBuilder.append(DELIMITER);
				stringBuilder.append(entry.getValue());
				stringBuilder.append(DELIMITER);
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates map out of the string.
	 */
	public Map<?, ?> convertToEntityAttribute(String dbValue) {
		if (StringUtils.isNotEmpty(dbValue)) {
			Map<Object, Object> map = new HashMap<Object, Object>();

			int i = 0;
			while (i < dbValue.length()) {
				int keyIndex = dbValue.indexOf(DELIMITER, i);
				int valueIndex = dbValue.indexOf(DELIMITER, keyIndex + DELIMITER.length());

				if (keyIndex < 0 || valueIndex < 0) {
					break;
				} else {
					String key, value;
					if (keyIndex == i) {
						key = "";
					} else {
						key = dbValue.substring(i, keyIndex);
					}
					if (valueIndex == keyIndex + DELIMITER.length()) {
						value = "";
					} else {
						value = dbValue.substring(keyIndex + DELIMITER.length(), valueIndex);
					}

					map.put(key, value);
					i = valueIndex + DELIMITER.length();
				}
			}

			return map;
		} else {
			return Collections.emptyMap();
		}
	}
}
