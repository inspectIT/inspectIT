package info.novatec.inspectit.rcp.preferences;

import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * Initializes the default preferences.
 * 
 * @author Patrice Bouillet
 * 
 */
public class InspectITPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		// CMR list
		List<CmrRepositoryDefinition> defaultCmrList = new ArrayList<CmrRepositoryDefinition>(1);
		CmrRepositoryDefinition defaultCmr = new CmrRepositoryDefinition(CmrRepositoryDefinition.DEFAULT_IP, CmrRepositoryDefinition.DEFAULT_PORT, CmrRepositoryDefinition.DEFAULT_NAME);
		defaultCmr.setDescription(CmrRepositoryDefinition.DEFAULT_DESCRIPTION);
		defaultCmrList.add(defaultCmr);
		PreferencesUtils.saveCmrRepositoryDefinitions(defaultCmrList, true);

		// Editor defaults
		PreferencesUtils.saveIntValue(PreferencesConstants.DECIMAL_PLACES, 0, true);
		PreferencesUtils.saveLongValue(PreferencesConstants.REFRESH_RATE, 5000L, true);
		PreferencesUtils.saveIntValue(PreferencesConstants.ITEMS_COUNT_TO_SHOW, 100, true);
		PreferencesUtils.saveDoubleValue(PreferencesConstants.INVOCATION_FILTER_EXCLUSIVE_TIME, Double.NaN, true);
		PreferencesUtils.saveDoubleValue(PreferencesConstants.INVOCATION_FILTER_TOTAL_TIME, Double.NaN, true);
		Set<Class<?>> invocDataTypes = new HashSet<>();
		invocDataTypes.add(InvocationSequenceData.class);
		invocDataTypes.add(TimerData.class);
		invocDataTypes.add(HttpTimerData.class);
		invocDataTypes.add(SqlStatementData.class);
		invocDataTypes.add(ExceptionSensorData.class);
		PreferencesUtils.saveObject(PreferencesConstants.INVOCATION_FILTER_DATA_TYPES, invocDataTypes, true);
	}

}
