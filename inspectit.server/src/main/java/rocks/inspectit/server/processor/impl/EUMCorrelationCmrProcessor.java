package rocks.inspectit.server.processor.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import rocks.inspectit.server.dao.impl.BufferSpanDaoImpl;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMSpanDetails;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Processor responsible for correlating existing back-end traces in the buffer.
 *
 * @author Jonas Kunz
 *
 */
public class EUMCorrelationCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link BufferSpanDaoImpl}.
	 */
	@Autowired
	private BufferSpanDaoImpl spanDao;

	/**
	 * Scheduled executor used for scheduling the Span correlation due to asynchronous indexing and
	 * adding data to the buffer.
	 */
	@Qualifier("scheduledExecutorService")
	@Autowired
	private ScheduledExecutorService scheduledExecutor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData data, EntityManager entityManager) {
		if (data instanceof EUMSpan) {
			EUMSpan frontEndSpan = (EUMSpan) data;
			AbstractEUMSpanDetails details = frontEndSpan.getDetails();
			if (details instanceof PageLoadRequest) {
				long traceId = frontEndSpan.getSpanIdent().getTraceId();
				long eumSpanId = frontEndSpan.getSpanIdent().getId();
				// if the ids are equal no correlation takes place, e.g. because the html was
				// cached.
				if (traceId != eumSpanId) {
					EumSpanCorrelationTask correlationTask = new EumSpanCorrelationTask(traceId, eumSpanId);
					correlationTask.schedule(true);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData data) {
		if (data instanceof EUMSpan) {
			AbstractEUMSpanDetails details = ((EUMSpan) data).getDetails();
			if (details instanceof PageLoadRequest) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Recurring task to correlate EUM Pageload requests with their back end span. Reoccuring
	 * because the updating of the buffer happens async and we don't know hen which span arrives in
	 * the CMR.
	 *
	 * @author Jonas Kunz
	 *
	 */
	private class EumSpanCorrelationTask implements Runnable {

		/**
		 * Number of seconds to wait between trials of correlation.
		 */
		private static final int NUMBER_OF_SECONDS_BETWEEN_TRIALS = 3;

		/**
		 * The number of retrials until the correlation is aborted for this trace.
		 */
		private int retrialsLeft = 20;

		/**
		 * The trace (and span)-id of the back end trace to correlate.
		 */
		private long traceId;

		/**
		 * The span ID of the EUM span to correlate.
		 */
		private long eumSpanId;

		/**
		 * Constructor. Does not schedule the task.
		 *
		 * @param traceId
		 *            the traceId of the span to correlate
		 * @param eumSpanId
		 *            the spanId of the front-end span
		 */
		EumSpanCorrelationTask(long traceId, long eumSpanId) {
			this.traceId = traceId;
			this.eumSpanId = eumSpanId;
		}

		/**
		 * Schedules another trial for correlation.
		 *
		 * @param immediate
		 *            true if the attempt should be run immediately
		 */
		void schedule(boolean immediate) {
			if (retrialsLeft > 0) {
				retrialsLeft--;
				scheduledExecutor.schedule(this, immediate ? 0 : NUMBER_OF_SECONDS_BETWEEN_TRIALS, TimeUnit.SECONDS);
			}
		}

		@Override
		public void run() {
			boolean reschedule = true;
			try {
				SpanIdent backEndSpanIdent = new SpanIdent(traceId, traceId);
				AbstractSpan backEndSpan = spanDao.get(backEndSpanIdent);
				if (backEndSpan != null) {
					backEndSpan.setParentSpanId(eumSpanId);
					reschedule = false;
				}
			} finally {
				if (reschedule) {
					schedule(false); // retry later
				}
			}
		}

	}

}
