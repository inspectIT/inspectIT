package info.novatec.inspectit.rcp.preferences;

import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.PlotDataSolver;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

		// auto check new version
		PreferencesUtils.saveBooleanValue(PreferencesConstants.AUTO_CHECK_NEW_VERSION, true, true);

		Map<String, PlotDataSolver> dataSolverBeanAssignmentMap = new HashMap<>();

		dataSolverBeanAssignmentMap.put("java.lang.ClassLoading:Verbose", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Compilation:CompilationTimeMonitoringSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.GarbageCollector.PS MarkSweep:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.GarbageCollector.PS Scavenge:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Memory:Verbose", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryManager.CodeCacheManager:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.Code Cache:CollectionUsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.Code Cache:CollectionUsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.Code Cache:UsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.Code Cache:UsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.Code Cache:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Eden Space:CollectionUsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Eden Space:CollectionUsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Eden Space:UsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Eden Space:UsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Eden Space:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Old Gen:CollectionUsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Old Gen:CollectionUsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Old Gen:UsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Old Gen:UsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Old Gen:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Perm Gen:CollectionUsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Perm Gen:CollectionUsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Perm Gen:UsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Perm Gen:UsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Perm Gen:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Survivor Space:CollectionUsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Survivor Space:CollectionUsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Survivor Space:UsageThresholdExceeded", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Survivor Space:UsageThresholdSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.MemoryPool.PS Survivor Space:Valid", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:CommittedVirtualMemorySize", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:FreePhysicalMemorySize", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:FreeSwapSpaceSize", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:ProcessCpuLoad", PlotDataSolver.CLAMPED_PERCENTAGE);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:SystemCpuLoad", PlotDataSolver.CLAMPED_PERCENTAGE);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:SystemLoadAverage", PlotDataSolver.CLAMPED_PERCENTAGE);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:TotalPhysicalMemorySize", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.lang.OperatingSystem:TotalSwapSpaceSize", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.lang.Runtime:BootClassPathSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:CurrentThreadCpuTimeSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:ObjectMonitorUsageSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:SynchronizerUsageSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:ThreadAllocatedMemoryEnabled", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:ThreadAllocatedMemorySupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:ThreadContentionMonitoringEnabled", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:ThreadContentionMonitoringSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:ThreadCpuTimeEnabled", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.lang.Threading:ThreadCpuTimeSupported", PlotDataSolver.BOOLEAN);
		dataSolverBeanAssignmentMap.put("java.nio.BufferPool.direct:MemoryUsed", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.nio.BufferPool.direct:TotalCapacity", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.nio.BufferPool.mapped:MemoryUsed", PlotDataSolver.MEMORY);
		dataSolverBeanAssignmentMap.put("java.nio.BufferPool.mapped:TotalCapacity", PlotDataSolver.MEMORY);

		PreferencesUtils.saveObject(PreferencesConstants.JMX_PLOT_DATA_SOLVER, dataSolverBeanAssignmentMap, true);
	}
}
