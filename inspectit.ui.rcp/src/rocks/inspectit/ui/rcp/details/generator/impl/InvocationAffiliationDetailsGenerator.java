package rocks.inspectit.ui.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.IAggregatedData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.YesNoDetailsCellContent;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Details about invocation affiliation read from the {@link InvocationAwareData}.
 * 
 * @author Ivan Senic
 * 
 */
public class InvocationAffiliationDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Display only for aggregated data.
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof InvocationAwareData && defaultData instanceof IAggregatedData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		InvocationAwareData invocationAwareData = (InvocationAwareData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Invocation Affiliation", 1);

		table.addContentRow("In Invocations:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(!invocationAwareData.isOnlyFoundOutsideInvocations()) });

		if (!invocationAwareData.isOnlyFoundOutsideInvocations()) {
			int percentage = (int) (invocationAwareData.getInvocationAffiliationPercentage() * 100);
			int invocations = invocationAwareData.getInvocationParentsIdSet().size();
			String affiliation = TextFormatter.getInvocationAffilliationPercentageString(percentage, invocations).getString();
			table.addContentRow("Affiliation:", null, new DetailsCellContent[] { new DetailsCellContent(affiliation) });
		}

		return table;
	}

}
