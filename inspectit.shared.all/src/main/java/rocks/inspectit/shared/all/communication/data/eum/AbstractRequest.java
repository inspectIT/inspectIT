package rocks.inspectit.shared.all.communication.data.eum;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.opentracing.tag.Tags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Represents information captured by the JS Agent about issued requests.
 *
 * @author David Monschein, Jonas Kunz
 *
 */
public abstract class AbstractRequest extends AbstractEUMSpanDetails {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 1685602159386035632L;

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AbstractRequest.class);

	/**
	 * The url of the request.
	 */
	@JsonProperty
	private String url;

	/**
	 * Gets {@link #url}.
	 *
	 * @return {@link #url}
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void collectTags(Map<String, String> tags) {
		tags.put(Tags.HTTP_URL.getKey(), url);
	}

	/**
	 * Makes the URL of this request absolute.
	 *
	 * @param baseURL
	 *            the context in which this request occurred
	 */
	protected void resolveRelativeUrl(String baseURL) {
		try {
			URL baseUrl = new URL(baseURL);
			url = new URL(baseUrl, getUrl()).toString();
		} catch (MalformedURLException e) {
			LOG.error("Could not resolve relative URL", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return PropagationType.HTTP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExternalCall() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.url == null) ? 0 : this.url.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractRequest other = (AbstractRequest) obj;
		if (this.url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!this.url.equals(other.url)) {
			return false;
		}
		return true;
	}
}
