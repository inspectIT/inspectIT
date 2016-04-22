package rocks.inspectit.ui.rcp.editor.testers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Tester for all navigations.
 *
 * @author Ivan Senic
 *
 */
public class NavigationTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("canNavigateToPlotting".equals(property)) {
			if (receiver instanceof StructuredSelection) {
				StructuredSelection selection = (StructuredSelection) receiver;
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof InvocationSequenceData) {
					// only navigate if a real TimerData is provided (not for HttpTimerData or SQL)
					TimerData timerData = ((InvocationSequenceData) selectedObject).getTimerData();
					return isTimerSensorBounded(timerData) && timerData.isCharting();
				} else if (selectedObject instanceof TimerData) {
					return isTimerSensorBounded((TimerData) selectedObject) && ((TimerData) selectedObject).isCharting();
				}
			}
		} else if ("canNavigateToInvocations".equals(property)) {
			if (receiver instanceof StructuredSelection) {
				StructuredSelection selection = (StructuredSelection) receiver;
				for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					Object selectedObject = iterator.next();
					if (selectedObject instanceof InvocationAwareData) {
						InvocationAwareData invocationAwareData = (InvocationAwareData) selectedObject;
						if (!invocationAwareData.isOnlyFoundOutsideInvocations()) {
							return true;
						}
					}
				}
			}
		} else if ("canNavigateToExceptionType".equals(property)) {
			StructuredSelection selection = (StructuredSelection) receiver;
			Object selectedObject = selection.getFirstElement();
			if (selectedObject instanceof InvocationSequenceData) {
				List<ExceptionSensorData> exceptions = ((InvocationSequenceData) selectedObject).getExceptionSensorDataObjects();
				if ((null != exceptions) && !exceptions.isEmpty()) {
					for (ExceptionSensorData exceptionSensorData : exceptions) {
						if (null != exceptionSensorData.getThrowableType()) {
							return true;
						}
					}
				}
			} else if (selectedObject instanceof ExceptionSensorData) {
				return ((ExceptionSensorData) selectedObject).getThrowableType() != null;
			}
		} else if ("canNavigateToAggregatedTimerData".equals(property)) {
			if (receiver instanceof StructuredSelection) {
				StructuredSelection selection = (StructuredSelection) receiver;
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof InvocationSequenceData) {
					// only navigate if a real TimerData is provided (not for HttpTimerData or SQL)
					TimerData timerData = ((InvocationSequenceData) selectedObject).getTimerData();
					return isTimerSensorBounded(timerData);
				} else if (selectedObject instanceof TimerData) {
					return isTimerSensorBounded((TimerData) selectedObject);
				}
			}
		} else if ("canNavigateToAggregatedSqlData".equals(property)) {
			if (receiver instanceof StructuredSelection) {
				StructuredSelection selection = (StructuredSelection) receiver;
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof InvocationSequenceData) {
					return null != ((InvocationSequenceData) selectedObject).getSqlStatementData();
				} else if (selectedObject instanceof SqlStatementData) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the given timer data has a sensor type that equals {@link SensorTypeEnum#TIMER} or
	 * {@link SensorTypeEnum#AVERAGE_TIMER}, so that a special navigation types are possible or not.
	 *
	 * @param timerData
	 *            {@link TimerData} to check.
	 * @return True if given object is of a TimerData class and mentioned sensor types are
	 *         registered. False otherwise.
	 */
	private boolean isTimerSensorBounded(TimerData timerData) {
		if ((null == timerData) || (!timerData.getClass().equals(TimerData.class) && !timerData.getClass().equals(AggregatedTimerData.class))) {
			return false;
		}

		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof AbstractRootEditor) {
			RepositoryDefinition repositoryDefinition = ((AbstractRootEditor) editor).getInputDefinition().getRepositoryDefinition();
			SensorTypeIdent sensorTypeIdent = repositoryDefinition.getCachedDataService().getSensorTypeIdentForId(timerData.getSensorTypeIdent());
			if (null != sensorTypeIdent) {
				SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(sensorTypeIdent.getFullyQualifiedClassName());
				return (sensorTypeEnum == SensorTypeEnum.TIMER) || (sensorTypeEnum == SensorTypeEnum.AVERAGE_TIMER);
			}
		}

		return false;
	}
}
