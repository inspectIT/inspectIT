package rocks.inspectit.ui.rcp.wizard.page;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.util.ObjectUtils;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.SqlStatementDataAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.TimerDataAggregator;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataAggregatorProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.DataSaverProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationClonerDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.impl.InvocationExtractorDataProcessor;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;

/**
 * Wizard page where data that will be saved in the storage is saved.
 *
 * @author Ivan Senic
 *
 */
public class DefineDataProcessorsWizardPage extends WizardPage {

	/**
	 * Marker for having buffer data available for selection.
	 */
	public static final int BUFFER_DATA = 1;

	/**
	 * Marker for having system data available for selection.
	 */
	public static final int SYSTEM_DATA = 2;

	/**
	 * Marker for extract from invocations available for selection.
	 */
	public static final int EXTRACT_INVOCATIONS = 4;

	/**
	 * Marker that marks that invocations are saved.
	 */
	public static final int INVOCATIONS = 8;

	/**
	 * Marker that marks that timer are saved.
	 */
	public static final int TIMERS = 16;

	/**
	 * Marker that marks that SQLs are saved.
	 */
	public static final int SQL_STATEMENTS = 32;

	/**
	 * Marker that marks that HTTP timers are saved.
	 */
	public static final int HTTP_TIMERS = 64;

	/**
	 * Marker that marks that exceptions are saved.
	 */
	public static final int EXCEPTIONS = 128;

	/**
	 * Marker that marks that spans are saved.
	 */
	public static final int SPANS = 256;

	/**
	 * Default message.
	 */
	private static final String DEFAULT_MESSAGE = "Define the data that should be stored in the storage and additional options";

	/**
	 * Input list for table containing all the classes.
	 */
	private Set<Class<?>> inputList = new LinkedHashSet<>();

	/**
	 * Style for providing different selection possibilities.
	 */
	private int selectionStyle;

	/**
	 * Table where all data types are displayed.
	 */
	private Table table;

	/**
	 * Spinner for the aggregation period.
	 */
	private Spinner aggregationPeriodSpiner;

	/**
	 * Page completed listener.
	 */
	private Listener pageCompleteListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			setPageComplete(isPageComplete());
		}
	};

	/**
	 * Default constructor.
	 *
	 * @param selectionStyle
	 *            Combination of styles for this page in the SWT way.
	 * @see #BUFFER_DATA
	 * @see #SYSTEM_DATA
	 * @see #EXTRACT_INVOCATIONS
	 */
	public DefineDataProcessorsWizardPage(int selectionStyle) {
		super("Define Data");
		setTitle("Define Data");
		setDescription(DEFAULT_MESSAGE);
		this.selectionStyle = selectionStyle;
		// use add if not contained to have always same order of elements in the page
		if (isStyleApplied(BUFFER_DATA) || isStyleApplied(INVOCATIONS)) {
			inputList.add(InvocationSequenceData.class);
			inputList.add(TimerData.class);
			inputList.add(HttpTimerData.class);
			inputList.add(SqlStatementData.class);
			inputList.add(ExceptionSensorData.class);
			inputList.add(AbstractSpan.class);
		}
		if (isStyleApplied(TIMERS)) {
			inputList.add(TimerData.class);
			inputList.add(InvocationSequenceData.class);
		}
		if (isStyleApplied(SQL_STATEMENTS)) {
			inputList.add(SqlStatementData.class);
			inputList.add(InvocationSequenceData.class);
		}
		if (isStyleApplied(HTTP_TIMERS)) {
			inputList.add(HttpTimerData.class);
			inputList.add(InvocationSequenceData.class);
		}
		if (isStyleApplied(EXCEPTIONS)) {
			inputList.add(ExceptionSensorData.class);
			inputList.add(InvocationSequenceData.class);
		}
		if (isStyleApplied(SPANS)) {
			inputList.add(AbstractSpan.class);
			inputList.add(InvocationSequenceData.class);
		}
		if (isStyleApplied(SYSTEM_DATA)) {
			inputList.add(MemoryInformationData.class);
			inputList.add(CpuInformationData.class);
			inputList.add(ClassLoadingInformationData.class);
			inputList.add(CompilationInformationData.class);
			inputList.add(ThreadInformationData.class);
			inputList.add(SystemInformationData.class);
			inputList.add(JmxSensorValueData.class);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(4, false));

		table = new Table(main, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2));

		TableViewer tableViewer = new TableViewer(table);
		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
		column.getColumn().setWidth(300);
		column.getColumn().setMoveable(false);
		column.getColumn().setResizable(false);
		column.setLabelProvider(new DataColumnLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(inputList);
		tableViewer.refresh();

		if (isStyleApplied(BUFFER_DATA) || isStyleApplied(SYSTEM_DATA)) {
			for (TableItem tableItem : table.getItems()) {
				tableItem.setChecked(true);
			}
		} else {
			for (TableItem tableItem : table.getItems()) {
				if (ObjectUtils.equals(tableItem.getData(), InvocationSequenceData.class) && isStyleApplied(INVOCATIONS)) {
					tableItem.setChecked(true);
				} else if (ObjectUtils.equals(tableItem.getData(), TimerData.class) && isStyleApplied(TIMERS)) {
					tableItem.setChecked(true);
				} else if (ObjectUtils.equals(tableItem.getData(), SqlStatementData.class) && isStyleApplied(SQL_STATEMENTS)) {
					tableItem.setChecked(true);
				} else if (ObjectUtils.equals(tableItem.getData(), ExceptionSensorData.class) && isStyleApplied(EXCEPTIONS)) {
					tableItem.setChecked(true);
				} else if (ObjectUtils.equals(tableItem.getData(), HttpTimerData.class) && isStyleApplied(HTTP_TIMERS)) {
					tableItem.setChecked(true);
				} else if (isStyleApplied(SPANS)) {
					tableItem.setChecked(true);
				} else {
					tableItem.setChecked(false);
				}
			}
		}

		Button selectAll = new Button(main, SWT.PUSH);
		selectAll.setText("Select All");
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TableItem tableItem : table.getItems()) {
					tableItem.setChecked(true);
					aggregationPeriodSpiner.setEnabled(true);
				}
			}
		});
		selectAll.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Button deselectAll = new Button(main, SWT.PUSH);
		deselectAll.setText("Deselect All");
		deselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TableItem tableItem : table.getItems()) {
					tableItem.setChecked(false);
					aggregationPeriodSpiner.setEnabled(false);
				}
			}
		});
		deselectAll.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label info = new Label(main, SWT.WRAP);
		info.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO));
		info.setToolTipText(
				"All Timer and SQL Statement Data objects need to be aggregated before saved to the storage, because the amount of objects to be saved is in most cases too high and can impose performance problems while writing to disk. Thus, please select the aggregation period for these two data types.");

		new Label(main, SWT.NONE).setText("Aggregation period for Timer and SQL Statement Data:");
		aggregationPeriodSpiner = new Spinner(main, SWT.BORDER);
		aggregationPeriodSpiner.setMinimum(5);
		aggregationPeriodSpiner.setIncrement(5);
		aggregationPeriodSpiner.setSelection(5);
		new Label(main, SWT.NONE).setText("seconds");

		table.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (e.detail == SWT.CHECK) {
					aggregationPeriodSpiner.setEnabled(isSelected(TimerData.class) || isSelected(SqlStatementData.class));
				}
			}
		});
		table.addListener(SWT.Selection, pageCompleteListener);
		selectAll.addListener(SWT.Selection, pageCompleteListener);
		deselectAll.addListener(SWT.Selection, pageCompleteListener);

		setControl(main);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageComplete() {
		if (!atLeastOneTypeSelected()) {
			setMessage("At least one data type has to be selected", ERROR);
			return false;
		}
		if (isStyleApplied(INVOCATIONS) && !getSelectedClassesFromTable().contains(InvocationSequenceData.class)) {
			setMessage("Invocation Sequence Data type has to be selected because it is source of data", ERROR);
			return false;
		}
		if (isStyleApplied(TIMERS) && !getSelectedClassesFromTable().contains(TimerData.class)) {
			setMessage("Timer Data type has to be selected because it is source of data", ERROR);
			return false;
		}
		if (isStyleApplied(SQL_STATEMENTS) && !getSelectedClassesFromTable().contains(SqlStatementData.class)) {
			setMessage("SQL Statement Data type has to be selected because it is source of data", ERROR);
			return false;
		}
		if (isStyleApplied(EXCEPTIONS) && !getSelectedClassesFromTable().contains(ExceptionSensorData.class)) {
			setMessage("Exception Sensor Data type has to be selected because it is source of data", ERROR);
			return false;
		}
		if (isStyleApplied(HTTP_TIMERS) && !getSelectedClassesFromTable().contains(HttpTimerData.class)) {
			setMessage("HTTP Timer Data type has to be selected because it is source of data", ERROR);
			return false;
		}
		setMessage(DEFAULT_MESSAGE);
		return true;
	}

	/**
	 * @return Processor list for the storage opening.
	 */
	public List<AbstractDataProcessor> getProcessorList() {
		List<AbstractDataProcessor> normalProcessors = new ArrayList<>();

		/**
		 * Normal saving processor.
		 */
		List<Class<? extends DefaultData>> saveClassesList = getSelectedClassesFromTable();
		// include both span types
		if (saveClassesList.contains(AbstractSpan.class)) {
			saveClassesList.add(ClientSpan.class);
			saveClassesList.add(ServerSpan.class);
		}
		boolean writeInvocationAffiliation = saveClassesList.contains(InvocationSequenceData.class);

		if (!saveClassesList.isEmpty()) {
			normalProcessors.add(new DataSaverProcessor(saveClassesList, writeInvocationAffiliation));
		}

		/**
		 * Aggregation.
		 */
		// aggregation period must be in the milliseconds, thus we multiply with 1000
		int aggregationPeriod = aggregationPeriodSpiner.getSelection() * 1000;
		if (saveClassesList.contains(TimerData.class)) {
			saveClassesList.remove(TimerData.class);
			DataAggregatorProcessor<TimerData> dataAggregatorProcessor = new DataAggregatorProcessor<>(TimerData.class, aggregationPeriod, new TimerDataAggregator(),
					writeInvocationAffiliation);
			normalProcessors.add(dataAggregatorProcessor);
		}

		if (saveClassesList.contains(SqlStatementData.class)) {
			saveClassesList.remove(SqlStatementData.class);
			DataAggregatorProcessor<SqlStatementData> dataAggregatorProcessor = new DataAggregatorProcessor<>(SqlStatementData.class, aggregationPeriod,
					new SqlStatementDataAggregator(true), writeInvocationAffiliation);
			normalProcessors.add(dataAggregatorProcessor);
		}

		/**
		 * Invocation extractor.
		 */
		// we only include the extractor of invocations if the style is specified
		if (isStyleApplied(EXTRACT_INVOCATIONS)) {
			List<AbstractDataProcessor> chainedProcessorsForExtractor = new ArrayList<>();
			chainedProcessorsForExtractor.addAll(normalProcessors);
			InvocationExtractorDataProcessor invocationExtractorDataProcessor = new InvocationExtractorDataProcessor(chainedProcessorsForExtractor);
			normalProcessors.add(invocationExtractorDataProcessor);
		}

		/**
		 * Invocation cloner.
		 */
		if (saveClassesList.contains(InvocationSequenceData.class)) {
			normalProcessors.add(new InvocationClonerDataProcessor());
		}

		return normalProcessors;
	}

	/**
	 * Tests if specific style is applied.
	 *
	 * @param style
	 *            Style to test.
	 * @return True if style is part of style with which view was instantiated.
	 */
	private boolean isStyleApplied(int style) {
		return (selectionStyle & style) != 0;
	}

	/**
	 * Returns if at least one item in the table is selected.
	 *
	 * @return Returns if at least one item in the table is selected.
	 */
	private boolean atLeastOneTypeSelected() {
		for (TableItem tableItem : table.getItems()) {
			if (tableItem.getChecked()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns list of selected classes in table.
	 *
	 * @return Returns list of selected classes in table.
	 */
	@SuppressWarnings("unchecked")
	private List<Class<? extends DefaultData>> getSelectedClassesFromTable() {
		List<Class<? extends DefaultData>> classList = new ArrayList<>();
		for (TableItem tableItem : table.getItems()) {
			if (tableItem.getChecked()) {
				classList.add((Class<? extends DefaultData>) tableItem.getData());
			}
		}
		return classList;
	}

	/**
	 * Returns if the class is selected in the table.
	 *
	 * @param clazz
	 *            Class to check.
	 * @return True if class is selected, otherwise false.
	 */
	private boolean isSelected(Class<? extends DefaultData> clazz) {
		for (TableItem tableItem : table.getItems()) {
			if (ObjectUtils.equals(tableItem.getData(), clazz)) {
				return tableItem.getChecked();
			}
		}
		return false;
	}

	/**
	 * Label provider for only column in the table.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class DataColumnLabelProvider extends ColumnLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getText(Object element) {
			if (ObjectUtils.equals(element, TimerData.class)) {
				return "Timer Data";
			} else if (ObjectUtils.equals(element, HttpTimerData.class)) {
				return "HTTP Timer Data";
			} else if (ObjectUtils.equals(element, SqlStatementData.class)) {
				return "SQL Statement Data";
			} else if (ObjectUtils.equals(element, InvocationSequenceData.class)) {
				return "Invocation Sequence Data";
			} else if (ObjectUtils.equals(element, ExceptionSensorData.class)) {
				return "Exception Sensor Data";
			} else if (ObjectUtils.equals(element, MemoryInformationData.class)) {
				return "Memory Information Data";
			} else if (ObjectUtils.equals(element, CpuInformationData.class)) {
				return "CPU Information Data";
			} else if (ObjectUtils.equals(element, ClassLoadingInformationData.class)) {
				return "Class Loading Information Data";
			} else if (ObjectUtils.equals(element, ThreadInformationData.class)) {
				return "Thread Informartion Data";
			} else if (ObjectUtils.equals(element, SystemInformationData.class)) {
				return "System Information Data";
			} else if (ObjectUtils.equals(element, CompilationInformationData.class)) {
				return "Compilation Information Data";
			} else if (ObjectUtils.equals(element, JmxSensorValueData.class)) {
				return "JMX Data";
			} else if (AbstractSpan.class.isAssignableFrom((Class<?>) element)) {
				return "Tracing Data";
			}
			return super.getText(element);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Image getImage(Object element) {
			if (ObjectUtils.equals(element, TimerData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_TIMER);
			} else if (ObjectUtils.equals(element, HttpTimerData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_HTTP);
			} else if (ObjectUtils.equals(element, SqlStatementData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE);
			} else if (ObjectUtils.equals(element, InvocationSequenceData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_INVOCATION);
			} else if (ObjectUtils.equals(element, ExceptionSensorData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR);
			} else if (ObjectUtils.equals(element, MemoryInformationData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_MEMORY_OVERVIEW);
			} else if (ObjectUtils.equals(element, CpuInformationData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_CPU_OVERVIEW);
			} else if (ObjectUtils.equals(element, ClassLoadingInformationData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS_OVERVIEW);
			} else if (ObjectUtils.equals(element, ThreadInformationData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_THREADS_OVERVIEW);
			} else if (ObjectUtils.equals(element, SystemInformationData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_SYSTEM_OVERVIEW);
			} else if (ObjectUtils.equals(element, CompilationInformationData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_COMPILATION_OVERVIEW);
			} else if (ObjectUtils.equals(element, JmxSensorValueData.class)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_BEAN);
			} else if (AbstractSpan.class.isAssignableFrom((Class<?>) element)) {
				return InspectIT.getDefault().getImage(InspectITImages.IMG_REMOTE);
			}
			return super.getImage(element);
		}
	}
}
