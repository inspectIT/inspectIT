package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.IElementCollector;

/**
 * This class only initializes the sub-tree if it is requested. Furthermore, the creation of the
 * objects is done piece after piece, so that an immediate visualization can be seen (important for
 * sub-trees which are very large).
 * 
 * @author Marius Oehler
 * 
 */
public class DeferredJmxBrowserComposite extends DeferredComposite {

	/**
	 * The platform ident is used to create the package elements in the sub-tree.
	 */
	private PlatformIdent platformIdent;

	/**
	 * This sensor type ident is used to create the sub-tree.
	 */
	private SensorTypeIdent sensorTypeIdent;

	/**
	 * Sets {@link #sensorTypeIdent}.
	 * 
	 * @param sensorTypeIdent
	 *            New value for {@link #sensorTypeIdent}
	 */
	public void setSensorTypeIdent(SensorTypeIdent sensorTypeIdent) {
		this.sensorTypeIdent = sensorTypeIdent;
	}

	/**
	 * The platform ident used to retrieve the monitored MBeans.
	 * 
	 * @param platformIdent
	 *            the platformIdent to set
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			Composite browserComposite = (Composite) object;
			Set<JmxDefinitionDataIdent> jmxIdents = platformIdent.getJmxDefinitionDataIdents();
			monitor.beginTask("Loading monitored JMX-Packages...", IProgressMonitor.UNKNOWN);

			Map<String, DeferredJmxPackageComposite> packageNames = new HashMap<String, DeferredJmxPackageComposite>(jmxIdents.size());

			for (JmxDefinitionDataIdent jmxIdent : jmxIdents) {
				String packageName = jmxIdent.getDerivedDomainName();

				// check if the given package was already added.
				if (!packageNames.containsKey(packageName)) {
					DeferredJmxPackageComposite composite = getNewChild();
					composite.setRepositoryDefinition(repositoryDefinition);
					composite.setName(packageName);

					collector.add(composite, monitor);
					browserComposite.addChild(composite);
					packageNames.put(packageName, composite);
				}

				DeferredJmxPackageComposite composite = packageNames.get(packageName);
				composite.setSensorTypeIdent(sensorTypeIdent);
				composite.addJmxDataToDisplay(jmxIdent);

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
	 * @return Returns the right implementation of the {@link DeferredJmxPackageComposite} to use
	 *         for the child.
	 */
	protected DeferredJmxPackageComposite getNewChild() {
		return new DeferredJmxPackageComposite();
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
		return InspectIT.getDefault().getImage(InspectITImages.IMG_INSTRUMENTATION_BROWSER);
	}
}
