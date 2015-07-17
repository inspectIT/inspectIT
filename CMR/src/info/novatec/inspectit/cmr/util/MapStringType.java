package info.novatec.inspectit.cmr.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

/**
 * Additional type for Hibernate which maps an arbitrary Map to a String in such a way that it
 * concatenates the Map entries with a {@value #DELIMITER} (by calling {@link #toString()} on every
 * Object).
 * 
 * @author Ivan Senic
 * 
 */
public class MapStringType implements UserType {

	/**
	 * Delimiter for key and value pairs.
	 */
	private static final String DELIMITER = "~:~";

	/**
	 * The sql types.
	 */
	private static final int[] TYPES = new int[] { Types.VARCHAR };

	@Override
	@SuppressWarnings("deprecation")
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		String dbValue = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names[0]);

		if (dbValue != null) {
			return explode(dbValue);
		} else {
			return null;
		}
	}

	/**
	 * Creates map out of the string.
	 * 
	 * @param dbValue
	 *            String value from the database.
	 * @return {@link Map}
	 */
	private Object explode(String dbValue) {
		if (StringUtils.isNotEmpty(dbValue)) {
			Map<Object, Object> map = new HashMap<>();
			String[] splitted = dbValue.split(DELIMITER);
			for (int i = 0; i < splitted.length; i += 2) {
				map.put(splitted[i], splitted[i + 1]);
			}
			return map;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if (value != null) {
			String v = concat((Map<?, ?>) value);
			StandardBasicTypes.STRING.nullSafeSet(st, v, index);
		} else {
			StandardBasicTypes.STRING.nullSafeSet(st, null, index);
		}

	}

	/**
	 * Transforms map to a string.
	 * 
	 * @param value
	 *            map to transform.
	 * @return String representation of map.
	 */
	private String concat(Map<?, ?> value) {
		StringBuilder stringBuilder = new StringBuilder();
		if (MapUtils.isNotEmpty(value)) {
			int i = 1;
			for (Map.Entry<?, ?> entry : value.entrySet()) {
				stringBuilder.append(entry.getKey().toString());
				stringBuilder.append(DELIMITER);
				stringBuilder.append(entry.getValue());
				if (i < value.size()) {
					stringBuilder.append(DELIMITER);
				}
				i++;
			}

		}
		return stringBuilder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMutable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) deepCopy(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] sqlTypes() {
		return TYPES.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> returnedClass() {
		return Map.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object x, Object y) throws HibernateException { // NOPMD
		if (x == y) { // NOPMD
			return true;
		}

		if (x == null) {
			return false;
		}

		return x.equals(y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

}
