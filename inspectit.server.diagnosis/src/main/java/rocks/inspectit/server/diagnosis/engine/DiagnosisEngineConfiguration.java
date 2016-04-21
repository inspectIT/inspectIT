package rocks.inspectit.server.diagnosis.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

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
 * @author Claudio Waldvogel
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
	public DiagnosisEngineConfiguration<I, R> setRuleClasses(Set<Class<?>> ruleClasses) {
		this.ruleClasses.addAll(checkNotNull(ruleClasses, "Set of rule classes must not be null."));
		return this;
	}

	/**
	 * Adds new Classes to {@link #ruleClasses}.
	 *
	 * @param ruleClasses
	 *            New values for {@link #ruleClasses}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setRuleClasses(Class<?>... ruleClasses) {
		this.ruleClasses.addAll(Arrays.asList(ruleClasses));
		return this;
	}

	/**
	 * Adds a new Class to {@link #ruleClasses}.
	 *
	 * @param ruleClass
	 *            A new value for {@link #ruleClasses}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setRuleClass(Class<?> ruleClass) {
		this.ruleClasses.add(checkNotNull(ruleClass, "Rule classes must not be null."));
		return this;
	}

	/**
	 * Sets {@link #shutdownTimeout}.
	 *
	 * @param shutdownTimeout
	 *            New value for {@link #shutdownTimeout}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setShutdownTimeout(int shutdownTimeout) {
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
		this.storageClass = storageClass;
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
		this.resultCollector = resultCollector;
		return this;
	}

	/**
	 * Adds a new callback to {@link #callbacks}.
	 *
	 * @param callback
	 *            A new callback for {@link #callbacks}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setSessionCallback(ISessionCallback<R> callback) {
		this.callbacks.add(callback);
		return this;
	}

	/**
	 * Ads a list of new {@link ISessionCallback}s {@link #callbacks}.
	 *
	 * @param sessionCallbacks
	 *            New entries for {@link #callbacks}
	 * @return DiagnosisEngineConfiguration itself
	 */
	public DiagnosisEngineConfiguration<I, R> setSessionCallbacks(List<ISessionCallback<R>> sessionCallbacks) {
		this.callbacks.addAll(sessionCallbacks);
		return this;
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
	 * @return {@link #callbacks}
	 */
	public List<ISessionCallback<R>> getSessionCallbacks() {
		return callbacks;
	}
}
