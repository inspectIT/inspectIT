package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.TimerMethodSensorAssignment;
import info.novatec.inspectit.ci.context.AbstractContextCapture;
import info.novatec.inspectit.ci.context.impl.FieldContextCapture;
import info.novatec.inspectit.ci.context.impl.ParameterContextCapture;
import info.novatec.inspectit.ci.context.impl.ReturnContextCapture;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.dialog.CaptureContextDialog;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * The details page for the {@link TimerMethodSensorAssignment}.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerSensorAssignmentDetailsPage extends MethodSensorAssignmentDetailsPage {

	/**
	 * Element being displayed.
	 */
	private TimerMethodSensorAssignment assignment;

	/**
	 * Context captures being displayed.
	 */
	private List<AbstractContextCapture> contextCaptures = new ArrayList<>();

	/**
	 * Selection for activating context capturing.
	 */
	private Button captureContextButton;

	/**
	 * Table for displaying captured context definitions.
	 */
	private TableViewer captureContextTableViewer;

	/**
	 * Add capture button.
	 */
	private Button addCaptureButton;

	/**
	 * Remove capture button.
	 */
	private Button removeCaptureButton;

	/**
	 * Selection for the invocation to be started or not.
	 */
	private Button startInvocationButton;

	/**
	 * Text box for the minimum time of the invocation to be send to the CMR.
	 */
	private Text minDurationText;

	/**
	 * Selection for if charting should be active.
	 */
	private Button chartingButton;

	/**
	 * Constructor.
	 * 
	 * @param masterBlockListener
	 *            listener to inform the master block on changes to the input
	 * @param canEdit
	 *            If the data can be edited.
	 */
	public TimerSensorAssignmentDetailsPage(ISensorAssignmentUpdateListener masterBlockListener, boolean canEdit) {
		super(masterBlockListener, canEdit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		TableWrapLayout parentLayout = new TableWrapLayout();
		parentLayout.topMargin = 5;
		parentLayout.leftMargin = 5;
		parentLayout.rightMargin = 2;
		parentLayout.bottomMargin = 2;
		parentLayout.numColumns = 2;
		parentLayout.makeColumnsEqualWidth = true;
		parent.setLayout(parentLayout);

		FormToolkit toolkit = managedForm.getToolkit();

		// abstract method definition
		super.createContents(parent, false);

		// special sensor definitions
		// section
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Sensor specific options");
		section.marginWidth = 10;
		section.marginHeight = 5;
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		section.setLayoutData(td);

		// main composite
		Composite mainComposite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(7, false);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		mainComposite.setLayout(layout);
		section.setClient(mainComposite);

		// capture context
		// first row
		toolkit.createLabel(mainComposite, "Capture context:");
		captureContextButton = toolkit.createButton(mainComposite, "Yes", SWT.CHECK);
		captureContextButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
		createInfoLabel(mainComposite, toolkit, "");

		// second row
		toolkit.createLabel(mainComposite, "");
		// table
		final Table captureContextTable = toolkit.createTable(mainComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		GridData captureContextGridData = new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1);
		captureContextGridData.heightHint = 120;
		captureContextTable.setLayoutData(captureContextGridData);
		captureContextTable.setHeaderVisible(true);
		// table viewer
		captureContextTableViewer = new TableViewer(captureContextTable);
		captureContextTableViewer.setContentProvider(new ArrayContentProvider());
		captureContextTableViewer.setInput(contextCaptures);
		createColumnsForcaptureParametersTable();
		// buttons
		Composite captureContextComposite = toolkit.createComposite(mainComposite);
		captureContextComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		captureContextComposite.setLayout(new FillLayout(SWT.VERTICAL));
		addCaptureButton = toolkit.createButton(captureContextComposite, "", SWT.PUSH);
		addCaptureButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_ADD));
		removeCaptureButton = toolkit.createButton(captureContextComposite, "", SWT.PUSH);
		removeCaptureButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_REMOVE));
		toolkit.createLabel(mainComposite, "");
		// decoration
		final ValidationControlDecoration<Table> nonEmptyCaptureContextDecoration = new ValidationControlDecoration<Table>(captureContextTable, this) {
			@Override
			protected boolean validate(Table control) {
				if (captureContextButton.getSelection() && contextCaptures.isEmpty()) {
					return false;
				}
				return true;
			}
		};
		nonEmptyCaptureContextDecoration.setDescriptionText("At least one capture context definition must be defined if the capture context is active.");
		addValidationControlDecoration(nonEmptyCaptureContextDecoration);
		// listeners
		captureContextButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean captureActive = captureContextButton.getSelection();
				captureContextTable.setEnabled(captureActive);
				addCaptureButton.setEnabled(captureActive);
				removeCaptureButton.setEnabled(captureActive);
				nonEmptyCaptureContextDecoration.executeValidation();
			}
		});
		addCaptureButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				CaptureContextDialog dialog = new CaptureContextDialog(managedForm.getForm().getShell());
				if (dialog.open() == Dialog.OK) {
					AbstractContextCapture contextCapture = dialog.getContextCapture();
					contextCaptures.add(contextCapture);
					captureContextTableViewer.refresh();
					nonEmptyCaptureContextDecoration.executeValidation();
					getMarkDirtyListener().handleEvent(event);
				}
			}
		});
		removeCaptureButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) captureContextTableViewer.getSelection();
				if (!selection.isEmpty()) {
					for (Object selectedObject : selection.toArray()) {
						contextCaptures.remove(selectedObject);
					}
					captureContextTableViewer.refresh();
					nonEmptyCaptureContextDecoration.executeValidation();
					getMarkDirtyListener().handleEvent(event);
				}
			}
		});
		captureContextTableViewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StructuredSelection selection = (StructuredSelection) captureContextTableViewer.getSelection();
				if (!selection.isEmpty()) {
					AbstractContextCapture selected = (AbstractContextCapture) selection.getFirstElement();
					CaptureContextDialog dialog = new CaptureContextDialog(managedForm.getForm().getShell(), selected);
					if (dialog.open() == Dialog.OK) {
						AbstractContextCapture contextCapture = dialog.getContextCapture();
						int index = contextCaptures.indexOf(selected);
						contextCaptures.remove(index);
						contextCaptures.add(index, contextCapture);
						captureContextTableViewer.refresh();
						nonEmptyCaptureContextDecoration.executeValidation();
						getMarkDirtyListener().handleEvent(event);
					}
				}
			}
		});

		// charting
		toolkit.createLabel(mainComposite, "Charting:");
		chartingButton = toolkit.createButton(mainComposite, "Yes", SWT.CHECK);
		chartingButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
		createInfoLabel(
				mainComposite,
				toolkit,
				"With the charting option it is possible to define what data should be considered as the long-term data available for charting in inspectIT User interface. This data is additionally saved to the database, thus even when the CMR is shutdown or buffer is cleared the data will be available via charts.");

		// starts invocation
		toolkit.createLabel(mainComposite, "Starts invocation:");
		startInvocationButton = toolkit.createButton(mainComposite, "Yes", SWT.CHECK);
		toolkit.createLabel(mainComposite, "Min duration:", SWT.RIGHT).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		minDurationText = toolkit.createText(mainComposite, "", SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd.widthHint = 50;
		minDurationText.setLayoutData(gd);
		toolkit.createLabel(mainComposite, "");
		createInfoLabel(mainComposite, toolkit,
				"Defines if method should start an invocation. Minimum duration defines the minimum time in milliseconds an invocation has to consume in order to be saved and transmitted to the server.");
		// validation
		// method validation
		final ValidationControlDecoration<Text> minDurationValidationDecoration = new ValidationControlDecoration<Text>(minDurationText, null, this) {
			@Override
			protected boolean validate(Text control) {
				if (StringUtils.isNotEmpty(control.getText())) {
					try {
						return Long.parseLong(control.getText()) > 0;
					} catch (NumberFormatException e) {
						return false;
					}
				} else {
					return true;
				}
			}
		};
		minDurationValidationDecoration.setDescriptionText("Value must be positive amount of milliseconds.");
		minDurationValidationDecoration.registerListener(SWT.Modify);
		addValidationControlDecoration(minDurationValidationDecoration);

		// listener
		startInvocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				minDurationText.setEnabled(startInvocationButton.getSelection());
				minDurationValidationDecoration.executeValidation();
			}
		});

		// dirty listener
		captureContextButton.addListener(SWT.Selection, getMarkDirtyListener());
		chartingButton.addListener(SWT.Selection, getMarkDirtyListener());
		startInvocationButton.addListener(SWT.Selection, getMarkDirtyListener());
		minDurationText.addListener(SWT.Modify, getMarkDirtyListener());

		if (!isCanEdit()) {
			setEnabled(mainComposite, false);
		}
	}

	/**
	 * Creates columns for parameters table.
	 */
	private void createColumnsForcaptureParametersTable() {
		TableViewerColumn typeColumn = new TableViewerColumn(captureContextTableViewer, SWT.NONE);
		typeColumn.getColumn().setResizable(true);
		typeColumn.getColumn().setWidth(150);
		typeColumn.getColumn().setText("Catch Type");
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof ReturnContextCapture) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_RETURN);
				} else if (element instanceof ParameterContextCapture) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_PARAMETER);
				} else if (element instanceof FieldContextCapture) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_FIELD);
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				if (element instanceof ReturnContextCapture) {
					return "Return value";
				} else if (element instanceof ParameterContextCapture) {
					return "Parameter index " + ((ParameterContextCapture) element).getIndex();
				} else if (element instanceof FieldContextCapture) {
					return "Field " + ((FieldContextCapture) element).getFieldName();
				}
				return "";
			}
		});

		TableViewerColumn nameColumn = new TableViewerColumn(captureContextTableViewer, SWT.NONE);
		nameColumn.getColumn().setResizable(true);
		nameColumn.getColumn().setWidth(100);
		nameColumn.getColumn().setText("Name");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AbstractContextCapture) element).getDisplayName();
			}
		});

		TableViewerColumn pathsColumn = new TableViewerColumn(captureContextTableViewer, SWT.NONE);
		pathsColumn.getColumn().setResizable(true);
		pathsColumn.getColumn().setWidth(100);
		pathsColumn.getColumn().setText("Accessor Path");
		pathsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Collection<String> paths = ((AbstractContextCapture) element).getPaths();
				if (CollectionUtils.isNotEmpty(paths)) {
					StringBuilder stringBuilder = new StringBuilder();
					for (String path : paths) {
						stringBuilder.append(" -> ");
						stringBuilder.append(path);
					}
					return stringBuilder.toString();
				} else {
					return "";
				}

			}
		});
	}

	/**
	 * Updates the display state.
	 */
	@Override
	protected void updateFromInput() {
		super.updateFromInput();
		captureContextButton.setSelection(false);
		chartingButton.setSelection(false);
		startInvocationButton.setSelection(false);
		minDurationText.setEnabled(false);
		minDurationText.setText("");
		contextCaptures.clear();
		if (null != assignment) {
			if (CollectionUtils.isNotEmpty(assignment.getContextCaptures())) {
				captureContextButton.setSelection(true);
				captureContextTableViewer.getTable().setEnabled(isCanEdit());
				addCaptureButton.setEnabled(isCanEdit());
				removeCaptureButton.setEnabled(isCanEdit());
				contextCaptures.addAll(assignment.getContextCaptures());
			} else {
				captureContextButton.setSelection(false);
				captureContextTableViewer.getTable().setEnabled(false);
				addCaptureButton.setEnabled(false);
				removeCaptureButton.setEnabled(false);
			}
			chartingButton.setSelection(assignment.isCharting());
			if (assignment.isStartsInvocation()) {
				startInvocationButton.setSelection(true);
				minDurationText.setEnabled(isCanEdit());
				if (0 != assignment.getMinInvocationDuration()) {
					minDurationText.setText(String.valueOf(assignment.getMinInvocationDuration()));
				}
			}
		}
		captureContextTableViewer.refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void commitToInput() {
		super.commitToInput();
		if (captureContextButton.getSelection() && CollectionUtils.isNotEmpty(contextCaptures)) {
			assignment.setContextCaptures(new ArrayList<>(contextCaptures));
		} else {
			assignment.setContextCaptures(null);
		}
		assignment.setStartsInvocation(startInvocationButton.getSelection());
		if (startInvocationButton.getSelection()) {
			String minDuration = minDurationText.getText();
			if (StringUtils.isNotBlank(minDuration)) {
				try {
					assignment.setMinInvocationDuration(Long.parseLong(minDuration));
				} catch (NumberFormatException e) {
					assignment.setMinInvocationDuration(0L);
				}
			} else {
				assignment.setMinInvocationDuration(0L);
			}
		}
		assignment.setCharting(chartingButton.getSelection());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected MethodSensorAssignment getInput() {
		return assignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setInput(ISelection selection) {
		super.setInput(selection);
		if (!selection.isEmpty()) {
			assignment = (TimerMethodSensorAssignment) ((IStructuredSelection) selection).getFirstElement();
		} else {
			assignment = null; // NOPMD
		}
	}

}
