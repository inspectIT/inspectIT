package rocks.inspectit.server.influx.builder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point.Builder;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
public class AbstractPointBuilderTest extends TestBase {

	protected String getMeasurement(Builder builder) throws Exception {
		return getFieldValue(builder, "measurement");
	}

	protected Map<String, String> getTags(Builder builder) throws Exception {
		return getFieldValue(builder, "tags");
	}

	protected Map<String, Object> getFields(Builder builder) throws Exception {
		return getFieldValue(builder, "fields");
	}

	protected Long getTime(Builder builder) throws Exception {
		return getFieldValue(builder, "time");
	}

	protected TimeUnit getPrecision(Builder builder) throws Exception {
		return getFieldValue(builder, "precision");
	}

	@SuppressWarnings("unchecked")
	private <R> R getFieldValue(Builder builder, String name) throws Exception {
		Field field = Builder.class.getDeclaredField(name);
		field.setAccessible(true);
		return (R) field.get(builder);
	}


}
