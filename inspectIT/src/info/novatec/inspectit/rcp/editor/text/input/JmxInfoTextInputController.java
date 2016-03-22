package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.PlotDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.PlotDataSolverFactory;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.util.SafeExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
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
	 * The label for the description.
	 */
	private Label descriptionLabel;

	/**
	 * The {@link JmxDefinitionDataIdent} of the plotted MBean.
	 */
	private JmxDefinitionDataIdent jmxIdent;

	/**
	 * The current data list.
	 */
	private List<JmxSensorValueData> currentData;

	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new JmxSensorValueData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setJmxSensorDefinitionDataIdentId(inputDefinition.getIdDefinition().getJmxDefinitionId());

		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		// first section
		addSection(parent, toolkit, SECTION_BEAN_DETAILS);

		if (sections.containsKey(SECTION_BEAN_DETAILS)) {
			// creates the labels

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Domain: ", InspectIT.getDefault().getImage(InspectITImages.IMG_PACKAGE));
			packageLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			packageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Type: ", InspectIT.getDefault().getImage(InspectITImages.IMG_BOOK));
			typeLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			typeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Attribute: ", InspectIT.getDefault().getImage(InspectITImages.IMG_BLUE_DOCUMENT_TABLE));
			attributeLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			attributeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Data type: ");
			dataTypeLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			dataTypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			addItemToSection(toolkit, SECTION_BEAN_DETAILS, "Description: ", InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
			descriptionLabel = toolkit.createLabel(sections.get(SECTION_BEAN_DETAILS), NOT_AVAILABLE, SWT.LEFT);
			descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		}

		// second section
		addSection(parent, toolkit, SECTION_VALUE_DETAILS);

		if (sections.containsKey(SECTION_VALUE_DETAILS)) {
			// creates the labels
			addItemToSection(toolkit, SECTION_VALUE_DETAILS, "Timestamp: ", InspectIT.getDefault().getImage(InspectITImages.IMG_TIMESTAMP));
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
			addItemToSection(toolkit, SECTION_AGGREGATED_DETAILS, "Count: ", InspectIT.getDefault().getImage(InspectITImages.IMG_COUNTER));
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh() {
		if (currentData != null && !currentData.isEmpty()) {
			final JmxSensorValueData jmxData = currentData.get(currentData.size() - 1);

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

			final String[] aggregatedData = getAggregatedData(dataSolver);

			SafeExecutor.asyncExec(new Runnable() {
				@Override
				public void run() {
					packageLabel.setText(jmxIdent.getDerivedDomainName());
					typeLabel.setText(jmxIdent.getDerivedTypeName());
					attributeLabel.setText(jmxIdent.getmBeanAttributeName());
					dataTypeLabel.setText(jmxIdent.getmBeanAttributeType());
					descriptionLabel.setText(jmxIdent.getmBeanAttributeDescription());

					timestampLabel.setText(NumberFormatter.formatTime(jmxData.getTimeStamp()));
					valueLabel.setText(dataSolver.valueToHumanReadable(jmxData.getValueAsDouble()));

					averageValueLabel.setText(aggregatedData[0]);
					minValueLabel.setText(aggregatedData[1]);
					maxValueLabel.setText(aggregatedData[2]);
					countLabel.setText(aggregatedData[3]);
				}
			}, packageLabel, typeLabel, attributeLabel, dataTypeLabel, descriptionLabel, timestampLabel, valueLabel, averageValueLabel, minValueLabel, maxValueLabel, countLabel);
		}
	}

	/**
	 * Aggregates the {@link #currentData} list and returns the result as an array of nicely
	 * formatted strings. The first array element represents the average value, the second element
	 * the minimum value and the third value is the maximum value of the data list.
	 * 
	 * @param dataSolver
	 *            the used {@link AbstractPlotDataSolver} to convert the values into a human
	 *            readable string
	 * 
	 * @return an array containing aggregated data as human readable strings
	 */
	private String[] getAggregatedData(AbstractPlotDataSolver dataSolver) {
		String[] returnArray = { NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE };
		if (currentData.isEmpty() || !dataSolver.isAggregatable()) {
			return returnArray;
		}

		// values: average, min, max
		double[] valueArray = new double[] { 0D, Double.MAX_VALUE, Double.MIN_VALUE };
		int count = 0;
		for (JmxSensorValueData data : currentData) {
			double avg = dataSolver.valueConvert(data.getAverageValue());
			double min = dataSolver.valueConvert(data.getMinValue());
			double max = dataSolver.valueConvert(data.getMaxValue());
			valueArray[0] += avg;
			if (valueArray[1] > min) {
				valueArray[1] = min;
			}
			if (valueArray[2] < max) {
				valueArray[2] = max;
			}
			count += data.getAggregationCount();
		}
		valueArray[0] = valueArray[0] / currentData.size();

		for (int i = 0; i < 3; i++) {
			returnArray[i] = dataSolver.valueToHumanReadable(valueArray[i]);
		}
		returnArray[3] = String.valueOf(count);
		return returnArray;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends DefaultData> dataInput) {
		currentData = new ArrayList<JmxSensorValueData>();
		for (DefaultData data : dataInput) {
			if (data instanceof JmxSensorValueData) {
				currentData.add((JmxSensorValueData) data);
			}
		}
		doRefresh();
	}
}
