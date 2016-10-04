package rocks.inspectit.shared.all.util;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This is a limited LinkedHashMap with a FIFO behaviour.
 *
 * @author Alexander Wert
 *
 * @param <K>
 *            The type of the key.
 * @param <V>
 *            The type of the value.
 */
public class FifoMap<K, V> extends LinkedHashMap<K, V> {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The capacity of the FifoMap.
	 */
	private int capacity;

	/**
	 * Constructor.
	 *
	 * @param capacity
	 *            the capacity of the FifoMap.
	 */
	public FifoMap(int capacity) {
		super(capacity + 1);
		this.capacity = capacity;
	}

	@Override
	public V put(K key, V value) {
		V forReturn = super.put(key, value);
		if (super.size() > capacity) {
			removeEldest();
		}

		return forReturn;
	}

	/**
	 * removes the eldest element from the FifoMap.
	 */
	private void removeEldest() {
		Iterator<K> iterator = this.keySet().iterator();
		if (iterator.hasNext()) {
			this.remove(iterator.next());
		}
	}

}
