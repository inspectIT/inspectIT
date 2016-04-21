package rocks.inspectit.server.diagnosis.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.engine.session.ISessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.Session;

/**
 * Class to configure a {@link DiagnosisEngine}.
 *
 * @param <I>
 *            The type of the input to the engine.
 * @param <R>
 *            The expected return type. Ensure that <R> matches the return type of the
 *            {@link ISessionResultCollector}.
 * @author Claudio Waldvogel, Alexander Wert
 */
public class DiagnosisEngineConfiguration<I, R> {

	/**
	 * The amount of threads which will run {@link Session}s.
	 */
	private int numSessionWorkers = 2;

	/**
	 * Specifies how long the the engine waits to shutdown properly. <b>Timeout is specified in
	 * seconds.</b>
	 */
	private int shutdownTimeout = 2;

	/**
	 * The set of classes implementing diagnosis rules.
	 */
	private final Set<Class<?>> ruleClasses = Sets.newHashSet();

	/**
	 * The {@link ExecutorService} to be used. If not provided, a default will be created
	 */
	private ExecutorService executorService;

	/**
	 * The {@link IRuleOutputStorage} implementation to be used.
	 */
	private Class<? extends IRuleOutputStorage> storageClass = DefaultRuleOutputStorage.class;

	/**
	 * The {@link ISessionCallback}s to be invoked if a session complete. If not provided, it is
	 * still possible to provided results from rules directly somehow. <b>It is strongly recommended
	 * to use callback mechanism instead of work around in rule implementations</b>
	 */
	private final List<ISessionCallback<R>> callbacks = Lists.newArrayList();

	/**
	 * The {@link ISessionResultCollector} to be used. Must not be null.
	 */
	private ISessionResultCollector<I, R> resultCollector;

	/**
	 * Sets {@link #numSessionWorkers}.
	 *
	 * @param numSessionWorkers
	 *            New value for {@link #numRuleWorkers}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setNumSessionWorkers(int numSessionWorkers) {
		checkArgument(numSessionWorkers > 0, "numSessionWorkers must be at least 1.");
		this.numSessionWorkers = numSessionWorkers;
		return this;
	}

	/**
	 * Sets {@link #executorService}.
	 *
	 * @param executorService
	 *            New value for {@link #executorService}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setExecutorService(ExecutorService executorService) {
		this.executorService = checkNotNull(executorService, "The ExecutorService must not be null.");
		return this;
	}

	/**
	 * Adds new Classes to {@link #ruleClasses}.
	 *
	 * @param ruleClasses
	 *            New values for {@link #ruleClasses}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> addRuleClasses(Collection<Class<?>> ruleClasses) {
		checkNotNull(ruleClasses, "Set of rule classes must not be null.");
		for (Class<?> ruleClass : ruleClasses) {
			addRuleClass(ruleClass);
		}
		return this;
	}

	/**
	 * Adds new Classes to {@link #ruleClasses}.
	 *
	 * @param ruleClasses
	 *            New values for {@link #ruleClasses}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> addRuleClasses(Class<?>... ruleClasses) {
		addRuleClasses(Arrays.asList(checkNotNull(ruleClasses, "Array of rule classes must not be null.")));
		return this;
	}

	/**
	 * Adds a new Class to {@link #ruleClasses}.
	 *
	 * @param ruleClass
	 *            A new value for {@link #ruleClasses}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> addRuleClass(Class<?> ruleClass) {
		this.ruleClasses.add(checkNotNull(ruleClass, "Rule class must not be null."));
		return this;
	}

	/**
	 * Sets {@link #shutdownTimeout}.
	 *
	 * @param shutdownTimeout
	 *            New value for {@link #shutdownTimeout} in seconds.
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setShutdownTimeout(int shutdownTimeout) {
		checkArgument(shutdownTimeout > 0, "shutdownTimeout must be at least 1.");
		this.shutdownTimeout = shutdownTimeout;
		return this;
	}

	/**
	 * Sets {@link #storageClass}.
	 *
	 * @param storageClass
	 *            New value for {@link #storageClass}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setStorageClass(Class<? extends IRuleOutputStorage> storageClass) {
		this.storageClass = checkNotNull(storageClass, "Storage classes must not be null.");
		return this;
	}

	/**
	 * Sets {@link #resultCollector}.
	 *
	 * @param resultCollector
	 *            New value for {@link #resultCollector}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setResultCollector(ISessionResultCollector<I, R> resultCollector) {
		this.resultCollector = checkNotNull(resultCollector, "Result collector must not be null.");
		return this;
	}

	/**
	 * Adds a new callback to {@link #callbacks}.
	 *
	 * @param callback
	 *            A new callback for {@link #callbacks}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> addSessionCallback(ISessionCallback<R> callback) {
		this.callbacks.add(checkNotNull(callback, "Session callback must not be null."));
		return this;
	}

	/**
	 * Adds a list of new {@link ISessionCallback}s {@link #callbacks}.
	 *
	 * @param sessionCallbacks
	 *            New entries for {@link #callbacks}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> addSessionCallbacks(List<ISessionCallback<R>> sessionCallbacks) {
		for (ISessionCallback<R> callback : sessionCallbacks) {
			addSessionCallback(callback);
		}
		return this;
	}

	/**
	 * Validates the {@link DiagnosisEngineConfiguration}.
	 *
	 * @throws DiagnosisEngineException
	 *             If any of the configuration elements is invalid or is missing.
	 */
	public void validate() throws DiagnosisEngineException {
		try {
			checkArgument(numSessionWorkers > 0, "numSessionWorkers must be at least 1.");
			checkArgument(shutdownTimeout > 0, "shutdownTimeout must be at least 1.");
			checkArgument(CollectionUtils.isNotEmpty(ruleClasses), "At least one rule class must be specified.");
			checkNotNull(resultCollector, "Result collector must not be null.");

			if (executorService == null) {
				executorService = Executors.newFixedThreadPool(getNumSessionWorkers());
			}
		} catch (IllegalArgumentException e) {
			throw new DiagnosisEngineException("Invalid Diagnosis Engine configuration!", e);
		}
	}

	// -------------------------------------------------------------
	// Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #numSessionWorkers}.
	 *
	 * @return {@link #numSessionWorkers}
	 */
	public int getNumSessionWorkers() {
		return numSessionWorkers;
	}

	/**
	 * Gets {@link #shutdownTimeout}.
	 *
	 * @return {@link #shutdownTimeout}
	 */
	public int getShutdownTimeout() {
		return shutdownTimeout;
	}

	/**
	 * Gets {@link #ruleClasses}.
	 *
	 * @return {@link #ruleClasses}
	 */
	public Set<Class<?>> getRuleClasses() {
		return ruleClasses;
	}

	/**
	 * Gets {@link #executorService}.
	 *
	 * @return {@link #executorService}
	 */
	public ExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Gets {@link #storageClass}.
	 *
	 * @return {@link #storageClass}
	 */
	public Class<? extends IRuleOutputStorage> getStorageClass() {
		return storageClass;
	}

	/**
	 * Gets {@link #resultCollector}.
	 *
	 * @return {@link #resultCollector}
	 */
	public ISessionResultCollector<I, R> getResultCollector() {
		return resultCollector;
	}

	/**
	 * Gets {@link #callbacks}.
	 *
	 * @return {@link #callbacks}. Is never <code>null</code>.
	 */
	public List<ISessionCallback<R>> getSessionCallbacks() {
		return callbacks;
	}
}
