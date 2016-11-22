package rocks.inspectit.server.processor.impl;

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
			CompletableFuture.runAsync(() -> {
				EUMSpan frontEndSpan = (EUMSpan) data;
				AbstractEUMSpanDetails details = frontEndSpan.getDetails();
				if (details instanceof PageLoadRequest) {

					long traceId = frontEndSpan.getSpanIdent().getTraceId();
					SpanIdent backEndSpanIdent = new SpanIdent(traceId, traceId, traceId);

					AbstractSpan backEndSpan = spanDao.get(backEndSpanIdent);
					if (backEndSpan != null) {
						backEndSpan.setSpanIdent(new SpanIdent(traceId, traceId, frontEndSpan.getSpanIdent().getId()));
					}
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
		}
		return false;
	}

}
