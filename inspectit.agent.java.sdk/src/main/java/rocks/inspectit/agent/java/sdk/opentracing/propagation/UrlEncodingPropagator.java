package rocks.inspectit.agent.java.sdk.opentracing.propagation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.opentracing.propagation.TextMap;

/**
 * Propagator that tries to encode the values using {@link URLEncoder} and {@link URLDecoder}. Uses
 * {@value #UTF_8} encoding.
 *
 * @author Ivan Senic
 *
 */
public class UrlEncodingPropagator extends AbstractPropagator<TextMap> {

	/**
	 * Constant.
	 */
	private static final String UTF_8 = "UTF-8";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void injectBaggage(TextMap carrier, String key, String value) {
		try {
			carrier.put(key, URLEncoder.encode(value, UTF_8));
		} catch (UnsupportedEncodingException e) {
			carrier.put(key, value);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Iterable<Entry<String, String>> extractBaggage(TextMap carrier) {
		if ((null == carrier) || (null == carrier.iterator())) {
			return null;
		}

		Map<String, String> map = new HashMap<String, String>();
		for (Entry<String, String> entry : carrier) {
			try {
				map.put(entry.getKey(), URLDecoder.decode(entry.getValue(), UTF_8));
			} catch (UnsupportedEncodingException e) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map.entrySet();
	}

}
