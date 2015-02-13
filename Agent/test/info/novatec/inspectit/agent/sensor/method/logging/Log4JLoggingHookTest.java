package info.novatec.inspectit.agent.sensor.method.logging;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.apache.log4j.Level;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.communication.data.LoggingData;
import info.novatec.inspectit.util.ObjectUtils;

@SuppressWarnings("PMD")
public class Log4JLoggingHookTest extends AbstractLogSupport {

	@Mock
	IIdManager idManager;

	@Mock
	ICoreService coreService;

	@Mock
	RegisteredSensorConfig rsc;

	private static final String defaultMessage = "the log message";

	// FATAL - ERROR - WARN - INFO - TRACE - DEBUG

	private static final Level[] possibleLevels = new Level[] { Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.TRACE, Level.DEBUG };

	@DataProvider
	public static Object[][] allCombinationShouldWeCapture() {
		Object[][] result = new Object[possibleLevels.length * possibleLevels.length][3];

		int counter = 0;
		for (int pickMinimumLevel = 0; pickMinimumLevel < possibleLevels.length; pickMinimumLevel++) {
			for (int logLevel = 0; logLevel < possibleLevels.length; logLevel++) {
				result[counter][0] = possibleLevels[pickMinimumLevel];
				result[counter][1] = possibleLevels[logLevel];
				result[counter][2] = possibleLevels[logLevel].isGreaterOrEqual(possibleLevels[pickMinimumLevel]);
				counter++;
			}
		}

		return result;
	}

	@Test(dataProvider = "allCombinationShouldWeCapture")
	public void checkForCorrectLogging(Level givenMinimumLevel, Level logThisLevel, Boolean shouldCapture) throws IdNotAvailableException {
		Log4JLoggingHook hook = new Log4JLoggingHook(idManager, givenMinimumLevel.toString());

		long methodId = 1l;
		long sensorTypeId = 3l;
		long platformId = 9l;

		when(idManager.getRegisteredMethodId(methodId)).thenReturn(methodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(sensorTypeId);
		when(idManager.getPlatformId()).thenReturn(platformId);

		String loggingMessage = "a logging message";

		// we expect the method protected void forcedLog(String fqcn, Priority level, Object
		// message, Throwable
		Object[] params = new Object[4];
		params[0] = "fqcn";
		params[1] = logThisLevel;
		params[2] = loggingMessage;
		params[3] = null;

		hook.secondAfterBody(coreService, methodId, sensorTypeId, null, params, null, rsc);

		LoggingData loggingData = new LoggingData(logThisLevel.toString(), loggingMessage);
		loggingData.setMethodIdent(methodId);
		loggingData.setSensorTypeIdent(sensorTypeId);
		loggingData.setPlatformIdent(platformId);

		if (shouldCapture) {
			Mockito.verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), eq((String) null), argThat(new LoggingDataVerifier(loggingData)));
		} else {
			Mockito.verifyNoMoreInteractions(coreService);
		}
	}

	/**
	 * Inner class used to verify the contents of LoggingData objects.
	 */
	private static class LoggingDataVerifier extends ArgumentMatcher<LoggingData> {
		private final LoggingData data;

		public LoggingDataVerifier(LoggingData data) {
			this.data = data;
		}

		@Override
		public boolean matches(Object object) {
			if (!LoggingData.class.isInstance(object)) {
				return false;
			}
			LoggingData other = (LoggingData) object;

			if (data.getPlatformIdent() != other.getPlatformIdent()) {
				return false;
			} else if (data.getMethodIdent() != other.getMethodIdent()) {
				return false;
			} else if (data.getSensorTypeIdent() != other.getSensorTypeIdent()) {
				return false;
			} else if (!ObjectUtils.equals(data.getMessage(), other.getMessage())) {
				return false;
			} else if (!ObjectUtils.equals(data.getLevel(), other.getLevel())) {
				return false;
			}

			return true;
		}
	}
}