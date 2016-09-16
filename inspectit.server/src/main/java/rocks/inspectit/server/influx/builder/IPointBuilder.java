package rocks.inspectit.server.influx.builder;

import java.util.Collection;

import org.influxdb.dto.Point.Builder;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 *
 * Basic interface for all point builders.
 *
 * @author Jonas Kunz
 * @param <E>
 *            the base type supported by this builder
 *
 */
public interface IPointBuilder<E extends DefaultData> {

	/**
	 * @return the exact classes of the data objects consumed by this point builder.
	 */
	Collection<? extends Class<? extends E>> getDataClasses();

	/**
	 * Creates a set of influx {@link Builder} for the given data type.
	 *
	 * @param dataObject
	 *            the data object to add to the DB.
	 * @return a (possibly empty) collection of Point Builders to add to the influx DB.
	 */
	Collection<Builder> createBuilders(E dataObject);

}
