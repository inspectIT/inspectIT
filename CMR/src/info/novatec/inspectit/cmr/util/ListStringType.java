package info.novatec.inspectit.cmr.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

/**
 * Additional type for Hibernate which maps an arbitrary List to a String in such a way that it
 * concatenates the List with a whitespace (by calling {@link #toString()} on every Object).
 * <p>
 * Example:<br>
 * <ul>
 * <li>java.lang.String</li>
 * <li>int</li>
 * <li>java.lang.Object[]</li>
 * </ul>
 * leads to: <b>java.lang.String int java.lang.Object[]</b> <br>
 * and vice versa.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ListStringType implements UserType {

	/**
	 * The sql types.
	 */
	private static final int[] TYPES = new int[] { Types.VARCHAR };

	/**
	 * {@inheritDoc}
	 */
	public Object assemble(Serializable arg0, Object arg1) {
		return deepCopy(arg0);

	}

	/**
	 * {@inheritDoc}
	 */
	public Object deepCopy(Object x) {
		return x;
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable disassemble(Object value) {
		return (Serializable) deepCopy(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object x, Object y) { // NOPMD
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
	public int hashCode(Object object) {
		return object.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isMutable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "deprecation" })
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException {
		String dbValue = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names[0]);

		if (dbValue != null) {
			return explode(dbValue);
		} else {
			return null;
		}

	}

	/**
	 * Explodes the given String by splitting it up. The split char is just a whitespace.
	 * 
	 * @param dbValue
	 *            The String to explode
	 * @return The exploded String.
	 */
	private List<? extends Object> explode(String dbValue) {
		return Arrays.asList(dbValue.split(" "));
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "deprecation" })
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException {
		if (value != null) {
			String v = concat((List<?>) value);
			StandardBasicTypes.STRING.nullSafeSet(st, v, index);
		} else {
			StandardBasicTypes.STRING.nullSafeSet(st, null, index);
		}
	}

	/**
	 * Concatenates the given list with the whitespace char and returns the generated String.
	 * 
	 * @param list
	 *            The list to concatenate.
	 * @return The generated String out of the list.
	 */
	private String concat(List<? extends Object> list) {
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
	 */
	public Object replace(Object arg0, Object arg1, Object arg2) {
		return deepCopy(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> returnedClass() {
		return List.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] sqlTypes() {
		return TYPES.clone();
	}

}
