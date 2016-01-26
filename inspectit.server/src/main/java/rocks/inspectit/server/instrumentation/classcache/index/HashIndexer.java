package rocks.inspectit.server.instrumentation.classcache.index;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.instrumentation.classcache.events.INodeChangeListener;
import rocks.inspectit.server.instrumentation.classcache.events.NodeEvent;
import rocks.inspectit.server.instrumentation.classcache.events.NodeEvent.NodeEventDetails;
import rocks.inspectit.server.instrumentation.classcache.events.NodeEvent.NodeEventType;
import rocks.inspectit.server.instrumentation.classcache.events.ReferenceEvent;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;

/**
 * Indexer that index types based on the hash.
 * <p>
 * Note that this indexer should not be used with multiple threads reading and writing. Multiple
 * threads reading is OK.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class HashIndexer implements INodeChangeListener {

	/**
	 * Simple hash map for holding hashes.
	 */
	private final Map<String, ImmutableType> storage = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void informNodeChange(NodeEvent event) {
		if (NodeEventDetails.INITIALIZED.equals(event.getEventDetails())) {
			// if it's initialized type index it cause he have hash
			ImmutableType type = event.getType();
			for (String hash : type.getHashes()) {
				storage.put(hash, type);
			}
		} else if (NodeEventType.CHANGED.equals(event.getEventType()) && NodeEventDetails.HASH_ADDED.equals(event.getEventDetails())) {
			// otherwise only index it if there is new hash available
			ImmutableType type = event.getType();
			for (String hash : type.getHashes()) {
				if (!storage.containsKey(hash)) {
					storage.put(hash, type);
				}
			}
		} else if (NodeEventType.REMOVED.equals(event.getEventType())) {
			// if removed kill all links
			ImmutableType type = event.getType();
			for (String hash : type.getHashes()) {
				storage.remove(hash);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void informReferenceChange(ReferenceEvent event) {
		// ignore
	}

	/**
	 * Finds type by exact hash.
	 *
	 * @param hash
	 *            Type hash
	 * @return Returns type or <code>null</code> if it can not be found.
	 */
	public ImmutableType lookup(String hash) {
		return storage.get(hash);
	}
}
