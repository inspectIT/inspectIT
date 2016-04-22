package rocks.inspectit.ui.rcp.details.generator.impl;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.YesNoDetailsCellContent;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * SQL information details generator.
 *
 * @author Ivan Senic
 *
 */
public class SqlDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof SqlStatementData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		SqlStatementData sqlStatementData = (SqlStatementData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "SQL Info", 1);

		table.addContentRow("Is Prepared:", null, new DetailsCellContent[] { new YesNoDetailsCellContent(sqlStatementData.isPreparedStatement()) });
		table.addContentRow("Database:", null, new DetailsCellContent[] { new DetailsCellContent(sqlStatementData.getDatabaseProductName()) });
		table.addContentRow("Database version:", null, new DetailsCellContent[] { new DetailsCellContent(sqlStatementData.getDatabaseProductVersion()) });
		table.addContentRow("Database URL:", null, new DetailsCellContent[] { new DetailsCellContent(sqlStatementData.getDatabaseUrl()) });
		table.addContentRow("SQL:", null, new DetailsCellContent[] { new DetailsCellContent(sqlStatementData.getSqlWithParameterValues()) });
		return table;
	}

}
