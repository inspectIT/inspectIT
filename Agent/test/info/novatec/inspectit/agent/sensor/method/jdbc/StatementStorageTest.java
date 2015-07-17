/**
 *
 */
package info.novatec.inspectit.agent.sensor.method.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import info.novatec.inspectit.agent.AbstractLogSupport;

import java.util.List;

import org.testng.annotations.Test;

/**
 * @author Stefan Siegl
 * 
 */
@SuppressWarnings("PMD")
public class StatementStorageTest extends AbstractLogSupport {

	@Test
	public void addSQLWithParameterAndRead() {
		// Setup
		StatementStorage storage = new StatementStorage();
		storage.addSql("SELECT * FROM CARS WHERE CAR_ID = ?");
		Object marker = "I am the prepared Statement";
		storage.addPreparedStatement(marker);

		storage.addParameter(marker, 0, "1");

		List<String> result = storage.getParameters(marker);

		assertThat(result, contains(equalTo("'1'")));
	}

	@Test
	public void addSQLWithoutParameterAndSetParameter() {
		// Setup
		StatementStorage storage = new StatementStorage();
		storage.addSql("SELECT * FROM CARS");
		Object marker = "I am the prepared Statement";
		storage.addPreparedStatement(marker);

		storage.addParameter(marker, 0, "1");

		List<String> result = storage.getParameters(marker);

		assertThat(result, is(not(equalTo(null))));
		assertThat(result, is(empty()));
	}

	@Test
	public void addSQLWithParameterAndAddWrongIndex() {
		// Setup
		StatementStorage storage = new StatementStorage();
		storage.addSql("SELECT * FROM CARS WHERE CAR_ID = ?");
		Object marker = "I am the prepared Statement";
		storage.addPreparedStatement(marker);

		storage.addParameter(marker, 1, "1");

		List<String> result = storage.getParameters(marker);

		assertThat(result, is(not(equalTo(null))));
		assertThat(result, contains(equalTo(null)));
	}

	@Test
	public void addSQLWithParameterAndAddNullValue() {
		// Setup
		StatementStorage storage = new StatementStorage();
		storage.addSql("SELECT * FROM CARS WHERE CAR_ID = ?");
		Object marker = "I am the prepared Statement";
		storage.addPreparedStatement(marker);

		storage.addParameter(marker, 0, null);

		List<String> result = storage.getParameters(marker);

		assertThat(result, is(not(equalTo(null))));
		assertThat(result, contains(equalTo("null")));
	}
}
