package rocks.inspectit.server.cache.impl;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.util.UnderlyingSystemInfo;

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
		boolean compressedOops = UnderlyingSystemInfo.IS_COMPRESSED_OOPS;
		if (!compressedOops) {
			return new ObjectSizes64Bits();
		} else {
			return new ObjectSizes64BitsCompressedOops();
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
