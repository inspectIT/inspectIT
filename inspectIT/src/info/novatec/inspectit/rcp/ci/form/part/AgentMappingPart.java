package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.AgentMappingInput;
import info.novatec.inspectit.rcp.ci.listener.IEnvironmentChangeListener;
import info.novatec.inspectit.rcp.editor.table.AbstractTableEditingSupport;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Part for agent mapping.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentMappingPart extends AbstractFormPart implements IEnvironmentChangeListener {

	/**
	 * CMR to define the mappings for.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Mappings.
	 */
	private AgentMappings agentMappings;

	/**
	 * Input list that will be displayed in table.
	 */
	private Collection<AgentMapping> inputList;

	/**
	 * Available environments.
	 */
	private List<Environment> environments;

	/**
	 * {@link IManagedForm}.
	 */
	private IManagedForm managedForm;

	/**
	 * W I D G E T S.
	 */
	private TableViewer tableViewer; // NOCHK
	private TableViewerColumn environmentColumn; // NOCHK
	private Button addButton; // NOCHK
	private Button removeButton; // NOCHK

	/**
	 * 
	 * @param formPage
	 *            {@link FormPage} creating the part.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	public AgentMappingPart(FormPage formPage, Composite parent, FormToolkit toolkit) {
		this.managedForm = formPage.getManagedForm();
		AgentMappingInput input = (AgentMappingInput) formPage.getEditor().getEditorInput();
		cmrRepositoryDefinition = input.getCmrRepositoryDefinition();
		agentMappings = input.getAgentMappings();
		inputList = new ArrayList<>();
		environments = new ArrayList<>(input.getEnvironments());

		if (CollectionUtils.isNotEmpty(agentMappings.getMappings())) {
			inputList.addAll(agentMappings.getMappings());
		}

		createPart(parent, toolkit);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addEnvironmentChangeListener(this);
	}

	/**
	 * Creates part.
	 * 
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createPart(Composite parent, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(parent, SWT.INHERIT_DEFAULT);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Table table = toolkit.createTable(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		createColumns();
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new AgentMappingLabelProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsState();
			}
		});

		tableViewer.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					if (e.item instanceof TableItem) {
						TableItem item = (TableItem) e.item;
						Object data = item.getData();
						if (data instanceof AgentMapping) {
							((AgentMapping) data).setActive(item.getChecked());
							tableViewer.update(data, null);
							markDirty();
						}
					}
				}
			}
		});

		Composite buttonComposite = toolkit.createComposite(mainComposite, SWT.INHERIT_DEFAULT);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		addButton = toolkit.createButton(buttonComposite, "Add", SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AgentMapping mapping = new AgentMapping();
				mapping.setEnvironmentId(environments.iterator().next().getId());
				inputList.add(mapping);
				tableViewer.refresh();
				updateCheckedItems();
				markDirty();
			}
		});

		removeButton = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
				for (Object selectedObject : selection.toArray()) {
					if (selectedObject instanceof AgentMapping) {
						inputList.remove((AgentMapping) selectedObject);
					}
				}
				tableViewer.refresh();
				updateCheckedItems();
				markDirty();
			}
		});

		Button testButton = toolkit.createButton(buttonComposite, "Test Mappings", SWT.PUSH);
		testButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		testButton.setEnabled(false);

		tableViewer.setInput(inputList);
		updateCheckedItems();

		checkEnvironments();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		managedForm.dirtyStateChanged();

		if (onSave) {
			agentMappings.setMappings(inputList);
		}
	}

	/**
	 * Creates columns for the tables.
	 */
	private void createColumns() {
		TableViewerColumn activeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		activeColumn.getColumn().setResizable(false);
		activeColumn.getColumn().setWidth(60);
		activeColumn.getColumn().setText("Active");
		activeColumn.getColumn().setToolTipText("If mapping is currently active. Deactivated mappings will not be considered when assigning Environment to the agent.");

		TableViewerColumn agentNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		agentNameColumn.getColumn().setResizable(true);
		agentNameColumn.getColumn().setWidth(150);
		agentNameColumn.getColumn().setText("Agent Name");
		agentNameColumn.getColumn().setToolTipText("Name of the agent. Use wild-card '*' for matching several agent names with one mapping.");
		agentNameColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		agentNameColumn.setEditingSupport(new AgentNameEditingSupport(tableViewer));

		TableViewerColumn ipColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		ipColumn.getColumn().setResizable(true);
		ipColumn.getColumn().setWidth(150);
		ipColumn.getColumn().setText("IP Address");
		ipColumn.getColumn().setToolTipText(
				"IP address of the agent. Use wild-card '*' for matching several IPs with one mapping. For example, 192.168.* will match all IP addresses in starting with 192.168.");
		ipColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		ipColumn.setEditingSupport(new IpAddressEditingSupport(tableViewer));

		environmentColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		environmentColumn.getColumn().setResizable(true);
		environmentColumn.getColumn().setWidth(150);
		environmentColumn.getColumn().setText("Environment");
		environmentColumn.getColumn().setToolTipText("Environment that should be assigned to the agent fulfilling the name and IP parameters.");
		environmentColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BLOCK));
		environmentColumn.setEditingSupport(new EnvironmentEditingSupport(tableViewer));

		TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		descriptionColumn.getColumn().setResizable(true);
		descriptionColumn.getColumn().setWidth(200);
		descriptionColumn.getColumn().setText("Description");
		descriptionColumn.getColumn().setToolTipText("Optional description of the mapping.");
		descriptionColumn.setEditingSupport(new DescriptionEditingSupport(tableViewer));
	}

	/**
	 * Updates states of the check boxes next to the elements.
	 */
	private void updateCheckedItems() {
		for (TableItem item : tableViewer.getTable().getItems()) {
			Object data = item.getData();
			if (data instanceof AgentMapping) {
				item.setChecked(((AgentMapping) data).isActive());
			}
		}
	}

	/**
	 * Updates the state of the remove button depending on the current table selection.
	 */
	private void updateButtonsState() {
		StructuredSelection structuredSelection = (StructuredSelection) tableViewer.getSelection();
		if (structuredSelection.isEmpty()) {
			removeButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
		}
	}

	/**
	 * Label provider for the table.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class AgentMappingLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Empty.
		 */
		private final StyledString empty = new StyledString();

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			if (element instanceof AgentMapping) {
				AgentMapping mapping = (AgentMapping) element;

				switch (index) {
				case 1:
					return TextFormatter.emptyStyledStringIfNull(mapping.getAgentName());
				case 2:
					return TextFormatter.emptyStyledStringIfNull(mapping.getIpAddress());
				case 3:
					String environmentId = mapping.getEnvironmentId();

					for (Environment environment : environments) {
						if (Objects.equals(environmentId, environment.getId())) {
							return TextFormatter.emptyStyledStringIfNull(environment.getName());
						}
					}
					return empty;
				case 4:
					return TextFormatter.emptyStyledStringIfNull(TextFormatter.clearLineBreaks(mapping.getDescription()));
				default:
					return empty;
				}
			}
			return empty;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Return gray font for disabled ones.
		 */
		@Override
		protected Color getForeground(Object element, int index) {
			if (element instanceof AgentMapping) {
				AgentMapping mapping = (AgentMapping) element;
				if (!mapping.isActive()) {
					return tableViewer.getTable().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
				}
			}

			return super.getForeground(element, index);
		}

	}

	/**
	 * Editing support for Agent name column.
	 * 
	 * @author Ivan Senic
	 */
	private class AgentNameEditingSupport extends AbstractTableEditingSupport<AgentMapping, String> {

		/**
		 * @param viewer
		 *            viewer
		 */
		public AgentNameEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getValueImpl(AgentMapping element) {
			return element.getAgentName();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setValueImpl(AgentMapping element, String value) {
			if (StringUtils.isNotBlank(value)) {
				element.setAgentName(value);
				markDirty();
			}
		}

	}

	/**
	 * Editing support for Ip Address column.
	 * 
	 * @author Ivan Senic
	 */
	private class IpAddressEditingSupport extends AbstractTableEditingSupport<AgentMapping, String> {

		/**
		 * @param viewer
		 *            viewer
		 */
		public IpAddressEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getValueImpl(AgentMapping element) {
			return element.getIpAddress();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setValueImpl(AgentMapping element, String value) {
			if (StringUtils.isNotBlank(value)) {
				element.setIpAddress(value);
				markDirty();
			}
		}

	}

	/**
	 * Editing support for Description column.
	 * 
	 * @author Ivan Senic
	 */
	private class DescriptionEditingSupport extends AbstractTableEditingSupport<AgentMapping, String> {

		/**
		 * @param viewer
		 *            viewer
		 */
		public DescriptionEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getValueImpl(AgentMapping element) {
			if (StringUtils.isNotBlank(element.getDescription())) {
				return element.getDescription();
			} else {
				return "";
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setValueImpl(AgentMapping element, String value) {
			if (StringUtils.isNotBlank(value)) {
				element.setDescription(value);
			} else {
				element.setDescription(null);
			}
			markDirty();
		}

	}

	/**
	 * Editing support for Environment column.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class EnvironmentEditingSupport extends EditingSupport {

		/**
		 * Cell editor.
		 */
		private CellEditor cellEditor;

		/**
		 * @param viewer
		 *            viewer
		 */
		public EnvironmentEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected CellEditor getCellEditor(Object element) {
			if (null == cellEditor) {
				String[] items = new String[environments.size()];
				int i = 0;
				for (Environment environment : environments) {
					items[i++] = environment.getName();
				}
				cellEditor = new ComboBoxCellEditor(tableViewer.getTable(), items, SWT.READ_ONLY);
			}
			return cellEditor;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object getValue(Object element) {
			int i = 0;
			for (Environment environment : environments) {
				if (Objects.equals(((AgentMapping) element).getEnvironmentId(), environment.getId())) {
					return i;
				}
				i++;
			}
			return -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void setValue(Object element, Object value) {
			((AgentMapping) element).setEnvironmentId(environments.get(((Integer) value).intValue()).getId());
			getViewer().update(element, null);
			markDirty();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentAdded(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		if (!Objects.equals(repositoryDefinition, cmrRepositoryDefinition)) {
			return;
		}

		environments.add(environment);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				environmentColumn.setEditingSupport(new EnvironmentEditingSupport(tableViewer));
				checkEnvironments();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentEdited(Environment environment) {
		for (Iterator<Environment> it = environments.iterator(); it.hasNext();) {
			Environment displayed = it.next();
			if (Objects.equals(displayed.getId(), environment.getId())) {
				int index = environments.indexOf(displayed);
				it.remove();
				environments.add(index, environment);

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						environmentColumn.setEditingSupport(new EnvironmentEditingSupport(tableViewer));
						tableViewer.refresh();
					}
				});

				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentDeleted(Environment environment) {
		for (Iterator<Environment> it = environments.iterator(); it.hasNext();) {
			Environment displayed = it.next();
			if (Objects.equals(displayed.getId(), environment.getId())) {
				it.remove();

				final List<AgentMapping> removeList = new ArrayList<>();
				for (AgentMapping mapping : inputList) {
					if (Objects.equals(mapping.getEnvironmentId(), environment.getId())) {
						removeList.add(mapping);
					}
				}
				inputList.removeAll(removeList);

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						environmentColumn.setEditingSupport(new EnvironmentEditingSupport(tableViewer));
						tableViewer.remove(removeList.toArray());

						checkEnvironments();
					}

				});

				break;
			}
		}
	}

	/**
	 * Checks if there is environments in the CMR and thus updates the part based on the state.
	 */
	private void checkEnvironments() {
		if (CollectionUtils.isEmpty(environments)) {
			tableViewer.getTable().setEnabled(false);
			addButton.setEnabled(false);
			managedForm.getForm().setMessage("No environment exists on the selected CMR, mapping can not be defined.", IMessageProvider.ERROR);
		} else {
			tableViewer.getTable().setEnabled(true);
			addButton.setEnabled(true);
			managedForm.getForm().setMessage("Define agent mapping properties for the '" + cmrRepositoryDefinition.getName() + "' repository.", IMessageProvider.NONE);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
		super.dispose();
	}
}
