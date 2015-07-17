package info.novatec.inspectit.rcp.log;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ILogListener} to correctly hook into the internal logging mechanism.
 * 
 * @author Ivan Senic
 * 
 */
public class LogListener implements ILogListener {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LogListener.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logging(IStatus status, String plugin) {
		if (status.getSeverity() == IStatus.INFO) {
			LOG.info(status.getMessage());
		} else if (status.getSeverity() == IStatus.WARNING) {
			if (null == status.getException()) {
				LOG.warn(status.getMessage());
			} else {
				LOG.warn(status.getMessage(), status.getException());
			}
		} else if (status.getSeverity() == IStatus.ERROR) {
			if (null == status.getException()) {
				LOG.error(status.getMessage());
			} else {
				LOG.error(status.getMessage(), status.getException());
			}
		}
	}

}
