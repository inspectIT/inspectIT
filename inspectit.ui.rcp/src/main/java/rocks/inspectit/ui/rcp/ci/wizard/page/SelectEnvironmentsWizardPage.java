package rocks.inspectit.ui.rcp.ci.wizard.page;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;

/**
 * Page for exporting/importing {@link Environment}s.
 *
 * @author Ivan Senic
 *
 */
public class SelectEnvironmentsWizardPage extends WizardPage {

	/**
	 * Default disallowed item message.
	 */
	private final String defaultMessage;

	/**
	 * Default disallowed item message.
	 */
	private String disallowedMessage = "Selected item is disallowed";

	/**
	 * Environments possible to export.
	 */
	private Collection<Environment> environments;

	/**
	 * Pre-selected environment IDs.
	 */
	private Collection<String> selectedIds;

	/**
	 * Disallowed environment IDs.
	 */
	private Collection<String> disallowedIds;

	/**
	 * Table for showing environments.
	 */
	private Table table;

	/**
	 * {@link TableViewer}.
	 */
	private TableViewer tableViewer;

	/**
	 * Default constructor.
	 *
	 * @param pageName
	 *            Page name.
	 * @param message
	 *            Page message.
	 * @param environments
	 *            Environment to offer for selection.
	 * @param selectedIds
	 *            Pre-selected environment IDs.
	 */
	public SelectEnvironmentsWizardPage(String pageName, String message, Collection<Environment> environments, Collection<String> selectedIds) {
		super(pageName);
		setTitle(pageName);
		setMessage(message);

		this.defaultMessage = message;
		this.environments = environments;
		this.selectedIds = selectedIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));

		table = new Table(main, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		createColumns(tableViewer);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(environments);

		updateCheckedItems();

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(isPageComplete());
			}
		});

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		for (TableItem item : table.getItems()) {
			if (item.getChecked()) {
				Environment data = (Environment) item.getData();
				if (CollectionUtils.isNotEmpty(disallowedIds) && disallowedIds.contains(data.getId())) {
					setMessage(disallowedMessage, ERROR);
					return false;
				}
			}
		}

		setMessage(defaultMessage);
		return true;
	}

	/**
	 * Updates checked items.
	 */
	private void updateCheckedItems() {
		for (TableItem item : table.getItems()) {
			Environment data = (Environment) item.getData();
			item.setChecked(selectedIds.contains(data.getId()));
		}
	}

	/**
	 * Creates columns.
	 *
	 * @param tableViewer
	 *            {@link TableViewer}
	 */
	private void createColumns(TableViewer tableViewer) {
		TableViewerColumn selectedColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		selectedColumn.getColumn().setResizable(false);
		selectedColumn.getColumn().setWidth(40);
		selectedColumn.getColumn().setText("Selected");
		selectedColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
		});

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setWidth(200);
		nameColumn.getColumn().setText("Name");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Environment) element).getName();
			}

			@Override
			public Image getImage(Object element) {
				return ImageFormatter.getEnvironmentImage((Environment) element);
			}
		});

		TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		descriptionColumn.getColumn().setWidth(300);
		descriptionColumn.getColumn().setText("Description");
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String desc = TextFormatter.emptyStringIfNull(((Environment) element).getDescription());
				return TextFormatter.clearLineBreaks(desc);
			}
		});
	}

	/**
	 * Returns selected environments.
	 *
	 * @return Returns selected environments.
	 */
	public Collection<Environment> getEnvironments() {
		Collection<Environment> selected = new HashSet<>();
		for (TableItem item : table.getItems()) {
			if (item.getChecked()) {
				selected.add((Environment) item.getData());
			}
		}
		return selected;
	}

	/**
	 * Sets {@link #environments}.
	 *
	 * @param environments
	 *            New value for {@link #environments}
	 */
	public void setEnvironments(Collection<Environment> environments) {
		this.environments = environments;
		tableViewer.setInput(environments);
	}

	/**
	 * Sets {@link #selectedIds}.
	 *
	 * @param selectedIds
	 *            New value for {@link #selectedIds}
	 */
	public void setSelectedIds(Collection<String> selectedIds) {
		this.selectedIds = selectedIds;
		updateCheckedItems();
	}

	/**
	 * Sets disallowed environments IDs and the message to show when they are selected.
	 *
	 * @param disallowedIds
	 *            Set of IDs.
	 * @param disallowedMessage
	 *            Message
	 */
	public final void setDisallowedIds(Collection<String> disallowedIds, String disallowedMessage) {
		this.disallowedIds = disallowedIds;
		if (null != disallowedMessage) {
			this.disallowedMessage = disallowedMessage;
		}

	}

}
