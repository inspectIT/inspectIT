package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.AbstractObjectSizesIbm;

/**
 * The object size class for 32bit IBM JVM. Works only with Java 7.
 * 
 * @author Ivan Senic
 * 
 */
public class ObjectSizes32BitsIbm extends AbstractObjectSizesIbm {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getReferenceSize() {
		return 4;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSizeOfObjectHeader() {
		return 8;
	}

}
