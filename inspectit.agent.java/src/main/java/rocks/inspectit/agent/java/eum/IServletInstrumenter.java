package rocks.inspectit.agent.java.eum;

/**
 * Interface used for Servlet Instrumentation.
 *
 * @author Jonas Kunz
 */
public interface IServletInstrumenter {

	/**
	 * Lets InspecIT decide whether it will intercept the request. If the request is intercepted,
	 * InspectIT will write the response of this request to the client and the application server
	 * will not process this request any further.
	 *
	 * @param servletOrFilter
	 *            the servlet or filter from which the call to this method originates
	 * @param request
	 *            the request (an instance of javax.servlet.ServletRequest)
	 * @param response
	 *            the response object (an instance of javax.servlet.ServletResponse)
	 * @return true if the request was intercepted (the application server should not continue
	 *         processing), false otherwise
	 */
	boolean interceptRequest(Object servletOrFilter, Object request, Object response);

	/**
	 * Lets InspectIT optionally instrument the given response by wrapping it.
	 *
	 * @param servletOrFilter
	 *            the servlet or filter from which the call to this method originates
	 * @param request
	 *            the request (an instance of javax.servlet.ServletRequest)
	 * @param response
	 *            the response object (an instance of javax.servlet.ServletResponse)
	 * @return the new response object to use, or the original one if it was not instrumented.
	 */
	Object instrumentResponse(Object servletOrFilter, Object request, Object response);

	/**
	 * Called after the instrumented servlet or filter has finished processing the request.
	 *
	 * @param servletOrFilter
	 *            the servlet or filter from which the call to this method originates
	 */
	void servletOrFilterExit(Object servletOrFilter);

}
