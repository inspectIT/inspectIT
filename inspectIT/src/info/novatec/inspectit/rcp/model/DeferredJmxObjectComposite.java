package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class DeferredJmxObjectComposite extends DeferredComposite {

	/**
	 * All the classes which are being displayed in the sub-tree.
	 */
	private List<JmxDefinitionDataIdent> childJmxData = new CopyOnWriteArrayList<JmxDefinitionDataIdent>();

	/**
	 * The repository definition.
	 */
	private RepositoryDefinition repositoryDefinition;

	/**
	 * This sensor type ident is used to create the nodes.
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

	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {

		try {
			Composite objectComposite = (Composite) object;
			monitor.beginTask("Loading monitored JMX-Attributes...", IProgressMonitor.UNKNOWN);

			for (JmxDefinitionDataIdent jmxIdent : childJmxData) {

				Component jmxAttribute = new Leaf();
				jmxAttribute.setName(jmxIdent.getmBeanAttributeName());
				jmxAttribute.setTooltip(jmxIdent.getmBeanAttributeDescription());
				jmxAttribute.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BLUE_DOCUMENT_TABLE));

				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.JMX_SENSOR_DATA);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setSensorImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BEAN));
				editorPropertiesData.setSensorName("JMX Data");

				editorPropertiesData.setViewName(TextFormatter.getJmxDefinitionString(jmxIdent));

				editorPropertiesData.setPartNameFlag(PartType.VIEW);
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				IdDefinition idDefinition = new IdDefinition();
				idDefinition.setPlatformId(jmxIdent.getPlatformIdent().getId());

				idDefinition.setSensorTypeId(sensorTypeIdent.getId());
				idDefinition.setJmxDefinitionId(jmxIdent.getId());

				inputDefinition.setIdDefinition(idDefinition);
				jmxAttribute.setInputDefinition(inputDefinition);

				collector.add(jmxAttribute, monitor);
				objectComposite.addChild(jmxAttribute);

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
		return InspectIT.getDefault().getImage(InspectITImages.IMG_BOOK);
	}

	/**
	 * Adds a {@link JmxDefinitionDataIdent} which will be displayed in this sub-tree.
	 * 
	 * @param jmxIdent
	 *            The {@link JmxDefinitionDataIdent} to be displayed.
	 */
	public void addJmxDataToDisplay(JmxDefinitionDataIdent jmxIdent) {
		childJmxData.add(jmxIdent);
	}
}
