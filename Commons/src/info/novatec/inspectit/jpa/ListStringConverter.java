package info.novatec.inspectit.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.AttributeConverter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * {@link AttributeConverter} that can convert List to a String value so it can be saved in a single
 * column in DB.
 * 
 * @author Ivan Senic
 * 
 */
public class ListStringConverter implements AttributeConverter<List<?>, String> {

	/**
	 * Delimiter for key and value pairs.
	 */
	private static final String DELIMITER = "~:~";

	/**
	 * {@inheritDoc}
	 * <p>
	 * Concatenates the given list with the whitespace char and returns the generated String.
	 */
	public String convertToDatabaseColumn(List<?> list) {
		StringBuilder stringBuilder = new StringBuilder();
		if (CollectionUtils.isNotEmpty(list)) {
			for (Object object : list) {
				stringBuilder.append(object);
				stringBuilder.append(DELIMITER);
			}
		}

		return stringBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Explodes the given String by splitting it up. The split char is just a whitespace.
	 */
	public List<?> convertToEntityAttribute(String dbValue) {
		if (StringUtils.isNotEmpty(dbValue)) {
			List<String> result = new ArrayList<String>();
			int i = 0;
			while (i < dbValue.length()) {
				int index = dbValue.indexOf(DELIMITER, i);

				if (index < 0) {
					break;
				} else if (index == i) {
					// empty one
					result.add("");
					i += DELIMITER.length();
				} else {
					result.add(dbValue.substring(i, index));
					i = index + DELIMITER.length();
				}
			}
			return result;
		} else {
			return Collections.emptyList();
		}
	}
}
