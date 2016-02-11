package rocks.inspectit.shared.all.versioning;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.version.InvalidVersionException;
import rocks.inspectit.shared.all.version.Version;

public class VersionTest extends TestBase {

	@DataProvider(name = "correctVersions")
	static Object[][] correctVersions() {
		return new Object[][] { { "1.5.49", new Version(1, 5, 49) }, { "1.6.1.56", new Version(1, 6, 1, 56) }, { "10.1.1.7666", new Version(10, 1, 1, 7666) } };
	}

	@DataProvider(name = "incorrectVersions")
	static Object[][] incorrectVersions() {
		return new Object[][] { { "1" }, { null }, { "dev" }, { "1.1" } };
	}

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(Version.class).suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test(dataProvider = "correctVersions")
	public void verifyAndCreateCorrectVersion(String version, Version expected) throws Exception {
		Version.verifyAndCreate(version).equals(expected);
	}

	@Test(dataProvider = "incorrectVersions", expectedExceptions = InvalidVersionException.class)
	public void verifyAndCreateIncorrectVersion(String version) throws Exception {
		Version.verifyAndCreate(version);
	}
}
