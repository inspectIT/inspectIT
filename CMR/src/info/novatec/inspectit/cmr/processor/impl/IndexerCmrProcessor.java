package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.spring.logger.Log;

import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor that index elements directly to the indexing tree.
 * 
 * @author Ivan Senic
 * 
 */
public class IndexerCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The logger of this class.
	 */
	@Log
	Logger log;

	/**
	 * The indexing tree for direct object indexing.
	 */
	@Autowired
	IBufferTreeComponent<DefaultData> indexingTree;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, StatelessSession session) {
		try {
			indexingTree.put(defaultData);
		} catch (IndexingException e) {
			// should never happen
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		if (null != defaultData) {
			// only directly index the invocation aware data that is in invocation
			return defaultData instanceof InvocationAwareData && ((InvocationAwareData) defaultData).isOnlyFoundInInvocations();
		}
		return false;
	}

}
