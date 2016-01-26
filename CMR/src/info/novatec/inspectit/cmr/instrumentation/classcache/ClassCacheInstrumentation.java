package info.novatec.inspectit.cmr.instrumentation.classcache;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.cmr.instrumentation.config.ClassCacheSearchNarrower;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.applier.RemoveAllInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationResult;
import info.novatec.inspectit.spring.logger.Log;

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

/**
 * Instrumentation service for the {@link ClassCache}.
 *
 * @author Ivan Senic
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ClassCacheInstrumentation implements IClassCacheInstrumentation {

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
	 * {@inheritDoc}
	 */
	@Override
	public InstrumentationResult instrument(final ImmutableClassType type, final AgentConfiguration agentConfiguration, final Collection<IInstrumentationApplier> appliers) {
		if (!type.isInitialized()) {
			return null;
		}

		try {
			return classCache.executeWithWriteLock(new Callable<InstrumentationResult>() {
				@Override
				public InstrumentationResult call() throws Exception {
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
	 * {@inheritDoc}
	 */
	@Override
	public Collection<InstrumentationResult> getInstrumentationResults() {
		return getInstrumentationResults(classCache.getLookupService().findAll());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<InstrumentationResult> getInstrumentationResults(final Collection<? extends ImmutableType> types) {
		if (CollectionUtils.isEmpty(types)) {
			return Collections.emptyList();
		}

		try {
			return classCache.executeWithReadLock(new Callable<Collection<InstrumentationResult>>() {
				@Override
				public Collection<InstrumentationResult> call() throws Exception {
					Collection<InstrumentationResult> results = new ArrayList<>();
					for (ImmutableType type : types) {
						if (type.isInitialized() && type.isClass()) {
							ImmutableClassType immutableClassType = type.castToClass();
							InstrumentationResult instrumentationResult = createInstrumentationResult(immutableClassType);
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
	 * {@inheritDoc}
	 */
	@Override
	public Map<Collection<String>, InstrumentationResult> getInstrumentationResultsWithHashes() {
		return getInstrumentationResultsWithHashes(classCache.getLookupService().findAll());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Collection<String>, InstrumentationResult> getInstrumentationResultsWithHashes(final Collection<? extends ImmutableType> types) {
		if (CollectionUtils.isEmpty(types)) {
			return Collections.emptyMap();
		}

		try {
			return classCache.executeWithReadLock(new Callable<Map<Collection<String>, InstrumentationResult>>() {
				@Override
				public Map<Collection<String>, InstrumentationResult> call() throws Exception {
					Map<Collection<String>, InstrumentationResult> map = new HashMap<>();
					for (ImmutableType type : types) {
						if (type.isInitialized() && type.isClass()) {
							ImmutableClassType immutableClassType = type.castToClass();
							InstrumentationResult instrumentationResult = createInstrumentationResult(immutableClassType);
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
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableClassType> addInstrumentationPoints(AgentConfiguration agentConfiguration, Collection<IInstrumentationApplier> appliers) {
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
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableClassType> addInstrumentationPoints(final Collection<? extends ImmutableType> types, final AgentConfiguration agentConfiguration,
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
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableClassType> removeInstrumentationPoints() {
		return removeInstrumentationPoints(classCache.getLookupService().findAll());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends ImmutableClassType> removeInstrumentationPoints(final Collection<? extends ImmutableType> types) {
		return removeInstrumentationPoints(types, Collections.<IInstrumentationApplier> singleton(RemoveAllInstrumentationApplier.getInstance()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * Creates {@link InstrumentationResult} for the given {@link ImmutableClassType}. Returns
	 * <code>null</code> if class has no instrumentation points.
	 *
	 * @param classType
	 *            {@link ImmutableClassType} to create {@link InstrumentationResult} for.
	 * @return {@link InstrumentationResult} for this class type or <code>null</code> if class has
	 *         no instrumentation points.
	 */
	private InstrumentationResult createInstrumentationResult(ImmutableClassType classType) {
		// if there are no instrumentation points return null
		if (!classType.hasInstrumentationPoints()) {
			return null;
		}

		InstrumentationResult instrumentationResult = new InstrumentationResult(classType.getFQN());
		if (classType.hasInstrumentationPoints()) {
			instrumentationResult.setMethodInstrumentationConfigs(classType.getInstrumentationPoints());
		}
		return instrumentationResult;
	}

}
