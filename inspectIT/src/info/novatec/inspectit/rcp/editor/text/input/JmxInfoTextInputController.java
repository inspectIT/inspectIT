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
public class JmxInfoTextInputController extends AbstractTextInputController {

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
	private Label dataTypeLabel;

	/**
	 * The label for the package.
	 */
	private Label packageLabel;

	/**
	 * The label for the timestamp.
	 */
	private Label timestampLabel;

	/**
	 * The label for the value.
	 */
	private Label valueLabel;

	/**
	 * The label for the type.
	 */
	private Label typeLabel;

	/**
	 * The label for the attribute.
	 */
	private Label attributeLabel;

	/**
	 * The label for the max value.
	 */
	private Label maxValueLabel;

	/**
	 * The label for the min value.
	 */
	private Label minValueLabel;

	/**
	 * The label for the average value.
	 */
	private Label averageValueLabel;

	/**
	 * The label for the element count.
	 */
	private Label countLabel;

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
			packageLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			packageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Type: ");
			typeLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			typeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Attribute: ");
			attributeLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			attributeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Data type: ");
			dataTypeLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			dataTypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		}

		// second section
		addSection(parent, toolkit, SECTION_VALUE_DETAILS);

		if (sections.containsKey(SECTION_VALUE_DETAILS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_VALUE_DETAILS, "Timestamp: ");
			timestampLabel = toolkit.createLabel(sections.get(SECTION_VALUE_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			timestampLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_VALUE_DETAILS, "Value: ");
			valueLabel = toolkit.createLabel(sections.get(SECTION_VALUE_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			valueLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		// third section
		addSection(parent, toolkit, SECTION_AGGREGATED_DETAILS);

		if (sections.containsKey(SECTION_AGGREGATED_DETAILS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Count: ");
			countLabel = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			countLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Average: ");
			averageValueLabel = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			averageValueLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Min: ");
			minValueLabel = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			minValueLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Max: ");
			maxValueLabel = toolkit.createLabel(sections.get(SECTION_AGGREGATED_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			maxValueLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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
					packageLabel.setText(jmxIdent.getDerivedDomainName());
					typeLabel.setText(jmxIdent.getDerivedTypeName());
					attributeLabel.setText(jmxIdent.getmBeanAttributeName());
					dataTypeLabel.setText(jmxIdent.getmBeanAttributeType());

					timestampLabel.setText(NumberFormatter.formatTime(jmxData.getTimeStamp()));
					valueLabel.setText(dataSolver.valueToHumanReadable(jmxData.getValue()));

					countLabel.setText(String.valueOf(currentData.size()));

					if (aggregatedData == null) {
						averageValueLabel.setText(NOT_AVAILABLE);
						minValueLabel.setText(NOT_AVAILABLE);
						maxValueLabel.setText(NOT_AVAILABLE);
					} else {
						averageValueLabel.setText(dataSolver.valueToHumanReadable(String.valueOf(aggregatedData[0])));
						minValueLabel.setText(dataSolver.valueToHumanReadable(String.valueOf(aggregatedData[1])));
						maxValueLabel.setText(dataSolver.valueToHumanReadable(String.valueOf(aggregatedData[2])));
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
