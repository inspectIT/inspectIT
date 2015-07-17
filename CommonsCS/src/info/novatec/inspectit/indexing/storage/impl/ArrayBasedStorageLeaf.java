package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.storage.util.StorageUtil;
import info.novatec.inspectit.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This implementation of leaf for the {@link IStorageTreeComponent} holds
 * {@link SimpleStorageDescriptor}s and IDs of elements in separate arrays.
 * <p>
 * <b>IMPORTANT!</b>
 * <p>
 * This class might not be as effective as wanted. The remove operation in problematic, as well as
 * the idea to have the elements in the sorted array. Thus, there should be an analysis if we should
 * use hashing like structure or keep the current structure. The point is that for the storage
 * purposes removals are not heavily expected, because an element is removed from the indexing tree
 * only when we fail to serialize it (so very rare). On the other hand, putting elements into would
 * usually bring elements with higher ID than the ones indexed before. Thus, there is a chance that
 * the complete array copy operations are not executed so often (actually only when resizing of
 * array is needed), which would have better performance in compare to the hash structure, cause
 * there is no need for any rehashing here.
 * <P>
 * <b>Important:</b><br>
 * Changing this class can cause the break of the backward/forward compatibility of the storage in
 * the way that we will not be able to read any data from the storage. Thus, please be careful with
 * performing any changes until there is a proper mechanism to protect against this problem.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of the elements indexed.
 */
public class ArrayBasedStorageLeaf<E extends DefaultData> implements IStorageTreeComponent<E> {

	/**
	 * Default capacity.
	 */
	private static final int DEFAULT_CAPACITY = 16;

	/**
	 * Leaf id.
	 */
	private int id;

	/**
	 * Current leaf capacity.
	 */
	private int capacity;

	/**
	 * Current size.
	 */
	private int size;

	/**
	 * Array of object IDs.
	 */
	private long[] idArray;

	/**
	 * Array of descriptors.
	 */
	private SimpleStorageDescriptor[] descriptorArray;

	/**
	 * Reading lock.
	 */
	private transient Lock readLock;

	/**
	 * Write lock.
	 */
	private transient Lock writeLock;

	/**
	 * Default constructor. Generates leaf ID.
	 */
	public ArrayBasedStorageLeaf() {
		this(StorageUtil.getRandomInt());
	}

	/**
	 * Secondary constructor. Assigns the leaf with the ID.
	 * 
	 * @param id
	 *            ID to be assigned to the leaf.
	 */
	public ArrayBasedStorageLeaf(int id) {
		this.id = id;

		capacity = DEFAULT_CAPACITY;
		size = 0;

		idArray = new long[capacity];
		descriptorArray = new SimpleStorageDescriptor[capacity];

		ReadWriteLock rwl = new ReentrantReadWriteLock();
		readLock = rwl.readLock();
		writeLock = rwl.writeLock();
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor put(E element) throws IndexingException {
		if (null == element) {
			throw new IndexingException("Element to index can not be null.");
		} else if (0 == element.getId()) {
			throw new IndexingException("Element to index can not have ID that is equal to zero.");
		}
		StorageDescriptor descriptor = new StorageDescriptor(this.id);
		if (insertIntoArrays(element.getId(), descriptor.getSimpleStorageDescriptor())) {
			return descriptor;
		} else {
			throw new IndexingException("Element already indexed.");
		}

	}

	/**
	 * Insert a new long and its {@link StorageDescriptor} into arrays, keeping the arrays sorted,
	 * and ensuring space.
	 * 
	 * @param id
	 *            New long to insert in {@link #idArray}.
	 * @param simpleDescriptor
	 *            New {@link StorageDescriptor} to insert into {@link #descriptorArray} at the same
	 *            position as the id in {@link #idArray}.
	 * @return False if the element with the id is already in the index and thus can not be
	 *         inserted. True otherwise.
	 */
	private boolean insertIntoArrays(long id, SimpleStorageDescriptor simpleDescriptor) {
		writeLock.lock();
		try {
			int index = ArrayUtil.binarySearch(idArray, 0, size, id);
			if (index >= 0) {
				// same element is already in leaf, we return
				return false;
			}
			// binary search returns index of the search key, if it is contained in the list;
			// otherwise,
			// (-(insertion point) - 1).
			index = -index - 1;

			// note that at the beginning all 0 are in the arrays, thus the binary search will
			// return
			// the index that is of array capacity, thus if the returned indexed is bigger than
			// current
			// size, just put it at the end
			if (index > size) {
				index = size;
			}

			int oldCapacity = capacity;
			int oldSize = size;
			long[] oldIds = idArray;
			SimpleStorageDescriptor[] oldDescriptors = descriptorArray;

			// ensure space and increase size
			size++;
			if (size == capacity) {
				capacity *= 2;
				idArray = new long[capacity];
				descriptorArray = new SimpleStorageDescriptor[capacity];
			}

			// copy elements till new index only if we have a new capacity array
			if (oldCapacity != capacity) {
				System.arraycopy(oldIds, 0, idArray, 0, index);
				System.arraycopy(oldDescriptors, 0, descriptorArray, 0, index);
			}
			// copy elements after new index only if index is not the last one
			if (index < oldSize) {
				System.arraycopy(oldIds, index, idArray, index + 1, oldSize - index);
				System.arraycopy(oldDescriptors, index, descriptorArray, index + 1, oldSize - index);
			}
			idArray[index] = id;
			descriptorArray[index] = simpleDescriptor;
			return true;
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor get(E element) {
		readLock.lock();
		try {
			int index = ArrayUtil.binarySearch(idArray, 0, size, element.getId());
			if (index >= 0) {
				SimpleStorageDescriptor simpleDescriptor = descriptorArray[index];
				if (null != simpleDescriptor) {
					return new StorageDescriptor(this.id, simpleDescriptor);
				} else {
					return null;
				}
			} else {
				return null;
			}
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IStorageDescriptor> query(IIndexQuery query) {
		if (query instanceof StorageIndexQuery) {
			return this.queryWithStorageQuery((StorageIndexQuery) query);
		} else {
			readLock.lock();
			try {
				List<IStorageDescriptor> returnList = new ArrayList<IStorageDescriptor>();
				int index = 0;

				// if min id is given, we will start from the first id that is bigger or equal than
				// min
				// id
				if (query.getMinId() != 0) {
					index = ArrayUtil.binarySearch(idArray, 0, size, query.getMinId());
					if (index < 0) {
						index = -index - 1;
					}
				}

				for (; index < size; index++) {
					if (0 != idArray[index]) {
						SimpleStorageDescriptor simpleDescriptor = descriptorArray[index];
						if (null != simpleDescriptor) {
							returnList.add(new StorageDescriptor(this.id, simpleDescriptor));
						}
					}
				}

				return returnList;
			} finally {
				readLock.unlock();
			}
		}
	}

	/**
	 * Queries the leaf with more information given in the {@link StorageIndexQuery}.
	 * 
	 * @param query
	 *            {@link StorageIndexQuery}.
	 * @return List of descriptors.
	 */
	private List<IStorageDescriptor> queryWithStorageQuery(StorageIndexQuery query) {
		readLock.lock();
		try {
			List<IStorageDescriptor> returnList = new ArrayList<IStorageDescriptor>();
			int index = 0;

			// if min id is given, we will start from the first id that is bigger or equal than min
			// id
			if (query.getMinId() != 0) {
				index = ArrayUtil.binarySearch(idArray, 0, size, query.getMinId());
				if (index < 0) {
					index = -index - 1;
				}
			}

			for (; index < size; index++) {
				if (query.getExcludeIds() != null && query.getExcludeIds().contains(idArray[index])) {
					continue;
				}
				if (0 != idArray[index] && (query.getIncludeIds() == null || query.getIncludeIds().contains(idArray[index]))) {
					SimpleStorageDescriptor simpleDescriptor = descriptorArray[index];
					if (null != simpleDescriptor) {
						returnList.add(new StorageDescriptor(this.id, simpleDescriptor));
					}
				}

			}

			return returnList;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IStorageDescriptor getAndRemove(E element) {
		// / first acquire the read lock to see if the element exists
		readLock.lock();
		int index;
		SimpleStorageDescriptor simpleDescriptor;
		try {
			index = ArrayUtil.binarySearch(idArray, 0, size, element.getId());
			if (index < 0) {
				return null;
			} else {
				simpleDescriptor = descriptorArray[index];
			}
		} finally {
			readLock.unlock();
		}

		// if exists acquire write lock and remove element
		writeLock.lock();
		try {
			System.arraycopy(idArray, index + 1, idArray, index, size - index - 1);
			System.arraycopy(descriptorArray, index + 1, descriptorArray, index, size - index - 1);
			size--;
			idArray[size] = 0;
			descriptorArray[size] = null; // NOPMD
			return new StorageDescriptor(id, simpleDescriptor);
		} finally {
			writeLock.unlock();
		}

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Does nothing.
	 */
	public void preWriteFinalization() {
	}

	/**
	 * {@inheritDoc}
	 */
	public long getComponentSize(IObjectSizes objectSizes) {
		long sizeInBytes = objectSizes.getSizeOfObjectHeader();
		sizeInBytes += objectSizes.getPrimitiveTypesSize(2, 0, 4, 0, 0, 0);
		sizeInBytes += objectSizes.getSizeOfArray(idArray.length);
		sizeInBytes += size * objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 0, 0);
		sizeInBytes += objectSizes.getSizeOfArray(descriptorArray.length);
		sizeInBytes += size * (objectSizes.alignTo8Bytes(objectSizes.getSizeOfObjectHeader() + objectSizes.getPrimitiveTypesSize(0, 0, 1, 0, 1, 0)));
		// ignore locks
		return objectSizes.alignTo8Bytes(sizeInBytes);
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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + capacity;
		result = prime * result + Arrays.hashCode(descriptorArray);
		result = prime * result + id;
		result = prime * result + Arrays.hashCode(idArray);
		result = prime * result + size;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
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
		ArrayBasedStorageLeaf<E> other = (ArrayBasedStorageLeaf<E>) obj;
		if (capacity != other.capacity) {
			return false;
		}
		if (!Arrays.equals(descriptorArray, other.descriptorArray)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (!Arrays.equals(idArray, other.idArray)) {
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
		toStringBuilder.append("elements", size);
		toStringBuilder.append("capacity", capacity);
		return toStringBuilder.toString();
	}

}
