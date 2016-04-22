package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.indexing.buffer.IBufferTreeComponent;
import rocks.inspectit.shared.cs.indexing.impl.IndexingException;

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
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
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
			return (defaultData instanceof InvocationAwareData) && ((InvocationAwareData) defaultData).isOnlyFoundInInvocations();
		}
		return false;
	}

	/**
	 * Sets {@link #indexingTree}.
	 *
	 * @param indexingTree
	 *            New value for {@link #indexingTree}
	 */
	public void setIndexingTree(IBufferTreeComponent<DefaultData> indexingTree) {
		this.indexingTree = indexingTree;
	}

}
