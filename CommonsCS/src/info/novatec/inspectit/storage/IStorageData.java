package info.novatec.inspectit.storage;

import info.novatec.inspectit.storage.label.AbstractStorageLabel;

import java.util.List;

/**
 * This interface serves as the connection between {@link StorageData} and {@link LocalStorageData},
 * in sense that both should be able to provide the ID and general data of storage.
 * 
 * @author Ivan Senic
 * 
 */
public interface IStorageData {

	/**
	 * Returns the storage id.
	 * 
	 * @return Returns the storage id.
	 */
	String getId();

	/**
	 * Returns the name of the directory where storage data is.
	 * 
	 * @return Returns the name of the directory where storage data is.
	 */
	String getStorageFolder();

	/**
	 * Returns the name of the storage.
	 * 
	 * @return Returns the storage name.
	 */
	String getName();

	/**
	 * Returns the size of the storage in bytes.
	 * 
	 * @return Returns the storage disk size.
	 */
	long getDiskSize();

	/**
	 * Returns the description of storage.
	 * 
	 * @return Returns storage description or <code>null</code> if there is no description.
	 */
	String getDescription();

	/**
	 * Returns the list of the currently bounded labels to the storage.
	 * 
	 * @return Returns the label list of storage.
	 */
	List<AbstractStorageLabel<?>> getLabelList();

	/**
	 * Returns version of the CMR on which the Storage is originally created.
	 * 
	 * @return Version of the CMR on which the Storage is originally created.
	 */
	String getCmrVersion();

}
