package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class DeferredPackageComposite extends DeferredComposite {

	/**
	 * All the classes which are being displayed in the sub-tree.
	 */
	private List<MethodIdent> classes = new CopyOnWriteArrayList<MethodIdent>();

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
			Composite packageComposite = (Composite) object;
			monitor.beginTask("Loading of Class Elements...", IProgressMonitor.UNKNOWN);
			Map<String, DeferredClassComposite> classNames = new HashMap<String, DeferredClassComposite>(classes.size());

			for (MethodIdent clazz : classes) {
				if (select(clazz)) {
					String className = clazz.getClassName();
					if (!classNames.containsKey(className)) {
						DeferredClassComposite composite = getNewChild();
						composite.setRepositoryDefinition(repositoryDefinition);
						composite.setName(className);

						collector.add(composite, monitor);
						packageComposite.addChild(composite);
						classNames.put(className, composite);
					}

					DeferredClassComposite composite = classNames.get(className);
					composite.addMethodToDisplay(clazz);
					composite.setHideInactiveInstrumentations(hideInactiveInstrumentations);
				}
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
		if (CollectionUtils.isNotEmpty(classes)) {
			for (MethodIdent methodIdent : classes) {
				if (methodIdent.hasActiveSensorTypes()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return Returns the right implementation of the {@link DeferredClassComposite} to use for the
	 *         child.
	 */
	protected DeferredClassComposite getNewChild() {
		return new DeferredClassComposite();
	}

	/**
	 * Should this method ident pass the selection process.
	 * 
	 * @param methodIdent
	 *            {@link MethodIdent}.
	 * @return Should this method ident pass the selection process.
	 */
	protected boolean select(MethodIdent methodIdent) {
		return !hideInactiveInstrumentations || methodIdent.hasActiveSensorTypes();
	}

	/**
	 * Adds a class which will be displayed in this sub-tree.
	 * 
	 * @param methodIdent
	 *            The class to be displayed.
	 */
	public void addClassToDisplay(MethodIdent methodIdent) {
		classes.add(methodIdent);
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
		return InspectIT.getDefault().getImage(InspectITImages.IMG_PACKAGE);
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
		return Objects.hashCode(super.hashCode(), classes, repositoryDefinition);
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
		DeferredPackageComposite that = (DeferredPackageComposite) object;
		return Objects.equal(this.classes, that.classes) && Objects.equal(this.repositoryDefinition, that.repositoryDefinition);
	}

}
