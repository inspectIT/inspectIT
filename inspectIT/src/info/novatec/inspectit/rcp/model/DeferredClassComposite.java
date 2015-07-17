package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.IElementCollector;

import com.google.common.base.Objects;

/**
 * This class only initializes the sub-tree if it is requested. Furthermore, the creation of the
 * objects is done piece after piece, so that an immediate visualization can be seen (important for
 * sub-trees which are very large).
 * 
 * @author Patrice Bouillet
 * 
 */
public class DeferredClassComposite extends DeferredComposite {

	/**
	 * All the methods which are displayed in the tree.
	 */
	private List<MethodIdent> methods = new CopyOnWriteArrayList<MethodIdent>();

	/**
	 * The format string of the output.
	 */
	protected static final String METHOD_FORMAT = "%s(%s)";

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * If inactive instrumentations should be hidden.
	 */
	private boolean hideInactiveInstrumentations;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			Composite classComposite = (Composite) object;
			monitor.beginTask("Loading of Method Elements...", methods.size());

			for (MethodIdent method : methods) {
				if (select(method)) {
					DeferredMethodComposite composite = new DeferredMethodComposite();
					composite.setRepositoryDefinition(repositoryDefinition);

					if (null != method.getParameters()) {
						String parameters = method.getParameters().toString();
						parameters = parameters.substring(1, parameters.length() - 1);

						composite.setName(String.format(METHOD_FORMAT, method.getMethodName(), parameters));
					} else {
						composite.setName(String.format(METHOD_FORMAT, method.getMethodName(), ""));
					}
					composite.setMethod(method);
					composite.setHideInactiveInstrumentations(hideInactiveInstrumentations);

					collector.add(composite, monitor);
					classComposite.addChild(composite);
				}

				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			collector.done();
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		if (CollectionUtils.isNotEmpty(methods)) {
			for (MethodIdent methodIdent : methods) {
				if (methodIdent.hasActiveSensorTypes()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean select(MethodIdent methodIdent) {
		return !hideInactiveInstrumentations || methodIdent.hasActiveSensorTypes();
	}

	/**
	 * Adds a method to be displayed later in this sub-tree.
	 * 
	 * @param methodIdent
	 *            The method to be displayed.
	 */
	public void addMethodToDisplay(MethodIdent methodIdent) {
		methods.add(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepositoryDefinition getRepositoryDefinition() {
		return repositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getImage() {
		return InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS);
	}

	/**
	 * @return the methods
	 */
	protected List<MethodIdent> getMethods() {
		return methods;
	}

	/**
	 * Gets {@link #hideInactiveInstrumentations}.
	 * 
	 * @return {@link #hideInactiveInstrumentations}
	 */
	public boolean isHideInactiveInstrumentations() {
		return hideInactiveInstrumentations;
	}

	/**
	 * Sets {@link #hideInactiveInstrumentations}.
	 * 
	 * @param hideInactiveInstrumentations
	 *            New value for {@link #hideInactiveInstrumentations}
	 */
	public void setHideInactiveInstrumentations(boolean hideInactiveInstrumentations) {
		this.hideInactiveInstrumentations = hideInactiveInstrumentations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), methods, repositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		DeferredClassComposite that = (DeferredClassComposite) object;
		return Objects.equal(this.methods, that.methods) && Objects.equal(this.repositoryDefinition, that.repositoryDefinition);
	}

}
