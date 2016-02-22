package rocks.inspectit.ui.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.LoggingData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

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
		table.addContentRow("Message:", InspectIT.getDefault().getImage(InspectITImages.IMG_LOGGING_MESSAGE), new DetailsCellContent[] { new DetailsCellContent(loggingData.getMessage()) });
		return table;
	}

}
