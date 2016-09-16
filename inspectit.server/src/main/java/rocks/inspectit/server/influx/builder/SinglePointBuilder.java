package rocks.inspectit.server.influx.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Base class for all influx point builders that we provide which will always return exactly one
 * Point per data object. Builders can be created by passing the data to the
 * {@link #createBuilder(DefaultData)} method.
 *
 * @param <E>
 *            type of data builder can work with
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
public abstract class SinglePointBuilder<E extends DefaultData> implements IPointBuilder<E> {

	/**
	 * {@link ICachedDataService} for resolving all needed names.
	 */
	@Autowired
	protected ICachedDataService cachedDataService;

	/**
	 * Returns series name for this builder.
	 *
	 * @return Returns series name for this builder.
	 */
	protected abstract String getSeriesName();

	/**
	 * Adds needed fields of the data to the builder. When overriding sub-classes should call super
	 * first.
	 *
	 * @param data
	 *            Data carrier
	 * @param builder
	 *            Builder to add fields to.
	 */
	protected abstract void addFields(E data, Builder builder);

	/**
	 * Adds needed tags related to the data to the builder. When overriding sub-classes should call
	 * super first.
	 *
	 * @param data
	 *            Data carrier
	 * @param builder
	 *            Builder to add tags to.
	 * @see Builder#tag(String, String)
	 */
	protected void addTags(E data, Builder builder) {
		PlatformIdent platformIdent = cachedDataService.getPlatformIdentForId(data.getPlatformIdent());

		builder.tag(Series.TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		if (null != platformIdent) {
			builder.tag(Series.TAG_AGENT_NAME, platformIdent.getAgentName());
		}
	}

	/**
	 * Creates the influx {@link Builder} for the given data type.
	 *
	 * @param data
	 *            Data carrier
	 * @return Builder that can be used to create influx Points.
	 */
	@Override
	public Collection<Builder> createBuilders(E data) {
		Builder builder = Point.measurement(getSeriesName());
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		this.addTags(data, builder);
		this.addFields(data, builder);

		return Collections.singletonList(builder);
	}

}
