package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.Sizeable;

import java.io.Serializable;

/**
 * This enumeration defines the possible types of the parameter capturing approach.
 * <code>FIELD</code> refers to a field being accessed. <code>PARAM</code> refers to a parameter
 * being read from a method call. <code>RETURN</code> refers to capturing the return value of the
 * method invocation.
 * 
 * @author Stefan Siegl
 */
public enum ParameterContentType implements Serializable, Sizeable {

	/** Property accessor refers to a field. */
	FIELD,

	/** Property accessor refers to a parameter. */
	PARAM,

	/** Property accessor refers to a return value. */
	RETURN;

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -8005782295084781051L;

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes) {
		return getObjectSize(objectSizes, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(1, 0, 1, 0, 0, 0);
		size += objectSizes.getSizeOf(name());

		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}
}