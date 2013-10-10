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
	private static final char DELIMITER = '¦';

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
			String[] split = StringUtils.splitPreserveAllTokens(dbValue, DELIMITER);

			// in order to support empty strings we add delimiter after each object (line 36/38)
			// as of splitPerserve will add additional object at the end that we don't want
			// thus iterate until i < length - 1
			for (int i = 0; i < split.length - 1; i += 2) {
				map.put(split[i], split[i + 1]);
			}

			return map;
		} else {
			return Collections.emptyMap();
		}
	}
}
