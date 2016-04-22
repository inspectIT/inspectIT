package rocks.inspectit.ui.rcp.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Abstract class of a deferred composite type where the sub tree is only initialized if it is
 * requested.
 *
 * @author Patrice Bouillet
 *
 */
public abstract class DeferredComposite extends Composite implements IDeferredWorkbenchAdapter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor);

	/**
	 * Sets the repository definition.
	 *
	 * @param repositoryDefinition
	 *            the repository definition.
	 */
	public abstract void setRepositoryDefinition(RepositoryDefinition repositoryDefinition);

	/**
	 * Returns the repository definition.
	 *
	 * @return the repository definition.
	 */
	public abstract RepositoryDefinition getRepositoryDefinition();

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
	public Object[] getChildren(Object object) {
		return super.getChildren().toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object object) {
		return super.getParent();
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
	public String getLabel(Object object) {
		return super.getName();
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
	public String toString() {
		return getName();
	}

}
