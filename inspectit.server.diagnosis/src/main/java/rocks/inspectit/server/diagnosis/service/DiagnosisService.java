package rocks.inspectit.server.diagnosis.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineException;
import rocks.inspectit.server.diagnosis.engine.IDiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.ExecutorServiceUtils;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;

/**
 * The implementation of the IDiagnosisService. First the {@link #DiagnosisEngine} is initialized.
 * Then the {@link #InvocationSequenceData} are offered to a BlockingQueue. The DiagnosisEngine
 * takes the {@link #InvocationSequenceData} from the queue and analyzes them.
 *
 * @author Claudio Waldvogel, Christian Voegele
 *
 */
public class DiagnosisService implements IDiagnosisService, Runnable {

	/**
	 * Specifies how long the the engine waits to shutdown properly. <b>Timeout is specified in
	 * seconds.</b>
	 */
	private static final int SHUTDOWN_TIMEOUT = 2;

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * How long to wait before the queue gives up, in units of unit.
	 */
	private long timeOut;

	/**
	 * Number of threads that runs within the diagnosis engine.
	 */
	private int numberOfSessionWorker;

	/**
	 * The Interface of the DiagnosisResultNotificationService that takes the resulting diagnosis
	 * results and creates ProblemOccurences.
	 */
	@Autowired
	private IDiagnosisResultNotificationService diagnosisResultService;

	/**
	 * The DiagnosisEngine interface.
	 */
	private IDiagnosisEngine<InvocationSequenceData> diagnosisEngine;

	/**
	 * The used queue for InvocationSequenceData which are the input for the diagnosis engine. The
	 * queue will be initialized in the constructor method.
	 */
	private final BlockingQueue<DiagnosisInput> queue;

	/**
	 * Executor thread of DiagnosisService.
	 */
	private final ExecutorService diagnosisServiceExecutor = Executors.newSingleThreadExecutor();

	/**
	 * The packages where all rules are defined which should be used during diagnosis.
	 */
	private final List<String> rulesPackages;

	/**
	 * Constructor for DiagnosisService.
	 *
	 * @param rulesPackages
	 *            List of packageNames where the classes of the rules are located
	 * @param numberOfSessionWorker
	 *            Number of parallel session worker
	 * @param timeOut
	 *            Timeout of queue
	 * @param queueCapacity
	 *            Capacity of the queue
	 */
	public DiagnosisService(List<String> rulesPackages, int numberOfSessionWorker, long timeOut, int queueCapacity) {
		this.rulesPackages = rulesPackages;
		this.numberOfSessionWorker = numberOfSessionWorker;
		this.timeOut = timeOut;
		this.queue = new LinkedBlockingQueue<>(queueCapacity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean diagnose(InvocationSequenceData invocation, double baseline) {
		try {
			return queue.offer(new DiagnosisInput(invocation, baseline), timeOut, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.info("Specified waiting of time of BlockingQueue for DiagnosisService elapses before space is available");
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			DiagnosisInput diagnosisInput = queue.take();
			diagnosisEngine.analyze(diagnosisInput.getInvocation(), Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, diagnosisInput.getBaseline()));
		} catch (DiagnosisEngineException e) {
			log.warn("During analyzing of DiagnosisEngine an exception occured");
		} catch (InterruptedException e) {
			log.warn("The DiagnosisService Thread is interrupted");
		} finally {
			if (!diagnosisServiceExecutor.isShutdown()) {
				diagnosisServiceExecutor.execute(this);
			}
		}
	}

	/**
	 * Initialization of the DiagnosisService and DiagnosisEngine.
	 *
	 * @return true if initialization was successful otherwise false
	 */
	@PostConstruct
	public boolean init() {
		Set<Class<?>> ruleClasses = readDiagnosisRuleClasses();
		if (ruleClasses.isEmpty()) {
			if (log.isInfoEnabled()) {
				log.info("|-Diagnosis Service inactive as no rules are found in " + rulesPackages);
			}
			return false;
		} else {
			DiagnosisEngineConfiguration<InvocationSequenceData, List<ProblemOccurrence>> configuration = new DiagnosisEngineConfiguration<InvocationSequenceData, List<ProblemOccurrence>>();
			configuration.setNumSessionWorkers(numberOfSessionWorker);
			configuration.addRuleClasses(ruleClasses);
			configuration.setResultCollector(new ProblemOccurenceResultCollector());
			configuration.addSessionCallback(new DelegatingResultHandler());

			try {
				diagnosisEngine = new DiagnosisEngine<>(configuration);
			} catch (DiagnosisEngineException e) {
				log.info("DiagnosisEngine could not be initialized.");
			}
			diagnosisServiceExecutor.execute(this);
			if (log.isInfoEnabled()) {
				log.info("|-Diagnosis Service active...");
			}
			return true;
		}
	}

	/**
	 * This method derives the classes that represent the diagnosis rules.
	 *
	 * @return Set of classes that represent the diagnosis rules.
	 */
	private Set<Class<?>> readDiagnosisRuleClasses() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Rule.class));
		Set<Class<?>> ruleClasses = new HashSet<>();
		ClassLoader classLoader = DiagnosisService.class.getClassLoader();
		for (String packageName : rulesPackages) {
			for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
				Class<?> clazz;
				try {
					clazz = classLoader.loadClass(bd.getBeanClassName());
					ruleClasses.add(clazz);
				} catch (ClassNotFoundException e) {
					log.warn("Rule class " + bd.getBeanClassName() + "could not be found");
				}
			}
		}
		return ruleClasses;
	}

	/**
	 * Checks whether the {@link DiagnosisService} is shut down.
	 *
	 * @return Returns true, if engine is shut down.
	 */
	public boolean isShutdown() {
		return diagnosisServiceExecutor.isShutdown();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown(boolean awaitShutdown) {
		ExecutorServiceUtils.shutdownExecutor(diagnosisServiceExecutor, SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
	}

	/**
	 * Helper class that defines the input for the BlockingQueue.
	 *
	 * @author Claudio Waldvogel
	 *
	 */
	public static class DiagnosisInput {

		/**
		 * The input InvocationSequenceData.
		 */
		private final InvocationSequenceData invocation;

		/**
		 * The input baseline.
		 */
		private final double baseline;

		/**
		 * Constructor for DiagnosisInput.
		 *
		 * @param invocation
		 *            The input InvocationSequenceData
		 * @param baseline
		 *            The input baseline
		 */
		DiagnosisInput(final InvocationSequenceData invocation, final double baseline) {
			this.invocation = invocation;
			this.baseline = baseline;
		}

		/**
		 * Gets {@link #invocation}.
		 *
		 * @return {@link #invocation}
		 */
		public InvocationSequenceData getInvocation() {
			return invocation;
		}

		/**
		 * Gets {@link #baseline}.
		 *
		 * @return {@link #baseline}
		 */
		public double getBaseline() {
			return baseline;
		}
	}

	/**
	 * Handling the results of a Diagnosis Engine Session execution.
	 *
	 * @author Claudio Waldvogel
	 *
	 */
	private class DelegatingResultHandler implements ISessionCallback<List<ProblemOccurrence>> {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(List<ProblemOccurrence> result) {
			diagnosisResultService.onNewDiagnosisResult(result);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable t) {
			log.warn("Failed conducting diagnosis!", t);
		}
	}

}