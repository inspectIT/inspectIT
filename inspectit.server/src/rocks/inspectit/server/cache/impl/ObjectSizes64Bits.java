package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.AbstractObjectSizes;
import info.novatec.inspectit.cmr.cache.IObjectSizes;

import java.util.List;

/**
 * This class provides a implementation of {@link IObjectSizes} appropriate for calculations of
 * object sizes on 64-bit Sun VM. Works only with Java 7.
 * 
 * @author Ivan Senic
 * 
 */
public class ObjectSizes64Bits extends AbstractObjectSizes {

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
	@Override
	public long getSizeOfObjectHeader() {
		return 16;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * With no-compressed oops we have internal padding between AbstractList and ArrayList that must
	 * be counted for.
	 */
	@Override
	public long getSizeOf(List<?> arrayList, int initialCapacity) {
		if (null == arrayList) {
			return 0;
		}
		int capacity = getArrayCapacity(arrayList.size(), initialCapacity);
		// first AbstractList
		long size = alignTo8Bytes(this.getSizeOfObjectHeader() + this.getPrimitiveTypesSize(0, 0, 1, 0, 0, 0));

		// then ArraList
		size = alignTo8Bytes(size + this.getPrimitiveTypesSize(1, 0, 1, 0, 0, 0));
		size += this.getSizeOfArray(capacity);
		return alignTo8Bytes(size);
	}
}
