package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.indexing.storage.AbstractStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Storage descriptor. POJO that keeps information about where element is stored on disk.
 * 
 * @see IStorageDescriptor
 * @author Ivan Senic
 * 
 */
public class StorageDescriptor extends AbstractStorageDescriptor {

	/**
	 * Channel id.
	 */
	private int channelId;

	/**
	 * Reference to the {@link SimpleStorageDescriptor} that will actually hold the position and
	 * size values.
	 */
	private SimpleStorageDescriptor simpleStorageDescriptor;

	/**
	 * Default constructor. Instantiates new {@link SimpleStorageDescriptor}.
	 */
	public StorageDescriptor() {
		simpleStorageDescriptor = new SimpleStorageDescriptor();
	}

	/**
	 * Instantiates new {@link SimpleStorageDescriptor} and assigns the channel ID.
	 * 
	 * @param channelId
	 *            Channel id to hold.
	 */
	public StorageDescriptor(int channelId) {
		this();
		this.channelId = channelId;
	}

	/**
	 * Assigns the channel ID and {@link SimpleStorageDescriptor}.
	 * 
	 * @param channelId
	 *            Channel id to hold.
	 * @param simpleStorageDescriptor
	 *            {@link SimpleStorageDescriptor} to hold.
	 */
	public StorageDescriptor(int channelId, SimpleStorageDescriptor simpleStorageDescriptor) {
		this.channelId = channelId;
		this.simpleStorageDescriptor = simpleStorageDescriptor;
	}

	/**
	 * Assigns the channel ID, position in file and size.
	 * 
	 * @param channelId
	 *            Channel id to hold.
	 * @param position
	 *            Position in file.
	 * @param size
	 *            Size.
	 */
	public StorageDescriptor(int channelId, long position, long size) {
		this.channelId = channelId;
		this.simpleStorageDescriptor = new SimpleStorageDescriptor(position, (int) size);
	}

	/**
	 * Joins the information from the other storage descriptor if possible. This method will return
	 * true if the join was successfully done, and false if no join was done. The join is possible
	 * only if the two descriptors are actually describing the data in the same channel that are
	 * next to each other. There is no difference if the other descriptor is pointing to the data
	 * after or before this descriptor. After successful join the joined data descriptor will be
	 * represented by this descriptor.
	 * 
	 * @param other
	 *            Descriptor information to join.
	 * @return This method will return true if the join was successfully done, and false if no join
	 *         was done.
	 */
	public boolean join(IStorageDescriptor other) {
		if (this.getChannelId() != other.getChannelId()) {
			return false;
		} else {
			return join(other.getPosition(), other.getSize());
		}
	}

	/**
	 * Joins the position and size information if possible. This method will return true if the join
	 * was successfully done, and false if no join was done. The join is possible only if the given
	 * position and size is pointing to the data that is next to the data currently described in
	 * {@link StorageDescriptor}.
	 * 
	 * @param otherPosition
	 *            Position
	 * @param otherSize
	 *            Size
	 * @return This method will return true if the join was successfully done, and false if no join
	 *         was done.
	 */
	public boolean join(long otherPosition, long otherSize) {
		return simpleStorageDescriptor.join(otherPosition, otherSize);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getChannelId() {
		return channelId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	/**
	 * @return the simpleStorageDescriptor
	 */
	public SimpleStorageDescriptor getSimpleStorageDescriptor() {
		return simpleStorageDescriptor;
	}

	/**
	 * @param simpleStorageDescriptor
	 *            the simpleStorageDescriptor to set
	 */
	public void setSimpleStorageDescriptor(SimpleStorageDescriptor simpleStorageDescriptor) {
		this.simpleStorageDescriptor = simpleStorageDescriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPosition() {
		return simpleStorageDescriptor.getPosition();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSize() {
		return simpleStorageDescriptor.getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPositionAndSize(long position, long size) {
		simpleStorageDescriptor.setPosition(position);
		simpleStorageDescriptor.setSize((int) size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channelId;
		result = prime * result + ((simpleStorageDescriptor == null) ? 0 : simpleStorageDescriptor.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StorageDescriptor other = (StorageDescriptor) obj;
		if (channelId != other.channelId) {
			return false;
		}
		if (simpleStorageDescriptor == null) {
			if (other.simpleStorageDescriptor != null) {
				return false;
			}
		} else if (!simpleStorageDescriptor.equals(other.simpleStorageDescriptor)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("channelId", channelId);
		toStringBuilder.append("position", simpleStorageDescriptor.getPosition());
		toStringBuilder.append("size", simpleStorageDescriptor.getSize());
		return toStringBuilder.toString();
	}

}
