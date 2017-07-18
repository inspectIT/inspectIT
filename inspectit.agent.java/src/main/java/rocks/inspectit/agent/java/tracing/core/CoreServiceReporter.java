package rocks.inspectit.agent.java.tracing.core;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.tracing.core.transformer.SpanTransformer;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;

/**
 * Tracing reporter that sends spans to the {@link ICoreService}.
 *
 * @author Ivan Senic
 *
 */
@Component
public class CoreServiceReporter implements Reporter {

	/**
	 * Logger for this class.
	 */
	@Log
	Logger log;

	/**
	 * {@link ICoreService}.
	 */
	@Autowired
	private ICoreService coreService;

	/**
	 * {@link IPlatformManager}.
	 */
	@Autowired
	private IPlatformManager platformManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void report(SpanImpl span) {
		AbstractSpan transformed = SpanTransformer.transformSpan(span);
		transformed.setPlatformIdent(platformManager.getPlatformId());
		coreService.addDefaultData(transformed);
	}

}
