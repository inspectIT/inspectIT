package rocks.inspectit.ui.rcp.editor.tree.input;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * This class is used to create the UI elements of tree-based views only if the parent element is
 * said to be opened.
 *
 * @author Patrice Bouillet
 *
 */
public class DeferredInvoc implements IDeferredWorkbenchAdapter {

	/**
	 * Defines the items per loop.
	 */
	public static final int ITEMS_PER_LOOP = 50;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		InvocationSequenceData parentData = (InvocationSequenceData) object;
		List<InvocationSequenceData> nestedSequences = parentData.getNestedSequences();
		monitor.beginTask("Loading of Invocation Sequence Data Objects...", nestedSequences.size());

		for (int i = 0; i < nestedSequences.size(); i = i + ITEMS_PER_LOOP) {
			List<InvocationSequenceData> subList;
			if ((i + ITEMS_PER_LOOP) > nestedSequences.size()) {
				subList = nestedSequences.subList(i, nestedSequences.size());
			} else {
				subList = nestedSequences.subList(i, i + ITEMS_PER_LOOP);
			}
			monitor.worked(subList.size());

			collector.add(subList.toArray(), monitor);

			if (monitor.isCanceled()) {
				break;
			}
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISchedulingRule getRule(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isContainer() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getChildren(Object o) {
		InvocationSequenceData invocationSequenceData = (InvocationSequenceData) o;
		List<InvocationSequenceData> nestedSequences = invocationSequenceData.getNestedSequences();

		return nestedSequences.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel(Object o) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object o) {
		InvocationSequenceData invocationSequenceData = (InvocationSequenceData) o;
		return invocationSequenceData.getParentSequence();
	}

}
