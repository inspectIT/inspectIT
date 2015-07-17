package info.novatec.inspectit.indexing.buffer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.AbstractBranch;
import info.novatec.inspectit.indexing.ITreeComponent;
import info.novatec.inspectit.indexing.buffer.IBufferBranchIndexer;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

/**
 * {@link Branch} is a {@link ITreeComponent} that holds references to other {@link ITreeComponent}
 * s, which are actually branch children.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Element type that the branch can index (and hold).
 */
public class Branch<E extends DefaultData> extends AbstractBranch<E, E> implements IBufferTreeComponent<E> {

	/**
	 * Buffer branch indexer.
	 */
	private IBufferBranchIndexer<E> bufferBranchIndexer;

	/**
	 * Default constructor.
	 * 
	 * @param bufferBranchIndexer
	 *            Indexer.
	 */
	public Branch(IBufferBranchIndexer<E> bufferBranchIndexer) {
		super(bufferBranchIndexer);
		this.bufferBranchIndexer = bufferBranchIndexer;
	}

	/**
	 * {@inheritDoc}
	 */
	protected ITreeComponent<E, E> getNextTreeComponent(E element) {
		return bufferBranchIndexer.getNextTreeComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanWithRunnable(ExecutorService executorService) {
		for (Entry<Object, ITreeComponent<E, E>> entry : getComponentMap().entrySet()) {
			if (entry.getValue() instanceof IBufferTreeComponent) {
				((IBufferTreeComponent<E>) entry.getValue()).cleanWithRunnable(executorService);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean clean() {
		ArrayList<Object> keysToRemove = new ArrayList<Object>();
		for (Entry<Object, ITreeComponent<E, E>> entry : getComponentMap().entrySet()) {
			if (entry.getValue() instanceof IBufferTreeComponent) {
				boolean toClear = ((IBufferTreeComponent<E>) entry.getValue()).clean();
				if (toClear) {
					keysToRemove.add(entry.getKey());
				}
			}
		}
		for (Object key : keysToRemove) {
			getComponentMap().remove(key);
		}

		if (getComponentMap().isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean clearEmptyComponents() {
		ArrayList<Object> keysToRemove = new ArrayList<Object>();
		for (Entry<Object, ITreeComponent<E, E>> entry : getComponentMap().entrySet()) {
			if (entry.getValue() instanceof IBufferTreeComponent) {
				boolean toClear = ((IBufferTreeComponent<E>) entry.getValue()).clearEmptyComponents();
				if (toClear) {
					keysToRemove.add(entry.getKey());
				}
			}
		}
		for (Object key : keysToRemove) {
			getComponentMap().remove(key);
		}

		if (getComponentMap().isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getNumberOfElements() {
		long sum = 0;
		for (ITreeComponent<E, E> treeComponent : getComponentMap().values()) {
			if (treeComponent instanceof IBufferTreeComponent) {
				sum += ((IBufferTreeComponent<E>) treeComponent).getNumberOfElements();
			}
		}
		return sum;
	}

	/**
	 * @return the bufferBranchIndexer
	 */
	public IBufferBranchIndexer<E> getBufferBranchIndexer() {
		return bufferBranchIndexer;
	}

}
