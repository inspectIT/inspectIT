package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.PlotDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.PlotDataSolverFactory;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This class represents the textual view of the {@link JmxSensorValueData} sensor-type.
 * 
 * @author Marius Oehler
 *
 */
public class JmxInputController extends AbstractTextInputController {

	/**
	 * The title of the third section.
	 */
	private static final String SECTION_AGGREGATED_DETAILS = "Aggregated Details";

	/**
	 * The title of the second section.
	 */
	private static final String SECTION_VALUE_DETAILS = "Latest Value Details";

	/**
	 * The title of the first section.
	 */
	private static final String SECTION_BEAN_DETAILS = "MBean Details";

	/**
	 * The string representing that something is not available.
	 */
	private static final String NOT_AVAILABLE = "N/A";

	/**
	 * The template object which is send to the server.
	 */
	private JmxSensorValueData template;

	/**
	 * The cached data service.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * The label for the data type.
	 */
	private Label labelDataType;

	/**
	 * The label for the package.
	 */
	private Label labelPackage;

	/**
	 * The label for the timestamp.
	 */
	private Label labelTimestamp;

	/**
	 * The label for the value.
	 */
	private Label labelValue;

	/**
	 * The label for the type.
	 */
	private Label labelType;

	/**
	 * The label for the attribute.
	 */
	private Label labelAttribute;

	/**
	 * The label for the max value.
	 */
	private Label labelMaxValue;

	/**
	 * The label for the min value.
	 */
	private Label labelMinValue;

	/**
	 * The label for the average value.
	 */
	private Label labelAverageValue;

	/**
	 * The label for the element count.
	 */
	private Label labelCount;
	/**
	 * The {@link JmxDefinitionDataIdent} of the plotted MBean.
	 */
	private JmxDefinitionDataIdent jmxIdent;
	/**
	 * The current data list.
	 */
	private List<? extends DefaultData> currentData;

	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new JmxSensorValueData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setJmxSensorDefinitionDataIdentId(inputDefinition.getIdDefinition().getSensorTypeId());

		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		// first section
		addSection(parent, toolkit, SECTION_BEAN_DETAILS);

		if (sections.containsKey(SECTION_BEAN_DETAILS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Domain: ");
			labelPackage = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelPackage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Type: ");
			labelType = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Attribute: ");
			labelAttribute = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelAttribute.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Data type: ");
			labelDataType = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelDataType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		}

		// second section
		addSection(parent, toolkit, SECTION_VALUE_DETAILS);

		if (sections.containsKey(SECTION_VALUE_DETAILS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_VALUE_DETAILS, "Timestamp: ");
			labelTimestamp = toolkit.createLabel(sections.get(SECTION_VALUE_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelTimestamp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_VALUE_DETAILS, "Value: ");
			labelValue = toolkit.createLabel(sections.get(SECTION_VALUE_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		// third section
		addSection(parent, toolkit, SECTION_AGGREGATED_DETAILS);

		if (sections.containsKey(SECTION_AGGREGATED_DETAILS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Count: ");
			labelCount = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Average: ");
			labelAverageValue = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelAverageValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Min: ");
			labelMinValue = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelMinValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Max: ");
			labelMaxValue = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			labelMaxValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
	}

	@Override
	public void doRefresh() {
		if (currentData != null && !currentData.isEmpty()) {
			final JmxSensorValueData jmxData = (JmxSensorValueData) currentData.get(currentData.size() - 1);

			if (jmxIdent == null) {
				jmxIdent = cachedDataService.getJmxDefinitionDataIdentForId(jmxData.getJmxSensorDefinitionDataIdentId());
			}

			Map<String, String> map = PreferencesUtils.getObject(PreferencesConstants.JMX_PLOT_DATA_SOLVER);
			final AbstractPlotDataSolver dataSolver;
			if (map.containsKey(jmxIdent.getDerivedFullName())) {
				dataSolver = PlotDataSolverFactory.getDataSolver(PlotDataSolver.valueOf(map.get(jmxIdent.getDerivedFullName())));
			} else {
				dataSolver = PlotDataSolverFactory.getDefaultDataSolver();
			}

			final double[] aggregatedData = getAggregatedData();

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					labelPackage.setText(jmxIdent.getDerivedDomainName());
					labelType.setText(jmxIdent.getDerivedTypeName());
					labelAttribute.setText(jmxIdent.getmBeanAttributeName());
					labelDataType.setText(jmxIdent.getmBeanAttributeType());

					labelTimestamp.setText(NumberFormatter.formatTime(jmxData.getTimeStamp()));
					labelValue.setText(dataSolver.valueToHumanReadable(jmxData.getValue()));

					labelCount.setText(String.valueOf(currentData.size()));

					if (aggregatedData == null) {
						labelAverageValue.setText(NOT_AVAILABLE);
						labelMinValue.setText(NOT_AVAILABLE);
						labelMaxValue.setText(NOT_AVAILABLE);
					} else {
						labelAverageValue.setText(dataSolver.valueToHumanReadable(String.valueOf(aggregatedData[0])));
						labelMinValue.setText(dataSolver.valueToHumanReadable(String.valueOf(aggregatedData[1])));
						labelMaxValue.setText(dataSolver.valueToHumanReadable(String.valueOf(aggregatedData[2])));
					}
				}
			});
		}
	}

	/**
	 * Aggregates the {@link #currentData} list and returns the result as an array. The first array
	 * element represents the average value, the second element the minimum value and the third
	 * value is the maximum value of the data list.
	 * 
	 * @return an array containing aggregated data
	 */
	private double[] getAggregatedData() {
		if (currentData.isEmpty()) {
			return null;
		}
		double[] returnArray = new double[] { 0D, Double.MAX_VALUE, Double.MIN_VALUE };
		for (DefaultData data : currentData) {
			try {
				double val = Double.parseDouble(((JmxSensorValueData) data).getValue());
				returnArray[0] += val;
				if (returnArray[1] > val) {
					returnArray[1] = val;
				}
				if (returnArray[2] < val) {
					returnArray[2] = val;
				}
			} catch (Exception e) {
				return null;
			}
		}
		returnArray[0] = returnArray[0] / currentData.size();
		return returnArray;
	}

	@Override
	public void setDataInput(List<? extends DefaultData> dataInput) {
		this.currentData = dataInput;
		doRefresh();
	}
}
