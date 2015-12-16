package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.InvocationSequenceDataHelper;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.YesNoDetailsCellContent;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Details of the {@link InvocationSequenceData}.
 * 
 * @author Ivan Senic
 * 
 */
public class InvocationSequenceDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Display only for root invocations.
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData && ((InvocationSequenceData) defaultData).getParentSequence() == null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		InvocationSequenceData invocationSequenceData = (InvocationSequenceData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Invocation Sequence Info", 1);

		table.addContentRow("Children Count:", null, new DetailsCellContent[] { new DetailsCellContent(String.valueOf(invocationSequenceData.getChildCount())) });
		table.addContentRow("Nested SQLs:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(InvocationSequenceDataHelper.hasNestedSqlStatements(invocationSequenceData)) });
		table.addContentRow("Nested Exceptions:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(InvocationSequenceDataHelper.hasNestedExceptions(invocationSequenceData)) });
		table.addContentRow("Nested Incomming Remote Calls:", null,
				new DetailsCellContent[] { new YesNoDetailsCellContent(InvocationSequenceDataHelper.hasNestedIncommingRemoteCalls(invocationSequenceData)) });
		table.addContentRow("Nested Outgoing Remote Calls:", null,
				new DetailsCellContent[] { new YesNoDetailsCellContent(InvocationSequenceDataHelper.hasNestedOutgoingRemoteCalls(invocationSequenceData)) });

		return table;
	}
}
