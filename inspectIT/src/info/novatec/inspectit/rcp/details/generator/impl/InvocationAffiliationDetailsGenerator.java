package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.YesNoDetailsCellContent;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

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
