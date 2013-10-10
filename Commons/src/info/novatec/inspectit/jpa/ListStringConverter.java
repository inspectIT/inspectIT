package info.novatec.inspectit.jpa;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.AttributeConverter;

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
	 * {@inheritDoc}
	 * <p>
	 * Concatenates the given list with the whitespace char and returns the generated String.
	 */
	public String convertToDatabaseColumn(List<?> list) {
		StringBuffer buffer = new StringBuffer();

		if (!list.isEmpty()) {
			buffer.append(list.get(0).toString());

			for (int i = 1; i < list.size(); i++) {
				Object object = list.get(i);
				buffer.append(' ');
				buffer.append(object);
			}
		}

		return buffer.toString();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Explodes the given String by splitting it up. The split char is just a whitespace.
	 */
	public List<?> convertToEntityAttribute(String dbValue) {
		if (StringUtils.isNotEmpty(dbValue)) {
			return Arrays.asList(dbValue.split(" "));
		} else {
			return Collections.emptyList();
		}
	}

}
