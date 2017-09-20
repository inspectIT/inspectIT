package rocks.inspectit.server.processor.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.builder.ProblemOccurrencePointBuilder;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;

/**
 * @author Isabel Vico Peinado
 *
 */
public class DiagnosisCmrProcessorConfigurationTest extends TestBase {

	@InjectMocks
	DiagnosisCmrProcessorConfiguration cmrProcessorConfiguration;

	public static class Accept extends DiagnosisCmrProcessorConfigurationTest {
		@Mock
		ProblemOccurrencePointBuilder problemOccurrencePointBuilder;

		@Mock
		ProblemOccurrence problemOccurrence;

		@Mock
		InfluxDBDao influxDBDao;

		@Test
		public void mustToCallToInsertDBDao() {
			cmrProcessorConfiguration.influxDBDao = influxDBDao;
			Builder builder = Point.measurement("test").addField("test", 1).time(1, TimeUnit.MILLISECONDS);
			when(cmrProcessorConfiguration.problemOccurrencePointBuilder.getBuilder(problemOccurrence)).thenReturn(builder);

			cmrProcessorConfiguration.accept(problemOccurrence);

			verify(cmrProcessorConfiguration.influxDBDao).insert(any(Point.class));
		}
	}
}
