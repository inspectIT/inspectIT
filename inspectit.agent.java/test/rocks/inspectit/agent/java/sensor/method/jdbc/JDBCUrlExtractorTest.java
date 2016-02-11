package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.agent.sensor.method.jdbc.ConnectionMetaDataStorage.JDBCUrlExtractor;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JDBCUrlExtractorTest {

	private JDBCUrlExtractor extractor;

	@BeforeTest
	public void init() {
		extractor = new JDBCUrlExtractor();
	}

	// jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
	// * jdbc:db2://<HOST>:<PORT>/<DATABASE_NAME>
	// * jdbc:h2:../../database/database/dvdstore22

	@DataProvider
	public Object[][] differentVendors() {
		return new Object[][] { { "jdbc:h2:../../database/database/dvdstore22", "../../database/database/dvdstore22" },
				{ "jdbc:h2:myh2server.mycompany.de:7000/dvdstore22", "myh2server.mycompany.de:7000/dvdstore22" }, { "jdbc:db2://mydb2instance:8000/mydatabase", "mydb2instance:8000/mydatabase" },
				{ "jdbc:sqlserver://sqlserver\\myinstance:5000;prop1=1;prop2=2", "sqlserver\\myinstance:5000" }, { "jdbc:oracle:thin:@//myhost:1521/orcl", "myhost:1521/orcl" },
				{ "jdbc:oracle:thin:@myhost:1521:orcl", "myhost:1521:orcl" }, { "jdbc:oracle:oci:@myhost:1521:orcl", "myhost:1521:orcl" },
				{ "jdbc:mysql://localhost/test?user=monty&password=greatsqldb", "localhost/test" }, { "jdbc:postgresql:database", "database" },
				{ "jdbc:postgresql://localhost/test", "localhost/test" }, { "jdbc:postgresql://localhost:5000/test", "localhost:5000/test" }, { "jdbc:odbc:HY_FLAT", "HY_FLAT" },
				{ "jdbc:odbc:SQL_SERVER;user=sa;password=HerongYang", "SQL_SERVER" } };
	}

	@Test(dataProvider = "differentVendors")
	public void extractFromFormat(String input, String expected) {
		String result = extractor.extractURLfromJDBCURL(input);
		assertThat(result, is(expected));
	}

	@Test
	public void invalidFormat() {
		String invalid = "this is just an invalid format";
		String result = extractor.extractURLfromJDBCURL(invalid);
		assertThat(result, is(invalid));
	}
}
