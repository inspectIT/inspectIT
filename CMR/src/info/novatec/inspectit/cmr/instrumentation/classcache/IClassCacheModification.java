package info.novatec.inspectit.cmr.instrumentation.classcache;

import info.novatec.inspectit.cmr.instrumentation.classcache.events.Events;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;

/**
 * Public interface to the <code>ClassCacheModification</code> service.
 * 
 * @author Stefan Siegl
 */
public interface IClassCacheModification {

	/**
	 * The modification service takes the given model class instance and applies it to the class
	 * structure.
	 * 
	 * <b> Merging is only done one level </b> </br /> Adding information to the cache is usually
	 * done by the class parsers. This parser gets one concrete class (byte code) and reads the
	 * information from it. As it is only possible to get the direct associations of a given entity
	 * (e.g. only get the direct superclass of a given class), the merging logic will only handle
	 * this one level and not continue on.
	 * 
	 * @param given
	 *            the type that should be added to the structure.
	 * @return Change events.
	 * @throws ClassCacheModificationException
	 *             if (a) the given entity was null (b) the FQN of the entity was null (c) the same
	 *             FQN is already used in the class cache but with a different type.
	 */
	Events merge(ImmutableType given) throws ClassCacheModificationException;
}
