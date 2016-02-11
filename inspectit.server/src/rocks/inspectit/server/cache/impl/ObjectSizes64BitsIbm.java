package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.AbstractObjectSizesIbm;

/**
 * The object size class for 64bit IBM JVM. Works only with Java 7.
 * 
 * @author Ivan Senic
 * 
 */
public class ObjectSizes64BitsIbm extends AbstractObjectSizesIbm {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getReferenceSize() {
		return 8;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSizeOfObjectHeader() {
		return 16;
	}

}
