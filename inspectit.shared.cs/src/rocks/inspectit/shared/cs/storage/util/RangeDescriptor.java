package info.novatec.inspectit.storage.util;

import info.novatec.inspectit.indexing.storage.IStorageDescriptor;

/**
 * Small class to keep track of ranges that need to be downloaded by HTTP requests.
 * 
 * @author Ivan Senic
 * 
 */
public class RangeDescriptor {

	/**
	 * Range start inclusive.
	 */
	private long start;

	/**
	 * Range end inclusive.
	 */
	private long end;

	/**
	 * Default no-arg constructor. Initializes the start and end values to zero.
	 */
	public RangeDescriptor() {
		this(0, 0);
	}

	/**
	 * Constructor that initializes the start and end values to given values.
	 * 
	 * @param start
	 *            Values of start.
	 * @param end
	 *            Values of end.
	 */
	public RangeDescriptor(long start, long end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Constructor that uses the {@link IStorageDescriptor} to initiate the initial values.
	 * 
	 * @param storageDescriptor
	 *            Storage descriptor that will be used to denote the range.
	 */
	public RangeDescriptor(IStorageDescriptor storageDescriptor) {
		start = storageDescriptor.getPosition();
		end = storageDescriptor.getPosition() + storageDescriptor.getSize() - 1;
	}

	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(long end) {
		this.end = end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return start + "-" + end;
	}

}