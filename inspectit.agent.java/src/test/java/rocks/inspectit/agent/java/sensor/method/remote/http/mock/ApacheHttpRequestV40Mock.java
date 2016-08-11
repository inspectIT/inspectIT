package rocks.inspectit.agent.java.sensor.method.remote.http.mock;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock class for {@link org.apache.http.ApacheHttpRequest} v4.0 and above.
 *
 * @author Thomas Kluge
 *
 */
public class ApacheHttpRequestV40Mock {

	private final Map<String, String> headers = new HashMap<String, String>();

	public ApacheRequestLineMock getRequestLine() {
		return null;
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public String getHeaders(String key) {
		return null;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}
}
