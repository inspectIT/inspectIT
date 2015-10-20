package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.ConnectionMetaData;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.ConnectionMetaDataExtractor;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.JDBCUrlExtractor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ConnectionMetaDataExtractorTest {

	private ConnectionMetaDataExtractor extractor;

	private static final String URL = "url";

	@SuppressWarnings("static-access")
	@BeforeMethod
	public void init() {
		extractor = new ConnectionMetaDataExtractor();
		JDBCUrlExtractor jdbExtractor = mock(JDBCUrlExtractor.class);
		when(jdbExtractor.extractURLfromJDBCURL(anyString())).thenReturn(URL);
		extractor.urlExtractor = jdbExtractor;
	}

	@Test
	public void extractInformation() throws SQLException {
		Connection mockedConnection = mock(Connection.class);
		DatabaseMetaData mockedMetaData = mock(DatabaseMetaData.class);

		String url = "url";
		String version = "version";
		String name = "name";

		when(mockedConnection.getMetaData()).thenReturn(mockedMetaData);
		when(mockedMetaData.getURL()).thenReturn(url);
		when(mockedMetaData.getDatabaseProductName()).thenReturn(name);
		when(mockedMetaData.getDatabaseProductVersion()).thenReturn(version);

		ConnectionMetaData data = extractor.parse(mockedConnection);

		assertThat(data.product, is(name));
		assertThat(data.version, is(version));
		assertThat(data.url, is(url));
	}

	@SuppressWarnings("static-access")
	@Test
	public void nullConnection() {
		Logger mockedLogger = Mockito.mock(Logger.class);
		extractor.logger = mockedLogger;

		ConnectionMetaData data = extractor.parse(null);

		assertThat(data, is(nullValue()));
		Mockito.verify(mockedLogger).warn(Mockito.anyString());
	}

	@Test
	public void closedConnection() throws SQLException {
		Connection mockedConnection = mock(Connection.class);
		when(mockedConnection.isClosed()).thenReturn(true);

		ConnectionMetaData data = extractor.parse(mockedConnection);

		assertThat(data, is(nullValue()));
	}

	@Test
	public void exceptionOnGet() throws SQLException {
		Connection mockedConnection = mock(Connection.class);
		when(mockedConnection.getMetaData()).thenThrow(new RuntimeException("test"));

		ConnectionMetaData data = extractor.parse(mockedConnection);

		// returns empty connection data object
		assertThat(data, is(not(nullValue())));
		assertThat(data.product, is(nullValue()));
		assertThat(data.version, is(nullValue()));
		assertThat(data.url, is(nullValue()));
	}
}
