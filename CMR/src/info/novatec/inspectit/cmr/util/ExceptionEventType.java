package info.novatec.inspectit.cmr.util;

import info.novatec.inspectit.communication.ExceptionEvent;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * User type for Hibernate to map the {@link ExceptionEvent} class into the database.
 * 
 * @author Patrice Bouillet
 * 
 */
public class ExceptionEventType implements UserType {

	/**
	 * The sql types.
	 */
	private static final int[] TYPES = new int[] { Types.NUMERIC };

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
	public Object deepCopy(Object value) {
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
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
		ExceptionEvent result = null;
		int dbValue = resultSet.getInt(names[0]);
		if (dbValue != -1) {
			result = ExceptionEvent.fromOrd(dbValue);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
		if (null == value) {
			statement.setInt(index, -1);
		} else {
			ExceptionEvent event = (ExceptionEvent) value;
			int dbValue = event.ordinal();
			statement.setInt(index, dbValue);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object replace(Object arg0, Object arg1, Object arg2) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<ExceptionEvent> returnedClass() {
		return ExceptionEvent.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object assemble(Serializable arg0, Object arg1) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable disassemble(Object arg0) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object x, Object y) { // NOPMD
		if (x == y) { // NOPMD
			return true;
		} else if (x == null || y == null) {
			return false;
		} else {
			return x.equals(y);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode(Object object) {
		return object.hashCode();
	}

}
