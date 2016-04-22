package rocks.inspectit.ui.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

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
