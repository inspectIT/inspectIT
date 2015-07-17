package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.storage.AbstractStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.storage.util.StorageUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This leaf for the storage is not keeping the {@link SimpleStorageDescriptor} for each element.
 * This leaf just keeps track of the total size of elements saved. It is expected that this leafs
 * are used when the elements does not have to be retrieved singularly.
 * <P>
 * The clear advantage with this leaf is size of the leaf in memory/disk. However, the price for
 * this is not being able to find one concrete element in the leaf. When this is necessary, the
 * other leaf type must be used.
 * <P>
 * <b>Important:</b><br>
 * Changing this class can cause the break of the backward/forward compatibility of the storage in
 * the way that we will not be able to read any data from the storage. Thus, please be careful with
 * performing any changes until there is a proper mechanism to protect against this problem.
 * 
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of data that can be indexed.
 */
public class LeafWithNoDescriptors<E extends DefaultData> implements IStorageTreeComponent<E> {

	/**
	 * Max size of one range is 8MB. If more that is going to be written in the same leaf then we
	 * need to split data in several ranges. The problem can occur if we write too much in one leaf
	 * (like > 100MB) loading of this data on the UI has to be in one request, making this request a
	 * high impact on the memory that can make OOME in the UI.
	 */
	private static final long MAX_RANGE_SIZE = 8388608;

	/**
	 * Leaf id.
	 */
	private int id;

	/**
	 * Bounded descriptor.
	 */
	private transient BoundedDecriptor boundedDecriptor = new BoundedDecriptor();

	/**
	 * This is a not thread-safe list of the descriptors that need to be synchronized on our own.
	 */
	private List<SimpleStorageDescriptor> descriptors = new ArrayList<SimpleStorageDescriptor>();

	/**
	 * Default constructor. Generates leaf ID.
	 */
	public LeafWithNoDescriptors() {
		this(StorageUtil.getRandomInt());
	}

	/**
	 * Secondary constructor. Assigns the leaf with the ID.
	 * 
	 * @param id
	 *            ID to be assigned to the leaf.
	 */
	public LeafWithNoDescriptors(int id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor put(E element) throws IndexingException {
		return boundedDecriptor;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calling this method with this implementation of the storage leaf is highly discouraged,
	 * because we simply can not find one element, and have to return descriptor for all elements.
	 */
	public IStorageDescriptor get(E element) {
		throw new UnsupportedOperationException("LeafWithNoDescriptors can not answer on the single element query.");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IStorageDescriptor> query(IIndexQuery query) {
		List<IStorageDescriptor> list = new ArrayList<IStorageDescriptor>();
		for (SimpleStorageDescriptor simpleStorageDescriptor : descriptors) {
			list.add(new StorageDescriptor(id, simpleStorageDescriptor));
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor getAndRemove(E template) {
		// FIXME Not good, when write fails, we need to remove somehow the peace that is missing..
		return null;
	};

	/**
	 * {@inheritDoc}
	 */
	public synchronized void preWriteFinalization() {
		optimiseDescriptors(descriptors);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getComponentSize(IObjectSizes objectSizes) {
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(2, 0, 1, 0, 0, 0);
		size += objectSizes.getSizeOf(descriptors);
		// manually calculate the descriptor size
		long descriptorSize = objectSizes.alignTo8Bytes(objectSizes.getSizeOfObjectObject() + objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 1, 0));
		size += descriptors.size() * descriptorSize;
		return objectSizes.alignTo8Bytes(size);
	}

	/**
	 * Adds the written position and size by updating the existing {@link #descriptors} list.
	 * 
	 * @param position
	 *            Position that was written.
	 * @param size
	 *            Size.
	 */
	private synchronized void addPositionAndSize(long position, long size) {
		for (SimpleStorageDescriptor storageDescriptor : descriptors) {
			if (storageDescriptor.getSize() + size < MAX_RANGE_SIZE && storageDescriptor.join(position, size)) {
				return;
			}
		}
		SimpleStorageDescriptor storageDescriptor = new SimpleStorageDescriptor(position, (int) size);
		descriptors.add(storageDescriptor);
	}

	/**
	 * Optimizes the list of the descriptors so that necessary joining is done. This method will
	 * also assure that no descriptor has bigger size than {@value #MAX_RANGE_SIZE} bytes, as
	 * defined in {@link #MAX_RANGE_SIZE}.
	 * 
	 * @param descriptorList
	 *            List of {@link StorageDescriptor}s to optimize.
	 */
	private void optimiseDescriptors(List<SimpleStorageDescriptor> descriptorList) {
		for (int i = 0; i < descriptorList.size() - 1; i++) {
			for (int j = i + 1; j < descriptorList.size(); j++) {
				SimpleStorageDescriptor descriptor = descriptors.get(i);
				SimpleStorageDescriptor other = descriptors.get(j);
				if (descriptor.getSize() + other.getSize() < MAX_RANGE_SIZE && descriptor.join(other)) {
					descriptorList.remove(j);
					j--;
				}
			}
		}
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	int getId() {
		return id;
	}

	/**
	 * This is the private implementation of {@link IStorageDescriptor} that reflects operations
	 * directly to the leaf. The usage of descriptor outside of this class should not be changed,
	 * but calling some methods won't create any actions.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class BoundedDecriptor extends AbstractStorageDescriptor {

		/**
		 * {@inheritDoc}
		 */
		public int getChannelId() {
			return id;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setChannelId(int channelId) {
		}

		/**
		 * {@inheritDoc}
		 */
		public long getPosition() {
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public long getSize() {
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setPositionAndSize(long position, long size) {
			addPositionAndSize(position, size);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("descriptors", descriptors);
		return toStringBuilder.toString();
	}
}
