package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.TimerMethodSensorAssignment;
import info.novatec.inspectit.ci.sensor.ISensorConfig;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.dialog.SensorAssignmentSelectionDialog;
import info.novatec.inspectit.rcp.ci.form.input.ProfileEditorInput;
import info.novatec.inspectit.rcp.ci.widget.SensorAssignmentTableProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.IMessagePrefixProvider;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Tree master block for the sensor definition form page.
 * 
 * @author Ivan Senic
 * 
 */
public class SensorAssignmentMasterBlock extends MasterDetailsBlock implements IFormPart, IPartSelectionListener, ISelectionChangedListener, IPropertyListener {

	/**
	 * If edit can be executed.
	 */
	private boolean canEdit = true;

	/**
	 * Profile being edited.
	 */
	private Profile profile;

	/**
	 * Map of assignments.
	 */
	private Map<Class<? extends ISensorConfig>, List<AbstractClassSensorAssignment<?>>> configToAssignmentMap = new HashMap<>();

	/**
	 * Map of sensors to the CTabItem.
	 */
	private Map<Class<? extends ISensorConfig>, CTabItem> sensorToTabMap = new HashMap<>();

	/**
	 * List of all currently invalid assignments in the page with connection to the full error
	 * message.
	 */
	private Map<AbstractClassSensorAssignment<?>, String> invalidAssignments = new IdentityHashMap<>();

	/**
	 * List of created {@link TableViewer}.
	 */
	private List<TableViewer> tableViewers = new ArrayList<>();

	/**
	 * {@link TableEditor}s to handle the validation decoration on table rows.
	 */
	private List<TableItemControlDecoration> tableItemControlDecorations = new ArrayList<>();

	/**
	 * Assignment currently being edited or <code>null</code> if no edit is done in the moment.
	 */
	private AbstractClassSensorAssignment<?> selectedAssignment;

	/**
	 * Dirty state.
	 */
	private boolean dirty;

	/**
	 * Form page block belongs to.
	 */
	private FormPage formPage;

	/**
	 * Managed form to report to.
	 */
	private IManagedForm managedForm;

	/**
	 * Tab folder, each tab displays different sensor type.
	 */
	private CTabFolder tabFolder;

	/**
	 * Add assignment button.
	 */
	private Button addButton;

	/**
	 * Remove selected assignment(s) button.
	 */
	private Button removeButton;

	/**
	 * Duplicate selected assignment button.
	 */
	private Button duplicateButton;

	/**
	 * Composite to be displayed when no assignment is existing in the profile.
	 * 
	 * @see {@link #createEmptyInputHint()}
	 */
	private Composite emptyHintComposite;

	/**
	 * @param formPage
	 *            {@link FormPage} part being displayed.
	 */
	public SensorAssignmentMasterBlock(FormPage formPage) {
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		ProfileEditorInput input = (ProfileEditorInput) formPage.getEditor().getEditorInput();
		this.profile = input.getProfile();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContent(IManagedForm managedForm, Composite parent) {
		super.createContent(managedForm, parent);

		// set sashes for more space a bit in bottom
		sashForm.setWeights(new int[] { 3, 4 });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPages(DetailsPart detailsPart) {
		this.detailsPart = detailsPart;
		ISensorAssignmentUpdateListener masterBlockListener = new MasterBlockValidationListener();
		detailsPart.registerPage(MethodSensorAssignment.class, new MethodSensorAssignmentDetailsPage(masterBlockListener, !profile.isCommonProfile()));
		detailsPart.registerPage(TimerMethodSensorAssignment.class, new TimerSensorAssignmentDetailsPage(masterBlockListener, !profile.isCommonProfile()));
		detailsPart.registerPage(ExceptionSensorAssignment.class, new ExceptionSensorAssignmentDetailsPage(masterBlockListener, !profile.isCommonProfile()));
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
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		this.managedForm = managedForm;
		FormToolkit toolkit = managedForm.getToolkit();

		// section
		Section section = toolkit.createSection(parent, Section.NO_TITLE | Section.EXPANDED);
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

		// tab folder
		tabFolder = new CTabFolder(mainComposite, SWT.TOP | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		gd.widthHint = 300;
		tabFolder.setLayoutData(gd);
		tabFolder.setSelectionBackground(new Color[] { toolkit.getColors().getColor(IFormColors.H_GRADIENT_START), toolkit.getColors().getColor(IFormColors.H_GRADIENT_END) }, new int[] { 100 }, true);
		tabFolder.setTabHeight(30);
		toolkit.adapt(tabFolder);

		// buttons
		Composite buttonComposite = toolkit.createComposite(mainComposite);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.widthHint = 50;
		buttonComposite.setLayoutData(gd);

		addButton = toolkit.createButton(buttonComposite, "", SWT.PUSH);
		addButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		addButton.setToolTipText("Add");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractClassSensorAssignment<?> toAdd = addRequested();
				if (null != toAdd) {
					addToInputMap(toAdd);

					// switch to correct tab
					CTabItem tabItem = sensorToTabMap.get(toAdd.getSensorConfigClass());
					// if one does not exists, create new
					if (null == tabItem) {
						createNewTabItem(toAdd.getSensorConfigClass(), configToAssignmentMap.get(toAdd.getSensorConfigClass()));
						tabItem = sensorToTabMap.get(toAdd.getSensorConfigClass());
					}
					tabFolder.setSelection(tabItem);

					// update table viewer
					TableViewer tableViewer = getActiveTableViewer();
					tableViewer.refresh();

					StructuredSelection ss = new StructuredSelection(toAdd);
					tableViewer.setSelection(ss, true);

					showHideFormMessage();
					markDirty();
				}
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
				TableViewer tableViewer = getActiveTableViewer();
				StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
				for (Object selectedObject : selection.toArray()) {
					if (selectedObject instanceof AbstractClassSensorAssignment) {
						try {
							// TODO use kryo copy operation after INSPECTIT-1923 is integrated
							AbstractClassSensorAssignment<?> clone = (AbstractClassSensorAssignment<?>) ((AbstractClassSensorAssignment<?>) selectedObject).clone();
							addToInputMap(clone);
						} catch (CloneNotSupportedException exception) {
							InspectIT.getDefault().log(IStatus.WARNING, "Unable to clone sensor assignment.", exception);
						}
					}
				}
				tableViewer.refresh();

				markDirty();
				showHideFormMessage();
			}
		});

		sashForm.setOrientation(SWT.VERTICAL);

		canEditCheck();

		// check input
		Map<Class<? extends ISensorConfig>, List<AbstractClassSensorAssignment<?>>> input = getInput();
		if (MapUtils.isNotEmpty(input)) {
			// if it s not empty create tabs
			for (Entry<Class<? extends ISensorConfig>, List<AbstractClassSensorAssignment<?>>> entry : input.entrySet()) {
				createNewTabItem(entry.getKey(), entry.getValue());
			}
		} else {
			// if empty, then create hit for the user
			createEmptyInputHint();
		}

		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableViewer tableViewer = getActiveTableViewer();
				if (null != tableViewer) {
					// refresh table viewer and set selection
					tableViewer.refresh();
					tableViewer.getTable().setFocus();
					SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(tableViewer, tableViewer.getSelection());
					SensorAssignmentMasterBlock.this.selectionChanged(selectionChangedEvent);

					// there is a bug with error icons not disappearing after table switch
					// thus we need to redraw the complete upper part
					mainComposite.getParent().redraw();
				}
			}

		});

		// info decoration
		showHideFormMessage();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * To be called when a selection in one of the tables changes.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		StructuredSelection ss = (StructuredSelection) event.getSelection();

		// don't do anything if selected element is same as before
		if (ss.getFirstElement() == selectedAssignment) {
			return;
		}

		updateButtonsState(ss);
		fireEdit(event.getSelection());
	}

	/**
	 * Executes the can edit check.
	 */
	private void canEditCheck() {
		canEdit = !profile.isCommonProfile(); // NOPMD
		if (!canEdit) {
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
			duplicateButton.setEnabled(false);
		}
	}

	/**
	 * @return Returns active table viewer or <code>null</code> if no table viewer exists.
	 */
	private TableViewer getActiveTableViewer() {
		int index = tabFolder.getSelectionIndex();
		if (index != -1 && CollectionUtils.isNotEmpty(tableViewers)) {
			return tableViewers.get(index);
		}

		return null;
	}

	/**
	 * Creates new tab item with the list of assignments to be used as input.
	 * 
	 * @param sensorClass
	 *            sensor class
	 * @param assignments
	 *            assignments
	 */
	private void createNewTabItem(Class<? extends ISensorConfig> sensorClass, List<AbstractClassSensorAssignment<?>> assignments) {
		// remove empty hint composite if it's still there
		if (null != emptyHintComposite && !emptyHintComposite.isDisposed()) {
			tabFolder.getItem(0).dispose();
			emptyHintComposite.dispose();
		}

		SensorAssignmentTableProvider tableProvider = new SensorAssignmentTableProvider(this, tabFolder);
		tableProvider.setInput(assignments);

		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setControl(tableProvider.getTableViewer().getTable());
		tabItem.setText(TextFormatter.getSensorConfigName(sensorClass));
		tabItem.setImage(ImageFormatter.getSensorConfigImage(sensorClass));

		sensorToTabMap.put(sensorClass, tabItem);

		TableViewer tableViewer = tableProvider.getTableViewer();
		tableViewer.getTable().setData(sensorClass);
		tableViewers.add(tableViewer);

		// add key adapter for delete
		tableViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					fireRemove();
				}
			}
		});
	}

	/**
	 * Removes a {@link CTabItem} for the sensor class.
	 * 
	 * @param sensorClass
	 *            sensor class
	 */
	private void removeTabItem(Class<? extends ISensorConfig> sensorClass) {
		CTabItem tabItem = sensorToTabMap.remove(sensorClass);

		if (null != tabItem) {
			int index = tabFolder.indexOf(tabItem);
			tableViewers.remove(index);
			tabItem.dispose();
		}

		if (CollectionUtils.isEmpty(tableViewers)) {
			createEmptyInputHint();
		}
	}

	/**
	 * Creates the empty profile hint for the user.
	 */
	private void createEmptyInputHint() {
		FormToolkit toolkit = managedForm.getToolkit();

		emptyHintComposite = toolkit.createComposite(tabFolder, SWT.NONE);
		emptyHintComposite.setLayout(new GridLayout(2, false));

		Label infoLabel = toolkit.createLabel(emptyHintComposite, null);
		infoLabel.setImage(Display.getDefault().getSystemImage(SWT.ICON_INFORMATION));
		infoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		FormText formText = toolkit.createFormText(emptyHintComposite, false);
		formText.setText("<form><p>Seems that there are no sensor assignments in this profile. Use <b>Add</b> action to start defining instrumentation points.</p></form>", true, false);
		formText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setControl(emptyHintComposite);
		tabItem.setText("Getting Started");

		tabFolder.setSelection(0);
	}

	/**
	 * Shows or hides the form message info.
	 */
	private void showHideFormMessage() {
		if (profile.isCommonProfile()) {
			managedForm.getMessageManager().addMessage(this, "Common profiles can not be edited", null, IMessageProvider.NONE);
		} else if (configToAssignmentMap.isEmpty()) {
			managedForm.getMessageManager().addMessage(this, "No sensor assignment defined", null, IMessageProvider.INFORMATION);
		} else {
			managedForm.getMessageManager().removeMessage(this);
		}
	}

	/**
	 * To be called when add is requested to the tree.
	 * 
	 * @return Returns new {@link AbstractClassSensorAssignment} with the correctly set sensor type.
	 */
	private AbstractClassSensorAssignment<?> addRequested() {
		SensorAssignmentSelectionDialog dialog = new SensorAssignmentSelectionDialog(managedForm.getForm().getShell());
		if (dialog.open() == Dialog.OK) {
			return dialog.getSensorAssignment();
		}
		return null;
	}

	/**
	 * Tries to remove the selected assignment(s) from the active table viewer.
	 */
	private void fireRemove() {
		if (!canEdit) {
			return;
		}

		TableViewer tableViewer = getActiveTableViewer();
		if (null == tableViewer) {
			// no action if table viewer is null
			return;
		}

		StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
		if (selection.isEmpty()) {
			// no action if selection is empty
			return;
		}

		Class<? extends ISensorConfig> sensorClass = null;
		for (Object selectedObject : selection.toArray()) {
			if (selectedObject instanceof AbstractClassSensorAssignment) {
				AbstractClassSensorAssignment<?> assignment = (AbstractClassSensorAssignment<?>) selectedObject;

				// remember sensor class, must be same for all removed ones
				sensorClass = assignment.getSensorConfigClass();

				// remove from input and from any error messages
				removeFromInputMap(assignment);
				formPage.getManagedForm().getMessageManager().removeMessage(getMessageKey(assignment));
			}
		}
		// inform details page about removal
		RemoveSelection removeSelection = new RemoveSelection(selection.toList());
		fireEdit(removeSelection);

		// Refresh and deal with the possible changes in the table row structure, so re-check error
		// descriptors
		tableViewer.refresh();
		for (Map.Entry<AbstractClassSensorAssignment<?>, String> entry : invalidAssignments.entrySet()) {
			showTableItemControlDecoration(entry.getKey(), entry.getValue());
		}

		// if we don't have any more assignment for sensor remove tab
		if (CollectionUtils.isEmpty(configToAssignmentMap.get(sensorClass))) {
			removeTabItem(sensorClass);
		}

		markDirty();
		showHideFormMessage();
	}

	/**
	 * Creates input map and returns the input.
	 * 
	 * @return Input for the tree
	 */
	private Map<Class<? extends ISensorConfig>, List<AbstractClassSensorAssignment<?>>> getInput() {
		configToAssignmentMap.clear();
		if (CollectionUtils.isNotEmpty(profile.getMethodSensorAssignments())) {
			for (MethodSensorAssignment methodSensorAssignment : profile.getMethodSensorAssignments()) {
				addToInputMap(methodSensorAssignment);
			}
		}
		if (CollectionUtils.isNotEmpty(profile.getExceptionSensorAssignments())) {
			for (ExceptionSensorAssignment exceptionSensorAssignment : profile.getExceptionSensorAssignments()) {
				addToInputMap(exceptionSensorAssignment);
			}
		}
		return configToAssignmentMap;
	}

	/**
	 * Adds one {@link AbstractClassSensorAssignment} to the input map.
	 * 
	 * @param assignment
	 *            {@link AbstractClassSensorAssignment}
	 */
	private void addToInputMap(AbstractClassSensorAssignment<?> assignment) {
		Class<? extends ISensorConfig> sensorClass = assignment.getSensorConfigClass();
		List<AbstractClassSensorAssignment<?>> list = configToAssignmentMap.get(sensorClass);
		if (null == list) {
			list = new ArrayList<>();
			configToAssignmentMap.put(sensorClass, list);
		}
		list.add(assignment);
	}

	/**
	 * Removes one {@link AbstractClassSensorAssignment} from the input map.
	 * 
	 * @param assignment
	 *            {@link AbstractClassSensorAssignment}
	 */
	private void removeFromInputMap(AbstractClassSensorAssignment<?> assignment) {
		Class<? extends ISensorConfig> sensorClass = assignment.getSensorConfigClass();
		List<AbstractClassSensorAssignment<?>> list = configToAssignmentMap.get(sensorClass);
		if (null != list) {
			list.remove(assignment);
			if (list.isEmpty()) {
				configToAssignmentMap.remove(sensorClass);
			}
		}

		invalidAssignments.remove(assignment);
	}

	/**
	 * Updates the state of the remove button depending on the current selection.
	 * 
	 * @param selection
	 *            Current selection.
	 */
	private void updateButtonsState(ISelection selection) {
		if (selection.isEmpty()) {
			removeButton.setEnabled(false);
			duplicateButton.setEnabled(false);
		} else {
			removeButton.setEnabled(canEdit);
			duplicateButton.setEnabled(canEdit);
		}
		addButton.setEnabled(canEdit);
	}

	/**
	 * Fires edit option on the selection.
	 * 
	 * @param selection
	 *            {@link ISelection}
	 */
	private void fireEdit(ISelection selection) {
		// then fire the selection
		managedForm.fireSelectionChanged(this, selection);
		if (selection.isEmpty()) {
			selectedAssignment = null; // NOPMD
		} else {
			selectedAssignment = (AbstractClassSensorAssignment<?>) ((StructuredSelection) selection).getFirstElement();
		}
		updateButtonsState(selection);
	}

	/**
	 * Checks if we are in dirty state.
	 */
	private void markDirty() {
		if (!dirty) {
			dirty = true;
			managedForm.dirtyStateChanged();
		}
	}

	/**
	 * Removes the error decoration for the sensor assignment.
	 * 
	 * @param sensorAssignment
	 *            {@link AbstractClassSensorAssignment}.
	 * @param message
	 *            Message to display.
	 * @return
	 */
	private void showTableItemControlDecoration(AbstractClassSensorAssignment<?> sensorAssignment, String message) {
		TableViewer tableViewer = getActiveTableViewer();
		if (null == tableViewer) {
			return;
		}

		// first check if we have it, if so shown
		for (TableItemControlDecoration decoration : tableItemControlDecorations) {
			if (sensorAssignment == decoration.getAssignment()) { // NOPMD == on purpose
				decoration.show();
				decoration.setDescriptionText(message);
				return;
			}
		}

		// if not find appropriate table item to place it
		for (TableItem tableItem : tableViewer.getTable().getItems()) {
			if (tableItem.getData() == sensorAssignment) { // NOPMD == on purpose
				TableItemControlDecoration decoration = new TableItemControlDecoration(tableItem);
				decoration.show();
				decoration.setDescriptionText(message);

				tableItemControlDecorations.add(decoration);
				return;
			}
		}
	}

	/**
	 * Removes the error decoration for the sensor assignment.
	 * 
	 * @param sensorAssignment
	 *            {@link AbstractClassSensorAssignment}.
	 */
	private void hideTableItemControlDecoration(AbstractClassSensorAssignment<?> sensorAssignment) {
		TableViewer tableViewer = getActiveTableViewer();
		if (null == tableViewer) {
			return;
		}

		// remove if it's there
		for (Iterator<TableItemControlDecoration> it = tableItemControlDecorations.iterator(); it.hasNext();) {
			TableItemControlDecoration decoration = it.next();
			if (sensorAssignment == decoration.getAssignment()) { // NOPMD == on purpose
				decoration.hide();
				return;
			}
		}
	}

	/**
	 * Returns message key to be used with the {@link IMessageManager} when reporting errors with
	 * provided assignment.
	 * 
	 * @param sensorAssignment
	 *            Assignment
	 * @return Object to be used as a key
	 */
	private Object getMessageKey(AbstractClassSensorAssignment<?> sensorAssignment) {
		return System.identityHashCode(sensorAssignment);
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
	public void setFocus() {
		tabFolder.setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			dirty = false;

			List<MethodSensorAssignment> methodSensorAssignments = new ArrayList<>();
			List<ExceptionSensorAssignment> exceptionSensorAssignments = new ArrayList<>();
			for (List<AbstractClassSensorAssignment<?>> assignments : configToAssignmentMap.values()) {
				for (AbstractClassSensorAssignment<?> assignment : assignments) {
					if (assignment instanceof MethodSensorAssignment) {
						methodSensorAssignments.add((MethodSensorAssignment) assignment);
					} else if (assignment instanceof ExceptionSensorAssignment) {
						exceptionSensorAssignments.add((ExceptionSensorAssignment) assignment);
					}
				}
			}
			profile.setMethodSensorAssignments(methodSensorAssignments);
			profile.setExceptionSensorAssignments(exceptionSensorAssignments);
		}
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
	public boolean setFormInput(Object input) {
		return false;
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
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		updateButtonsState((StructuredSelection) selection);
		fireEdit(selection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			ProfileEditorInput input = (ProfileEditorInput) formPage.getEditor().getEditorInput();
			profile = input.getProfile();
		}

	}

	/**
	 * Returns short (1 line) error message for the assignment based on the validation decorations.
	 * 
	 * @param sensorAssignment
	 *            assignment
	 * @param validationDecorations
	 *            {@link ValidationControlDecoration}
	 * @return short error message
	 */
	private String getErroMessageShort(AbstractClassSensorAssignment<?> sensorAssignment, List<ValidationControlDecoration<?>> validationDecorations) {
		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatter.getSensorConfigName(sensorAssignment.getSensorConfigClass()));
		builder.append(" Assignment (");
		int count = 0;
		for (ValidationControlDecoration<?> decoration : validationDecorations) {
			if (!decoration.isValid()) {
				count++;
			}
		}
		builder.append(count);
		if (count > 1) {
			builder.append(" fields contain validation errors)");
		} else {
			builder.append(" field contains validation error)");
		}

		return builder.toString();
	}

	/**
	 * Returns full error message for the assignment based on the validation decorations. In this
	 * message each line will contain error reported by any invalid
	 * {@link ValidationControlDecoration}
	 * 
	 * @param sensorAssignment
	 *            assignment
	 * @param validationDecorations
	 *            {@link ValidationControlDecoration}
	 * @return fill error message
	 */
	private String getErroMessageFull(AbstractClassSensorAssignment<?> sensorAssignment, List<ValidationControlDecoration<?>> validationDecorations) {
		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatter.getSensorConfigName(sensorAssignment.getSensorConfigClass()));
		builder.append(" Assignment:");

		IMessagePrefixProvider messagePrefixProvider = managedForm.getMessageManager().getMessagePrefixProvider();
		for (ValidationControlDecoration<?> decoration : validationDecorations) {
			if (!decoration.isValid()) {
				builder.append('\n');
				String prefix = messagePrefixProvider.getPrefix(decoration.getControl());
				// don't append if no prefix can be found
				if (!": ".equals(prefix)) {
					builder.append(prefix);
				}
				builder.append(decoration.getDescriptionText());
			}
		}
		return builder.toString();
	}

	/**
	 * Helper selection class to denote remove was executed.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class RemoveSelection extends StructuredSelection {

		/**
		 * Constructor.
		 * 
		 * @param elements
		 *            removed elements
		 */
		public RemoveSelection(List<?> elements) {
			super(elements);
		}

	}

	/**
	 * Class to help with displaying control decorations on the table rows.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class TableItemControlDecoration extends ControlDecoration {

		/**
		 * TableItem to create decoration for.
		 */
		private TableItem tableItem;

		/**
		 * Internal {@link TableEditor} to show decoration.
		 */
		private TableEditor tableEditor;

		/**
		 * Assignment being connected to the table item.
		 */
		private AbstractClassSensorAssignment<?> assignment;

		/**
		 * Constructor.
		 * 
		 * @param tableItem
		 *            TableItem to create decoration for.
		 */
		public TableItemControlDecoration(TableItem tableItem) {
			super(new Composite(tableItem.getParent(), SWT.NONE), SWT.BOTTOM);
			Assert.isNotNull(tableItem);
			Assert.isLegal(tableItem.getData() instanceof AbstractClassSensorAssignment);

			this.tableItem = tableItem;
			this.assignment = (AbstractClassSensorAssignment<?>) tableItem.getData();
			tableEditor = new TableEditor(tableItem.getParent());
			tableEditor.horizontalAlignment = SWT.LEFT;
			tableEditor.verticalAlignment = SWT.BOTTOM;
			tableEditor.setEditor(getControl(), tableItem, 0);

			setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
			hide();

			tableItem.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					// in any case hide, dispose and remove
					tableItemControlDecorations.remove(TableItemControlDecoration.this);
					hide();
					dispose();
				}
			});
		}

		/**
		 * Gets {@link #assignment}.
		 * 
		 * @return {@link #assignment}
		 */
		public AbstractClassSensorAssignment<?> getAssignment() {
			return assignment;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			Control c = getControl();
			if (!tableItem.isDisposed()) {
				tableEditor.dispose();
			}

			super.dispose();

			// we need to dispose the composite that we have created
			if (null != c) {
				c.dispose();
			}
		}
	}

	/**
	 * {@link ISensorAssignmentUpdateListener} to handle the validations in the master view.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class MasterBlockValidationListener implements ISensorAssignmentUpdateListener {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void sensorAssignmentUpdated(AbstractClassSensorAssignment<?> sensorAssignment, boolean dirty, boolean isValid, List<ValidationControlDecoration<?>> validationDecorations) {
			Assert.isNotNull(sensorAssignment);

			if (dirty) {
				TableViewer tableViewer = getActiveTableViewer();
				tableViewer.refresh();

				markDirty();
			}

			Object key = getMessageKey(sensorAssignment);
			if (!isValid) {
				String fullErrorMessage = getErroMessageFull(sensorAssignment, validationDecorations);
				invalidAssignments.put(sensorAssignment, fullErrorMessage);
				showTableItemControlDecoration(sensorAssignment, fullErrorMessage);
				formPage.getManagedForm().getMessageManager().addMessage(key, getErroMessageShort(sensorAssignment, validationDecorations), null, IMessageProvider.ERROR);
			} else {
				invalidAssignments.remove(sensorAssignment);
				hideTableItemControlDecoration(sensorAssignment);
				formPage.getManagedForm().getMessageManager().removeMessage(key);
			}
		}

	}

}
