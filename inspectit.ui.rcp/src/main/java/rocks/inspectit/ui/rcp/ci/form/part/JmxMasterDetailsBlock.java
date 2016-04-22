package rocks.inspectit.ui.rcp.ci.form.part;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.all.storage.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.storage.serializer.provider.SerializationManagerProvider;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;
import rocks.inspectit.shared.cs.ci.profile.data.JmxDefinitionProfileData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.ProfileEditorInput;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.editor.tooltip.ColumnAwareToolTipSupport;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.util.RemoveSelection;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.TableItemControlDecorationManager;
import rocks.inspectit.ui.rcp.validation.ValidationState;
import rocks.inspectit.ui.rcp.viewer.ReferenceElementComparer;

/**
 * {@link MasterDetailsBlock} for displaying the overview for the {@link JmxBeanSensorAssignment}.
 *
 * @author Ivan Senic
 *
 */
public class JmxMasterDetailsBlock extends MasterDetailsBlock implements IFormPart, IPartSelectionListener, IPropertyListener {

	/**
	 * Profile being edited.
	 */
	private Profile profile;

	/**
	 * Profile data.
	 */
	private JmxDefinitionProfileData profileData;

	/**
	 * Assignments to display.
	 */
	private final List<JmxBeanSensorAssignment> jmxAssignments;

	/**
	 * Currently selected assignment.
	 */
	private JmxBeanSensorAssignment selectedAssignment;

	/**
	 * {@link TableItemControlDecorationManager}.
	 */
	private final TableItemControlDecorationManager tableItemControlDecorationManager = new TableItemControlDecorationManager();

	/**
	 * Validation manager.
	 */
	private final JmxValidationManager validationManager = new JmxValidationManager();

	/**
	 * Form page the block is being created on.
	 */
	private final FormPage formPage;

	/**
	 * Table viewer to display assignments.
	 */
	private TableViewer tableViewer;

	/**
	 * Add button.
	 */
	private Button addButton;

	/**
	 * Remove button.
	 */
	private Button removeButton;

	/**
	 * Duplicate button.
	 */
	private Button duplicateButton;

	/**
	 * Dirty state.
	 */
	private boolean dirty;

	/**
	 * {@link SerializationManager} used for duplicating assignments.
	 */
	private final SerializationManager serializationManager;

	/**
	 * Default constructor.
	 *
	 * @param formPage
	 *            Form page the block is being created on.
	 */
	public JmxMasterDetailsBlock(FormPage formPage) {
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		checkAndGetEditorInput();

		this.jmxAssignments = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(profileData.getJmxBeanAssignments())) {
			jmxAssignments.addAll(profileData.getJmxBeanAssignments());
		}

		this.serializationManager = InspectIT.getService(SerializationManagerProvider.class).createSerializer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();

		// section
		Section section = toolkit.createSection(parent, ExpandableComposite.NO_TITLE | ExpandableComposite.EXPANDED);
		section.marginWidth = 10;
		section.marginHeight = 5;

		// main composite
		final Composite mainComposite = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);

		// form part
		section.setClient(mainComposite);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				managedForm.getForm().reflow(false);
			}
		});
		managedForm.addPart(this);

		// Table
		Table table = new Table(mainComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(false);
		tableViewer.setComparer(ReferenceElementComparer.INSTANCE);
		createColumns(tableViewer);
		tableViewer.setContentProvider(getContentProvider());
		tableViewer.setLabelProvider(getLabelProvider());
		ColumnAwareToolTipSupport.enableFor(tableViewer);

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				// don't do anything if selected element is same as before
				if (selection.getFirstElement() == selectedAssignment) {
					return;
				}

				updateButtonsState(selection);
				fireEdit(selection);
			}
		});
		// add key adapter for delete
		tableViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					fireRemove();
				}
			}
		});

		// buttons
		Composite buttonComposite = toolkit.createComposite(mainComposite);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = 50;
		buttonComposite.setLayoutData(gd);

		addButton = toolkit.createButton(buttonComposite, "", SWT.PUSH);
		addButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		addButton.setToolTipText("Add");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JmxBeanSensorAssignment toAdd = new JmxBeanSensorAssignment();
				jmxAssignments.add(toAdd);

				tableViewer.refresh();
				StructuredSelection ss = new StructuredSelection(toAdd);
				tableViewer.setSelection(ss, true);

				showHideFormMessage();
				markDirty();
			}
		});

		removeButton = toolkit.createButton(buttonComposite, "", SWT.PUSH);
		removeButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_REMOVE));
		removeButton.setToolTipText("Remove");
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireRemove();
			}
		});

		duplicateButton = toolkit.createButton(buttonComposite, "", SWT.PUSH);
		duplicateButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_COPY));
		duplicateButton.setToolTipText("Duplicate");
		duplicateButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		duplicateButton.setEnabled(false);
		duplicateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
				for (Object selectedObject : selection.toArray()) {
					if (selectedObject instanceof JmxBeanSensorAssignment) {
						JmxBeanSensorAssignment assignment = (JmxBeanSensorAssignment) selectedObject;
						JmxBeanSensorAssignment copy = serializationManager.copy(assignment);
						jmxAssignments.add(copy);
					}
				}
				tableViewer.refresh();

				markDirty();
				showHideFormMessage();
			}
		});

		sashForm.setOrientation(SWT.VERTICAL);

		// info decoration
		showHideFormMessage();

		// set input
		tableViewer.setInput(jmxAssignments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPages(DetailsPart detailsPart) {
		IDetailsModifiedListener<JmxBeanSensorAssignment> listener = new IDetailsModifiedListener<JmxBeanSensorAssignment>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void contentModified(JmxBeanSensorAssignment modifiedElement) {
				Assert.isNotNull(modifiedElement);

				tableViewer.update(modifiedElement, null);
				markDirty();
			}
		};
		detailsPart.registerPage(JmxBeanSensorAssignment.class, new JmxDetailsPage(listener, validationManager));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			checkAndGetEditorInput();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		this.formPage.getEditor().removePropertyListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			dirty = false;

			profileData.setJmxBeanAssignments(jmxAssignments);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStale() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
	}

	/**
	 * Checks if we are in dirty state.
	 */
	private void markDirty() {
		if (!dirty) {
			dirty = true;
			formPage.getManagedForm().dirtyStateChanged();
		}
	}

	/**
	 * Checks that the editor input has profile with the {@link JmxDefinitionProfileData}. If so,
	 * sets the {@link #profile} and {@link #profileData}.
	 */
	private void checkAndGetEditorInput() {
		ProfileEditorInput input = (ProfileEditorInput) formPage.getEditor().getEditorInput();
		Assert.isNotNull(input.getProfile());
		Assert.isNotNull(input.getProfile().getProfileData());
		Assert.isLegal(input.getProfile().getProfileData().isOfType(JmxDefinitionProfileData.class), "Given profile can not be opened with the exclude rules part.");

		profile = input.getProfile();
		profileData = profile.getProfileData().getIfInstance(JmxDefinitionProfileData.class);
	}

	/**
	 * Fires edit option on the selection.
	 *
	 * @param selection
	 *            {@link ISelection}
	 */
	private void fireEdit(ISelection selection) {
		// then fire the selection
		formPage.getManagedForm().fireSelectionChanged(this, selection);
		if (selection.isEmpty()) {
			selectedAssignment = null; // NOPMD
		} else {
			selectedAssignment = (JmxBeanSensorAssignment) ((StructuredSelection) selection).getFirstElement();
		}
		updateButtonsState(selection);
	}

	/**
	 * Remove the current selection of the {@link #tableViewer}.
	 */
	private void fireRemove() {
		StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
		if (selection.isEmpty()) {
			// no action if selection is empty
			return;
		}

		for (Object selectedObject : selection.toArray()) {
			if (selectedObject instanceof JmxBeanSensorAssignment) {
				JmxBeanSensorAssignment assignment = (JmxBeanSensorAssignment) selectedObject;
				jmxAssignments.remove(assignment);
				validationManager.validationStatesRemoved(assignment);
			}
		}

		// inform details page about removal
		RemoveSelection removeSelection = new RemoveSelection(selection.toList());
		fireEdit(removeSelection);

		// Refresh and deal with the possible changes in the table row structure, so re-check error
		// descriptors
		tableViewer.refresh();
		validationManager.checkTableControlDecorations();

		markDirty();
		showHideFormMessage();
	}

	/**
	 * Shows or hides the form message info.
	 */
	private void showHideFormMessage() {
		if (jmxAssignments.isEmpty()) {
			formPage.getManagedForm().getMessageManager().addMessage(this, "No JMX bean assignment defined", null, IMessageProvider.INFORMATION);
		} else {
			formPage.getManagedForm().getMessageManager().removeMessage(this);
		}
	}

	/**
	 * Updates the state of the remove button depending on the current selection.
	 *
	 * @param selection
	 *            Current selection.
	 */
	private void updateButtonsState(ISelection selection) {
		removeButton.setEnabled(!selection.isEmpty());
		duplicateButton.setEnabled(!selection.isEmpty());
	}

	/**
	 * @return Return label provider for the {@link #tableViewer}.
	 */
	private IBaseLabelProvider getLabelProvider() {
		return new StyledCellIndexLabelProvider() {

			/**
			 * Empty StyledString.
			 */
			private final StyledString empty = new StyledString("");

			/**
			 * All attributes decoration.
			 */
			private final StyledString allAttributes = new StyledString("<all attributes>", StyledString.QUALIFIER_STYLER);

			/**
			 * All attributes decoration.
			 */
			private final StyledString noProperty = new StyledString("{}", StyledString.QUALIFIER_STYLER);

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (element instanceof JmxBeanSensorAssignment) {
					JmxBeanSensorAssignment jmxAssignment = (JmxBeanSensorAssignment) element;
					switch (index) {
					case 0:
						return TextFormatter.emptyStyledStringIfNull(jmxAssignment.getDomain());
					case 1:
						if (MapUtils.isEmpty(jmxAssignment.getObjectNameParameters())) {
							return noProperty;
						} else {
							return new StyledString(jmxAssignment.getObjectNameParameters().toString());
						}
					case 2:
						if (CollectionUtils.isEmpty(jmxAssignment.getAttributes())) {
							return allAttributes;
						} else {
							return new StyledString(jmxAssignment.getAttributes().toString());
						}
					default:
						return empty;
					}
				}
				return empty;
			}
		};
	}

	/**
	 * @return Return content provider for the {@link #tableViewer}.
	 */
	private IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}

	/**
	 * Creates columns for the {@link #tableViewer}.
	 *
	 * @param tableViewer
	 *            {@link TableViewer}
	 */
	private void createColumns(TableViewer tableViewer) {
		TableViewerColumn domainColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		domainColumn.getColumn().setResizable(true);
		domainColumn.getColumn().setWidth(150);
		domainColumn.getColumn().setText("Domain");
		domainColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_PACKAGE));
		domainColumn.getColumn().setToolTipText("Domain that the MBean object name is belonging to.");

		TableViewerColumn objectNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		objectNameColumn.getColumn().setResizable(true);
		objectNameColumn.getColumn().setWidth(250);
		objectNameColumn.getColumn().setText("Object Name Properties");
		objectNameColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BOOK));
		objectNameColumn.getColumn().setToolTipText("Object name properties patterns.");

		TableViewerColumn attributesColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		attributesColumn.getColumn().setResizable(true);
		attributesColumn.getColumn().setWidth(400);
		attributesColumn.getColumn().setText("Attributes");
		attributesColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BLUE_DOCUMENT_TABLE));
		attributesColumn.getColumn().setToolTipText("Attributes to monitor.");
	}

	/**
	 * Validation manager for this master block.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class JmxValidationManager extends AbstractValidationManager<JmxBeanSensorAssignment> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void notifyUpstream(JmxBeanSensorAssignment key, Set<ValidationState> states) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void showMessage(JmxBeanSensorAssignment key, Set<ValidationState> states) {
			tableItemControlDecorationManager.showTableItemControlDecoration(tableViewer, key, getErroMessageFull(states));
			formPage.getManagedForm().getMessageManager().addMessage(key, getErroMessageShort(states), null, IMessageProvider.ERROR);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void hideMessage(JmxBeanSensorAssignment key) {
			tableItemControlDecorationManager.hideTableItemControlDecoration(tableViewer, key);
			formPage.getManagedForm().getMessageManager().removeMessage(key);
		}

		/**
		 * Re-checks the control decorations. Should be called when elements are removed from the
		 * table viewer.
		 */
		public void checkTableControlDecorations() {
			for (JmxBeanSensorAssignment assignment : jmxAssignments) {
				Set<ValidationState> states = super.getValidationErrorStates(assignment);
				if (CollectionUtils.isNotEmpty(states)) {
					tableItemControlDecorationManager.showTableItemControlDecoration(tableViewer, assignment, getErroMessageFull(states));
				} else {
					tableItemControlDecorationManager.hideTableItemControlDecoration(tableViewer, assignment);
				}
			}
		}

		/**
		 * Creates short error message based on validation states. Should be used for the message
		 * manager.
		 *
		 * @param states
		 *            Set of states.
		 * @return Short error message.
		 */
		private String getErroMessageShort(Set<ValidationState> states) {
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (ValidationState state : states) {
				if (!state.isValid()) {
					count++;
				}
			}
			builder.append(count);
			if (count > 1) {
				builder.append(" fields contain validation errors");
			} else {
				builder.append(" field contains validation error");
			}

			return builder.toString();
		}

		/**
		 * Returns full error message for the assignment based on the validation states. In this
		 * message each line will contain error reported by any invalid {@link ValidationState}.
		 *
		 * @param states
		 *            Set of states.
		 * @return full error message
		 */
		private String getErroMessageFull(Set<ValidationState> states) {
			StringBuilder builder = new StringBuilder("Jmx Sensor Assignment:");
			for (ValidationState state : states) {
				if (!state.isValid()) {
					builder.append('\n');
					builder.append(state.getMessage());
				}
			}
			return builder.toString();
		}

	}

}
