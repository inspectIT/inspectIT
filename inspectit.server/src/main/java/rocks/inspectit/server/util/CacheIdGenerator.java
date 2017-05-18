package rocks.inspectit.server.util;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Class that generates the ID for the objects that reside in cache. The ID can be generated either
 * for the objects that are going to the indexing structure, or to the elements that go to the
 * buffer.
 *
 * @author Ivan Senic
 *
 */
@Component
public class CacheIdGenerator {

	/**
	 * Atomic long to keep next available id.
	 */
	private AtomicLong nextId = new AtomicLong(Long.MAX_VALUE / (long) Math.pow(2, 32));

	/**
	 * Assigns the {@link DefaultData} object a unique ID.
	 *
	 * @param defaultData
	 *            Object to assign the ID for.
	 */
	public void assignObjectAnId(DefaultData defaultData) {
		long id = nextId.incrementAndGet();
		defaultData.setId(id);
	}
}
