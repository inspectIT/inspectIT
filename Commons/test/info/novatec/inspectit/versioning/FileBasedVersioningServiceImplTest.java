package info.novatec.inspectit.versioning;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FileBasedVersioningServiceImplTest {

	@DataProvider(name = "correctVersions")
	public static Object[][] correctVersions() {
		return new Object[][] { { "1.5.49", "1.5" }, { "1.6.1.56", "1.6" }, { "10.1.1.7666", "10.1" } };
	}

	@DataProvider(name = "correctVersionsNoDots")
	public static Object[][] correctVersionsNoDots() {
		return new Object[][] { { "1.5.49", "15" }, { "1.6.1.56", "16" }, { "10.1.1.7666", "101" } };
	}

	@DataProvider(name = "incorrectVersions")
	public static Object[][] incorrectVersions() {
		return new Object[][] { { "1" }, { null }, { "dev" }, { "1.1" } };
	}

	@Test(dataProvider="correctVersions")
	public void parseMajorVersion(String version, String expectedMajorVersion) throws UnknownVersionException {
		Assert.assertEquals(getMockedVersionService(version).getMajorVersion(), expectedMajorVersion);
	}

	@Test(dataProvider = "correctVersionsNoDots")
	public void parseMajorVersionNoDots(String version, String expectedMajorVersion) throws UnknownVersionException {
		Assert.assertEquals(getMockedVersionService(version).getMajorVersionNoDots(), expectedMajorVersion);
	}

	@Test(dataProvider = "incorrectVersions", expectedExceptions = UnknownVersionException.class)
	public void parseMajorVersionHandleIncorrect(String incorrectVersion) throws UnknownVersionException {
		System.out.println(getMockedVersionService(incorrectVersion).getMajorVersion());
	}

	@Test(dataProvider = "incorrectVersions", expectedExceptions = UnknownVersionException.class)
	public void parseMajorVersionHandleIncorrectNoDots(String incorrectVersion) throws UnknownVersionException {
		System.out.println(getMockedVersionService(incorrectVersion).getMajorVersionNoDots());
	}

	private IVersioningService getMockedVersionService(String version) throws UnknownVersionException {
		IVersioningService versioning = Mockito.spy(new FileBasedVersioningServiceImpl());
		Mockito.doReturn(version).when(versioning).getVersion();
		return versioning;
	}
}
