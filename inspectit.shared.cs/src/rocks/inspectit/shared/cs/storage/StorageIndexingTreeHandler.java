package info.novatec.inspectit.storage;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.StorageWriter.WriteTask;
import info.novatec.inspectit.storage.util.StorageIndexTreeProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class provides a layer of abstraction between {@link StorageWriter} and
 * {@link IStorageTreeComponent}.
 * <p>
 * The class is responsible for correct handling of the indexing trees and their saving. The storage
 * writer will use this component to get the channel where the write should be executed, and also
 * signal if the write was successful or not, as well as to signal when is the write completely
 * finished.
 * <p>
 * The class will cache the data that is currently in write with the information to which indexing
 * tree it is going and which descriptor was assigned to the data in write. Because of this for each
 * write there is a put and remove from a {@link HashMap} as an overhead, but since the size of the
 * map is constant (data currently in write can not be greater than the number of threads writing
 * the data, there should not be any serious performance problems.
 * 
 * @author Ivan Senic
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class StorageIndexingTreeHandler {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * Time to wait in milliseconds for all operations on indexing tree to be finished, so that tree
	 * can be saved.
	 */
	private static final long WAITING_FOR_TREE_TO_BE_READY = 1000;

	/**
	 * Number of sleeps that will be executed at the {@link #finish()} method. This actually means
	 * that the {@link #finish()} method will maximally wait for
	 * {@value #WAITING_FOR_TREE_TO_BE_READY} * {@value #FINISH_WAITING_ITERATIONS} milliseconds.
	 */
	private static final int FINISH_WAITING_ITERATIONS = 30;

	/**
	 * Delay of rescheduling check tree size task.
	 */
	private static final long TREE_CHECK_DELAY = 30;

	/**
	 * {@link TimeUnit} of the delay of rescheduling check tree size task.
	 */
	private static final TimeUnit TREE_CHECK_DELAY_TIME_UNIT = TimeUnit.SECONDS;

	/**
	 * {@link StorageWriter}.
	 */
	private StorageWriter storageWriter;

	/**
	 * {@link StorageIndexTreeProvider}.
	 */
	@Autowired
	StorageIndexTreeProvider<DefaultData> storageIndexTreeProvider;

	/**
	 * {@link ExecutorService} for tasks of the tree handling.
	 */
	@Autowired
	@Resource(name = "scheduledExecutorService")
	ScheduledExecutorService executorService;

	/**
	 * Indexing tree of the Storage.
	 */
	private AtomicReference<IStorageTreeComponent<DefaultData>> storageIndexingTreeReference;

	/**
	 * Write tasks currently in process.
	 */
	private Map<WriteTask, TreeDescriptorPair> writeTasksInProcess = new ConcurrentHashMap<WriteTask, TreeDescriptorPair>(16, 0.75f, 2);

	/**
	 * Object size for indexing tree size calculation.
	 */
	@Autowired
	IObjectSizes objectSizes;

	/**
	 * Max size of indexing tree after which a new tree is needed, and the old one is saved.
	 */
	@Value(value = "${storage.maximumIndexingTreeSize}")
	long maximumIndexingTreeSize;

	/**
	 * Future for the task of saving the indexing tree.
	 */
	private volatile ScheduledFuture<?> indexingTreeSavingFuture;

	/**
	 * Prepares for write by creating the new indexing tree. This method must be called before
	 * asking for the position of the data to be written to.
	 */
	public void prepare() {
		storageIndexingTreeReference = new AtomicReference<IStorageTreeComponent<DefaultData>>(getNewStorageIndexingTree());
		indexingTreeSavingFuture = executorService.scheduleWithFixedDelay(new IndexingTreeSavingTask(), TREE_CHECK_DELAY, TREE_CHECK_DELAY, TREE_CHECK_DELAY_TIME_UNIT);
	}

	/**
	 * Returns the channel ID where the data should be written.
	 * <p>
	 * Internally this method saves the {@link TreeDescriptorPair} for the given task, so when the
	 * write is done the descriptor can be updated with correct write information.
	 * 
	 * @param writeTask
	 *            Write task that starts the write.
	 * @return Returns the channel ID where the data should be written.
	 * @throws IndexingException
	 *             If indexing fails.
	 */
	public int startWrite(WriteTask writeTask) throws IndexingException {
		DefaultData data = writeTask.getData();
		if (null == data) {
			throw new IndexingException("Indexing failed. Data to index was null.");
		}

		TreeDescriptorPair treeDescriptorPair = new TreeDescriptorPair();
		// save tree-descriptor pair into the map that holds data that is written
		writeTasksInProcess.put(writeTask, treeDescriptorPair);

		// get the descriptor from tree
		IStorageTreeComponent<DefaultData> indexingTree = storageIndexingTreeReference.get();
		IStorageDescriptor storageDescriptor = indexingTree.put(data);
		if (null == storageDescriptor) {
			throw new IndexingException("Indexing failed. Storage descriptor was null.");
		}

		// update the tree-descriptor pair
		treeDescriptorPair.setIndexingTree(indexingTree);
		treeDescriptorPair.setStorageDescriptor(storageDescriptor);

		return storageDescriptor.getChannelId();
	}

	/**
	 * Signals to the {@link StorageIndexingTreeHandler} that the write has been successful with
	 * correct information about write position and size.
	 * <p>
	 * Internally this method will update the {@link IStorageDescriptor} for the given
	 * {@link DefaultData} object in the write task, and remove the task from the set of tasks being
	 * currently processed.
	 * 
	 * @param writeTask
	 *            Write task that succeeded.
	 * @param position
	 *            Write position.
	 * @param size
	 *            Write size.
	 */
	public void writeSuccessful(WriteTask writeTask, long position, long size) {
		// get the data from the map
		TreeDescriptorPair treeDescriptorPair = writeTasksInProcess.get(writeTask);
		if (null != treeDescriptorPair) {
			IStorageDescriptor storageDescriptor = treeDescriptorPair.getStorageDescriptor();
			// update the descriptor with the information provided
			if (null != storageDescriptor) {
				storageDescriptor.setPositionAndSize(position, size);
			}
		}
		// remove the entry in map after the data has been updated in indexing tree
		writeTasksInProcess.remove(writeTask);
	}

	/**
	 * Signals to the {@link StorageIndexingTreeHandler} that the write has failed.
	 * <p>
	 * Internally this method will update the {@link IStorageTreeComponent} by removing the given
	 * {@link DefaultData} object in the write task, and remove the task from the set of tasks being
	 * currently processed.
	 * 
	 * @param writeTask
	 *            Write task that failed.
	 */
	public void writeFailed(WriteTask writeTask) {
		// get the data from the map
		TreeDescriptorPair treeDescriptorPair = writeTasksInProcess.get(writeTask);
		if (null != treeDescriptorPair) {
			IStorageTreeComponent<DefaultData> indexingTree = treeDescriptorPair.getIndexingTree();
			// if write fails, remove the descriptor for the data from indexing tree
			if (null != indexingTree) {
				indexingTree.getAndRemove(writeTask.getData());
			}
		}
		// remove the entry in map after the indexing tree was informed
		writeTasksInProcess.remove(writeTask);
	}

	/**
	 * Cancels the {@link #indexingTreeSavingFuture}.
	 */
	public void cancelIndexingTreeSavingFuture() {
		if (!indexingTreeSavingFuture.isDone() && !indexingTreeSavingFuture.isCancelled()) {
			indexingTreeSavingFuture.cancel(false);
		}
	}

	/**
	 * Signals to the {@link StorageIndexingTreeHandler} that the write is finished and current tree
	 * should be saved.
	 */
	public void finish() {
		cancelIndexingTreeSavingFuture();

		IStorageTreeComponent<DefaultData> currentIndexingTree = null;
		while (true) {
			// try to set the indexing tree to null
			currentIndexingTree = storageIndexingTreeReference.get();
			if (storageIndexingTreeReference.compareAndSet(currentIndexingTree, null)) {
				break;
			}
		}

		if (null != currentIndexingTree) {
			// wait until no more data is there
			int sleepCount = 0;
			while (!writeTasksInProcess.isEmpty()) {
				log.info("Indexing tree handler still waiting for " + writeTasksInProcess.size() + " task(s) to be finished. Going for sleep " + (sleepCount + 1) + " out of "
						+ FINISH_WAITING_ITERATIONS + ".");
				if (sleepCount > FINISH_WAITING_ITERATIONS) {
					log.warn("Indexing tree handler waited " + (sleepCount * WAITING_FOR_TREE_TO_BE_READY) + " milliseconds for all tasks to be finished. There are " + writeTasksInProcess.size()
							+ " tasks still in-progress. Saving of the indexing tree will continue without waiting for these tasks.");
					break;
				}
				try {
					Thread.sleep(WAITING_FOR_TREE_TO_BE_READY);
					sleepCount++;
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
			currentIndexingTree.preWriteFinalization();
			boolean written = storageWriter.writeNonDefaultDataObject(currentIndexingTree, getRandomFileName() + StorageFileType.INDEX_FILE.getExtension());
			if (!written) {
				log.error("Indexing tree saving failed. Indexing tree might be lost.");
			}
		}
	}

	/**
	 * Returns amount of write tasks in progress.
	 * 
	 * @return Returns amount of write tasks in progress.
	 */
	int getWriteTaskInProgressCount() {
		return writeTasksInProcess.size();
	}

	/**
	 * Returns random file name.
	 * 
	 * @return Returns random file name.
	 * 
	 */
	private String getRandomFileName() {
		return UUID.randomUUID().toString();
	}

	/**
	 * This task periodically checks if the size of the indexing tree is bigger that the
	 * {@link #maximumIndexingTreeSize}, and if it is provides new indexing tree for the other
	 * tasks, and safely saves the old tree.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	class IndexingTreeSavingTask implements Runnable {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			// the complete run block has to be guarded against exceptions, because the executor
			// service will throw away any rescheduling of the task if exception is thrown
			try {
				while (true) {
					final IStorageTreeComponent<DefaultData> currentIndexingTree = storageIndexingTreeReference.get();
					if (null != currentIndexingTree) {
						long treeSize = currentIndexingTree.getComponentSize(objectSizes);
						// check if the tree has grown enough for saving
						if (treeSize > maximumIndexingTreeSize) {
							IStorageTreeComponent<DefaultData> newIndexingTree = getNewStorageIndexingTree();
							// put new fresh tree to the Atomic reference
							if (storageIndexingTreeReference.compareAndSet(currentIndexingTree, newIndexingTree)) {
								// collect the information about tasks currently in write
								final Collection<WriteTask> writeTasksToWait = new HashSet<StorageWriter.WriteTask>(writeTasksInProcess.keySet());
								// here we are safe to know that when all of the tasks in the
								// collection is gone from the tasks in process map, we can save the
								// tree
								Runnable writeOldIndexingTree = new Runnable() {
									@Override
									public void run() {
										boolean safeToSave = Collections.disjoint(writeTasksToWait, writeTasksInProcess.keySet());
										if (safeToSave) {
											currentIndexingTree.preWriteFinalization();
											boolean written = storageWriter.writeNonDefaultDataObject(currentIndexingTree, getRandomFileName() + StorageFileType.INDEX_FILE.getExtension());
											if (!written) {
												log.error("Indexing tree saving failed. Indexing tree might be lost.");
											}
										} else {
											executorService.schedule(this, WAITING_FOR_TREE_TO_BE_READY, TimeUnit.MILLISECONDS);
										}
									}
								};
								executorService.submit(writeOldIndexingTree);
								break;
							}
						} else {
							break;
						}
					} else {
						break;
					}
				}
			} catch (Exception e) {
				log.error("Indexing tree saving task encountered an error.", e);
			}
		}
	}

	/**
	 * 
	 * @return Returns new empty storage indexing tree.
	 */
	private IStorageTreeComponent<DefaultData> getNewStorageIndexingTree() {
		return storageIndexTreeProvider.getStorageIndexingTree();
	}

	/**
	 * Registers the {@link StorageWriter} to work with.
	 * 
	 * @param storageWriter
	 *            {@link StorageWriter}.
	 */
	public void registerStorageWriter(StorageWriter storageWriter) {
		this.storageWriter = storageWriter;
	}

	/**
	 * Utility class that holds a pair of {@link IStorageDescriptor} and
	 * {@link IStorageTreeComponent}.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static final class TreeDescriptorPair {

		/**
		 * {@link IStorageDescriptor}.
		 */
		private IStorageDescriptor storageDescriptor;

		/**
		 * {@link IStorageTreeComponent}.
		 */
		private IStorageTreeComponent<DefaultData> indexingTree;

		/**
		 * @return the storageDescriptor
		 */
		public IStorageDescriptor getStorageDescriptor() {
			return storageDescriptor;
		}

		/**
		 * @param storageDescriptor
		 *            the storageDescriptor to set
		 */
		public void setStorageDescriptor(IStorageDescriptor storageDescriptor) {
			this.storageDescriptor = storageDescriptor;
		}

		/**
		 * @return the indexingTree
		 */
		public IStorageTreeComponent<DefaultData> getIndexingTree() {
			return indexingTree;
		}

		/**
		 * @param indexingTree
		 *            the indexingTree to set
		 */
		public void setIndexingTree(IStorageTreeComponent<DefaultData> indexingTree) {
			this.indexingTree = indexingTree;
		}

	}

}
