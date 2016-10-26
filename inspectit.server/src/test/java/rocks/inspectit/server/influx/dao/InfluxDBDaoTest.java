package rocks.inspectit.server.influx.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.InfluxAvailabilityChecker;
import rocks.inspectit.server.influx.util.InfluxClientFactory;
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
	InfluxClientFactory clientFactory;

	@Mock
	Logger log;

	@Mock
	ScheduledExecutorService executor;

	@Mock
	InfluxAvailabilityChecker availabilityChecker;

	/**
	 * Test the {@link InfluxDBDao#insert(org.influxdb.dto.Point)} method.
	 */
	public static class Insert extends InfluxDBDaoTest {

		@Mock
		Point dataPoint;

		Future<?> future;

		@BeforeMethod
		public void beforeMethod() {
			future = Mockito.mock(Future.class);
			when(executor.submit(any(Runnable.class))).thenAnswer(new Answer<Future<?>>() {
				@Override
				public Future<?> answer(InvocationOnMock invocation) throws Throwable {
					Runnable runnable = (Runnable) invocation.getArguments()[0];
					runnable.run();
					return future;
				}
			});
		}

		@Test
		public void insertPoint() {
			influxDao.active = true;
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(clientFactory.createClient()).thenReturn(influxDb);
			influxDao.propertiesUpdated();

			influxDao.insert(dataPoint);

			assertThat(influxDao.isConnected(), is(true));
			Mockito.verifyZeroInteractions(future);
			Mockito.verifyZeroInteractions(dataPoint);
			Mockito.verify(executor).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(influxDb).write(influxDao.database, influxDao.retentionPolicy, dataPoint);
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
		}

		@Test
		public void insertNull() {
			influxDao.active = true;
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(clientFactory.createClient()).thenReturn(influxDb);
			influxDao.propertiesUpdated();

			influxDao.insert(null);

			assertThat(influxDao.isConnected(), is(true));
			Mockito.verifyZeroInteractions(future);
			Mockito.verifyZeroInteractions(dataPoint);
			Mockito.verify(executor).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
		}

		@Test
		public void notConnected() {
			influxDao.insert(dataPoint);

			assertThat(influxDao.isConnected(), is(false));
			Mockito.verifyZeroInteractions(future);
			Mockito.verifyZeroInteractions(dataPoint);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(availabilityChecker);
			Mockito.verifyZeroInteractions(clientFactory);
		}
	}

	/**
	 * Test the {@link InfluxDBDao#query(String)} method.
	 */
	public static class QueryTest extends InfluxDBDaoTest {

		Future<?> future;

		@BeforeMethod
		public void beforeMethod() {
			future = Mockito.mock(Future.class);
			when(executor.submit(any(Runnable.class))).thenAnswer(new Answer<Future<?>>() {
				@Override
				public Future<?> answer(InvocationOnMock invocation) throws Throwable {
					Runnable runnable = (Runnable) invocation.getArguments()[0];
					runnable.run();
					return future;
				}
			});
		}

		@Test
		public void executeQuery() {
			influxDao.active = true;
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(clientFactory.createClient()).thenReturn(influxDb);
			influxDao.propertiesUpdated();

			influxDao.query("myQuery");

			assertThat(influxDao.isConnected(), is(true));
			ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
			Mockito.verify(influxDb).query(queryCaptor.capture());
			assertThat(queryCaptor.getValue().getCommand(), equalTo("myQuery"));
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verify(executor).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		public void executeNullQuery() {
			influxDao.active = true;
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(clientFactory.createClient()).thenReturn(influxDb);
			influxDao.propertiesUpdated();

			influxDao.query(null);

			assertThat(influxDao.isConnected(), is(true));
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verify(executor).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		public void notConnected() {
			influxDao.query("myQuery");

			assertThat(influxDao.isConnected(), is(false));
			Mockito.verifyZeroInteractions(future);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(availabilityChecker);
			Mockito.verifyZeroInteractions(clientFactory);
		}
	}

	/**
	 * Test the {@link InfluxDBDao#propertiesUpdated()} method.
	 *
	 */
	public static class PropertiesUpdated extends InfluxDBDaoTest {

		Future<?> future;

		@BeforeMethod
		public void beforeMethod() {
			future = Mockito.mock(Future.class);
			when(executor.submit(any(Runnable.class))).thenAnswer(new Answer<Future<?>>() {
				@Override
				public Future<?> answer(InvocationOnMock invocation) throws Throwable {
					Runnable runnable = (Runnable) invocation.getArguments()[0];
					runnable.run();
					return future;
				}
			});
		}

		@Test
		public void notActive() {
			influxDao.propertiesUpdated();

			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verifyZeroInteractions(clientFactory);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		public void enableInflux() {
			influxDao.active = true;
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(clientFactory.createClient()).thenReturn(influxDb);

			influxDao.propertiesUpdated();

			assertThat(influxDao.isConnected(), is(true));
			Mockito.verify(executor, times(1)).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void enableInfluxButNotAvailable() {
			influxDao.active = true;
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(influxDb.ping()).thenThrow(Exception.class);
			when(clientFactory.createClient()).thenReturn(influxDb);

			influxDao.propertiesUpdated();

			assertThat(influxDao.isConnected(), is(false));
			Mockito.verify(executor, times(1)).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		public void enableInfluxDatabaseExists() {
			influxDao.active = true;
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(influxDb.describeDatabases()).thenReturn(Arrays.asList(influxDao.database));
			when(clientFactory.createClient()).thenReturn(influxDb);

			influxDao.propertiesUpdated();

			assertThat(influxDao.isConnected(), is(true));
			Mockito.verify(executor, times(1)).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		public void disableInfluxConnectionOngoing() {
			when(future.isDone()).thenReturn(false);
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(influxDb.isBatchEnabled()).thenReturn(false, true);
			when(clientFactory.createClient()).thenReturn(influxDb);
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.active = false;
			influxDao.propertiesUpdated();

			assertThat(influxDao.isConnected(), is(false));
			Mockito.verify(future).cancel(true);
			Mockito.verify(future).isDone();
			Mockito.verifyNoMoreInteractions(future);
			Mockito.verify(executor, times(1)).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb, times(2)).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).disableBatch();
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(availabilityChecker, times(2)).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
		}

		@Test
		public void disableInflux() {
			when(future.isDone()).thenReturn(true);
			InfluxDB influxDb = Mockito.mock(InfluxDB.class);
			when(influxDb.isBatchEnabled()).thenReturn(false, true);
			when(clientFactory.createClient()).thenReturn(influxDb);
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.active = false;
			influxDao.propertiesUpdated();

			assertThat(influxDao.isConnected(), is(false));
			Mockito.verify(future).isDone();
			Mockito.verifyNoMoreInteractions(future);
			Mockito.verify(executor, times(1)).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(influxDb).ping();
			Mockito.verify(influxDb, times(2)).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).disableBatch();
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verify(availabilityChecker, times(2)).deactivate();
			Mockito.verify(availabilityChecker).setAvailabilityListener(influxDao);
			Mockito.verify(availabilityChecker).setInflux(influxDb);
			Mockito.verify(availabilityChecker).activate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
		}

		@Test
		public void clientFactoryReturnsNull() {
			influxDao.active = true;
			when(clientFactory.createClient()).thenReturn(null);

			influxDao.propertiesUpdated();

			assertThat(influxDao.isConnected(), is(false));
			Mockito.verify(executor, times(1)).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
			Mockito.verifyZeroInteractions(future);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void clientFactoryThrowsException() {
			influxDao.active = true;
			when(clientFactory.createClient()).thenThrow(Exception.class);

			influxDao.propertiesUpdated();

			assertThat(influxDao.isConnected(), is(false));
			Mockito.verify(executor, times(1)).submit(any(Runnable.class));
			Mockito.verifyNoMoreInteractions(executor);
			Mockito.verify(availabilityChecker).deactivate();
			Mockito.verifyNoMoreInteractions(availabilityChecker);
			Mockito.verify(clientFactory).createClient();
			Mockito.verifyNoMoreInteractions(clientFactory);
			Mockito.verifyZeroInteractions(future);
		}
	}

	/**
	 * Tests the {@link InfluxDBDao#onDisconnection()} method.
	 */
	public static class OnDisconnection extends InfluxDBDaoTest {

		@Mock
		InfluxDB influxDb;

		@Test
		public void disconnected() {
			when(influxDb.isBatchEnabled()).thenReturn(true);

			influxDao.onDisconnection();

			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).disableBatch();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(clientFactory);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(availabilityChecker);
		}

		@Test
		public void disconnectedBatchDisabled() {
			when(influxDb.isBatchEnabled()).thenReturn(false);

			influxDao.onDisconnection();

			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(clientFactory);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(availabilityChecker);
		}
	}

	/**
	 * Tests the {@link InfluxDBDao#onDisconnection()} method.
	 */
	public static class OnReconnection extends InfluxDBDaoTest {

		@Mock
		InfluxDB influxDb;

		@Test
		public void reconnected() {
			when(influxDb.isBatchEnabled()).thenReturn(false);

			influxDao.onReconnection();

			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(clientFactory);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(availabilityChecker);
		}

		@Test
		public void reconnectedBatchEnabled() {
			when(influxDb.isBatchEnabled()).thenReturn(true);

			influxDao.onReconnection();

			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verify(influxDb).createDatabase(influxDao.database);
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(clientFactory);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(availabilityChecker);
		}

		@Test
		public void reconnectedDatabaseExists() {
			when(influxDb.isBatchEnabled()).thenReturn(false);
			when(influxDb.describeDatabases()).thenReturn(Arrays.asList(influxDao.database));

			influxDao.onReconnection();

			Mockito.verify(influxDb).isBatchEnabled();
			Mockito.verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			Mockito.verify(influxDb).describeDatabases();
			Mockito.verifyNoMoreInteractions(influxDb);
			Mockito.verifyZeroInteractions(clientFactory);
			Mockito.verifyZeroInteractions(executor);
			Mockito.verifyZeroInteractions(availabilityChecker);
		}
	}
}
