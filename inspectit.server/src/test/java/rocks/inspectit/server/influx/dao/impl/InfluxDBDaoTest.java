package rocks.inspectit.server.influx.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test the {@link InfluxDBDao}.
 *
 * @author Marius Oehler
 *
 */
public class InfluxDBDaoTest extends TestBase {

	@InjectMocks
	InfluxDBDao influxDao;

	@Mock
	Logger log;

	/**
	 * Test the {@link InfluxDBDao#insert(org.influxdb.dto.Point)} method.
	 */
	public static class Insert extends InfluxDBDaoTest {

		@Mock
		Point dataPoint;

		@Test
		public void insertDataPoint() {
			InfluxDB influxDb = mock(InfluxDB.class);
			influxDao.influxDB = influxDb;
			influxDao.isConnected = true;

			influxDao.insert(dataPoint);

			verify(influxDb, times(1)).write(influxDao.database, influxDao.retentionPolicy, dataPoint);
		}

		@Test
		public void insertDataPointNotConnected() {
			influxDao.isConnected = false;

			influxDao.insert(dataPoint);
		}

		@Test
		public void insertNull() {
			influxDao.isConnected = true;

			influxDao.insert(null);
		}

		@Test
		public void insertNullNotConnected() {
			influxDao.isConnected = false;

			influxDao.insert(null);
		}
	}

	/**
	 * Test the {@link InfluxDBDao#query(String)} method.
	 */
	public static class QueryTest extends InfluxDBDaoTest {

		private static final String QUERY_STRING = "query";

		@Test
		public void query() {
			InfluxDB influxDb = mock(InfluxDB.class);
			when(influxDb.query(any(Query.class))).thenReturn(mock(QueryResult.class));
			influxDao.influxDB = influxDb;
			influxDao.isConnected = true;
			ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

			QueryResult result = influxDao.query(QUERY_STRING);

			verify(influxDb).query(queryCaptor.capture());
			assertThat(queryCaptor.getValue().getCommand(), equalTo(QUERY_STRING));
			assertThat(result, notNullValue());
		}

		@Test
		public void queryNoInflux() {
			influxDao.isConnected = true;

			QueryResult result = influxDao.query(QUERY_STRING);

			assertThat(result, nullValue());
		}

		@Test
		public void queryNotConnected() {
			influxDao.isConnected = false;

			QueryResult result = influxDao.query(QUERY_STRING);

			assertThat(result, nullValue());
		}

		@Test
		public void queryNull() {
			influxDao.isConnected = true;

			QueryResult result = influxDao.query(null);

			assertThat(result, nullValue());
		}

		@Test
		public void queryNullNotConnected() {
			influxDao.isConnected = false;

			QueryResult result = influxDao.query(null);

			assertThat(result, nullValue());
		}
	}

	/**
	 * Test the {@link InfluxDBDao#propertiesUpdated()} method.
	 *
	 */
	public static class PropertiesUpdated extends InfluxDBDaoTest {

		@Mock
		ScheduledExecutorService executor;

		@Test
		@SuppressWarnings("rawtypes")
		public void update() {
			ScheduledFuture availabilityFuture = mock(ScheduledFuture.class);
			ScheduledFuture connectingFuture = mock(ScheduledFuture.class);
			when(availabilityFuture.isDone()).thenReturn(false);
			when(connectingFuture.isDone()).thenReturn(false);
			influxDao.availabilityCheckTask = availabilityFuture;
			influxDao.connectingFuture = connectingFuture;

			influxDao.propertiesUpdated();

			verify(executor).submit(influxDao.connectingTask);
			verify(availabilityFuture, times(1)).cancel(any(boolean.class));
			verify(connectingFuture, times(1)).cancel(any(boolean.class));
			assertThat(influxDao.isConnected, equalTo(false));
		}
	}
}
