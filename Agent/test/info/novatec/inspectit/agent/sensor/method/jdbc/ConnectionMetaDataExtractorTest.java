package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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
		JDBCUrlExtractor jdbExtractor = Mockito.mock(JDBCUrlExtractor.class);
		Mockito.when(jdbExtractor.extractURLfromJDBCURL(Mockito.anyString())).thenReturn(URL);
		extractor.urlExtractor = jdbExtractor;
	}

	@Test
	public void extractInformation() throws SQLException {
		Connection mockedConnection = Mockito.mock(Connection.class);
		DatabaseMetaData mockedMetaData = Mockito.mock(DatabaseMetaData.class);

		String url = "url";
		String version = "version";
		String name = "name";

		Mockito.when(mockedConnection.getMetaData()).thenReturn(mockedMetaData);
		Mockito.when(mockedMetaData.getURL()).thenReturn(url);
		Mockito.when(mockedMetaData.getDatabaseProductName()).thenReturn(name);
		Mockito.when(mockedMetaData.getDatabaseProductVersion()).thenReturn(version);

		ConnectionMetaData data = extractor.parse(mockedConnection);

		assertThat(data.product, is(name));
		assertThat(data.version, is(version));
		assertThat(data.url, is(url));
	}

	@SuppressWarnings("static-access")
	@Test
	public void extractInformationForNullConnectionResultsInNoErrorButAWarningMessage() {
		Logger mockedLogger = Mockito.mock(Logger.class);
		extractor.logger = mockedLogger;

		ConnectionMetaData data = extractor.parse(null);

		assertThat(data, is(not(nullValue())));
		Mockito.verify(mockedLogger).warn(Mockito.anyString());
	}
}
