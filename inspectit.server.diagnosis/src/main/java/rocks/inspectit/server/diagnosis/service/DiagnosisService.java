package rocks.inspectit.server.diagnosis.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.util.Pair;
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
import rocks.inspectit.shared.all.communication.data.diagnosis.results.ProblemOccurrence;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * The implementation of the IDiagnosisService. First the {@link #DiagnosisEngine} is initialized.
 * Then the {@link #InvocationSequenceData} are offered to a BlockingQueue. The DiagnosisEngine
 * takes the InvocationSequenceData from the queue and analyzes them.
 *
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisService implements IDiagnosisService, Runnable {

	/**
	 * Specifies how long the the engine waits to shutdown properly. <b>Timeout is specified in
	 * seconds.</b>
	 */
	private static final int SHUTDOWNTIMEOUT = 2;

	/**
	 * The capacity of the queue.
	 */
	private static final int QUEUECAPACITY = 100;

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Start and Stop the DiagnosisService.
	 */
	boolean diagnosisServiceStopped = false;

	/**
	 * How long to wait before the queue gives up, in units of unit.
	 */
	private long timeOut;

	/**
	 * How long to wait before the queue gives up, in units of unit.
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
	private IDiagnosisEngine<InvocationSequenceData> engine;

	/**
	 * The uses queue for InvocationSequenceData.
	 */
	private final BlockingQueue<DiagnosisInput> queue = new LinkedBlockingQueue<>(QUEUECAPACITY);

	/**
	 * Executor thread of DiagnosisEngine.
	 */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * The packages where all rules are defined which should be used during diagnosis.
	 */
	private final List<String> rulesPackages;

	/**
	 * Constructor for DiagnosisService.
	 *
	 * @param rulesPackages
	 *            List of PackageNames
	 * @param numberOfSessionWorker
	 *            Number of parallel session worker
	 * @param timeOut
	 *            Timeout of queue
	 */
	public DiagnosisService(List<String> rulesPackages, int numberOfSessionWorker, long timeOut) {
		this.rulesPackages = rulesPackages;
		this.numberOfSessionWorker = numberOfSessionWorker;
		this.timeOut = timeOut;
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
	public int diagnose(Collection<Pair<InvocationSequenceData, Double>> invocationBaselinePairs) {
		int count = 0;
		for (Pair<InvocationSequenceData, Double> invocationBaselinePair : invocationBaselinePairs) {
			boolean successfullySubmitted = diagnose(invocationBaselinePair.getFirst(), invocationBaselinePair.getSecond());
			if (!successfullySubmitted) {
				break;
			}
			count++;
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			while (!executor.isShutdown()) {
				if (!diagnosisServiceStopped) {
					DiagnosisInput diagnosisInput = queue.take();
					Map<String, Double> sessionVariables = new HashMap<String, Double>();
					sessionVariables.put(RuleConstants.VAR_BASELINE, diagnosisInput.getBaseline());
					try {
						engine.analyze(diagnosisInput.getInvocation(), sessionVariables);
					} catch (DiagnosisEngineException e) {
						log.info("During analyzing of DiagnosisEngine an exception occured");
					}
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Initialization of the DiagnosisService and DiagnosisEngine.
	 *
	 * @return true if initialization was successful otherwise false
	 */
	@PostConstruct
	public boolean init() {

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Rule.class));
		Set<Class<?>> ruleClasses = new HashSet<>();
		for (String packageName : rulesPackages) {
			for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
				Class<?> clazz;
				try {
					clazz = Class.forName(bd.getBeanClassName());
					ruleClasses.add(clazz);
				} catch (ClassNotFoundException e) {
					log.info("Rule class could not be found");
				}
			}
		}

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
				engine = new DiagnosisEngine<>(configuration);
			} catch (DiagnosisEngineException e) {
				log.info("DiagnosisEngine could not be initialized.");
			}
			executor.execute(this);
			if (log.isInfoEnabled()) {
				log.info("|-Diagnosis Service active...");
			}
			return true;
		}
	}

	/**
	 * Gets {@link #diagnosisServiceStopped}.
	 *
	 * @return {@link #diagnosisServiceStopped}
	 */
	public final boolean isDiagnosisServiceStopped() {
		return this.diagnosisServiceStopped;
	}

	/**
	 * Sets {@link #diagnosisServiceStopped}.
	 *
	 * @param diagnosisServiceStopped
	 *            New value for {@link #diagnosisServiceStopped}
	 */
	public final void setDiagnosisServiceStopped(boolean diagnosisServiceStopped) {
		this.diagnosisServiceStopped = diagnosisServiceStopped;
	}

	/**
	 * Checks whether the {@link DiagnosisService} is shut down.
	 *
	 * @return Returns true, if engine is shut down.
	 */
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdown(boolean awaitShutdown) {
		if (!executor.isShutdown()) {
			executor.shutdown();
			if (awaitShutdown) {
				try {
					if (!executor.awaitTermination(SHUTDOWNTIMEOUT, TimeUnit.SECONDS)) {
						log.error("DiagnosisService executor did not shutdown within: {} seconds.", SHUTDOWNTIMEOUT);
					}
				} catch (InterruptedException e) {
					log.error("InterruptedException occured during termination of the DiagnosisService executor.", e);
				}
			}
		}
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
