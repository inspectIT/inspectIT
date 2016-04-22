package rocks.inspectit.shared.cs.indexing.storage.impl;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.cs.indexing.indexer.impl.InvocationChildrenIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.MethodIdentIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.ObjectTypeIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.PlatformIdentIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.SqlStringIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.impl.TimestampIndexer;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;

/**
 * Factory for producing {@link IStorageTreeComponent}.
 *
 * @author Ivan Senic
 *
 */
@Component("storageRootBranchFactory")
public class StorageRootBranchFactory implements FactoryBean<IStorageTreeComponent<DefaultData>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IStorageTreeComponent<DefaultData> getObject() throws Exception {
		// the time-stamp indexer has to be the last indexer in the tree, so that the amount of
		// files in storage won't grow with the time passing by
		StorageBranchIndexer<DefaultData> timestampIndexer = new StorageBranchIndexer<>(new TimestampIndexer<>(), true);
		StorageBranchIndexer<DefaultData> sqlStringIndexer = new StorageBranchIndexer<>(new SqlStringIndexer<>(10), timestampIndexer, false);
		StorageBranchIndexer<DefaultData> methodIdentIndexer = new StorageBranchIndexer<>(new MethodIdentIndexer<>(), sqlStringIndexer, false);
		StorageBranchIndexer<DefaultData> objectTypeIndexer = new StorageBranchIndexer<>(new ObjectTypeIndexer<>(), methodIdentIndexer, false);
		StorageBranchIndexer<DefaultData> invocationChildrenIndexer = new StorageBranchIndexer<>(new InvocationChildrenIndexer<>(), objectTypeIndexer, false);
		StorageBranchIndexer<DefaultData> platformIndexer = new StorageBranchIndexer<>(new PlatformIdentIndexer<>(), invocationChildrenIndexer, false);
		return new StorageBranch<>(platformIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getObjectType() {
		return IStorageTreeComponent.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return false;
	}

}
