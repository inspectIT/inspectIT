package rocks.inspectit.agent.java.sdk.opentracing.internal.impl;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import rocks.inspectit.agent.java.sdk.opentracing.ExtendedTracer;
import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.Timer;
import rocks.inspectit.agent.java.sdk.opentracing.TracerProvider;
import rocks.inspectit.agent.java.sdk.opentracing.internal.TracerLogger;
import rocks.inspectit.agent.java.sdk.opentracing.internal.propagation.TextMapPropagator;
import rocks.inspectit.agent.java.sdk.opentracing.internal.propagation.UrlEncodingPropagator;
import rocks.inspectit.agent.java.sdk.opentracing.noop.NoopReporter;
import rocks.inspectit.agent.java.sdk.opentracing.propagation.Propagator;
import rocks.inspectit.agent.java.sdk.opentracing.util.SystemTimer;

/**
 * The io.opentracing tracer implementation. This tracer keeps the thread context state. Every time
 * a span is started it will be added to the current thread span stack. Every time span is finished
 * it will be removed from the thread stack.
 * <p>
 * The tracer uses {@link Timer} for time measurement. inspectIT SDK provides a simple timer
 * implementation ({@link SystemTimer}), as it needs to be compatible with java 6.
 * <p>
 * The tracer uses {@link Reporter} for reporting finished spans. In inspectIT SDK there is an
 * option to explicitly state that span should not be reported, as inspectIT itself adds other
 * information to the span it creates and reports them itself. User created spans will always be
 * reported if not explicitly stated otherwise.
 *
 * @author Ivan Senic
 *
 */
public class TracerImpl implements ExtendedTracer {

	/**
	 * {@link TracerLogger} of this class.
	 */
	private static final TracerLogger LOGGER = TracerLoggerWrapper.getTraceLogger(TracerImpl.class);

	/**
	 * Span stack.
	 */
	private final ThreadLocal<Stack<SpanImpl>> spanStack = new ThreadLocal<Stack<SpanImpl>>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Stack<SpanImpl> initialValue() {
			return new Stack<SpanImpl>();
		}
	};

	/**
	 * Timer for measuring times.
	 */
	private Timer timer;

	/**
	 * Reporter to report spans to.
	 */
	private final Reporter reporter;

	/**
	 * Usable propagators.
	 */
	private final Map<Format<?>, Propagator<?>> propagators = new ConcurrentHashMap<Format<?>, Propagator<?>>(4, 1f);

	/**
	 * Initializes the tracer with {@link SystemTimer} and {@link NoopReporter}. Please use
	 * {@link TracerImpl#TracerImpl(Timer, Reporter, boolean)} for initialization with reporter of
	 * your choice.
	 */
	public TracerImpl() {
		this(new SystemTimer(), new NoopReporter(), false);
	}

	/**
	 * Default constructor. Timer and Reporter for this tracer must be provided.
	 *
	 * @param timer
	 *            {@link Timer}
	 * @param reporter
	 *            {@link Reporter}
	 * @param setToTracerProvider
	 *            If this tracer should be set to the {@link TracerProvider} class for static usage.
	 */
	public TracerImpl(Timer timer, Reporter reporter, boolean setToTracerProvider) {
		if (null == timer) {
			throw new IllegalArgumentException("Timer can not be null.");
		}
		if (null == reporter) {
			throw new IllegalArgumentException("Reporter can not be null.");
		}
		this.timer = timer;
		this.reporter = reporter;

		registerDefaultPropagators();

		if (setToTracerProvider) {
			TracerProvider.set(this);
		}
	}

	/**
	 * Registers default propagators. Users can overwrite by using
	 * {@link #registerPropagator(Format, Propagator)}.
	 */
	private void registerDefaultPropagators() {
		registerPropagator(Format.Builtin.TEXT_MAP, new TextMapPropagator());
		registerPropagator(Format.Builtin.HTTP_HEADERS, new UrlEncodingPropagator());
	}

	/**
	 * Registers propagator.
	 *
	 * @param <C>
	 *            format type
	 * @param format
	 *            opentracing {@link Format}
	 * @param propagator
	 *            {@link Propagator}
	 */
	@Override
	public <C> void registerPropagator(Format<C> format, Propagator<C> propagator) {
		propagators.put(format, propagator);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("New propagator registered for the format" + format.toString() + ".");
		}
	}

	/**
	 * Builds span with no operation name.
	 *
	 * @return {@link SpanBuilder}.
	 */
	@Override
	public SpanBuilderImpl buildSpan() {
		return buildSpan(null);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Note that as tracer is thread span context aware this method will automatically add reference
	 * (CHILD_OF) to the current thread span context if the one exists. If you want to manually
	 * specify reference type or want to ignore the current thread context then use
	 * {@link #buildSpan(String, String, boolean)}.
	 */
	@Override
	public SpanBuilderImpl buildSpan(String operationName) {
		return buildSpan(operationName, References.CHILD_OF, true);
	}

	/**
	 * Creates {@link SpanBuilder} that optionally adds the reference to the current thread context
	 * span.
	 *
	 * @param operationName
	 *            Operation name of the span.
	 * @param referenceType
	 *            Reference type to the current context.
	 * @param useThreadContext
	 *            If thread context should be used.
	 * @return {@link SpanBuilder}.
	 */
	@Override
	public SpanBuilderImpl buildSpan(String operationName, String referenceType, boolean useThreadContext) {
		SpanBuilderImpl spanBuilder = new SpanBuilderImpl(this, operationName);

		if (useThreadContext) {
			// check the current thread context
			SpanContextImpl threadContext = getCurrentContext();
			if (threadContext != null) {
				spanBuilder.addReference(referenceType, threadContext);
			}
		}

		return spanBuilder;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
		if ((format == null) || (carrier == null)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Context can not be injected, both format and carrier must be provided.");
			}
			return;
		}

		if (spanContext instanceof SpanContextImpl) {
			Propagator<C> propagator = (Propagator<C>) propagators.get(format);
			if (null != propagator) {
				propagator.inject((SpanContextImpl) spanContext, carrier);
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Context can not be injected, propagator does not exists for the format " + format.toString() + ".");
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <C> SpanContext extract(Format<C> format, C carrier) {
		if ((format == null) || (carrier == null)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Context can not be extracted, both format and carrier must be provided.");
			}
			return null;
		}

		Propagator<C> propagator = (Propagator<C>) propagators.get(format);
		if (null != propagator) {
			SpanContextImpl context = propagator.extract(carrier);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Context extracted: " + context);
			}

			return context;
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Context can not be extracted, propagator does not exists for the format " + format.toString() + ".");
			}
			return null;
		}
	}

	/**
	 * Returns current context.
	 *
	 * @return Returns current context.
	 */
	public SpanContextImpl getCurrentContext() {
		Stack<SpanImpl> stack = spanStack.get();
		if (!stack.isEmpty()) {
			return stack.peek().context();
		}
		return null;
	}

	/**
	 * Returns if the thread context exists.
	 *
	 * @return Returns if the thread context exists.
	 */
	public boolean isCurrentContextExisting() {
		return !spanStack.get().isEmpty();
	}

	/**
	 * Reports the span to the tracer once the span is started.
	 *
	 * @param span
	 *            Span.
	 */
	void spanStarted(SpanImpl span) {
		if (null == span) {
			return;
		}

		spanStack.get().push(span);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Span started " + span);
		}
	}

	/**
	 * Reports the span to the tracer once the span is finished.
	 *
	 * @param span
	 *            Span.
	 */
	void spanEnded(SpanImpl span) {
		if (null == span) {
			return;
		}

		// check if we have the span in the stack
		Stack<SpanImpl> stack = spanStack.get();
		if (stack.contains(span)) {
			// if so clear the stack until we reach it
			// it should be the first one, but just for safety
			// (users might forget to finish spans or could finish them in wrong order)
			SpanImpl removed = stack.pop();
			boolean wrongEndOrder = false;
			while (!stack.isEmpty() && !removed.equals(span)) {
				wrongEndOrder = true;
				stack.pop();
			}

			if (wrongEndOrder && LOGGER.isWarnEnabled()) {
				LOGGER.warn("Finishing of spans is not done in starting order, span " + span.toString() + " is not the last started one by current thread. Thread context state can be affected.");
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Span finished " + span);
			}
		}

		// check if we need to report the span
		if (span.isReport()) {
			reporter.report(span);
		}
	}

	/**
	 * Gets {@link #timer}.
	 *
	 * @return {@link #timer}
	 */
	Timer getTimer() {
		return this.timer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimer(Timer timer) {
		if (null == timer) {
			throw new IllegalArgumentException("Timer must not be null.");
		}
		this.timer = timer;
	}

}
