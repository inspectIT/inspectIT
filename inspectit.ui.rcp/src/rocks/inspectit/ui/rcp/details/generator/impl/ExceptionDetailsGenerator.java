package rocks.inspectit.ui.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Generator for the exception details.
 * 
 * @author Ivan Senic
 * 
 */
public class ExceptionDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof ExceptionSensorData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		ExceptionSensorData exceptionSensorData = (ExceptionSensorData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Exception Info", 1);

		table.addContentRow("Type:", null, new DetailsCellContent[] { new DetailsCellContent(exceptionSensorData.getThrowableType()) });

		if (null != exceptionSensorData.getExceptionEvent()) {
			table.addContentRow("Event:", null, new DetailsCellContent[] { new DetailsCellContent(exceptionSensorData.getExceptionEvent().toString()) });
		}

		if (null != exceptionSensorData.getErrorMessage()) {
			table.addContentRow("Error Message:", null, new DetailsCellContent[] { new DetailsCellContent(exceptionSensorData.getErrorMessage()) });
		}

		if (null != exceptionSensorData.getCause()) {
			table.addContentRow("Cause:", null, new DetailsCellContent[] { new DetailsCellContent(exceptionSensorData.getCause()) });
		}

		if (null != exceptionSensorData.getStackTrace()) {
			table.addContentRow("Stack Trace:", null, new DetailsCellContent[] { new DetailsCellContent(exceptionSensorData.getStackTrace()) });
		}

		return table;
	}

}
