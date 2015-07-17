package info.novatec.inspectit.indexing.storage.impl;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simple storage descriptor stores only position as long and size as int value, since this is
 * enough information to read a object if file is known. File specification will be done by
 * {@link StorageLeaf}s directly.
 * 
 * @author Ivan Senic
 * 
 */
public class SimpleStorageDescriptor {

	/**
	 * Position in file.
	 */
	private long position;

	/**
	 * Size.
	 */
	private int size;

	/**
	 * No-arg constructor.
	 */
	public SimpleStorageDescriptor() {
	}

	/**
	 * Constructor to set the fields.
	 * 
	 * @param position
	 *            Position in file.
	 * @param size
	 *            Size.
	 */
	public SimpleStorageDescriptor(long position, int size) {
		this.position = position;
		this.size = size;
	}

	/**
	 * Joins the position and size information if possible contained in other descriptor. This
	 * method will return true if the join was successfully done, and false if no join was done. The
	 * join is possible only if the given position and size is pointing to the data that is next to
	 * the data currently described in {@link SimpleStorageDescriptor}.
	 * 
	 * @param other
	 *            Descriptor to join
	 * @return This method will return true if the join was successfully done, and false if no join
	 *         was done.
	 */
	public boolean join(SimpleStorageDescriptor other) {
		return join(other.getPosition(), other.getSize());
	}

	/**
	 * Joins the position and size information if possible. This method will return true if the join
	 * was successfully done, and false if no join was done. The join is possible only if the given
	 * position and size is pointing to the data that is next to the data currently described in
	 * {@link SimpleStorageDescriptor}.
	 * 
	 * @param otherPosition
	 *            Position
	 * @param otherSize
	 *            Size
	 * @return This method will return true if the join was successfully done, and false if no join
	 *         was done.
	 */
	public boolean join(long otherPosition, long otherSize) {
		if (this.position + this.size == otherPosition) {
			this.size += otherSize;
			return true;
		} else if (otherPosition + otherSize == this.position) {
			this.position = otherPosition;
			this.size += otherSize;
			return true;
		}
		return false;
	}

	/**
	 * @return the position
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(long position) {
		this.position = position;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (position ^ (position >>> 32));
		result = prime * result + size;
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
		SimpleStorageDescriptor other = (SimpleStorageDescriptor) obj;
		if (position != other.position) {
			return false;
		}
		if (size != other.size) {
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
		toStringBuilder.append("position", position);
		toStringBuilder.append("size", size);
		return toStringBuilder.toString();
	}
}
