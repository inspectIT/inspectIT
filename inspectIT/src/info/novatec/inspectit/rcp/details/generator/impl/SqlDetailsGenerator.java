package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.YesNoDetailsCellContent;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

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
		table.addContentRow("SQL:", null, new DetailsCellContent[] { new DetailsCellContent(sqlStatementData.getSqlWithParameterValues(), true) });
		return table;
	}

}
