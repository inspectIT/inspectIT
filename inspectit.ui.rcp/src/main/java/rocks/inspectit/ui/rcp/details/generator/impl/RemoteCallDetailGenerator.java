package rocks.inspectit.ui.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Remote Call details generator. Displays information like request/response, URL, response code,
 * etc.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteCallDetailGenerator implements IDetailsGenerator {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof RemoteCallData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		RemoteCallData remoteCallData = (RemoteCallData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Remote Call Info", 1);

		table.addContentRow("Kind:", null, new DetailsCellContent[] { new DetailsCellContent(remoteCallData.isCalling() ? "Request" : "Response") });
		if (remoteCallData.isCalling()) {
			table.addContentRow("Specific Data:", null, new DetailsCellContent[] { new DetailsCellContent(remoteCallData.getSpecificData()) });
		}

		return table;
	}

}
