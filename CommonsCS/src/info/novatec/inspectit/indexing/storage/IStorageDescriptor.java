package info.novatec.inspectit.indexing.storage;

/**
 * Interface for the peace of data describing one entry/object in the data file. The descriptor
 * provides information where in the file the data is located ({@link #getPosition()}), what is the
 * data size ({@link #getSize()} and what is the channel ID where the data is saved (
 * {@link #getChannelId()}.
 * 
 * @author Ivan Senic
 * 
 */
public interface IStorageDescriptor extends Comparable<IStorageDescriptor> {

	/**
	 * @return the channelId
	 */
	int getChannelId();

	/**
	 * @param channelId
	 *            the channelId to set
	 */
	void setChannelId(int channelId);

	/**
	 * @return the position
	 */
	long getPosition();

	/**
	 * @return the size
	 */
	long getSize();

	/**
	 * Sets the position and size in the file for this descriptor.
	 * 
	 * @param position
	 *            the position to set
	 * 
	 * @param size
	 *            the size to set
	 */
	void setPositionAndSize(long position, long size);

}