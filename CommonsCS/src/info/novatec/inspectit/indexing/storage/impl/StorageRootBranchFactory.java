package info.novatec.inspectit.indexing.storage.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.indexer.impl.InvocationChildrenIndexer;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SqlStringIndexer;
import info.novatec.inspectit.indexing.indexer.impl.TimestampIndexer;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

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
	public IStorageTreeComponent<DefaultData> getObject() throws Exception {
		// the time-stamp indexer has to be the last indexer in the tree, so that the amount of
		// files in storage won't grow with the time passing by
		StorageBranchIndexer<DefaultData> timestampIndexer = new StorageBranchIndexer<DefaultData>(new TimestampIndexer<DefaultData>(), true);
		StorageBranchIndexer<DefaultData> sqlStringIndexer = new StorageBranchIndexer<DefaultData>(new SqlStringIndexer<DefaultData>(10), timestampIndexer, false);
		StorageBranchIndexer<DefaultData> methodIdentIndexer = new StorageBranchIndexer<DefaultData>(new MethodIdentIndexer<DefaultData>(), sqlStringIndexer, false);
		StorageBranchIndexer<DefaultData> objectTypeIndexer = new StorageBranchIndexer<DefaultData>(new ObjectTypeIndexer<DefaultData>(), methodIdentIndexer, false);
		StorageBranchIndexer<DefaultData> invocationChildrenIndexer = new StorageBranchIndexer<DefaultData>(new InvocationChildrenIndexer<DefaultData>(), objectTypeIndexer, false);
		StorageBranchIndexer<DefaultData> platformIndexer = new StorageBranchIndexer<DefaultData>(new PlatformIdentIndexer<DefaultData>(), invocationChildrenIndexer, false);
		return new StorageBranch<DefaultData>(platformIndexer);
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getObjectType() {
		return IStorageTreeComponent.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSingleton() {
		return false;
	}

}
