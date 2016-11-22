package rocks.inspectit.server.influx.builder.eum;

import java.util.Collection;

import org.influxdb.dto.Point.Builder;

import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMElement;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;

/**
 * Abstract base class for all specialized EUM Point Builders.
 *
 * @author Jonas Kunz
 *
 * @param <E>
 *            The type of data which this Point Builder accepts.
 */
public abstract class AbstractEUMPointBuilder<E extends AbstractEUMElement> {

	/**
	 * @return A set of all classes which this point builder consumes to generate influx series.
	 */
	public abstract Collection<Class<? extends AbstractEUMElement>> getSupportedTypes();

	/**
	 * @return true, if this point builder requires the {@link UserSessionInfo} in combination with
	 *         the accepted data type.
	 */
	public abstract boolean requiresSessionMetaInfo();

	/**
	 * @return true, if this point builder requires the {@link PageLoadRequest} of the tab in which
	 *         the data consumed by this point builder occured.
	 */
	public abstract boolean requiresPageLoadRequest();

	/**
	 * Generates Influx data points based on the element given. Note that the page load request and
	 * the session meta info are only available if {@link #requiresPageLoadRequest()} or
	 * {@link #requiresSessionMetaInfo()} return true respectively. Be aware that even if these
	 * methods return true, the corresponding parameters passed here can still be null, for example
	 * in case of a data loss due to the network.
	 *
	 * @param sessionInfo
	 *            the user session info corresponding to the session of the given data object, can
	 *            be null!
	 * @param plr
	 *            the page load request corresponding to the tab of the given data object, can be
	 *            null!
	 * @param data
	 *            the data element to build measurement poitns for
	 * @return the newly generated points.
	 */
	public abstract Collection<Builder> build(UserSessionInfo sessionInfo, PageLoadRequest plr, E data);

}
