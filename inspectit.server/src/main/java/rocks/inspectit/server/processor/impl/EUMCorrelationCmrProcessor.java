package rocks.inspectit.server.processor.impl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.dao.impl.BufferSpanDaoImpl;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMSpanDetails;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
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
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData data, EntityManager entityManager) {
		if (data instanceof EUMSpan) {
			EUMSpan frontEndSpan = (EUMSpan) data;
			AbstractEUMSpanDetails details = frontEndSpan.getDetails();
			if (details instanceof PageLoadRequest) {
				CompletableFuture.runAsync(() -> {
					long traceId = frontEndSpan.getSpanIdent().getTraceId();
					SpanIdent backEndSpanIdent = new SpanIdent(traceId, traceId);

					AbstractSpan backEndSpan = spanDao.get(backEndSpanIdent);
					if (backEndSpan != null) {
						backEndSpan.setParentSpanId(frontEndSpan.getSpanIdent().getId());
					}
				});
			}
		} else if (data instanceof AbstractSpan) { // guaranteed to be root and HTTP span
			CompletableFuture.runAsync(() -> {
				AbstractSpan backEndSpan = (AbstractSpan) data;
				// check if the corresponding EUM span already arrived
				long traceId = backEndSpan.getSpanIdent().getTraceId();

				Optional<EUMSpan> frontEndSpan = spanDao.getSpans(traceId).stream()
						.filter((s) -> s instanceof EUMSpan)
						.map((s) -> (EUMSpan) s)
						.filter((s) -> s.getDetails() instanceof PageLoadRequest)
						.filter(AbstractSpan::isRoot).findFirst();
				if (frontEndSpan.isPresent()) {
					backEndSpan.setParentSpanId(frontEndSpan.get().getSpanIdent().getId());
				}
			});

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
		} else if (data instanceof AbstractSpan) {
			AbstractSpan span = (AbstractSpan) data;
			return (span.getPropagationType() == PropagationType.HTTP) && span.isRoot();
		}
		return false;
	}

}
