package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * HTTP details generator. Displays information like URI, request method, parameters, attributes,
 * etc.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof HttpTimerData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		HttpTimerData httpTimerData = (HttpTimerData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "HTTP Info", 1);

		table.addContentRow("Method:", null, new DetailsCellContent[] { new DetailsCellContent(httpTimerData.getRequestMethod()) });
		table.addContentRow("URI:", null, new DetailsCellContent[] { new DetailsCellContent(httpTimerData.getUri()) });

		if (httpTimerData.hasInspectItTaggingHeader()) {
			table.addContentRow("Tag Value:", null, new DetailsCellContent[] { new DetailsCellContent(httpTimerData.getInspectItTaggingHeaderValue()) });
		}

		// parameters
		if (MapUtils.isNotEmpty(httpTimerData.getParameters())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String[]> entry : httpTimerData.getParameters().entrySet()) {
				rows.add(new String[] { entry.getKey(), Arrays.toString(entry.getValue()) });
			}
			table.addContentTable("Parameters:", null, 2, new String[] { "Parameter", "Value" }, rows);
		}

		// attributes
		if (MapUtils.isNotEmpty(httpTimerData.getAttributes())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String> entry : httpTimerData.getAttributes().entrySet()) {
				rows.add(new String[] { entry.getKey(), entry.getValue() });
			}
			table.addContentTable("Attributes:", null, 2, new String[] { "Attribute", "Value" }, rows);
		}

		// headers
		if (MapUtils.isNotEmpty(httpTimerData.getHeaders())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String> entry : httpTimerData.getHeaders().entrySet()) {
				rows.add(new String[] { entry.getKey(), entry.getValue() });
			}
			table.addContentTable("Headers:", null, 2, new String[] { "Header", "Value" }, rows);
		}

		// session attributes
		if (MapUtils.isNotEmpty(httpTimerData.getSessionAttributes())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String> entry : httpTimerData.getSessionAttributes().entrySet()) {
				rows.add(new String[] { entry.getKey(), entry.getValue() });
			}
			table.addContentTable("Session Attributes:", null, 2, new String[] { "Session Attribute", "Value" }, rows);
		}

		return table;
	}
}
