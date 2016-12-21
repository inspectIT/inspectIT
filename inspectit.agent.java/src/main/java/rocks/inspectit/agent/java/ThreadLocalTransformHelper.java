package rocks.inspectit.agent.java;

import org.springframework.stereotype.Component;

/**
 * Basic implementation of the {@link IThreadTransformHelper} that uses thread local.
 *
 * @author Ivan Senic
 *
 */
@Component
public class ThreadLocalTransformHelper implements IThreadTransformHelper {

	/**
	 * Thread local to control the instrumentation transform disabled states for threads.
	 */
	private ThreadLocal<Boolean> transformDisabledThreadLocal = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		};
	};


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isThreadTransformDisabled() {
		return transformDisabledThreadLocal.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setThreadTransformDisabled(boolean disabled) {
		transformDisabledThreadLocal.set(Boolean.valueOf(disabled));
	}

}
