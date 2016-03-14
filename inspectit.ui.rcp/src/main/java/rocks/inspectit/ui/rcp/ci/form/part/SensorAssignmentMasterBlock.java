package rocks.inspectit.ui.rcp.ci.form.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.all.storage.serializer.impl.SerializationManager;
import rocks.inspectit.shared.all.storage.serializer.provider.SerializationManagerProvider;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;
import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.dialog.SensorAssignmentSelectionDialog;
import rocks.inspectit.ui.rcp.ci.form.input.ProfileEditorInput;
import rocks.inspectit.ui.rcp.ci.listener.IDetailsModifiedListener;
import rocks.inspectit.ui.rcp.ci.widget.SensorAssignmentTableProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.util.RemoveSelection;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.TableItemControlDecorationManager;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

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
	 * Sensor assignment profile data.
	 */
	private SensorAssignmentProfileData profileData;

	/**
	 * Map of assignments.
	 */
	private final Map<Class<? extends ISensorConfig>, List<AbstractClassSensorAssignment<?>>> configToAssignmentMap = new HashMap<>();

	/**
	 * Map of sensors to the CTabItem.
	 */
	private final Map<Class<? extends ISensorConfig>, CTabItem> sensorToTabMap = new HashMap<>();

	/**
	 * List of created {@link TableViewer}.
	 */
	private final List<TableViewer> tableViewers = new ArrayList<>();

	/**
	 * {@link TableItemControlDecorationManager}.
	 */
	private final TableItemControlDecorationManager tableItemControlDecorationManager = new TableItemControlDecorationManager();

	/**
	 * Validation manager for this master block.
	 */
	private final SensorAssignmentValidationManager validationManager = new SensorAssignmentValidationManager();

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
	private final FormPage formPage;

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
	 * {@link SerializationManager} used for duplicating assignments.
	 */
	private final SerializationManager serializationManager;

	/**
	 * @param formPage
	 *            {@link FormPage} part being displayed.
	 */
	public SensorAssignmentMasterBlock(FormPage formPage) {
		checkAndGetEditorInput();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		this.serializationManager = InspectIT.getService(SerializationManagerProvider.class).createSerializer();
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
		IDetailsModifiedListener<AbstractClassSensorAssignment<?>> masterBlockListener = new DetailsModifiedListener();
		detailsPart.registerPage(MethodSensorAssignment.class, new MethodSensorAssignmentDetailsPage(masterBlockListener, validationManager, !profile.isCommonProfile()));
		detailsPart.registerPage(TimerMethodSensorAssignment.class, new TimerSensorAssignmentDetailsPage(masterBlockListener, validationManager, !profile.isCommonProfile()));
		detailsPart.registerPage(ExceptionSensorAssignment.class, new ExceptionSensorAssignmentDetailsPage(masterBlockListener, validationManager, !profile.isCommonProfile()));
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
						AbstractClassSensorAssignment<?> assignment = (AbstractClassSensorAssignment<?>) selectedObject;
						AbstractClassSensorAssignment<?> clone = serializationManager.copy(assignment);
						addToInputMap(clone);
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
			}
		}
		// inform details page about removal
		RemoveSelection removeSelection = new RemoveSelection(selection.toList());
		fireEdit(removeSelection);

		// Refresh and deal with the possible changes in the table row structure, so re-check error
		// descriptors
		tableViewer.refresh();
		validationManager.checkTableControlDecorations();

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
		if (CollectionUtils.isNotEmpty(profileData.getMethodSensorAssignments())) {
			for (MethodSensorAssignment methodSensorAssignment : profileData.getMethodSensorAssignments()) {
				addToInputMap(methodSensorAssignment);
			}
		}
		if (CollectionUtils.isNotEmpty(profileData.getExceptionSensorAssignments())) {
			for (ExceptionSensorAssignment exceptionSensorAssignment : profileData.getExceptionSensorAssignments()) {
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

		validationManager.validationStatesRemoved(assignment);
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
			profileData.setMethodSensorAssignments(methodSensorAssignments);
			profileData.setExceptionSensorAssignments(exceptionSensorAssignments);
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
		updateButtonsState(selection);
		fireEdit(selection);
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
	 * Checks that the editor input has profile with the {@link SensorAssignmentProfileData}. If so,
	 * sets the {@link #profile} and {@link #profileData}.
	 */
	private void checkAndGetEditorInput() {
		ProfileEditorInput input = (ProfileEditorInput) formPage.getEditor().getEditorInput();

		Assert.isNotNull(input.getProfile());
		Assert.isNotNull(input.getProfile().getProfileData());
		Assert.isLegal(input.getProfile().getProfileData().isOfType(SensorAssignmentProfileData.class), "Given profile can not be opened with the exclude rules part.");

		this.profile = input.getProfile();
		this.profileData = profile.getProfileData().getIfInstance(SensorAssignmentProfileData.class);
	}

	/**
	 * Validation manager for this master block.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class SensorAssignmentValidationManager extends AbstractValidationManager<AbstractClassSensorAssignment<?>> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void notifyUpstream(AbstractClassSensorAssignment<?> key, Set<ValidationState> states) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void showMessage(AbstractClassSensorAssignment<?> key, Set<ValidationState> states) {
			tableItemControlDecorationManager.showTableItemControlDecoration(getActiveTableViewer(), key, getErroMessageFull(key, states));
			formPage.getManagedForm().getMessageManager().addMessage(key, getErroMessageShort(key, states), null, IMessageProvider.ERROR);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void hideMessage(AbstractClassSensorAssignment<?> key) {
			tableItemControlDecorationManager.hideTableItemControlDecoration(getActiveTableViewer(), key);
			formPage.getManagedForm().getMessageManager().removeMessage(key);
		}

		/**
		 * Re-checks the control decorations. Should be called when elements are removed from the
		 * table viewer.
		 */
		public void checkTableControlDecorations() {
			TableViewer activeTableViewer = getActiveTableViewer();
			Object sensorClass = activeTableViewer.getTable().getData();
			for (List<AbstractClassSensorAssignment<?>> assignments : configToAssignmentMap.values()) {
				for (AbstractClassSensorAssignment<?> assignment : assignments) {
					if (!Objects.equals(sensorClass, assignment.getSensorConfigClass())) {
						continue;
					}

					Set<ValidationState> states = super.getValidationErrorStates(assignment);
					if (CollectionUtils.isNotEmpty(states)) {
						tableItemControlDecorationManager.showTableItemControlDecoration(activeTableViewer, assignment, getErroMessageFull(assignment, states));
					} else {
						tableItemControlDecorationManager.hideTableItemControlDecoration(activeTableViewer, assignment);
					}
				}
			}
		}

		/**
		 * Returns short (1 line) error message for the assignment based on the validation states.
		 *
		 * @param sensorAssignment
		 *            assignment
		 * @param states
		 *            {@link ValidationControlDecoration}
		 * @return short error message
		 */
		private String getErroMessageShort(AbstractClassSensorAssignment<?> sensorAssignment, Collection<ValidationState> states) {
			StringBuilder builder = new StringBuilder();
			builder.append(TextFormatter.getSensorConfigName(sensorAssignment.getSensorConfigClass()));
			builder.append(" Assignment (");
			int count = 0;
			for (ValidationState state : states) {
				if (!state.isValid()) {
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
		 * Returns full error message for the assignment based on the validation states. In this
		 * message each line will contain error reported by any invalid {@link ValidationState}
		 *
		 * @param sensorAssignment
		 *            assignment
		 * @param states
		 *            {@link ValidationState}s
		 * @return fill error message
		 */
		private String getErroMessageFull(AbstractClassSensorAssignment<?> sensorAssignment, Collection<ValidationState> states) {
			StringBuilder builder = new StringBuilder();
			builder.append(TextFormatter.getSensorConfigName(sensorAssignment.getSensorConfigClass()));
			builder.append(" Assignment:");

			for (ValidationState state : states) {
				if (!state.isValid()) {
					builder.append('\n');
					builder.append(state.getMessage());
				}
			}
			return builder.toString();
		}

	}

	/**
	 * {@link ISensorAssignmentUpdateListener} to handle the validations in the master view.
	 *
	 * @author Ivan Senic
	 *
	 */
	private class DetailsModifiedListener implements IDetailsModifiedListener<AbstractClassSensorAssignment<?>> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void contentModified(AbstractClassSensorAssignment<?> modifiedElement) {
			Assert.isNotNull(modifiedElement);

			TableViewer tableViewer = getActiveTableViewer();
			tableViewer.refresh(modifiedElement);

			markDirty();
		}

	}

}
