package info.novatec.inspectit.communication;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.RuntimeInformationData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.TimerData;

import java.sql.Timestamp;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test all domain model classes to verify whether the contract for the equals and hashCode methods
 * in a class is met. The contracts are described in the Javadoc comments for the java.lang.Object
 * class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class EqualsVerifierTest {

	/**
	 * Classes to be tested.
	 */
	public static final Object[][] TESTING_CLASSES = new Object[][] { { TimerData.class }, { SqlStatementData.class }, { ExceptionSensorData.class }, { InvocationSequenceData.class },
			{ ClassLoadingInformationData.class }, { CompilationInformationData.class }, { MemoryInformationData.class }, { RuntimeInformationData.class }, { SystemInformationData.class },
			{ ThreadInformationData.class }, { HttpTimerData.class }, { MethodIdent.class }, { MethodSensorTypeIdent.class }, { PlatformIdent.class }, { PlatformSensorTypeIdent.class },
			{ SensorTypeIdent.class } };

	/**
	 * Verify equals contract test.
	 * 
	 * @param clazz
	 *            Class to test.
	 */
	@Test(dataProvider = "classProvider")
	public void equalsContract(Class<?> clazz) {
		EqualsVerifier.forClass(clazz).usingGetClass().withPrefabValues(Timestamp.class, new Timestamp(1), new Timestamp(2))
				.withPrefabValues(ExceptionSensorData.class, new ExceptionSensorData(new Timestamp(1), 1, 1, 1), new ExceptionSensorData(new Timestamp(2), 2, 2, 2))
				.withPrefabValues(InvocationSequenceData.class, new InvocationSequenceData(new Timestamp(1), 1, 1, 1), new InvocationSequenceData(new Timestamp(2), 2, 2, 2)).withRedefinedSuperclass()
				.suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Provides classes to be tested.
	 * 
	 * @return Provides classes to be tested.
	 */
	@DataProvider(name = "classProvider")
	protected Object[][] classprovider() {
		return TESTING_CLASSES;
	}

}
