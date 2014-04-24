package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.TimerMethodSensorAssignment;
import info.novatec.inspectit.ci.sensor.ISensorConfig;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.dialog.SensorAssignmentSelectionDialog;
import info.novatec.inspectit.rcp.ci.form.input.ProfileEditorInput;
import info.novatec.inspectit.rcp.ci.widget.SensorAssignmentTableProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Abstract tree master block for the sensor definition form page.
 * 
 * @author Ivan Senic
 * 
 */
public class SensorAssignmentMasterBlock extends MasterDetailsBlock implements IFormPart, IPartSelectionListener, ISelectionChangedListener, IPropertyListener {

	/** L O G I C S */

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
	 * List of created {@link TableViewer}.
	 */
	private List<TableViewer> tableViewers = new ArrayList<>();

	/**
	 * Assignment currently being edited or <code>null</code> if no edit is done in the moment.
	 */
	private AbstractClassSensorAssignment<?> selectedAssignment;

	/**
	 * If adding of element is in progress.
	 */
	protected boolean addInProgress;

	/**
	 * Dirty state.
	 */
	private boolean dirty;

	/** W I G E T S. */
	protected FormPage formPage; // NOCHK
	protected IManagedForm managedForm; // NOCHK
	private CTabFolder tabFolder; // NOCHK
	private Button addButton; // NOCHK
	private Button removeButton; // NOCHK
	private Button duplicateButton; // NOCHK

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
		detailsPart.registerPage(MethodSensorAssignment.class, new MethodSensorAssignmentDetailsPage(!profile.isCommonProfile()));
		detailsPart.registerPage(TimerMethodSensorAssignment.class, new TimerSensorAssignmentDetailsPage(!profile.isCommonProfile()));
		detailsPart.registerPage(ExceptionSensorAssignment.class, new ExceptionSensorAssignmentDetailsPage(!profile.isCommonProfile()));
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
		Composite mainComposite = toolkit.createComposite(section, SWT.WRAP);
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
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		addButton = toolkit.createButton(buttonComposite, "Add", SWT.PUSH);
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
					StructuredSelection ss = new StructuredSelection(toAdd);
					tableViewer.refresh();
					tableViewer.setSelection(ss);

					fireEdit(ss);
					addInProgress = true;

					showHideFormMessage();
					markDirty();
				}
			}

		});

		removeButton = toolkit.createButton(buttonComposite, "Remove", SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableViewer tableViewer = getActiveTableViewer();
				StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
				Class<?> sensorClass = null;
				for (Object selectedObject : selection.toArray()) {
					if (selectedObject instanceof AbstractClassSensorAssignment) {
						removeFromInputMap((AbstractClassSensorAssignment<?>) selectedObject);

						// remember sensor class
						sensorClass = ((AbstractClassSensorAssignment<?>) selectedObject).getSensorConfigClass();

						// if we are removing currently selected assignment then reset edit
						// selection
						if (Objects.equals(selectedObject, selectedAssignment)) {
							fireEdit(StructuredSelection.EMPTY);
						}
					}
				}
				tableViewer.refresh();

				// if we don't have any more assignment for sensor remove tab
				if (CollectionUtils.isEmpty(configToAssignmentMap.get(sensorClass))) {
					CTabItem tabItem = sensorToTabMap.remove(sensorClass);
					int index = tabFolder.indexOf(tabItem);
					tableViewers.remove(index);
					tabItem.dispose();
				}

				markDirty();
				showHideFormMessage();
			}
		});

		duplicateButton = toolkit.createButton(buttonComposite, "Duplicate", SWT.PUSH);
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
		for (Entry<Class<? extends ISensorConfig>, List<AbstractClassSensorAssignment<?>>> entry : getInput().entrySet()) {
			createNewTabItem(entry.getKey(), entry.getValue());
		}

		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableViewer tableViewer = getActiveTableViewer();
				if (null != tableViewer) {
					tableViewer.refresh();
					tableViewer.getTable().setFocus();
					SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(tableViewer, tableViewer.getSelection());
					SensorAssignmentMasterBlock.this.selectionChanged(selectionChangedEvent);
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
		if (index != -1) {
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
		SensorAssignmentTableProvider tableProvider = new SensorAssignmentTableProvider(tabFolder, this);
		tableProvider.setInput(assignments);

		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setControl(tableProvider.getTableViewer().getTable());
		tabItem.setText(TextFormatter.getSensorConfigName(sensorClass));
		tabItem.setImage(ImageFormatter.getSensorConfigImage(sensorClass));

		sensorToTabMap.put(sensorClass, tabItem);
		tableViewers.add(tableProvider.getTableViewer());
	}

	/**
	 * Shows or hides the form message info.
	 */
	private void showHideFormMessage() {
		if (profile.isCommonProfile()) {
			managedForm.getForm().setMessage("Common profiles can not be edited", IMessageProvider.NONE);
		} else if (configToAssignmentMap.isEmpty()) {
			managedForm.getForm().setMessage("No sensor assignment defined", IMessageProvider.INFORMATION);
		} else {
			managedForm.getForm().setMessage(null, IMessageProvider.NONE);
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
	 * Creates input map and returns the input.
	 * 
	 * @return Input for the tree
	 */
	private Map<Class<? extends ISensorConfig>, List<AbstractClassSensorAssignment<?>>> getInput() {
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
	}

	/**
	 * Updates the state of the remove button depending on the current selection.
	 * 
	 * @param structuredSelection
	 *            Current selection.
	 */
	private void updateButtonsState(StructuredSelection structuredSelection) {
		if (structuredSelection.isEmpty()) {
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
		if (null != detailsPart) {
			if (isCurrentPageValid()) {
				if (detailsPart.isDirty()) {
					markDirty();
				}
				detailsPart.commit(false);

				addInProgress = false;
			} else {
				if (addInProgress) {
					removeFromInputMap(selectedAssignment);

					// if we don't have any more assignment for sensor remove tab
					if (CollectionUtils.isEmpty(configToAssignmentMap.get(selectedAssignment.getSensorConfigClass()))) {
						CTabItem tabItem = sensorToTabMap.remove(selectedAssignment.getSensorConfigClass());
						int index = tabFolder.indexOf(tabItem);
						tableViewers.remove(index);
						tabItem.dispose();
					}

					showHideFormMessage();
					addInProgress = false;

					TableViewer tableViewer = getActiveTableViewer();
					if (null != tableViewer) {
						tableViewer.remove(selectedAssignment);
					}
				}
			}

			if (null != selectedAssignment) {
				TableViewer tableViewer = getActiveTableViewer();
				if (null != tableViewer) {
					tableViewer.update(selectedAssignment, null);
				}
			}
		}

		// then fire the selection
		managedForm.fireSelectionChanged(this, selection);
		if (selection.isEmpty()) {
			selectedAssignment = null; // NOPMD
		} else {
			selectedAssignment = (AbstractClassSensorAssignment<?>) ((StructuredSelection) selection).getFirstElement();
		}
	}

	/**
	 * Returns if the current details page is valid.
	 * 
	 * @return Returns if the current details page is valid.
	 */
	private boolean isCurrentPageValid() {
		IDetailsPage detailsPage = detailsPart.getCurrentPage();
		if (detailsPage instanceof AbstractClassSensorAssignmentDetailsPage) {
			AbstractClassSensorAssignmentDetailsPage assignmentDetailsPage = (AbstractClassSensorAssignmentDetailsPage) detailsPage;
			return assignmentDetailsPage.isValid();
		}
		return true;
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
		dirty = false;
		managedForm.dirtyStateChanged();

		detailsPart.commit(onSave);

		if (onSave) {
			// if add is in progress make sure input is valid
			// otherwise don't
			if (addInProgress) {
				if (!isCurrentPageValid() && null != selectedAssignment) {
					removeFromInputMap(selectedAssignment);
				}
			}

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

}
