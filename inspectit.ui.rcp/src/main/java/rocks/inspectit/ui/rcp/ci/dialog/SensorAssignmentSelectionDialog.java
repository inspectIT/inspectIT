package rocks.inspectit.ui.rcp.ci.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.factory.ConfigurationDefaultsFactory;
import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.IExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;

/**
 * Dialog for the selecting the sensor type for the sensor assignment.
 *
 * @author Ivan Senic
 *
 */
public class SensorAssignmentSelectionDialog extends ListDialog {

	/**
	 * Default dialog title.
	 */
	private static final String DIALOG_TITLE = "Select Sensor Type";

	/**
	 * Default dialog message.
	 */
	private static final String DIALOG_MESSAGE = "Select sensor type to use in the assignment";

	/**
	 * Default constructor.
	 *
	 * @param parentShell
	 *            Shell
	 */
	public SensorAssignmentSelectionDialog(Shell parentShell) {
		super(parentShell);
		setTitle(DIALOG_TITLE);
		setMessage(DIALOG_MESSAGE);
		setBlockOnOpen(true);
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new SensorConfigLabelProvider());
		setInput(getInput(false));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite container) {
		Composite parent = (Composite) super.createDialogArea(container);

		final Button showAdvancedButton = new Button(parent, SWT.CHECK);
		showAdvancedButton.setText("Show advanced sensor types");
		showAdvancedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getTableViewer().setInput(getInput(showAdvancedButton.getSelection()));
				getTableViewer().refresh();
			}
		});

		return parent;
	}

	/**
	 * Creates input for the dialog.
	 *
	 * @param showAdvanced
	 *            If advanced types of {@link IMethodSensorConfig}s should be included in the input.
	 * @return Input for dialog.
	 */
	private static Object getInput(boolean showAdvanced) {
		List<IMethodSensorConfig> input = new ArrayList<IMethodSensorConfig>();
		for (IMethodSensorConfig sensorConfig : ConfigurationDefaultsFactory.getAvailableMethodSensorConfigs()) {
			if (sensorConfig instanceof InvocationSequenceSensorConfig) {
				// skip invocation sensor, as we don't want to allow direct configuration on the UI
				continue;
			}

			if (showAdvanced || !sensorConfig.isAdvanced()) {
				input.add(sensorConfig);
			}
		}
		input.add(ConfigurationDefaultsFactory.getDefaultExceptionSensorConfig());
		Collections.sort(input, new Comparator<IMethodSensorConfig>() {
			@Override
			public int compare(IMethodSensorConfig s1, IMethodSensorConfig s2) {
				int result = Boolean.compare(s1.isAdvanced(), s2.isAdvanced());
				if (result != 0) {
					return result;
				}

				return s1.getName().compareTo(s2.getName());
			}
		});
		return input;
	}

	/**
	 * @return Returns empty sensor assignment with set sensor type as result of dialog selection.
	 */
	@SuppressWarnings("unchecked")
	public AbstractClassSensorAssignment<?> getSensorAssignment() {
		Object[] result = getResult();
		if (ArrayUtils.isNotEmpty(result)) {
			ISensorConfig sensorConfig = (ISensorConfig) result[0];
			if (sensorConfig instanceof IExceptionSensorConfig) {
				return new ExceptionSensorAssignment();
			} else if (sensorConfig instanceof TimerSensorConfig) {
				return new TimerMethodSensorAssignment();
			} else if (sensorConfig instanceof IMethodSensorConfig) {
				return new MethodSensorAssignment((Class<? extends IMethodSensorConfig>) sensorConfig.getClass());
			}
		}
		return null;
	}

	/**
	 * Simple {@link LabelProvider} for the dialog.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class SensorConfigLabelProvider extends LabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getText(Object element) {
			return TextFormatter.getSensorConfigName((ISensorConfig) element);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Image getImage(Object element) {
			return ImageFormatter.getSensorConfigImage((ISensorConfig) element);
		}
	}
}
