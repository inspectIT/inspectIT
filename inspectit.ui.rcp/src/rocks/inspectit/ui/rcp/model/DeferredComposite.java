package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

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
	public boolean isContainer() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object object) {
		return super.getChildren().toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object object) {
		return super.getParent();
	}

	/**
	 * {@inheritDoc}
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel(Object object) {
		return super.getName();
	}

	/**
	 * {@inheritDoc}
	 */
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
