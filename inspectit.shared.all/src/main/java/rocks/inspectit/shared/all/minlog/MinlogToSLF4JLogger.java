package rocks.inspectit.shared.all.minlog;

import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.minlog.Log.Logger;

/**
 * A bridge to log the minlog message to the slf4j.
 *
 * @author Ivan Senic
 *
 */
public class MinlogToSLF4JLogger extends Logger {

	/**
	 * Log everything under <i>com.esotericsoftware.minlog</i>. We have no better solution.
	 */
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MinlogToSLF4JLogger.class);

	/**
	 * Initializes the bridge. Must be called for correct setup.
	 */
	public static void init() {
		// set to info max, this means that we will never be able to log trace & debug message from
		// Minlog; let slf4j decide if we are printing from INFO on
		Log.set(Log.LEVEL_INFO);
		Log.setLogger(new MinlogToSLF4JLogger());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Forward to the slf4j.
	 */
	@Override
	public void log(int level, String category, String message, Throwable ex) {
		// we implement log for all levels, although we know that INFO is maximum we can get
		switch (level) {
		case Log.LEVEL_TRACE:
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(getMessage(category, message), ex);
			}
			break;
		case Log.LEVEL_DEBUG:
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getMessage(category, message), ex);
			}
			break;
		case Log.LEVEL_INFO:
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(getMessage(category, message), ex);
			}
			break;
		case Log.LEVEL_WARN:
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(getMessage(category, message), ex);
			}
			break;
		case Log.LEVEL_ERROR:
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(getMessage(category, message), ex);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Constructs message for the given category/message.
	 *
	 * @param category
	 *            Category description.
	 * @param message
	 *            Message
	 * @return combinedMessage
	 */
	private String getMessage(String category, String message) {
		int size = (category != null) ? category.length() + 2 : (0 + message) != null ? message.length() : 0;
		StringBuilder stringBuilder = new StringBuilder(size);
		if (null != category) {
			stringBuilder.append(category);
			stringBuilder.append(": ");
		}
		stringBuilder.append(message);
		return stringBuilder.toString();
	}

}
