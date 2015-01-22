package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.cache.IObjectSizes;

/**
 * Interface for all objects that can report its size.
 * 
 * @author Ivan Senic
 * 
 */
public interface Sizeable {

	/**
	 * Returns the approximate size of the object in the memory in bytes.
	 * 
	 * @param objectSizes
	 *            Appropriate instance of {@link IObjectSizes} depending on the VM architecture.
	 * @return Approximate object size in bytes.
	 */
	long getObjectSize(IObjectSizes objectSizes);

	/**
	 * Returns the approximate size of the object in the memory in bytes.
	 * <p>
	 * This method needs to be overridden by all subclasses.
	 * 
	 * @param objectSizes
	 *            Appropriate instance of {@link IObjectSizes} depending on the VM architecture.
	 * @param doAlign
	 *            Should the align of the bytes occur. Note that super classes objects should never
	 *            align the result because the align occurs only one time per whole object.
	 * @return Approximate object size in bytes.
	 */
	long getObjectSize(IObjectSizes objectSizes, boolean doAlign);
}
