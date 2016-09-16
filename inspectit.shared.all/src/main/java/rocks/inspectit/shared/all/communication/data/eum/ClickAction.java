/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Represents a user action by the user. This is doesn't need to be a click.
 *
 * @author David Monschein
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickAction extends UserAction implements IJavaScriptEvListenerAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 136838753285568797L;

	/**
	 * Method name which was triggered by the user action.
	 */
	private String methodName;

	/**
	 * Timestamp when the method which was triggered gets started.
	 */
	private long beginTime;
	/**
	 * Timestamp when the executed method was finished.
	 */
	private long endTime;
	/**
	 * HTML tag name of the element which triggered the user action.
	 */
	private String tagName;
	/**
	 * HTML id attribute value of the element which triggered the user action.
	 */
	private String elementId;
	/**
	 * HTML name attribute value of the element which triggered the user action.
	 */
	private String elementName;

	/**
	 * Contains all subrequests belonging to this user action.
	 */
	private List<Request> requests;

	/**
	 * Creates a user action with no child requests.
	 */
	public ClickAction() {
		this.requests = new ArrayList<Request>();
	}

	/**
	 * Gets {@link #executionTime}.
	 *
	 * @return {@link #executionTime}
	 */
	@Override
	public long getExecutionTime() {
		return endTime - beginTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getJSMethodName() {
		return methodName;
	}

	/**
	 * Gets {@link #methodName}.
	 *
	 * @return {@link #methodName}
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Sets {@link #methodName}.
	 *
	 * @param methodName
	 *            New value for {@link #methodName}
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Gets {@link #beginTime}.
	 *
	 * @return {@link #beginTime}
	 */
	public long getBeginTime() {
		return beginTime;
	}

	/**
	 * Sets {@link #beginTime}.
	 *
	 * @param beginTime
	 *            New value for {@link #beginTime}
	 */
	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * Gets {@link #endTime}.
	 *
	 * @return {@link #endTime}
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Sets {@link #endTime}.
	 *
	 * @param endTime
	 *            New value for {@link #endTime}
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets {@link #tagName}.
	 *
	 * @return {@link #tagName}
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Sets {@link #tagName}.
	 *
	 * @param tagName
	 *            New value for {@link #tagName}
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * Gets {@link #elementId}.
	 *
	 * @return {@link #elementId}
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * Sets {@link #elementId}.
	 *
	 * @param elementId
	 *            New value for {@link #elementId}
	 */
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	/**
	 * Gets {@link #elementName}.
	 *
	 * @return {@link #elementName}
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * Sets {@link #elementName}.
	 *
	 * @param elementName
	 *            New value for {@link #elementName}
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * Gets {@link #requests}.
	 *
	 * @return {@link #requests}
	 */
	public List<Request> getRequests() {
		return requests;
	}

	/**
	 * Sets {@link #requests}.
	 *
	 * @param r
	 *            Value to add to the child requests
	 */
	public void addRequest(Request r) {
		this.requests.add(r);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Request> getChildRequests() {
		return this.requests;
	}

}
