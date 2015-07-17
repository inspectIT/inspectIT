package info.novatec.inspectit.cmr.service.rest.error;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * Class for displaying exceptions in RESTful services with the {@link ModelAndView}.
 * 
 * @author Ivan Senic
 * 
 */
public class JsonError {

	/**
	 * Cause of the error.
	 */
	private final Exception exception;

	/**
	 * Constructor.
	 * 
	 * @param exception
	 *            Cause of the error.
	 */
	public JsonError(Exception exception) {
		this.exception = exception;
	}

	/**
	 * @return Returns {@link ModelAndView} created from {@link MappingJacksonJsonView} holding the
	 *         information about the exception.
	 */
	public ModelAndView asModelAndView() {
		MappingJacksonJsonView jsonView = new MappingJacksonJsonView();
		Map<String, String> map = new HashMap<>();
		map.put("error", exception.getMessage());
		map.put("exceptionType", exception.getClass().getName());
		return new ModelAndView(jsonView, map);
	}
}
