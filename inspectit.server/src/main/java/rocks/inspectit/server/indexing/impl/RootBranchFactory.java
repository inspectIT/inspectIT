package rocks.inspectit.server.indexing.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.indexing.impl.RootBranchFactory.RootBranch;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.indexing.buffer.IBufferBranchIndexer;
import rocks.inspectit.shared.cs.indexing.buffer.IBufferTreeComponent;
import rocks.inspectit.shared.cs.indexing.buffer.impl.Branch;
import rocks.inspectit.shared.cs.indexing.buffer.impl.BufferBranchIndexer;
import rocks.inspectit.shared.cs.indexing.impl.IndexingException;
import rocks.inspectit.shared.cs.indexing.indexer.impl.ObjectTypeIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.PlatformIdentIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.TimestampIndexer;

/**
 * Factory that creates the root branch for indexing tree. This root branch will be injected in
 * Spring as a bean.
 *
 * @author Ivan Senic
 *
 */
@Component
public class RootBranchFactory implements FactoryBean<RootBranch<DefaultData>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RootBranch<DefaultData> getObject() throws Exception {
		BufferBranchIndexer<DefaultData> timestampIndexer = new BufferBranchIndexer<>(new TimestampIndexer<>());
		BufferBranchIndexer<DefaultData> objectTypeIndexer = new BufferBranchIndexer<>(new ObjectTypeIndexer<>(), timestampIndexer);
		BufferBranchIndexer<DefaultData> platformIndexer = new BufferBranchIndexer<>(new PlatformIdentIndexer<>(), objectTypeIndexer);
		return new RootBranch<>(platformIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getObjectType() {
		return IBufferTreeComponent.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Root branch. It has additional functionality of generating IDs for the elements that need to
	 * be put into the indexing tree.
	 *
	 * @author Ivan Senic
	 *
	 */
	public static class RootBranch<E extends DefaultData> extends Branch<E> {

		/**
		 * Runnable for cutting the empty tree components.
		 */
		private Runnable clearEmptyComponentsRunnable = new Runnable() {

			@Override
			public void run() {
				RootBranch.this.clearEmptyComponents();
			}
		};

		/**
		 * Future that holds state of clear empty components runnable.
		 */
		private Future<?> clearEmptyComponentsFuture;

		/**
		 * Default constructor.
		 *
		 * @param branchIndexer
		 *            Branch indexer for root branch.
		 */
		public RootBranch(IBufferBranchIndexer<E> branchIndexer) {
			super(branchIndexer);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This method also sets the ID of the element that is put into the indexing tree.
		 */
		@Override
		public E put(E element) throws IndexingException {
			if (null == element) {
				throw new IndexingException("Null object can not be indexed.");
			}
			return super.put(element);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void cleanWithRunnable(ExecutorService executorService) {
			super.cleanWithRunnable(executorService);
			if ((clearEmptyComponentsFuture == null) || clearEmptyComponentsFuture.isDone()) {
				// Submit runnable only if the future is signaling that the last one was done.
				clearEmptyComponentsFuture = executorService.submit(clearEmptyComponentsRunnable);
			}
		}

	}

}
