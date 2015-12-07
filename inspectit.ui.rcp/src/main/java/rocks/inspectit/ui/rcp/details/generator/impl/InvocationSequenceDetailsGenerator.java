package rocks.inspectit.ui.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.YesNoDetailsCellContent;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

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
		return (defaultData instanceof InvocationSequenceData) && (((InvocationSequenceData) defaultData).getParentSequence() == null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		InvocationSequenceData invocationSequenceData = (InvocationSequenceData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Invocation Sequence Info", 1);

		ApplicationData application = repositoryDefinition.getCachedDataService().getApplicationForId(invocationSequenceData.getApplicationId());
		if (null != application) {
			table.addContentRow("Application:", null, new DetailsCellContent[] { new DetailsCellContent(application.getName()) });
		}

		BusinessTransactionData businessTxData = repositoryDefinition.getCachedDataService().getBusinessTransactionForId(invocationSequenceData.getApplicationId(),
				invocationSequenceData.getBusinessTransactionId());
		if (null != businessTxData) {
			table.addContentRow("Business Transactions:", null, new DetailsCellContent[] { new DetailsCellContent(businessTxData.getName()) });
		}

		table.addContentRow("Children Count:", null, new DetailsCellContent[] { new DetailsCellContent(String.valueOf(invocationSequenceData.getChildCount())) });
		table.addContentRow("Nested SQLs:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(InvocationSequenceDataHelper.hasNestedSqlStatements(invocationSequenceData)) });
		table.addContentRow("Nested Exceptions:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(InvocationSequenceDataHelper.hasNestedExceptions(invocationSequenceData)) });

		return table;
	}

}
