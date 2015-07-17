package info.novatec.inspectit.agent.config.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.config.ParserException;
import info.novatec.inspectit.agent.config.PriorityEnum;
import info.novatec.inspectit.agent.config.StorageException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class FileConfigurationReaderTest extends AbstractLogSupport {

	private File file;

	private PrintWriter writer;

	@Mock
	private IConfigurationStorage configurationStorage;

	private FileConfigurationReader fileConfigurationReader;

	@BeforeMethod(alwaysRun = true)
	public void initConfigurationFile() throws Exception {
		if (null == file) {
			String tmpdir = System.getProperty("java.io.tmpdir");
			file = new File(tmpdir + "/inspectit-agent.cfg");
		} else {
			file.delete();
			file.createNewFile();
		}

		System.setProperty("inspectit.config", System.getProperty("java.io.tmpdir"));
		writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	}

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		fileConfigurationReader = new FileConfigurationReader(configurationStorage);
		fileConfigurationReader.log = LoggerFactory.getLogger(FileConfigurationReader.class);
	}

	@Test
	public void loadAndVerifyRepository() throws ParserException, StorageException {
		String localhost = "localhost";
		int port = 1099;
		String agentName = "CalculatorTestAgent";

		writer.println("repository " + localhost + " " + port + " " + agentName);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).setRepository(localhost, 1099);
		verify(configurationStorage, times(1)).setAgentName(agentName);
	}

	@Test
	public void loadAndVerifyMethodSensorType() throws ParserException, StorageException {
		String name = "average-timer";
		String clazz = "info.novatec.inspectit.agent.sensor.method.averagetimer.AverageTimerSensor";
		PriorityEnum priority = PriorityEnum.HIGH;

		writer.println("method-sensor-type " + name + " " + clazz + " " + priority);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addMethodSensorType(name, clazz, priority, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifyMethodSensorTypeWithParameter() throws ParserException, StorageException {
		String name = "average-timer";
		String clazz = "info.novatec.inspectit.agent.sensor.method.averagetimer.AverageTimerSensor";
		PriorityEnum priority = PriorityEnum.HIGH;

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("stringLength", "500");
		String parameterString = "stringLength=500";

		writer.println("method-sensor-type " + name + " " + clazz + " " + priority + " " + parameterString);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addMethodSensorType(name, clazz, priority, settings);
	}

	@Test
	public void loadAndVerifyPlatformSensorType() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.platform.ClassLoadingInformation";

		writer.println("platform-sensor-type " + clazz);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addPlatformSensorType(clazz, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifyExceptionSensorType() throws ParserException, StorageException {
		String name = "exception-sensor-type";
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";

		writer.println(name + " " + clazz);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorType(clazz, Collections.<String, Object> emptyMap());
		verify(configurationStorage, times(1)).setEnhancedExceptionSensorActivated(false);
	}

	@Test
	public void loadAndVerifyExceptionSensorTypeAdvanced() throws ParserException, StorageException {
		String name = "exception-sensor-type";
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("mode", "enhanced");
		settings.put("stringLength", "500");
		String parameterString = "mode=enhanced stringLength=500";

		writer.println(name + " " + clazz + " " + parameterString);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorType(clazz, settings);
		verify(configurationStorage, times(1)).setEnhancedExceptionSensorActivated(true);
	}

	@Test
	public void loadAndVerifyExceptionSensorNoParameter() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String name = "exception-sensor";
		String targetClass = "java.lang.Throwable";
		writer.println(name + " " + targetClass);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorTypeParameter(clazz, targetClass, false, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifyExceptionSensorWithParameter() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String name = "exception-sensor";
		String targetClass = "java.lang.Throwable";

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("superclass", "true");
		String parameterString = "superclass=true";

		writer.println(name + " " + targetClass + " " + parameterString);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorTypeParameter(clazz, "java.lang.Throwable", false, settings);
	}

	@Test
	public void loadAndVerifyExceptionSensorWildcardParameter() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String name = "exception-sensor";
		String targetClass = "java.lang.*";
		writer.println(name + " " + targetClass);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addExceptionSensorTypeParameter(clazz, targetClass, true, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifyExceptionSensorModeSimple() throws ParserException, StorageException {
		String name = "exception-sensor-type";
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String mode = "mode=simple";
		writer.println(name + " " + clazz + " " + mode);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).setEnhancedExceptionSensorActivated(false);
		verify(configurationStorage, times(1)).addExceptionSensorType(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class));
	}

	@Test
	public void loadAndVerifyExceptionSensorModeEnhanced() throws ParserException, StorageException {
		String name = "exception-sensor-type";
		String clazz = "info.novatec.inspectit.agent.sensor.exception.ExceptionSensor";
		String mode = "mode=enhanced";
		writer.println(name + " " + clazz + " " + mode);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).setEnhancedExceptionSensorActivated(true);
		verify(configurationStorage, times(1)).addExceptionSensorType(Mockito.anyString(), Mockito.anyMapOf(String.class, Object.class));
	}

	@Test
	public void loadAndVerifyBufferStrategy() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.buffer.impl.SimpleBufferStrategy";

		writer.println("buffer-strategy " + clazz);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).setBufferStrategy(clazz, Collections.<String, String> emptyMap());
	}

	@Test
	public void loadAndVerifySendingStrategy() throws ParserException, StorageException {
		String clazz = "info.novatec.inspectit.agent.sending.impl.TimeStrategy";
		String time = "time=5000";

		writer.println("send-strategy " + clazz + " " + time);
		writer.close();

		fileConfigurationReader.load();

		Map<String, String> settings = new HashMap<String, String>();
		settings.put("time", "5000");

		verify(configurationStorage, times(1)).addSendingStrategy(clazz, settings);
	}

	@Test
	public void loadAndVerifyStandardSensor() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() ");
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), false, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifyRegexSensor() throws ParserException, StorageException {
		String sensorTypeName = "timer";
		String className = "*";
		String methodName = "*";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() ");
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), false, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifySensorIgnoreSignature() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + " ");
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), true, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifySensorWithOneParameter() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String parameter = "java.lang.String";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "(" + parameter + ") ");
		writer.close();

		fileConfigurationReader.load();

		List<String> parameterList = new ArrayList<String>();
		parameterList.add(parameter);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, parameterList, false, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifySensorWithManyParameter() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String parameterOne = "java.lang.String";
		String parameterTwo = "java.lang.Object";
		String parameterThree = "java.io.File";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "(" + parameterOne + "," + parameterTwo + "," + parameterThree + ") ");
		writer.close();

		fileConfigurationReader.load();

		List<String> parameterList = new ArrayList<String>();
		parameterList.add(parameterOne);
		parameterList.add(parameterTwo);
		parameterList.add(parameterThree);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, parameterList, false, Collections.<String, Object> emptyMap());
	}

	@Test
	public void loadAndVerifySensorWithParameterRecord() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String parameterRecord = "0;Source;text";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " p=" + parameterRecord);
		writer.close();

		fileConfigurationReader.load();

		Map<String, Object> settings = new HashMap<String, Object>();
		List<String> propertyList = new ArrayList<String>();
		propertyList.add(parameterRecord);
		settings.put("property", propertyList);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), false, settings);
	}

	@Test
	public void loadAndVerifySensorWithFieldRecord() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String fieldRecord = "LastOutput;jlbOutput.text";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " f=" + fieldRecord);
		writer.close();

		fileConfigurationReader.load();

		Map<String, Object> settings = new HashMap<String, Object>();
		List<String> propertyList = new ArrayList<String>();
		propertyList.add(fieldRecord);
		settings.put("field", propertyList);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), false, settings);
	}

	@Test
	public void loadAndVerifyInvocSensorWithMinDuration() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String minDuration = "100.0";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " minDuration=" + minDuration);
		writer.close();

		fileConfigurationReader.load();

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("minDuration", minDuration);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), false, settings);
	}

	@Test
	public void loadAndVerifyAnnotation() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String annotationClassName = "javax.ejb.StatelessBean";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " @" + annotationClassName);
		writer.close();

		fileConfigurationReader.load();

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("annotation", annotationClassName);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), false, settings);
	}

	@Test(expectedExceptions = { ParserException.class })
	public void loadInvalidFile() throws ParserException {
		System.setProperty("inspectit.config", "");

		fileConfigurationReader.load();
	}

	@Test
	public void loadAndVerifyModifiers() throws ParserException, StorageException {
		String sensorTypeName = "isequence";
		String className = "info.novatec.inspectitsamples.calculator.Calculator";
		String methodName = "actionPerformed";
		String modifiers = "pub,priv";

		writer.println("sensor " + sensorTypeName + " " + className + " " + methodName + "() " + " modifiers=" + modifiers);
		writer.close();

		fileConfigurationReader.load();

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("modifiers", modifiers);

		verify(configurationStorage, times(1)).addSensor(sensorTypeName, className, methodName, Collections.<String> emptyList(), false, settings);
	}

	@Test
	public void loadAndVerifyExcludeClasses() throws ParserException {
		String patternString = "info.novatec.*";

		writer.println("exclude-class" + " " + patternString);
		writer.close();

		fileConfigurationReader.load();

		verify(configurationStorage, times(1)).addIgnoreClassesPattern(patternString);
	}

	@Test
	public void loadIncludeWithAbsolutePath() throws ParserException, IOException {
		String include = "$include";
		String tmpdir = System.getProperty("java.io.tmpdir");

		String additionalFileName = "add-config-file.cfg";
		File additionalFile = new File(tmpdir + File.separatorChar + additionalFileName);
		additionalFile.createNewFile();

		writer.println(include + " " + additionalFile.getAbsolutePath());
		writer.close();

		fileConfigurationReader.load();

		additionalFile.delete();
	}

	@Test
	public void loadIncludeWithRelativePath() throws ParserException, IOException {
		String include = "$include";
		String tmpdir = System.getProperty("java.io.tmpdir");

		String additionalFileName = "add-config-file.cfg";
		File additionalFile = new File(tmpdir + File.separatorChar + additionalFileName);
		additionalFile.createNewFile();

		writer.println(include + " " + additionalFileName);
		writer.close();

		fileConfigurationReader.load();

		additionalFile.delete();
	}

	@Test
	public void loadIncludesWithRelativePathAndSubDirectories() throws ParserException, IOException {
		String include = "$include";
		String tmpdir = System.getProperty("java.io.tmpdir");
		new File(tmpdir + File.separatorChar + "config" + File.separatorChar + "sub").mkdirs();

		String additionalFileNameOne = "config" + File.separatorChar + "add-config-file.cfg";
		File additionalFileOne = new File(tmpdir + File.separatorChar + additionalFileNameOne);
		additionalFileOne.createNewFile();
		PrintWriter additionalFileOneWriter = new PrintWriter(new BufferedWriter(new FileWriter(additionalFileOne)));

		String additionalFileNameTwo = "sub" + File.separatorChar + "another-config-file.cfg";
		File additionalFileTwo = new File(tmpdir + File.separatorChar + "config" + File.separatorChar + additionalFileNameTwo);
		additionalFileTwo.createNewFile();

		writer.println(include + " " + additionalFileNameOne);
		writer.close();

		additionalFileOneWriter.println(include + " " + additionalFileNameTwo);
		additionalFileOneWriter.close();

		fileConfigurationReader.load();

		additionalFileOne.delete();
		additionalFileTwo.delete();
	}

	@AfterClass(alwaysRun = true)
	public void deleteConfiguration() {
		if (null != file) {
			file.delete();
		}
	}

}
