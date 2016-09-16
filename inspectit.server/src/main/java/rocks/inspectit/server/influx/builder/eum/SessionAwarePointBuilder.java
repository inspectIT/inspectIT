package rocks.inspectit.server.influx.builder.eum;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.influx.builder.IPointBuilder;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMData;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;

/**
 * A buidler collecting EUM data requiring session information.
 *
 * @author Jonas Kunz
 *
 * @param E
 *            the AbstractEUMData type this builder is responsible for.
 */
public abstract class SessionAwarePointBuilder<E extends AbstractEUMData> implements IPointBuilder<E> {

	/**
	 * used if no session-ID was attached to the received data object.
	 */
	private static final UserSessionInfo UNKOWN_SESSION_INFO = new UserSessionInfo("unknown", "unknown", "unknown", "unknown");

	/**
	 * performs the caching of session information and of data whose session info has not arrived
	 * yet.
	 */
	@Autowired
	private SessionPointBuilder sessionBuilder;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Builder> createBuilders(E dataObject) {
		String sessionID = dataObject.getSessionId();
		UserSessionInfo session;
		if (sessionID == null) {
			session = UNKOWN_SESSION_INFO;
		} else {
			session = sessionBuilder.getSessionInfo(dataObject.getSessionId());
		}
		if (session == null) {
			sessionBuilder.addPendingPoints(dataObject, this);
			return Collections.emptyList();
		} else {
			return createBuildersWithSession(session, dataObject);
		}
	}

	/**
	 * Builds the influx poitns with the session meta info available.
	 *
	 * @param session
	 *            the session belonging to the data object.
	 * @param dataObject
	 *            the data object to handle
	 * @return A collection of Point builders to pass to influx
	 */
	public abstract Collection<Builder> createBuildersWithSession(UserSessionInfo session, E dataObject);

}
