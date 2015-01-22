package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

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
			table.addContentRow("URL:", null, new DetailsCellContent[] { new DetailsCellContent(remoteCallData.getUrl()) });
			table.addContentRow("Responce Code:", null, new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatInteger(remoteCallData.getResponseCode())) });
		}

		return table;
	}

}
