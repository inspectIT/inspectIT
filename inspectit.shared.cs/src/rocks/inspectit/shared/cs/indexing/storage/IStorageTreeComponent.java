package info.novatec.inspectit.indexing.storage;

import info.novatec.inspectit.indexing.ITreeComponent;

/**
 * Special tree component for purposes of indexing for storage.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of elements to be stored.
 */
public interface IStorageTreeComponent<E> extends ITreeComponent<IStorageDescriptor, E> {

	/**
	 * Informs the component that the pre write finalization is needed.
	 */
	void preWriteFinalization();
}
