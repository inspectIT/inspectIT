package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.sensor.ISensorConfig;
import info.novatec.inspectit.ci.sensor.platform.AbstractPlatformSensorConfig;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Part responsible for selection of platform sensors.
 * 
 * @author Ivan Senic
 * 
 */
public class PlatformSensorSelectionPart extends SectionPart implements IPropertyListener {

	/**
	 * Environment being edited.
	 */
	private Environment environment;

	/**
	 * {@link TableViewer} for displaying the sensors.
	 */
	private TableViewer tableViewer;

	/**
	 * Form page part is created on.
	 */
	private FormPage formPage;

	/**
	 * Default constructor.
	 * 
	 * @param formPage
	 *            {@link FormPage} section belongs to.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public PlatformSensorSelectionPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createClient(getSection(), toolkit);

		// text and description on our own
		getSection().setText("Platform Sensors");
		Label label = toolkit.createLabel(getSection(), "Select platform sensor to be active within environment");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(label);
	}

	/**
	 * Creates complete client.
	 * 
	 * @param section
	 *            {@link Section}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createClient(Section section, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(section);
		mainComposite.setLayout(new GridLayout(1, true));
		section.setClient(mainComposite);

		Table table = toolkit.createTable(mainComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		createColumns();
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(environment.getPlatformSensorConfigs());
		tableViewer.refresh();

		updateCheckedItems();
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					if (!isDirty()) {
						markDirty();
					}
				}
			}
		});
	}

	/**
	 * Creates columns for table.
	 */
	private void createColumns() {
		TableViewerColumn activeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		activeColumn.getColumn().setResizable(false);
		activeColumn.getColumn().setWidth(60);
		activeColumn.getColumn().setText("Active");
		activeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
		});
		activeColumn.getColumn().setToolTipText("If sensor is active then it is sending monitoring data.");

		TableViewerColumn sensorNameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		sensorNameColumn.getColumn().setResizable(true);
		sensorNameColumn.getColumn().setWidth(250);
		sensorNameColumn.getColumn().setText("Sensor");
		sensorNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return TextFormatter.getSensorConfigName((ISensorConfig) element);
			}

			@Override
			public Image getImage(Object element) {
				return ImageFormatter.getSensorConfigImage((ISensorConfig) element);
			}
		});
		sensorNameColumn.getColumn().setToolTipText("Sensor type.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);

			for (TableItem item : tableViewer.getTable().getItems()) {
				AbstractPlatformSensorConfig sensorConfig = (AbstractPlatformSensorConfig) item.getData();
				sensorConfig.setActive(item.getChecked());
			}
		}
	}

	/**
	 * Updates states of the check boxes next to the elements.
	 */
	private void updateCheckedItems() {
		for (TableItem item : tableViewer.getTable().getItems()) {
			AbstractPlatformSensorConfig sensorConfig = (AbstractPlatformSensorConfig) item.getData();
			item.setChecked(sensorConfig.isActive());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
			environment = input.getEnvironment();

			tableViewer.setInput(environment.getPlatformSensorConfigs());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		super.dispose();
	}

}
