package info.novatec.inspectit.rcp.ci.widget;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.TimerMethodSensorAssignment;
import info.novatec.inspectit.ci.context.AbstractContextCapture;
import info.novatec.inspectit.ci.context.impl.FieldContextCapture;
import info.novatec.inspectit.ci.context.impl.ParameterContextCapture;
import info.novatec.inspectit.ci.context.impl.ReturnContextCapture;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.part.SensorAssignmentMasterBlock;
import info.novatec.inspectit.rcp.editor.tooltip.ColumnAwareToolTipSupport;
import info.novatec.inspectit.rcp.editor.viewers.ImageFixStyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * Table provider for the tab folder containing the sensor assignments.
 * 
 * @author Ivan Senic
 * 
 */
public class SensorAssignmentTableProvider {

	/**
	 * Viewer used to display the data.
	 */
	private TableViewer tableViewer;

	/**
	 * Constructor. Table can be retrieved by calling {@link #getTableViewer()}.
	 * 
	 * @param masterBlock
	 *            Master block containing this table.
	 * @param parent
	 *            Parent composite
	 */
	public SensorAssignmentTableProvider(SensorAssignmentMasterBlock masterBlock, Composite parent) {
		init(parent, masterBlock);
	}

	/**
	 * Initializes the table.
	 * 
	 * @param parent
	 *            Parent composite
	 * @param selectionChangedListener
	 *            selection change listener to report events to or <code>null</code> for no
	 *            reporting
	 */
	private void init(Composite parent, ISelectionChangedListener selectionChangedListener) {
		// Table
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(false);
		createColumns(tableViewer);
		tableViewer.setContentProvider(getContentProvider());
		tableViewer.setLabelProvider(getLabelProvider());
		ColumnAwareToolTipSupport.enableFor(tableViewer);

		if (null != selectionChangedListener) {
			tableViewer.addSelectionChangedListener(selectionChangedListener);
		}
	}

	/**
	 * Gets {@link #tableViewer}.
	 * 
	 * @return {@link #tableViewer}
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * Sets the input for the table in the tab item.
	 * 
	 * @param assignments
	 *            Assignments as input.
	 */
	public void setInput(List<AbstractClassSensorAssignment<?>> assignments) {
		tableViewer.setInput(assignments);
		tableViewer.setSelection(StructuredSelection.EMPTY);
	}

	/**
	 * Creates columns for Table.
	 * 
	 * @param tableViewer
	 *            Table viewer to create columns for.
	 */
	private void createColumns(TableViewer tableViewer) {
		TableViewerColumn classColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		classColumn.getColumn().setResizable(true);
		classColumn.getColumn().setWidth(350);
		classColumn.getColumn().setText("Class");
		classColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS));
		classColumn.getColumn().setToolTipText("Fully qualified name of the class or interface.");

		TableViewerColumn methodColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		methodColumn.getColumn().setResizable(true);
		methodColumn.getColumn().setWidth(450);
		methodColumn.getColumn().setText("Method");
		methodColumn.getColumn().setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_METHOD_PUBLIC));
		methodColumn.getColumn().setToolTipText("Method name with parameters. Note that constructors are displayed as '<init>' methods.");

		TableViewerColumn optionsColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		optionsColumn.getColumn().setResizable(true);
		optionsColumn.getColumn().setWidth(200);
		optionsColumn.getColumn().setText("Options");
		optionsColumn.getColumn().setToolTipText("Additional options that are defined for the sensor assignment.");
	}

	/**
	 * @return {@link IContentProvider}
	 */
	private IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}

	/**
	 * @return {@link IBaseLabelProvider} or null if each column has one set correctly.
	 */
	private IBaseLabelProvider getLabelProvider() {
		return new ImageFixStyledCellIndexLabelProvider() {

			/**
			 * The resource manager is used for the images etc.
			 */
			private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

			/**
			 * Empty styled string.
			 */
			private final StyledString empty = new StyledString("");

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected StyledString getStyledText(Object element, int index) {
				if (0 == index && element instanceof AbstractClassSensorAssignment<?>) {
					AbstractClassSensorAssignment<?> assignment = (AbstractClassSensorAssignment<?>) element;
					if (null != assignment.getClassName()) {
						return new StyledString(assignment.getClassName());
					}
				} else if (1 == index && element instanceof MethodSensorAssignment) {
					MethodSensorAssignment assignment = (MethodSensorAssignment) element;
					return new StyledString(TextFormatter.getMethodWithParameters(assignment));
				}

				return empty;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected Image getColumnImage(Object element, int index) {
				// first column have images only
				if (0 == index && element instanceof AbstractClassSensorAssignment) {
					AbstractClassSensorAssignment<?> assignment = (AbstractClassSensorAssignment<?>) element;
					if (assignment.isSuperclass()) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_SUPERCLASS);
					} else if (assignment.isInterf()) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_INTERFACE);
					} else {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS);
					}
				} else if (1 == index && element instanceof MethodSensorAssignment) {
					MethodSensorAssignment assignment = (MethodSensorAssignment) element;
					return ImageFormatter.getMethodVisibilityImage(resourceManager, assignment);
				} else if (2 == index && element instanceof AbstractClassSensorAssignment) {
					AbstractClassSensorAssignment<?> assignment = (AbstractClassSensorAssignment<?>) element;
					return ImageFormatter.getSensorAssignmentOptionsImage(resourceManager, assignment);
				}
				return super.getColumnImage(element, index);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getToolTipText(Object element, int index) {
				if (0 == index && element instanceof AbstractClassSensorAssignment) {
					AbstractClassSensorAssignment<?> assignment = (AbstractClassSensorAssignment<?>) element;
					if (assignment.isSuperclass()) {
						return "Superclass instrumentation";
					} else if (assignment.isInterf()) {
						return "Interface instrumentation";
					}
				} else if (1 == index && element instanceof MethodSensorAssignment) {
					MethodSensorAssignment assignment = (MethodSensorAssignment) element;
					StringBuilder stringBuilder = new StringBuilder("Method visibility:");
					if (assignment.isPublicModifier()) {
						stringBuilder.append(" public,");
					}
					if (assignment.isProtectedModifier()) {
						stringBuilder.append(" protected,");
					}
					if (assignment.isDefaultModifier()) {
						stringBuilder.append(" default,");
					}
					if (assignment.isPrivateModifier()) {
						stringBuilder.append(" private,");
					}
					return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
				} else if (2 == index) {
					StringBuilder stringBuilder = new StringBuilder();

					if (element instanceof AbstractClassSensorAssignment) {
						AbstractClassSensorAssignment<?> assignment = (AbstractClassSensorAssignment<?>) element;
						if (StringUtils.isNotEmpty(assignment.getAnnotation())) {
							stringBuilder.append("Annotation filtering = " + assignment.getAnnotation() + "\n");
						}
					}

					if (element instanceof TimerMethodSensorAssignment) {
						TimerMethodSensorAssignment assignment = (TimerMethodSensorAssignment) element;

						if (assignment.isStartsInvocation()) {
							stringBuilder.append("Starts invocation = ON\n");
						}
						if (assignment.isCharting()) {
							stringBuilder.append("Charting = ON\n");
						}
						if (CollectionUtils.isNotEmpty(assignment.getContextCaptures())) {
							for (AbstractContextCapture contextCapture : assignment.getContextCaptures()) {
								if (contextCapture instanceof ReturnContextCapture) {
									stringBuilder.append("Capture return value");
								} else if (contextCapture instanceof ParameterContextCapture) {
									stringBuilder.append("Capture parameter (index " + ((ParameterContextCapture) contextCapture).getIndex() + ")");
								} else if (contextCapture instanceof FieldContextCapture) {
									stringBuilder.append("Capture field (named '" + ((FieldContextCapture) contextCapture).getFieldName() + "')");
								}

								stringBuilder.append(" as '" + contextCapture.getDisplayName() + "'");
								if (CollectionUtils.isNotEmpty(contextCapture.getPaths())) {
									stringBuilder.append(" and follow path ");
									for (String path : contextCapture.getPaths()) {
										stringBuilder.append(" -> ");
										stringBuilder.append(path);
									}
								}
								stringBuilder.append('\n');

							}
						}
					}
					if (stringBuilder.length() > 0) {
						return stringBuilder.substring(0, stringBuilder.length() - 1);
					}
				}

				return super.getToolTipText(element, index);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void dispose() {
				resourceManager.dispose();
				super.dispose();
			}

		};
	}

}
