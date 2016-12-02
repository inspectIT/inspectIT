package rocks.inspectit.server.influx.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.influx.InfluxAvailabilityChecker;
import rocks.inspectit.server.influx.util.InfluxClientFactory;
import rocks.inspectit.shared.all.externalservice.ExternalServiceStatus;
import rocks.inspectit.shared.all.externalservice.ExternalServiceType;
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

	InfluxDB influxDb;

	Future<?> future;

	@BeforeMethod
	public void setup() {
		influxDb = mock(InfluxDB.class);
		when(clientFactory.createClient()).thenReturn(influxDb);

		future = mock(Future.class);
		when(executor.submit(any(Runnable.class))).thenAnswer(new Answer<Future<?>>() {
			@Override
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				Runnable runnable = (Runnable) invocation.getArguments()[0];
				if (runnable != null) {
					runnable.run();
				}
				return future;
			}
		});
	}

	/**
	 * Test the {@link InfluxDBDao#insert(org.influxdb.dto.Point)} method.
	 */
	public static class Insert extends InfluxDBDaoTest {

		@Mock
		Point dataPoint;

		@Test
		public void insertPoint() {
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.insert(dataPoint);

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.CONNECTED));
			verify(executor).submit(any(Runnable.class));
			verify(influxDb).write(influxDao.database, influxDao.retentionPolicy, dataPoint);
			verify(influxDb).ping();
			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verify(availabilityChecker).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(executor, influxDb, availabilityChecker, clientFactory);
			verifyZeroInteractions(future, dataPoint);
		}

		@Test
		public void insertNull() {
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.insert(null);

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.CONNECTED));
			verify(executor).submit(any(Runnable.class));
			verify(influxDb).ping();
			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verify(availabilityChecker).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(executor, influxDb, availabilityChecker, clientFactory);
			verifyZeroInteractions(future, dataPoint);
		}

		@Test
		public void notConnected() {
			influxDao.active = true;

			influxDao.insert(dataPoint);

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISCONNECTED));
			verifyZeroInteractions(future, dataPoint, executor, availabilityChecker, clientFactory);
		}
	}

	/**
	 * Test the {@link InfluxDBDao#query(String)} method.
	 */
	public static class QueryTest extends InfluxDBDaoTest {

		@Test
		public void executeQuery() {
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.query("myQuery");

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.CONNECTED));
			ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
			verify(influxDb).query(queryCaptor.capture());
			assertThat(queryCaptor.getValue().getCommand(), equalTo("myQuery"));
			verify(influxDb).ping();
			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verify(executor).submit(any(Runnable.class));
			verify(availabilityChecker).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(clientFactory, availabilityChecker, executor, influxDb);
			verifyZeroInteractions(future);
		}

		@Test
		public void executeNullQuery() {
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.query(null);

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.CONNECTED));
			verify(influxDb).ping();
			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verify(executor).submit(any(Runnable.class));
			verify(availabilityChecker).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(clientFactory, availabilityChecker, executor, influxDb);
			verifyZeroInteractions(future);
		}

		@Test
		public void notConnected() {
			influxDao.active = true;

			influxDao.query("myQuery");

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISCONNECTED));
			verifyZeroInteractions(future, executor, availabilityChecker, clientFactory);
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
			future = mock(Future.class);
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

			verify(availabilityChecker).deactivate();
			verifyNoMoreInteractions(availabilityChecker);
			verifyZeroInteractions(future, executor, clientFactory);
		}

		@Test
		public void enableInflux() {
			influxDao.active = true;

			influxDao.propertiesUpdated();

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.CONNECTED));
			assertThat(influxDao.getServiceType(), is(ExternalServiceType.INFLUXDB));
			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.CONNECTED));
			verify(executor, times(1)).submit(any(Runnable.class));
			verify(influxDb).ping();
			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verify(availabilityChecker).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(influxDb, availabilityChecker, executor, clientFactory);
			verifyZeroInteractions(future);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void enableInfluxButNotAvailable() {
			influxDao.active = true;
			when(influxDb.ping()).thenThrow(Exception.class);

			influxDao.propertiesUpdated();

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISCONNECTED));
			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISCONNECTED));
			verify(executor, times(1)).submit(any(Runnable.class));
			verify(influxDb).ping();
			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(availabilityChecker).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(influxDb, availabilityChecker, executor, clientFactory);
			verifyZeroInteractions(future);
		}

		@Test
		public void enableInfluxDatabaseExists() {
			influxDao.active = true;
			when(influxDb.describeDatabases()).thenReturn(Arrays.asList(influxDao.database));

			influxDao.propertiesUpdated();

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.CONNECTED));
			verify(executor, times(1)).submit(any(Runnable.class));
			verify(influxDb).ping();
			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verify(availabilityChecker).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(influxDb, availabilityChecker, executor, clientFactory);
			verifyZeroInteractions(future);
		}

		@Test
		public void disableInfluxConnectionOngoing() {
			when(future.isDone()).thenReturn(false);
			when(influxDb.isBatchEnabled()).thenReturn(false, true);
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.active = false;
			influxDao.propertiesUpdated();

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISABLED));
			verify(future).cancel(true);
			verify(future).isDone();
			verify(executor, times(1)).submit(any(Runnable.class));
			verify(influxDb).ping();
			verify(influxDb, times(2)).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).disableBatch();
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verify(availabilityChecker, times(2)).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(future, influxDb, availabilityChecker, executor, clientFactory);
		}

		@Test
		public void disableInflux() {
			when(future.isDone()).thenReturn(true);
			when(influxDb.isBatchEnabled()).thenReturn(false, true);
			influxDao.active = true;
			influxDao.propertiesUpdated();

			influxDao.active = false;
			influxDao.propertiesUpdated();

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISABLED));
			verify(future).isDone();
			verify(executor, times(1)).submit(any(Runnable.class));
			verify(influxDb).ping();
			verify(influxDb, times(2)).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).disableBatch();
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verify(availabilityChecker, times(2)).deactivate();
			verify(availabilityChecker).setInflux(influxDb);
			verify(availabilityChecker).activate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(future, influxDb, availabilityChecker, executor, clientFactory);
		}

		@Test
		public void clientFactoryReturnsNull() {
			influxDao.active = true;
			when(clientFactory.createClient()).thenReturn(null);

			influxDao.propertiesUpdated();

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISCONNECTED));
			verify(executor, times(1)).submit(any(Runnable.class));
			verify(availabilityChecker).deactivate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(influxDb, availabilityChecker, executor, clientFactory);
			verifyZeroInteractions(future);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void clientFactoryThrowsException() {
			influxDao.active = true;
			when(clientFactory.createClient()).thenThrow(Exception.class);

			influxDao.propertiesUpdated();

			assertThat(influxDao.getServiceStatus(), is(ExternalServiceStatus.DISCONNECTED));
			verify(executor, times(1)).submit(any(Runnable.class));
			verify(availabilityChecker).deactivate();
			verify(clientFactory).createClient();
			verifyNoMoreInteractions(influxDb, availabilityChecker, executor, clientFactory);
			verifyZeroInteractions(future);
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

			verify(influxDb).isBatchEnabled();
			verify(influxDb).disableBatch();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(future, executor, availabilityChecker, clientFactory);
		}

		@Test
		public void disconnectedBatchDisabled() {
			when(influxDb.isBatchEnabled()).thenReturn(false);

			influxDao.onDisconnection();

			verify(influxDb).isBatchEnabled();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(future, executor, availabilityChecker, clientFactory);
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

			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(future, executor, availabilityChecker, clientFactory);
		}

		@Test
		public void reconnectedBatchEnabled() {
			when(influxDb.isBatchEnabled()).thenReturn(true);

			influxDao.onReconnection();

			verify(influxDb).isBatchEnabled();
			verify(influxDb).describeDatabases();
			verify(influxDb).createDatabase(influxDao.database);
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(future, executor, availabilityChecker, clientFactory);
		}

		@Test
		public void reconnectedDatabaseExists() {
			when(influxDb.isBatchEnabled()).thenReturn(false);
			when(influxDb.describeDatabases()).thenReturn(Arrays.asList(influxDao.database));

			influxDao.onReconnection();

			verify(influxDb).isBatchEnabled();
			verify(influxDb).enableBatch(InfluxDBDao.BATCH_BUFFER_SIZE, InfluxDBDao.BATCH_FLUSH_TIMER, TimeUnit.SECONDS);
			verify(influxDb).describeDatabases();
			verifyNoMoreInteractions(influxDb);
			verifyZeroInteractions(future, executor, availabilityChecker, clientFactory);
		}
	}
}
