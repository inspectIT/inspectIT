package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.YesNoDetailsCellContent;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * JMX information details generator.
 * 
 * @author Marius Oehler
 *
 */
public class JmxDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof JmxSensorValueData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		JmxSensorValueData jmxSensorData = (JmxSensorValueData) defaultData;
		JmxDefinitionDataIdent jmxData = repositoryDefinition.getCachedDataService().getJmxDefinitionDataIdentForId(jmxSensorData.getJmxSensorDefinitionDataIdentId());

		DetailsTable table = new DetailsTable(parent, toolkit, "MBean Details", 1);

		table.addContentRow("Domain:", InspectIT.getDefault().getImage(InspectITImages.IMG_PACKAGE), new DetailsCellContent[] { new DetailsCellContent(jmxData.getDerivedDomainName()) });
		table.addContentRow("Type:", InspectIT.getDefault().getImage(InspectITImages.IMG_BOOK), new DetailsCellContent[] { new DetailsCellContent(jmxData.getDerivedTypeName()) });
		table.addContentRow("Attribute:", InspectIT.getDefault().getImage(InspectITImages.IMG_BLUE_DOCUMENT_TABLE),
				new DetailsCellContent[] { new DetailsCellContent(jmxData.getmBeanAttributeName()) });
		table.addContentRow("Description:", InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION),
				new DetailsCellContent[] { new DetailsCellContent(jmxData.getmBeanAttributeDescription()) });
		table.addContentRow("Type:", null, new DetailsCellContent[] { new DetailsCellContent(jmxData.getmBeanAttributeType()) });
		table.addContentRow("Value:", null, new DetailsCellContent[] { new DetailsCellContent(jmxSensorData.getValue()) });

		table.addContentRow("Readable:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(jmxData.getmBeanAttributeIsReadable()) });
		table.addContentRow("Writeable:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(jmxData.getmBeanAttributeIsWritable()) });
		table.addContentRow("Is-Getter:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(jmxData.getmBeanAttributeIsIs()) });

		return table;
	}
}
