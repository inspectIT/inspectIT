package rocks.inspectit.ui.rcp.details.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * {@link IDetailsGenerator} that generates details for the {@link Span} interface.
 * 
 * @author Ivan Senic
 *
 */
public class SpanDetailsGenerator implements IDetailsGenerator {

	/**
	 * Comparator for the row displaying the tags etc.
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
		return defaultData instanceof Span;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		Span span = (Span) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Span Info", 1);

		// description
		table.addContentRow("Description:", null, new DetailsCellContent[] { new DetailsCellContent(TextFormatter.getSpanDetails(span, repositoryDefinition.getCachedDataService()).toString()) });

		// mark client
		if (span.isCaller()) {
			table.addContentRow("Client:", null, new DetailsCellContent[] { new DetailsCellContent(InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK), "Yes") });
		}

		// propagation
		table.addContentRow("Propagation:", null, new DetailsCellContent[] {
				new DetailsCellContent(ImageFormatter.getPropagationImage(span.getPropagationType()), TextFormatter.getPropagationStyled(span.getPropagationType()).toString()) });

		// reference
		if (null != span.getReferenceType()) {
			table.addContentRow("Reference:", null, new DetailsCellContent[] { new DetailsCellContent(ImageFormatter.getReferenceImage(span.getReferenceType()), span.getReferenceType()) });
		}

		// duration
		table.addContentRow("Duration (ms):", null, new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatDouble(span.getDuration(), 3)) });

		// tags
		if (MapUtils.isNotEmpty(span.getTags())) {
			List<String[]> rows = new ArrayList<>();
			for (Map.Entry<String, String> entry : span.getTags().entrySet()) {
				rows.add(new String[] { entry.getKey(), entry.getValue() });
			}
			Collections.sort(rows, ROW_COMPARATOR);
			table.addContentTable("All tags:", null, 2, new String[] { "Key", "Value" }, rows);
		}

		return table;
	}

}
