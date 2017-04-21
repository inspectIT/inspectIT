package rocks.inspectit.shared.all.communication.data.eum;

/**
 * Interface for getting information about an executed Javascript method.
 *
 * @author David Monschein
 */
public interface IJavaScriptEventListenerAction {

	/**
	 * Gets the execution time of the Javascript method.
	 *
	 * @return execution time of the JS method in milliseconds
	 */
	double getExecutionTime();

	/**
	 * Gets the Javascript method name.
	 *
	 * @return JS method name
	 */
	String getJSMethodName();

}
