package rocks.inspectit.ui.rcp.details.generator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.springframework.http.HttpStatus;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.HttpTimerDataHelper;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * HTTP details generator. Displays information like URI, request method, parameters, attributes,
 * etc.
 *
 * @author Ivan Senic
 *
 */
public class HttpDetailsGenerator implements IDetailsGenerator {

	/**
	 * Comparator for the rows displaying the HTTP parameters, attributes, etc.
	 */
	private static final Comparator<String[]> ROW_COMPARATOR = new Comparator<String[]>() {
		@Override
		public int compare(String[] o1, String[] o2) {
			return ObjectUtils.compare(o1[0], o2[0]);
		}
	};

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

		table.addContentRow("Method:", null, new DetailsCellContent[] { new DetailsCellContent(httpTimerData.getHttpInfo().getRequestMethod()) });
		table.addContentRow("URI:", null, new DetailsCellContent[] { new DetailsCellContent(httpTimerData.getHttpInfo().getUri()) });

		if (HttpTimerDataHelper.hasResponseCode(httpTimerData)) {
			String statusString;
			try {
				HttpStatus status = HttpStatus.valueOf(httpTimerData.getHttpResponseStatus());
				statusString = httpTimerData.getHttpResponseStatus() + " " + status.getReasonPhrase();
			} catch (IllegalArgumentException e) {
				// non standard response code
				statusString = String.valueOf(httpTimerData.getHttpResponseStatus());
			}
			table.addContentRow("Response Status:", null, new DetailsCellContent[] { new DetailsCellContent(statusString) });
		}

		if (httpTimerData.getHttpInfo().hasInspectItTaggingHeader()) {
			table.addContentRow("Tag Value:", null, new DetailsCellContent[] { new DetailsCellContent(httpTimerData.getHttpInfo().getInspectItTaggingHeaderValue()) });
		}

		// parameters
		if (MapUtils.isNotEmpty(httpTimerData.getParameters())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String[]> entry : httpTimerData.getParameters().entrySet()) {
				rows.add(new String[] { entry.getKey(), Arrays.toString(entry.getValue()) });
			}
			Collections.sort(rows, ROW_COMPARATOR);
			table.addContentTable("Parameters:", null, 2, new String[] { "Parameter", "Value" }, rows);
		}

		// attributes
		if (MapUtils.isNotEmpty(httpTimerData.getAttributes())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String> entry : httpTimerData.getAttributes().entrySet()) {
				rows.add(new String[] { entry.getKey(), entry.getValue() });
			}
			Collections.sort(rows, ROW_COMPARATOR);
			table.addContentTable("Attributes:", null, 2, new String[] { "Attribute", "Value" }, rows);
		}

		// headers
		if (MapUtils.isNotEmpty(httpTimerData.getHeaders())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String> entry : httpTimerData.getHeaders().entrySet()) {
				rows.add(new String[] { entry.getKey(), entry.getValue() });
			}
			Collections.sort(rows, ROW_COMPARATOR);
			table.addContentTable("Headers:", null, 2, new String[] { "Header", "Value" }, rows);
		}

		// session attributes
		if (MapUtils.isNotEmpty(httpTimerData.getSessionAttributes())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String> entry : httpTimerData.getSessionAttributes().entrySet()) {
				rows.add(new String[] { entry.getKey(), entry.getValue() });
			}
			Collections.sort(rows, ROW_COMPARATOR);
			table.addContentTable("Session Attributes:", null, 2, new String[] { "Session Attribute", "Value" }, rows);
		}

		return table;
	}
}
