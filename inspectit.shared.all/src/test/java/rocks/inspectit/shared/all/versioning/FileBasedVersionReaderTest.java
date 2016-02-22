package rocks.inspectit.shared.all.versioning;

import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.util.ResourceUtils;
import rocks.inspectit.shared.all.version.FileBasedVersionReader;
import rocks.inspectit.shared.all.version.InvalidVersionException;

/**
 * Tests the <code>FileBasedVersionReader</code>
 * 
 * @author Stefan Siegl
 */
public class FileBasedVersionReaderTest extends TestBase {

	@InjectMocks
	FileBasedVersionReader reader;

	@Mock
	ResourceUtils resourceUtils;

	@Test(expectedExceptions = InvalidVersionException.class)
	public void versionLogNotFound() throws Exception {
		when(resourceUtils.getAsStream(Mockito.anyString())).thenReturn(null);
		reader.readVersion();
	}

	@Test(expectedExceptions = InvalidVersionException.class)
	public void versionLogEmpty() throws Exception {
		String content = "";
		when(resourceUtils.getAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")));
		reader.readVersion();
	}

	@Test(expectedExceptions = InvalidVersionException.class)
	public void versionLogEmptyFirstLine() throws Exception {
		String content = "\n1.5.2.24";
		when(resourceUtils.getAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")));
		reader.readVersion();
	}

	@Test
	public void versionLogCorrect() throws Exception {
		String content = "1.5.2.24";
		when(resourceUtils.getAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")));
		Assert.assertEquals(reader.readVersion(), content);
	}

	@Test
	public void versionLogCorrectMoreLines() throws Exception {
		String version = "1.5.2.24";
		String content = version + "\n\n";
		when(resourceUtils.getAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream(content.getBytes("UTF-8")));
		Assert.assertEquals(reader.readVersion(), version);
	}
}
