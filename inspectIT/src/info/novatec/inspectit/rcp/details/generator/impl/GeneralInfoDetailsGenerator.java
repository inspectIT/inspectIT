package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The general info information. Currently only displaying the time-stamp.
 * 
 * @author Ivan Senic
 * 
 */
public class GeneralInfoDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return null != defaultData.getTimeStamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		DetailsTable table = new DetailsTable(parent, toolkit, "General Info", 1);

		// time stamp
		if (null != defaultData.getTimeStamp()) {
			table.addContentRow("Timestamp:", InspectIT.getDefault().getImage(InspectITImages.IMG_CALENDAR),
					new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatTime(defaultData.getTimeStamp().getTime())) });
		}

		return table;
	}

}
