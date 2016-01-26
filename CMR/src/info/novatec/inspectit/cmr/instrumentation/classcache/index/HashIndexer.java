package info.novatec.inspectit.cmr.instrumentation.classcache.index;

import info.novatec.inspectit.cmr.instrumentation.classcache.events.INodeChangeListener;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.NodeEvent;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.ReferenceEvent;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.NodeEvent.NodeEventDetails;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.NodeEvent.NodeEventType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
	private Map<String, ImmutableType> storage = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void informNodeChange(NodeEvent event) {
		if (NodeEventDetails.INITIALIZED.equals(event.eventDetails)) {
			// if it's initialized type index it cause he have hash
			storage.put(event.type.getHash(), event.type);
		} else if (NodeEventType.CHANGED.equals(event.eventType) && NodeEventDetails.HASH_ADDED.equals(event.eventDetails)) {
			// otherwise only index it if there is new hash available
			storage.put(event.additionalInformation.toString(), event.type);
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
