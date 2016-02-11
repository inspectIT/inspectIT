package info.novatec.inspectit.jpa;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.AttributeConverter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
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
	private static final char DELIMITER = '¦';

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
			String[] split = StringUtils.splitPreserveAllTokens(dbValue, DELIMITER);

			// in order to support empty strings we add delimiter after each object (line 37)
			// as of splitPerserve will add additional object at the end that we don't want
			return Arrays.asList(ArrayUtils.subarray(split, 0, split.length - 1));
		} else {
			return Collections.emptyList();
		}
	}
}
