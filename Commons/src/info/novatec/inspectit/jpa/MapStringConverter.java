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
			int i = 1;
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				stringBuilder.append(entry.getKey().toString());
				stringBuilder.append(DELIMITER);
				stringBuilder.append(entry.getValue());
				if (i < map.size()) {
					stringBuilder.append(DELIMITER);
				}
				i++;
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
			String[] splitted = dbValue.split(DELIMITER);
			for (int i = 0; i < splitted.length; i += 2) {
				map.put(splitted[i], splitted[i + 1]);
			}
			return map;
		} else {
			return Collections.emptyMap();
		}
	}
}
