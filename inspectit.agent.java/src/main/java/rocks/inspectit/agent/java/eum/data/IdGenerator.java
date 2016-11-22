package rocks.inspectit.agent.java.eum.data;

import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.sdk.opentracing.internal.util.RandomUtils;

/**
 * Component responsible for generating unique Browser EUM IDs.
 *
 * @author Jonas Kunz
 *
 */
@Component
public class IdGenerator {

	/**
	 * @return a new unique session ID.
	 */
	public long generateSessionID() {
		return RandomUtils.randomLong();
	}

	/**
	 * @return a new unique tab ID.
	 */
	public long generateTabID() {
		return RandomUtils.randomLong();
	}

}
