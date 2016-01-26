package rocks.inspectit.server.instrumentation.classcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.instrumentation.config.ClassCacheSearchNarrower;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.server.instrumentation.config.applier.RemoveAllInstrumentationApplier;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

/**
 * Instrumentation service for the {@link ClassCache}. This class is responsible for adding, getting
 * or removing the instrumentation points to/from class types. Also provides the
 * {@link #addAndGetInstrumentationResult(ImmutableClassType, AgentConfig, Collection)} method for easy add/get
 * instrumentation points for a single type.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ClassCacheInstrumentation {

	/**
	 * Log of the class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ClassCache} instrumentation service belongs to.
	 */
	private ClassCache classCache;

	/**
	 * {@link ClassCacheSearchNarrower} to help when analyzing the whole class cache.
	 */
	@Autowired
	private ClassCacheSearchNarrower searchNarrower;

	/**
	 * Init the {@link ClassCacheLookup}.
	 *
	 * @param classcache
	 *            {@link ClassCache} it belongs to.
	 */
	public void init(ClassCache classcache) {
		this.classCache = classcache;
	}

	/**
	 * Directly adds instrumentation points the given {@link ImmutableClassType} and return
	 * instrumentation result if process added instrumentation points. Otherwise this method returns
	 * <code>null</code> and this means that type has no instrumentation.
	 *
	 * @param type
	 *            {@link ImmutableClassType} to check.
	 * @param agentConfiguration
	 *            configuration to use
	 * @param appliers
	 *            Collection of {@link IInstrumentationApplier}s to process type against.
	 * @return {@link InstrumentationDefinition} if the class has been instrumented, otherwise
	 *         <code>null</code>.
	 */
	public InstrumentationDefinition addAndGetInstrumentationResult(final ImmutableClassType type, final AgentConfig agentConfiguration, final Collection<IInstrumentationApplier> appliers) {
		if (!type.isInitialized()) {
			return null;
		}

		try {
			return classCache.executeWithWriteLock(new Callable<InstrumentationDefinition>() {
				@Override
				public InstrumentationDefinition call() throws Exception {
					ClassType classType = (ClassType) type;
					boolean added = false;

					for (IInstrumentationApplier applier : appliers) {
						added |= applier.addInstrumentationPoints(agentConfiguration, classType);
					}

					if (added) {
						return createInstrumentationResult(type);
					} else {
						return null;
					}
				}
			});
		} catch (Exception e) {
			log.error("Error occurred while trying to instrument class type from the class cache.", e);
			return null;
		}
	}

	/**
	 * Collects instrumentation points for all the initialized class types in the class cache.
	 *
	 * @return Collection holding all the {@link InstrumentationDefinition}.
	 */
	public Collection<InstrumentationDefinition> getInstrumentationResults() {
		return getInstrumentationResults(classCache.getLookupService().findAll());
	}

	/**
	 * Collects instrumentation points for given types in the given class cache. Only initialized
	 * class types will be checked.
	 *
	 * @param types
	 *            to get instrumentation results
	 * @return Collection holding all the {@link InstrumentationDefinition}.
	 */
	public Collection<InstrumentationDefinition> getInstrumentationResults(final Collection<? extends ImmutableType> types) {
		if (CollectionUtils.isEmpty(types)) {
			return Collections.emptyList();
		}

		try {
			return classCache.executeWithReadLock(new Callable<Collection<InstrumentationDefinition>>() {
				@Override
				public Collection<InstrumentationDefinition> call() throws Exception {
					Collection<InstrumentationDefinition> results = new ArrayList<>();
					for (ImmutableType type : types) {
						if (type.isInitialized() && type.isClass()) {
							ImmutableClassType immutableClassType = type.castToClass();
							InstrumentationDefinition instrumentationResult = createInstrumentationResult(immutableClassType);
							if (null != instrumentationResult) {
								results.add(instrumentationResult);
							}
						}
					}
					return results;
				}
			});
		} catch (Exception e) {
			log.error("Error occurred while trying to collect instrumentation results from the class cache.", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Collects instrumentation results for all the initialized class types in the class cache. The
	 * return map will contain a key-value pairs where key is set of hashes that correspond to the
	 * instrumentation result (value).
	 *
	 * @return Map holding key-value pairs that connect set of hashes to the
	 *         {@link InstrumentationDefinition}.
	 */
	public Map<Collection<String>, InstrumentationDefinition> getInstrumentationResultsWithHashes() {
		return getInstrumentationResultsWithHashes(classCache.getLookupService().findAll());
	}

	/**
	 * Collects instrumentation results for for given types in the given class cache. Only
	 * initialized class types will be checked. The return map will contain a key-value pairs where
	 * key is set of hashes that correspond to the instrumentation result (value).
	 *
	 * @param types
	 *            to get instrumentation results
	 * @return Map holding key-value pairs that connect set of hashes to the
	 *         {@link InstrumentationDefinition}.
	 */
	public Map<Collection<String>, InstrumentationDefinition> getInstrumentationResultsWithHashes(final Collection<? extends ImmutableType> types) {
		if (CollectionUtils.isEmpty(types)) {
			return Collections.emptyMap();
		}

		try {
			return classCache.executeWithReadLock(new Callable<Map<Collection<String>, InstrumentationDefinition>>() {
				@Override
				public Map<Collection<String>, InstrumentationDefinition> call() throws Exception {
					Map<Collection<String>, InstrumentationDefinition> map = new HashMap<>();
					for (ImmutableType type : types) {
						if (type.isInitialized() && type.isClass()) {
							ImmutableClassType immutableClassType = type.castToClass();
							InstrumentationDefinition instrumentationResult = createInstrumentationResult(immutableClassType);
							if (null != instrumentationResult) {
								map.put(immutableClassType.getHashes(), instrumentationResult);
							}
						}
					}
					return map;
				}
			});
		} catch (Exception e) {
			log.error("Error occurred while trying to collect instrumentation results (with hashes) from the class cache.", e);
			return Collections.emptyMap();
		}
	}

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
	public Collection<? extends ImmutableClassType> addInstrumentationPoints(AgentConfig agentConfiguration, Collection<IInstrumentationApplier> appliers) {
		Collection<ImmutableClassType> results = new ArrayList<>(0);

		for (IInstrumentationApplier applier : appliers) {
			AbstractClassSensorAssignment<?> assignment = applier.getSensorAssignment();
			Collection<? extends ImmutableType> types;
			if (null != assignment) {
				types = searchNarrower.narrowByClassSensorAssignment(classCache, assignment);
			} else {
				types = classCache.getLookupService().findAll();
			}

			Collection<? extends ImmutableClassType> instrumented = addInstrumentationPoints(types, agentConfiguration, appliers);
			if (CollectionUtils.isNotEmpty(instrumented)) {
				results.addAll(instrumented);
			}
		}

		return results;
	}

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
	public Collection<? extends ImmutableClassType> addInstrumentationPoints(final Collection<? extends ImmutableType> types, final AgentConfig agentConfiguration,
			final Collection<IInstrumentationApplier> appliers) {
		if (CollectionUtils.isEmpty(types)) {
			return Collections.emptyList();
		}

		try {
			return classCache.executeWithWriteLock(new Callable<Collection<? extends ImmutableClassType>>() {
				@Override
				public Collection<? extends ImmutableClassType> call() throws Exception {
					Collection<ImmutableClassType> results = new ArrayList<>();
					for (ImmutableType type : types) {
						// only initialized class types can have instrumentation points
						if (type.isClass() && type.isInitialized()) {
							ClassType classType = (ClassType) type.castToClass();
							boolean added = false;

							for (IInstrumentationApplier applier : appliers) {
								added |= applier.addInstrumentationPoints(agentConfiguration, classType);
							}

							if (added) {
								results.add(type.castToClass());
							}
						}
					}
					return results;
				}
			});
		} catch (Exception e) {
			log.error("Error occurred while trying to add instrumentation points from the class cache.", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Removes all instrumentation points from the class cache.
	 *
	 * @return types from which instrumentation points have been removed
	 */
	public Collection<? extends ImmutableClassType> removeInstrumentationPoints() {
		return removeInstrumentationPoints(classCache.getLookupService().findAll());
	}

	/**
	 * Removes all instrumentation point from the given types.
	 *
	 * @param types
	 *            to remove instrumentation points
	 * @return types from which instrumentation points have been removed
	 */
	public Collection<? extends ImmutableClassType> removeInstrumentationPoints(final Collection<? extends ImmutableType> types) {
		return removeInstrumentationPoints(types, Collections.<IInstrumentationApplier> singleton(RemoveAllInstrumentationApplier.getInstance()));
	}

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
	public Collection<? extends ImmutableClassType> removeInstrumentationPoints(final Collection<? extends ImmutableType> types, final Collection<IInstrumentationApplier> instrumentationAppliers) {
		if (CollectionUtils.isEmpty(types)) {
			return Collections.emptyList();
		}

		try {
			return classCache.executeWithWriteLock(new Callable<Collection<? extends ImmutableClassType>>() {
				@Override
				public Collection<? extends ImmutableClassType> call() throws Exception {
					Collection<ImmutableClassType> results = new ArrayList<>();
					for (ImmutableType type : types) {
						// only initialized class types can have instrumentation points
						if (type.isClass() && type.isInitialized()) {
							ClassType classType = (ClassType) type.castToClass();
							boolean added = false;

							for (IInstrumentationApplier applier : instrumentationAppliers) {
								added |= applier.removeInstrumentationPoints(classType);
							}

							if (added) {
								results.add(type.castToClass());
							}
						}
					}
					return results;
				}
			});
		} catch (Exception e) {
			log.error("Error occurred while trying to remove specific instrumentation points from the class cache.", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Creates {@link InstrumentationDefinition} for the given {@link ImmutableClassType}. Returns
	 * <code>null</code> if class has no instrumentation points.
	 *
	 * @param classType
	 *            {@link ImmutableClassType} to create {@link InstrumentationDefinition} for.
	 * @return {@link InstrumentationDefinition} for this class type or <code>null</code> if class has
	 *         no instrumentation points.
	 */
	private InstrumentationDefinition createInstrumentationResult(ImmutableClassType classType) {
		// if there are no instrumentation points return null
		if (!classType.hasInstrumentationPoints()) {
			return null;
		}

		InstrumentationDefinition instrumentationResult = new InstrumentationDefinition(classType.getFQN());
		if (classType.hasInstrumentationPoints()) {
			instrumentationResult.setMethodInstrumentationConfigs(classType.getInstrumentationPoints());
		}
		return instrumentationResult;
	}

}
