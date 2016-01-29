package info.novatec.inspectit.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.LoggingData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

/**
 * Realization of the <code>IDetailsGenerator</code> for logging entries.
 * 
 * @author Stefan Siegl
 */
public class LoggingDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof LoggingData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		LoggingData loggingData = (LoggingData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Logging Info", 1);
		table.addContentRow("Level:", InspectIT.getDefault().getImage(InspectITImages.IMG_LOGGING_LEVEL), new DetailsCellContent[] { new DetailsCellContent(loggingData.getLevel()) });
		table.addContentRow("Message:", InspectIT.getDefault().getImage(InspectITImages.IMG_LOG), new DetailsCellContent[] { new DetailsCellContent(loggingData.getMessage()) });
		return table;
	}

}
