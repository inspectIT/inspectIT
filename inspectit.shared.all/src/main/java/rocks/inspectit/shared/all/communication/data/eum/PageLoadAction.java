package rocks.inspectit.shared.all.communication.data.eum;

/**
 * Represents the user action of loading the page. This is guaranteed to be always the first action
 * within a tab.
 *
 * @author David Monschein, Jonas Kunz
 *
 */
public class PageLoadAction extends AbstractEUMTraceElement {

	/**
	 * the serial version UID.
	 */
	private static final long serialVersionUID = 9073441470224398605L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAsyncCall() {
		return false;
	}

}
