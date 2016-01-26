package info.novatec.inspectit.cmr.instrumentation.classcache;

import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationResult;

import java.util.Collection;
import java.util.Map;

/**
 * Service for the instrumentation of classes inside the class cache.
 *
 * @author Ivan Senic
 *
 */
public interface IClassCacheInstrumentation {

	/**
	 * Directly instruments the given {@link ImmutableClassType} and return instrumentation result
	 * if instrumentation process added instrumentation points. Otherwise this method returns
	 * <code>null</code> and this means that type has no instrumentation.
	 *
	 * @param type
	 *            {@link ImmutableClassType} to check.
	 * @param agentConfiguration
	 *            configuration to use
	 * @param appliers
	 *            Collection of {@link IInstrumentationApplier}s to process type against.
	 * @return {@link InstrumentationResult} if the class has been instrumented, otherwise
	 *         <code>null</code>.
	 */
	InstrumentationResult instrument(ImmutableClassType type, AgentConfiguration agentConfiguration, Collection<IInstrumentationApplier> appliers);

	/**
	 * Collects instrumentation points for all the initialized class types in the class cache.
	 *
	 * @return Collection holding all the {@link InstrumentationResult}.
	 */
	Collection<InstrumentationResult> getInstrumentationResults();

	/**
	 * Collects instrumentation points for given types in the given class cache. Only initialized
	 * class types will be checked.
	 *
	 * @param types
	 *            to get instrumentation results
	 * @return Collection holding all the {@link InstrumentationResult}.
	 */
	Collection<InstrumentationResult> getInstrumentationResults(Collection<? extends ImmutableType> types);

	/**
	 * Collects instrumentation results for all the initialized class types in the class cache. The
	 * return map will contain a key-value pairs where key is set of hashes that correspond to the
	 * instrumentation result (value).
	 *
	 * @return Map holding key-value pairs that connect set of hashes to the
	 *         {@link InstrumentationResult}.
	 */
	Map<Collection<String>, InstrumentationResult> getInstrumentationResultsWithHashes();

	/**
	 * Collects instrumentation results for for given types in the given class cache. Only
	 * initialized class types will be checked. The return map will contain a key-value pairs where
	 * key is set of hashes that correspond to the instrumentation result (value).
	 *
	 * @param types
	 *            to get instrumentation results
	 * @return Map holding key-value pairs that connect set of hashes to the
	 *         {@link InstrumentationResult}.
	 */
	Map<Collection<String>, InstrumentationResult> getInstrumentationResultsWithHashes(Collection<? extends ImmutableType> types);

	/**
	 * Processes all types in the class cache in order to add instrumentation points for the given
	 * agent configuration. Instrumentation points added will be created based on given
	 * {@link IInstrumentationApplier}s.
	 *
	 * @param agentConfiguration
	 *            configuration to use
	 * @param appliers
	 *            Collection of {@link IInstrumentationApplier}s to process types against.
	 * @return Returns collection of class types to which the instrumentation points have been
	 *         added.
	 */
	Collection<? extends ImmutableClassType> addInstrumentationPoints(AgentConfiguration agentConfiguration, Collection<IInstrumentationApplier> appliers);

	/**
	 * Processes given types in the class cache in order to add instrumentation points.
	 * Instrumentation points added will be created based on given {@link IInstrumentationApplier}s.
	 *
	 * @param types
	 *            to add instrumentation points based on given configuration and environment.
	 * @param agentConfiguration
	 *            configuration to use
	 * @param appliers
	 *            Collection of {@link IInstrumentationApplier}s to process types against.
	 * @return Returns collection of class types to which the instrumentation points have been
	 *         added.
	 */
	Collection<? extends ImmutableClassType> addInstrumentationPoints(Collection<? extends ImmutableType> types, AgentConfiguration agentConfiguration, Collection<IInstrumentationApplier> appliers);

	/**
	 * Removes all instrumentation points from the class cache.
	 *
	 * @return types from which instrumentation points have been removed
	 */
	Collection<? extends ImmutableClassType> removeInstrumentationPoints();

	/**
	 * Removes all instrumentation point from the given types.
	 *
	 * @param types
	 *            to remove instrumentation points
	 * @return types from which instrumentation points have been removed
	 */
	Collection<? extends ImmutableClassType> removeInstrumentationPoints(Collection<? extends ImmutableType> types);

	/**
	 * Removes all instrumentation point from the given types that that might be created as result
	 * of given instrumentation appliers.
	 *
	 * @param types
	 *            to remove instrumentation points
	 * @param instrumentationAppliers
	 *            Collection of {@link IInstrumentationApplier}s to process types against.
	 * @return types from which instrumentation points have been removed
	 */
	Collection<? extends ImmutableClassType> removeInstrumentationPoints(Collection<? extends ImmutableType> types, Collection<IInstrumentationApplier> instrumentationAppliers);

}
