package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.util.UnderlyingSystemInfo;
import info.novatec.inspectit.util.UnderlyingSystemInfo.JvmProvider;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * Factory for returning the correct instance of {@link IObjectSizes} for Spring initialization. The
 * factory will check if the IBM JVM is used, and in that case provide the different
 * {@link IObjectSizes} objects that support IBM JVM object memory footprint. Further more the
 * factory will provide different instances for a 32bit and 64bit JVMs, and even check if the
 * compressed OOPs are used with 64bit, and also provide a support for them.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ObjectSizesFactory implements FactoryBean<IObjectSizes> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IObjectSizes getObject() throws Exception {
		boolean isIbm = UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.IBM;
		boolean is64Bit = UnderlyingSystemInfo.IS_64BIT;
		boolean compresedOops = UnderlyingSystemInfo.IS_COMPRESSED_OOPS;
		if (is64Bit && !compresedOops) {
			if (isIbm) {
				return new ObjectSizes64BitsIbm();
			} else {
				return new ObjectSizes64Bits();
			}
		} else if (is64Bit && compresedOops) {
			if (isIbm) {
				return new ObjectSizes64BitsCompressedOopsIbm();
			} else {
				return new ObjectSizes64BitsCompressedOops();
			}
		} else {
			if (isIbm) {
				return new ObjectSizes32BitsIbm();
			} else {
				return new ObjectSizes32Bits();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<IObjectSizes> getObjectType() {
		return IObjectSizes.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

}
