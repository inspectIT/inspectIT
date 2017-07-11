package rocks.inspectit.server.diagnosis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.mockito.ArgumentCaptor;
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
public class DiagnosisCmrConfigurationTest extends TestBase {

	@InjectMocks
	DiagnosisCmrConfiguration diagnosisCmrConfiguration;

	public static class Accept extends DiagnosisCmrConfigurationTest {
		@Mock
		ProblemOccurrencePointBuilder problemOccurrencePointBuilder;

		@Mock
		ProblemOccurrence problemOccurrence;

		@Mock
		InfluxDBDao influxDBDao;

		@Test
		public void mustToCallToInsertDBDao() {
			Builder builder = Point.measurement("test").addField("test", 1).time(1, TimeUnit.MILLISECONDS);
			when(diagnosisCmrConfiguration.problemOccurrencePointBuilder.getBuilder(problemOccurrence)).thenReturn(builder);

			diagnosisCmrConfiguration.accept(problemOccurrence);

			ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
			verify(diagnosisCmrConfiguration.influxDBDao).insert(pointCaptor.capture());
			assertThat(pointCaptor.getValue().lineProtocol(), is(builder.build().lineProtocol()));
		}
	}
}
