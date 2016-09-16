package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.builder.IPointBuilder;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("all")
public class InfluxProcessorTest extends TestBase {

	InfluxProcessor processor;

	@Mock
	InfluxDBDao influxDBDao;

	@Mock
	EntityManager entityManager;

	@Mock
	IPointBuilder<DefaultData> pointBuilder;

	Builder builder = Point.measurement("test").addField("test", 1).time(1, TimeUnit.MILLISECONDS);

	public class Process extends InfluxProcessorTest {

		@Test
		public void processed() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			when(influxDBDao.isConnected()).thenReturn(true);
			doReturn(Collections.singleton(InvocationSequenceData.class)).when(pointBuilder).getDataClasses();
			when(pointBuilder.createBuilders(invocationData)).thenReturn(Collections.singleton(builder));
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> singletonList(pointBuilder));

			processor.process(invocationData, entityManager);

			verify(influxDBDao).isConnected();
			ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
			verify(influxDBDao).insert(pointCaptor.capture());
			assertThat(pointCaptor.getValue().lineProtocol(), is(builder.build().lineProtocol()));
			verifyZeroInteractions(entityManager);
		}

		@Test
		public void noBuilders() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			when(influxDBDao.isConnected()).thenReturn(true);
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> emptyList());

			processor.process(invocationData, entityManager);

			verify(influxDBDao).isConnected();
			verifyNoMoreInteractions(influxDBDao);
			verifyZeroInteractions(entityManager);
		}

		@Test
		public void influxOffline() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			when(influxDBDao.isConnected()).thenReturn(false);
			doReturn(Collections.singleton(InvocationSequenceData.class)).when(pointBuilder).getDataClasses();
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> singletonList(pointBuilder));

			processor.process(invocationData, entityManager);

			verify(influxDBDao).isConnected();
			verifyNoMoreInteractions(influxDBDao);
			verifyZeroInteractions(entityManager);
		}

		@Test
		public void builderForClassDoesNotExist() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			when(influxDBDao.isConnected()).thenReturn(true);
			doReturn(Collections.singleton(HttpTimerData.class)).when(pointBuilder).getDataClasses();
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> singletonList(pointBuilder));

			processor.process(invocationData, entityManager);

			verify(influxDBDao).isConnected();
			verify(pointBuilder).getDataClasses();
			verifyNoMoreInteractions(influxDBDao, pointBuilder);
			verifyZeroInteractions(entityManager);
		}

		@Test
		public void timerNotCharting() {
			TimerData data = new TimerData();
			data.setCharting(false);
			when(influxDBDao.isConnected()).thenReturn(true);
			doReturn(Collections.singleton(TimerData.class)).when(pointBuilder).getDataClasses();
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> singletonList(pointBuilder));

			processor.process(data, entityManager);

			verify(influxDBDao).isConnected();
			verify(pointBuilder).getDataClasses();
			verifyNoMoreInteractions(influxDBDao, pointBuilder);
			verifyZeroInteractions(entityManager);
		}

		@Test
		public void timerCharting() {
			TimerData data = new TimerData();
			data.setCharting(true);
			when(influxDBDao.isConnected()).thenReturn(true);
			doReturn(Collections.singleton(TimerData.class)).when(pointBuilder).getDataClasses();
			when(pointBuilder.createBuilders(data)).thenReturn(Collections.singleton(builder));
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> singletonList(pointBuilder));

			processor.process(data, entityManager);

			verify(influxDBDao).isConnected();
			ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
			verify(influxDBDao).insert(pointCaptor.capture());
			assertThat(pointCaptor.getValue().lineProtocol(), is(builder.build().lineProtocol()));
			verifyZeroInteractions(entityManager);
		}

		@Test
		public void jmxNotNumeric() {
			JmxSensorValueData data = new JmxSensorValueData();
			data.setValue("string value");
			when(influxDBDao.isConnected()).thenReturn(true);
			doReturn(Collections.singleton(JmxSensorValueData.class)).when(pointBuilder).getDataClasses();
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> singletonList(pointBuilder));

			processor.process(data, entityManager);

			verify(influxDBDao).isConnected();
			verify(pointBuilder).getDataClasses();
			verifyNoMoreInteractions(influxDBDao, pointBuilder);
			verifyZeroInteractions(entityManager);
		}

		@Test
		public void jmxNumeric() {
			JmxSensorValueData data = new JmxSensorValueData();
			data.setValue("1");
			when(influxDBDao.isConnected()).thenReturn(true);
			doReturn(Collections.singleton(JmxSensorValueData.class)).when(pointBuilder).getDataClasses();
			when(pointBuilder.createBuilders(data)).thenReturn(Collections.singleton(builder));
			processor = new InfluxProcessor(influxDBDao, Collections.<IPointBuilder<DefaultData>> singletonList(pointBuilder));

			processor.process(data, entityManager);

			verify(influxDBDao).isConnected();
			ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
			verify(influxDBDao).insert(pointCaptor.capture());
			assertThat(pointCaptor.getValue().lineProtocol(), is(builder.build().lineProtocol()));
			verifyZeroInteractions(entityManager);
		}

	}
}
