package info.novatec.inspectit.agent.config.impl;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.ParserException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConfigurationFilesTest extends AbstractLogSupport {

	private static final String PATH_TO_CONFIG_FILES = "config";

	private FileConfigurationReader fileConfigurationReader;

	@Mock
	private IConfigurationStorage configurationStorage;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		fileConfigurationReader = new FileConfigurationReader(configurationStorage);
		fileConfigurationReader.log = LoggerFactory.getLogger(FileConfigurationReader.class);
	}

	@Test(dataProvider = "Paths-To-Config-Files-Provider")
	public void processConfigurationFile(String path) throws FileNotFoundException, ParserException {
		File configFile = new File(path);
		InputStream is = new FileInputStream(configFile);
		InputStreamReader reader = new InputStreamReader(is);
		fileConfigurationReader.parse(reader, path);
	}

	@DataProvider(name = "Paths-To-Config-Files-Provider")
	public Object[][] getConfigurationFiles() throws IOException {
		File dir = new File(PATH_TO_CONFIG_FILES);
		List<String> paths = new ArrayList<String>();
		processDir(dir, paths);
		Object[][] result = new Object[paths.size()][1];
		for (int i = 0; i < paths.size(); i++) {
			result[i][0] = paths.get(i);
		}
		return result;
	}

	private void processDir(File dir, List<String> paths) throws IOException {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".cfg");
				}
			});
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					processDir(files[i], paths);
				} else {
					paths.add(files[i].getPath());
				}
			}
		} else {
			throw new IOException("Given path to the configuration files is not valid.");
		}
	}
}
